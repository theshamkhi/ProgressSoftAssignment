package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.dto.DealDTO;
import com.progressoft.fxdeals.dto.ImportResultDTO;
import com.progressoft.fxdeals.exception.ValidationException;
import com.progressoft.fxdeals.mapper.DealMapper;
import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVImporterServiceImpl implements CSVImporterService {

    private final DealService dealService;

    @Override
    public ImportResultDTO importDeals(MultipartFile file) {
        log.info("Starting import: {}", file.getOriginalFilename());
        ImportResultDTO result = ImportResultDTO.builder().build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) {
                result.addError("Empty file");
                return result;
            }

            String line;
            int rowNumber = 1; // header = row 1
            int totalRecords = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                // Skip completely empty lines (don't count them)
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Count this as a record
                totalRecords++;

                String[] fields = line.split(",", -1); // keep empty columns

                // Check for correct number of columns
                if (fields.length != 5) {
                    result.incrementFailed();
                    result.addError(String.format("Row %d: Unexpected number of columns", rowNumber));
                    continue;
                }

                DealDTO dto = DealDTO.builder()
                        .dealId(fields[0])
                        .fromCurrency(fields[1])
                        .toCurrency(fields[2])
                        .dealTimestamp(fields[3])
                        .dealAmount(fields[4])
                        .build();

                processRow(dto, rowNumber, result);
            }

            result.setTotalRecords(totalRecords);

            log.info("Import complete: {} successful, {} duplicates, {} failed",
                    result.getSuccessfulRecords(),
                    result.getDuplicateRecords(),
                    result.getFailedRecords());

            return result;

        } catch (Exception e) {
            log.error("Import failed", e);
            result.addError("Failed to process file: " + e.getMessage());
            return result;
        }
    }

    private void processRow(DealDTO dealDTO, int rowNumber, ImportResultDTO result) {
        try {
            ValidationUtil.validateDeal(dealDTO);
            Deal deal = DealMapper.toEntity(dealDTO);

            boolean saved = dealService.saveDeal(deal);
            if (saved) {
                result.incrementSuccessful();
            } else {
                result.incrementDuplicate();
                result.addWarning(String.format("Row %d: Duplicate deal ID '%s'", rowNumber, dealDTO.getDealId()));
            }

        } catch (ValidationException e) {
            result.incrementFailed();
            result.addError(String.format("Row %d: %s", rowNumber, e.getMessage()));
            log.error("Row {} validation failed: {}", rowNumber, e.getMessage());
        }
    }
}