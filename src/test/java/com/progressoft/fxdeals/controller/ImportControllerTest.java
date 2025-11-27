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

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportController.class)
@DisplayName("ImportController Tests")
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CSVImporterService csvImporterService;

    @Test
    @DisplayName("Should return 200 OK for successful import")
    void shouldReturn200ForSuccessfulImport() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.successfulRecords").value(1))
                .andExpect(jsonPath("$.failedRecords").value(0))
                .andExpect(jsonPath("$.duplicateRecords").value(0));
    }

    @Test
    @DisplayName("Should return 400 for empty file")
    void shouldReturn400ForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("The uploaded file is empty"));
    }

    @Test
    @DisplayName("Should return 400 for invalid file type")
    void shouldReturn400ForInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "some content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Invalid file type. Please upload a CSV file."));
    }

    @Test
    @DisplayName("Should accept CSV file with correct content type")
    void shouldAcceptCSVFileWithCorrectContentType() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept CSV file with application/csv content type")
    void shouldAcceptCSVFileWithApplicationCsvContentType() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "application/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when all records fail")
    void shouldReturn400WhenAllRecordsFail() throws Exception {
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

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(0)
                .failedRecords(1)
                .duplicateRecords(0)
                .errors(List.of("Row 2: Deal ID is required"))
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.successfulRecords").value(0))
                .andExpect(jsonPath("$.failedRecords").value(1))
                .andExpect(jsonPath("$.errors[0]").value("Row 2: Deal ID is required"));
    }

    @Test
    @DisplayName("Should return 200 with mixed results")
    void shouldReturn200WithMixedResults() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                ,USD,EUR,2025-01-15T11:00:00,2000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(2)
                .successfulRecords(1)
                .failedRecords(1)
                .duplicateRecords(0)
                .errors(List.of("Row 3: Deal ID is required"))
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRecords").value(1))
                .andExpect(jsonPath("$.failedRecords").value(1));
    }

    @Test
    @DisplayName("Should handle duplicate records")
    void shouldHandleDuplicateRecords() throws Exception {
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

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(2)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(1)
                .warnings(List.of("Row 3: Duplicate deal ID 'DEAL001'"))
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicateRecords").value(1))
                .andExpect(jsonPath("$.warnings[0]").value(containsString("Duplicate deal ID")));
    }

    @Test
    @DisplayName("Should return 200 for health check")
    void shouldReturn200ForHealthCheck() throws Exception {
        mockMvc.perform(get("/api/deals/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("FX Deals Importer is running"));
    }

    @Test
    @DisplayName("Should handle file with .CSV extension (uppercase)")
    void shouldHandleFileWithUppercaseExtension() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.CSV",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return proper JSON structure")
    void shouldReturnProperJSONStructure() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(1)
                .successfulRecords(1)
                .failedRecords(0)
                .duplicateRecords(0)
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").exists())
                .andExpect(jsonPath("$.successfulRecords").exists())
                .andExpect(jsonPath("$.failedRecords").exists())
                .andExpect(jsonPath("$.duplicateRecords").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.warnings").exists());
    }

    @Test
    @DisplayName("Should include errors array in response")
    void shouldIncludeErrorsArrayInResponse() throws Exception {
        String csvContent = """
                dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
                ,USD,EUR,2025-01-15T10:30:00,1000.50
                DEAL002,XXX,EUR,2025-01-15T11:00:00,2000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDTO result = ImportResultDTO.builder()
                .totalRecords(2)
                .successfulRecords(0)
                .failedRecords(2)
                .duplicateRecords(0)
                .errors(List.of(
                        "Row 2: Deal ID is required",
                        "Row 3: Invalid From Currency code: 'XXX' is not a valid ISO 4217 currency."
                ))
                .build();

        when(csvImporterService.importDeals(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/deals/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0]").value("Row 2: Deal ID is required"))
                .andExpect(jsonPath("$.errors[1]").value(containsString("XXX")));
    }
}