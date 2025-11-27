package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import com.progressoft.fxdeals.model.Deal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSV Importer Service Tests")
class CSVImporterServiceImplTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private CSVImporterServiceImpl csvImporterService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(dealService);
    }

    @Test
    @DisplayName("Should import valid CSV successfully")
    void shouldImportValidCSVSuccessfully() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL002,GBP,JPY,2025-01-15T11:00:00,2000.75
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class))).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getDuplicateRecords()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();

        verify(dealService, times(2)).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle duplicate deals in CSV")
    void shouldHandleDuplicateDealsInCSV() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL001,GBP,JPY,2025-01-15T11:00:00,2000.75
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class)))
                .thenReturn(true)   // First deal succeeds
                .thenReturn(false); // Second deal is duplicate

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getDuplicateRecords()).isEqualTo(1);
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getWarnings().get(0)).contains("Duplicate deal ID 'DEAL001'");

        verify(dealService, times(2)).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle validation errors in CSV")
    void shouldHandleValidationErrorsInCSV() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                ,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL002,INVALID,EUR,2025-01-15T11:00:00,2000.75
                DEAL003,USD,EUR,invalid-timestamp,3000.00
                DEAL004,USD,EUR,2025-01-15T12:00:00,-500.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(4);
        assertThat(result.getSuccessfulRecords()).isEqualTo(0);
        assertThat(result.getFailedRecords()).isEqualTo(4);
        assertThat(result.getDuplicateRecords()).isEqualTo(0);
        assertThat(result.getErrors()).hasSize(4);
        assertThat(result.getErrors().get(0)).contains("Deal ID is required");
        assertThat(result.getErrors().get(1)).contains("Must be 3 uppercase letters");
        assertThat(result.getErrors().get(2)).contains("Invalid timestamp format");
        assertThat(result.getErrors().get(3)).contains("Deal amount must be positive");

        verify(dealService, never()).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle empty CSV file")
    void shouldHandleEmptyCSVFile() {
        String csvContent = "";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Empty file");
        assertThat(result.getTotalRecords()).isEqualTo(0);

        verify(dealService, never()).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle CSV with only header")
    void shouldHandleCSVWithOnlyHeader() {
        String csvContent = "Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "header-only.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(0);
        assertThat(result.getSuccessfulRecords()).isEqualTo(0);
        assertThat(result.getFailedRecords()).isEqualTo(0);

        verify(dealService, never()).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should skip empty lines in CSV")
    void shouldSkipEmptyLinesInCSV() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                
                DEAL002,GBP,JPY,2025-01-15T11:00:00,2000.75
                
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals-with-blanks.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class))).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getFailedRecords()).isEqualTo(0);

        verify(dealService, times(2)).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle incorrect number of columns")
    void shouldHandleIncorrectNumberOfColumns() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00
                DEAL002,GBP,JPY,2025-01-15T11:00:00,2000.75,EXTRA
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "incorrect-columns.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getFailedRecords()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors().get(0)).contains("Unexpected number of columns");
        assertThat(result.getErrors().get(1)).contains("Unexpected number of columns");

        verify(dealService, never()).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle mixed success, duplicates, and failures")
    void shouldHandleMixedResults() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL001,GBP,JPY,2025-01-15T11:00:00,2000.75
                ,CHF,CAD,2025-01-15T12:00:00,3000.00
                DEAL003,AUD,NZD,2025-01-15T13:00:00,4000.25
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mixed-results.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class)))
                .thenReturn(true)   // DEAL001 succeeds
                .thenReturn(false)  // DEAL001 duplicate
                .thenReturn(true);  // DEAL003 succeeds

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(4); // Empty line is not counted
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getDuplicateRecords()).isEqualTo(1);
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle invalid currency codes")
    void shouldHandleInvalidCurrencyCodes() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,XXX,EUR,2025-01-15T10:30:00,1000.50
                DEAL002,USD,XTS,2025-01-15T11:00:00,2000.75
                DEAL003,USDD,EUR,2025-01-15T12:00:00,3000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid-currencies.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(3);
        assertThat(result.getFailedRecords()).isEqualTo(3);
        assertThat(result.getErrors()).hasSize(3);

        verify(dealService, never()).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should trim whitespace from CSV fields")
    void shouldTrimWhitespaceFromCSVFields() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                  DEAL001  ,  USD  ,  EUR  ,  2025-01-15T10:30:00  ,  1000.50  
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "whitespace.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class))).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);

        verify(dealService).saveDeal(argThat(deal ->
                deal.getDealId().equals("DEAL001") &&
                        deal.getFromCurrency().equals("USD") &&
                        deal.getToCurrency().equals("EUR") &&
                        deal.getDealAmount().compareTo(new BigDecimal("1000.50")) == 0
        ));
    }

    @Test
    @DisplayName("Should handle large decimal amounts")
    void shouldHandleLargeDecimalAmounts() {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,999999999.9999
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-amount.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(dealService.saveDeal(any(Deal.class))).thenReturn(true);

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);

        verify(dealService).saveDeal(argThat(deal ->
                deal.getDealAmount().compareTo(new BigDecimal("999999999.9999")) == 0
        ));
    }

    @Test
    @DisplayName("Should handle file processing exception")
    void shouldHandleFileProcessingException() {
        MockMultipartFile file = mock(MockMultipartFile.class);

        try {
            when(file.getInputStream()).thenThrow(new RuntimeException("File read error"));
        } catch (Exception e) {
            // Ignore setup exception
        }

        ImportResultDTO result = csvImporterService.importDeals(file);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Failed to process file");
    }
}