package com.progressoft.fxdeals.controller;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import com.progressoft.fxdeals.service.CSVImporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/deals/csv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Import", description = "CSV Import API for FX Deals")
public class ImportController {

    private final CSVImporterService csvImporterService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import deals from CSV", description = "Upload a CSV file containing FX deals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed (check result for details)",
                    content = @Content(schema = @Schema(implementation = ImportResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or format",
                    content = @Content(schema = @Schema(implementation = ImportResultDTO.class)))
    })
    public ResponseEntity<ImportResultDTO> importDeals(
            @RequestParam("file") MultipartFile file) {

        log.info("Received import request for file: {}", file.getOriginalFilename());

        // Validate file is not empty
        if (file.isEmpty()) {
            log.warn("Empty file uploaded");
            ImportResultDTO result = ImportResultDTO.builder().build();
            result.addError("The uploaded file is empty");
            return ResponseEntity.badRequest().body(result);
        }

        // Validate file type
        if (!isCSVFile(file)) {
            log.warn("Invalid file type: {}", file.getContentType());
            ImportResultDTO result = ImportResultDTO.builder().build();
            result.addError("Invalid file type. Please upload a CSV file.");
            return ResponseEntity.badRequest().body(result);
        }

        ImportResultDTO result = csvImporterService.importDeals(file);

        if (result.getSuccessfulRecords() == 0 && result.getFailedRecords() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.ok(result);
    }

    private boolean isCSVFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        return (filename != null && filename.toLowerCase().endsWith(".csv")) ||
                (contentType != null && (contentType.equals("text/csv") ||
                        contentType.equals("application/csv") ||
                        contentType.equals("application/vnd.ms-excel")));
    }
}