package com.progressoft.fxdeals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    private int totalRows;
    private int successfulInserts;
    private int duplicates;
    private int validationErrors;
    private int databaseErrors;

    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();

    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }
}