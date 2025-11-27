package com.progressoft.fxdeals.controller;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import com.progressoft.fxdeals.service.CSVImporterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class ImportController {

    private final CSVImporterService csvImporterService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importDeals(
            @RequestParam("file") MultipartFile file) {

        log.info("Received import request for file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Empty file uploaded");
            ImportResultDTO result = ImportResultDTO.builder().build();
            result.addError("The uploaded file is empty");
            return ResponseEntity.badRequest().body(result);
        }

        if (!isCSVFile(file)) {
            log.warn("Invalid file type: {}", file.getContentType());
            ImportResultDTO result = ImportResultDTO.builder().build();
            result.addError("Invalid file type. Please upload a CSV file.");
            return ResponseEntity.badRequest().body(result);
        }

        ImportResultDTO result = csvImporterService.importDeals(file);

        if (result.getSuccessfulRecords() > 0) {
            return ResponseEntity.ok(result);
        } else if (result.getFailedRecords() > 0 || result.getTotalRecords() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FX Deals Importer is running");
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