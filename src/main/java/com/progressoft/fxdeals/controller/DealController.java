package com.progressoft.fxdeals.controller;

import com.progressoft.fxdeals.dto.*;
import com.progressoft.fxdeals.mapper.DealMapper;
import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.service.DealService;
import com.progressoft.fxdeals.util.ValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FX Deals", description = "Foreign Exchange Deals Management API")
public class DealController {

    private final DealService dealService;

    @PostMapping
    @Operation(summary = "Create a single FX deal", description = "Creates a new foreign exchange deal record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deal created successfully",
                    content = @Content(schema = @Schema(implementation = DealResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Deal ID already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody CreateDealRequest request) {
        log.info("Creating deal: {}", request.getDealId());

        Deal deal = convertToDeal(request);
        Deal savedDeal = dealService.createDeal(deal);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedDeal));
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple FX deals", description = "Creates multiple foreign exchange deals in a single request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch operation completed",
                    content = @Content(schema = @Schema(implementation = ImportResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ImportResultDTO> createDealsInBatch(@Valid @RequestBody BatchCreateRequest request) {
        log.info("Creating {} deals in batch", request.getDeals().size());

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(request.getDeals().size())
                .build();

        int rowNumber = 0;
        for (CreateDealRequest dealRequest : request.getDeals()) {
            rowNumber++;
            try {
                Deal deal = convertToDeal(dealRequest);
                boolean saved = dealService.saveDeal(deal);

                if (saved) {
                    result.incrementSuccessful();
                } else {
                    result.incrementDuplicate();
                    result.addWarning(String.format("Deal %d: Duplicate deal ID '%s'",
                            rowNumber, dealRequest.getDealId()));
                }

            } catch (Exception e) {
                result.incrementFailed();
                result.addError(String.format("Deal %d: %s", rowNumber, e.getMessage()));
                log.error("Deal {} failed: {}", rowNumber, e.getMessage());
            }
        }

        log.info("Batch complete: {} successful, {} duplicates, {} failed",
                result.getSuccessfulRecords(), result.getDuplicateRecords(), result.getFailedRecords());

        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get all FX deals with pagination", description = "Retrieves all foreign exchange deals with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deals"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Page<DealResponse>> getAllDeals(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Deal> deals = dealService.getAllDeals(pageable);
        Page<DealResponse> response = deals.map(this::mapToResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{dealId}")
    @Operation(summary = "Get FX deal by ID", description = "Retrieves a specific foreign exchange deal by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deal found",
                    content = @Content(schema = @Schema(implementation = DealResponse.class))),
            @ApiResponse(responseCode = "404", description = "Deal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DealResponse> getDealById(
            @Parameter(description = "Deal ID", required = true) @PathVariable String dealId) {

        log.info("Retrieving deal: {}", dealId);
        Deal deal = dealService.getDealById(dealId);
        return ResponseEntity.ok(mapToResponse(deal));
    }

    @GetMapping("/count")
    @Operation(summary = "Get total deal count", description = "Returns the total number of deals in the system")
    @ApiResponse(responseCode = "200", description = "Count returned successfully")
    public ResponseEntity<Long> getTotalDealsCount() {
        long count = dealService.getTotalDealsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ClusteredData Warehouse is running");
    }

    private Deal convertToDeal(CreateDealRequest request) {
        DealDTO dealDTO = DealDTO.builder()
                .dealId(request.getDealId())
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .dealTimestamp(request.getDealTimestamp())
                .dealAmount(request.getDealAmount())
                .build();

        ValidationUtil.validateDeal(dealDTO);
        return DealMapper.toEntity(dealDTO);
    }

    private DealResponse mapToResponse(Deal deal) {
        return DealResponse.builder()
                .dealId(deal.getDealId())
                .fromCurrency(deal.getFromCurrency())
                .toCurrency(deal.getToCurrency())
                .dealTimestamp(deal.getDealTimestamp())
                .dealAmount(deal.getDealAmount())
                .createdAt(deal.getCreatedAt())
                .build();
    }
}