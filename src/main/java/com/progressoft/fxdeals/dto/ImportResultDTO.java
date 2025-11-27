package com.progressoft.fxdeals.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the result of a CSV import operation.
 * Contains counts of successful, failed, and duplicate records.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDTO {

    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private int duplicateRecords;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void incrementSuccessful() {
        this.successfulRecords++;
    }

    public void incrementFailed() {
        this.failedRecords++;
    }

    public void incrementDuplicate() {
        this.duplicateRecords++;
    }
}