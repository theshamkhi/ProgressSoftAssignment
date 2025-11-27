package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSVImporterService Tests")
class CSVImporterServiceImplTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private CSVImporterServiceImpl csvImporterService;

    @Test
    @DisplayName("Should handle empty file")
    void shouldHandleEmptyFile() {
        String csvContent = "";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(0, result.getTotalRecords());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Empty file"));
    }

    @Test
    @DisplayName("Should handle file with only header")
    void shouldHandleFileWithOnlyHeader() {
        String csvContent = "dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(0, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
    }

    @Test
    @DisplayName("Should process valid records successfully")
    void shouldProcessValidRecordsSuccessfully() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL002,GBP,USD,2025-01-15T11:00:00,2500.75
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(dealService.saveDeal(any())).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
        assertEquals(0, result.getDuplicateRecords());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should handle missing deal ID")
    void shouldHandleMissingDealId() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                ,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Deal ID is required"));
    }

    @Test
    @DisplayName("Should handle invalid currency code")
    void shouldHandleInvalidCurrencyCode() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,XXX,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Invalid From Currency code"));
        assertTrue(result.getErrors().get(0).contains("XXX"));
    }

    @Test
    @DisplayName("Should handle invalid timestamp format")
    void shouldHandleInvalidTimestampFormat() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,invalid-date,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Invalid timestamp format"));
    }

    @Test
    @DisplayName("Should handle negative amount")
    void shouldHandleNegativeAmount() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,-500.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Deal amount must be positive"));
    }

    @Test
    @DisplayName("Should handle non-numeric amount")
    void shouldHandleNonNumericAmount() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,not-a-number
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Invalid amount format"));
    }

    @Test
    @DisplayName("Should handle duplicate deal IDs")
    void shouldHandleDuplicateDealIds() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(dealService.saveDeal(any()))
                .thenReturn(true)  // First save succeeds
                .thenReturn(false); // Second save fails (duplicate)

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
        assertEquals(1, result.getDuplicateRecords());
        assertEquals(1, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("Row 3"));
        assertTrue(result.getWarnings().get(0).contains("Duplicate deal ID"));
        assertTrue(result.getWarnings().get(0).contains("DEAL001"));
    }

    @Test
    @DisplayName("Should handle unexpected number of columns")
    void shouldHandleUnexpectedNumberOfColumns() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50,EXTRA_FIELD
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertTrue(result.getErrors().get(0).contains("Row 2"));
        assertTrue(result.getErrors().get(0).contains("Unexpected number of columns"));
    }

    @Test
    @DisplayName("Should handle mixed valid and invalid records")
    void shouldHandleMixedValidAndInvalidRecords() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                ,USD,EUR,2025-01-15T11:00:00,2000.00
                DEAL003,XXX,EUR,2025-01-15T12:00:00,3000.00
                DEAL004,USD,EUR,2025-01-15T13:00:00,4000.00
                DEAL005,USD,EUR,invalid-date,5000.00
                DEAL006,USD,EUR,2025-01-15T15:00:00,-6000.00
                DEAL007,USD,EUR,2025-01-15T16:00:00,7000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(dealService.saveDeal(any())).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(7, result.getTotalRecords());
        assertEquals(3, result.getSuccessfulRecords()); // DEAL001, DEAL004, DEAL007
        assertEquals(4, result.getFailedRecords()); // Empty ID, XXX, invalid date, negative amount
        assertEquals(0, result.getDuplicateRecords());
        assertEquals(4, result.getErrors().size());
    }

    @Test
    @DisplayName("Should skip empty lines and not count them")
    void shouldSkipEmptyLinesAndNotCountThem() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                
                DEAL002,GBP,USD,2025-01-15T11:00:00,2000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(dealService.saveDeal(any())).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertEquals(2, result.getTotalRecords()); // Should not count the empty line
        assertEquals(2, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
    }

    @Test
    @DisplayName("Should handle comprehensive test scenario")
    void shouldHandleComprehensiveTestScenario() {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL100,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL101,GBP,USD,2025-01-15T11:00:00,2500.75
                ,EUR,JPY,2025-01-15T12:15:00,5000.00
                DEAL103,XXX,AUD,2025-01-15T13:45:00,1500.25
                DEAL104,CHF,GBP,2025-01-15T14:20:00,3200.80
                DEAL105,USD,CAD,invalid-date,100.00
                DEAL106,AUD,NZD,2025-01-15T15:00:00,-500.00
                DEAL107,JPY,USD,2025-01-15T16:00:00,95000.00
                DEAL108,USD,EUR,2025-01-15T17:00:00,not-a-number
                DEAL109,USD,EUR,2025-01-15T18:00:00,200.00
                DEAL109,USD,EUR,2025-01-15T18:00:00,200.00
                DEAL110,USD,EUR,,300.00
                DEAL111,USD,EUR,2025-01-15T19:00:00,
                DEAL112,USD,EUR,2025-01-15T20:00:00,400.00,EXTRA_FIELD
                , , , ,
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Mock behavior: first 5 valid deals save successfully, 6th is duplicate
        when(dealService.saveDeal(any()))
                .thenReturn(true)   // DEAL100
                .thenReturn(true)   // DEAL101
                .thenReturn(true)   // DEAL104
                .thenReturn(true)   // DEAL107
                .thenReturn(true)   // DEAL109 (first)
                .thenReturn(false); // DEAL109 (duplicate)

        ImportResultDTO result = csvImporterService.importDeals(file);

        // Debug: Print actual errors to understand what's happening
        System.out.println("=== ACTUAL ERRORS ===");
        result.getErrors().forEach(System.out::println);
        System.out.println("=== ACTUAL WARNINGS ===");
        result.getWarnings().forEach(System.out::println);
        System.out.println("Total: " + result.getTotalRecords() + ", Success: " + result.getSuccessfulRecords() +
                ", Failed: " + result.getFailedRecords() + ", Duplicates: " + result.getDuplicateRecords());

        // Verify counts (matching expected output exactly)
        assertEquals(15, result.getTotalRecords(), "Total records should be 15");
        assertEquals(5, result.getSuccessfulRecords(), "Successful records should be 5");
        assertEquals(9, result.getFailedRecords(), "Failed records should be 9");
        assertEquals(1, result.getDuplicateRecords(), "Duplicate records should be 1");

        // Verify we have 9 errors total
        assertEquals(9, result.getErrors().size(), "Should have exactly 9 errors");

        // Verify specific error messages exist
        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 4") && e.contains("Deal ID is required")),
                "Should have 'Deal ID is required' error at Row 4");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 5") && e.contains("Invalid From Currency code") && e.contains("XXX")),
                "Should have invalid currency XXX error at Row 5");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 7") && e.contains("Invalid timestamp format")),
                "Should have invalid timestamp error at Row 7");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 8") && e.contains("Deal amount must be positive")),
                "Should have negative amount error at Row 8");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 10") && e.contains("Invalid amount format")),
                "Should have invalid amount format error at Row 10");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 13") && e.contains("Deal Timestamp is required")),
                "Should have missing timestamp error at Row 13");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 14") && e.contains("Deal Amount is required")),
                "Should have missing amount error at Row 14");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 15") && e.contains("Unexpected number of columns")),
                "Should have unexpected columns error at Row 15");

        assertTrue(result.getErrors().stream().anyMatch(e ->
                        e.contains("Row 16") && e.contains("Deal ID is required")),
                "Should have 'Deal ID is required' error at Row 16");

        // Verify warnings
        assertEquals(1, result.getWarnings().size(), "Should have exactly 1 warning");
        assertTrue(result.getWarnings().get(0).contains("Row 12"),
                "Warning should be at Row 12");
        assertTrue(result.getWarnings().get(0).contains("Duplicate deal ID 'DEAL109'"),
                "Warning should mention duplicate DEAL109");
    }
}