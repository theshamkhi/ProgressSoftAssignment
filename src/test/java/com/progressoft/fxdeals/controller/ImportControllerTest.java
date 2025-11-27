package com.progressoft.fxdeals.controller;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import com.progressoft.fxdeals.service.CSVImporterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportController.class)
@DisplayName("Import Controller Tests")
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CSVImporterService csvImporterService;

    @Test
    @DisplayName("Should import CSV successfully")
    void shouldImportCSVSuccessfully() throws Exception {
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

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(2)
                .successfulRecords(2)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulRecords").value(2))
                .andExpect(jsonPath("$.failedRecords").value(0))
                .andExpect(jsonPath("$.duplicateRecords").value(0));

        verify(csvImporterService, times(1)).importDeals(any());
    }

    @Test
    @DisplayName("Should reject empty file")
    void shouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]").value("The uploaded file is empty"));

        verify(csvImporterService, never()).importDeals(any());
    }

    @Test
    @DisplayName("Should reject non-CSV file")
    void shouldRejectNonCSVFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "some text content".getBytes()
        );

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]").value("Invalid file type. Please upload a CSV file."));

        verify(csvImporterService, never()).importDeals(any());
    }

    @Test
    @DisplayName("Should handle import with duplicates")
    void shouldHandleImportWithDuplicates() throws Exception {
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

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(2)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(1)
                .build();
        result.addWarning("Row 3: Duplicate deal ID 'DEAL001'");

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulRecords").value(1))
                .andExpect(jsonPath("$.duplicateRecords").value(1))
                .andExpect(jsonPath("$.warnings", hasSize(1)));
    }

    @Test
    @DisplayName("Should handle import with validation errors")
    void shouldHandleImportWithValidationErrors() throws Exception {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,INVALID,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(0)
                .failedRecords(1)
                .duplicateRecords(0)
                .build();
        result.addError("Row 2: Invalid From Currency code: 'INVALID' is not a valid ISO 4217 currency.");

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.failedRecords").value(1))
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    @DisplayName("Should accept CSV with application/vnd.ms-excel content type")
    void shouldAcceptCSVWithExcelContentType() throws Exception {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "application/vnd.ms-excel",
                csvContent.getBytes()
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRecords").value(1));
    }

    @Test
    @DisplayName("Should return 200 when all records fail")
    void shouldReturn400WhenAllRecordsFail() throws Exception {
        String csvContent = """
                Deal ID,From Currency,To Currency,Deal Timestamp,Deal Amount
                ,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(0)
                .failedRecords(1)
                .duplicateRecords(0)
                .build();
        result.addError("Row 2: Deal ID is required");

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/csv/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.failedRecords").value(1))
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }
}