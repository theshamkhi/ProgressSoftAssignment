package com.progressoft.fxdeals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressoft.fxdeals.dto.BatchCreateRequest;
import com.progressoft.fxdeals.dto.CreateDealRequest;
import com.progressoft.fxdeals.exception.DuplicateRecordException;
import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.service.DealService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DealController.class)
@DisplayName("Deal Controller Tests")
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DealService dealService;

    private Deal sampleDeal;
    private CreateDealRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleDeal = Deal.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .dealAmount(new BigDecimal("1000.50"))
                .createdAt(LocalDateTime.now())
                .build();

        validRequest = CreateDealRequest.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();
    }

    @Test
    @DisplayName("Should create deal successfully")
    void shouldCreateDealSuccessfully() throws Exception {
        when(dealService.createDeal(any(Deal.class))).thenReturn(sampleDeal);

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealId").value("DEAL001"))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.dealAmount").value(1000.50));

        verify(dealService, times(1)).createDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should return 409 when deal ID already exists")
    void shouldReturn409WhenDealIdExists() throws Exception {
        when(dealService.createDeal(any(Deal.class)))
                .thenThrow(new DuplicateRecordException("Deal with ID 'DEAL001' already exists"));

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Record"))
                .andExpect(jsonPath("$.message").value("Deal with ID 'DEAL001' already exists"));
    }

    @Test
    @DisplayName("Should return 400 when deal ID is blank")
    void shouldReturn400WhenDealIdIsBlank() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when from currency is invalid")
    void shouldReturn400WhenFromCurrencyIsInvalid() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("DEAL002")
                .fromCurrency("INVALID")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when timestamp format is invalid")
    void shouldReturn400WhenTimestampFormatIsInvalid() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("DEAL003")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("15-01-2025 10:30:00")
                .dealAmount("1000.50")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when amount is negative")
    void shouldReturn400WhenAmountIsNegative() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("DEAL004")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("-100.00")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void shouldReturn400WhenAmountIsZero() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("DEAL005")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("0")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create batch deals successfully")
    void shouldCreateBatchDealsSuccessfully() throws Exception {
        CreateDealRequest request1 = CreateDealRequest.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        CreateDealRequest request2 = CreateDealRequest.builder()
                .dealId("DEAL002")
                .fromCurrency("GBP")
                .toCurrency("JPY")
                .dealTimestamp("2025-01-15T11:00:00")
                .dealAmount("2000.75")
                .build();

        BatchCreateRequest batchRequest = BatchCreateRequest.builder()
                .deals(Arrays.asList(request1, request2))
                .build();

        when(dealService.saveDeal(any(Deal.class))).thenReturn(true);

        mockMvc.perform(post("/api/deals/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulRecords").value(2))
                .andExpect(jsonPath("$.failedRecords").value(0))
                .andExpect(jsonPath("$.duplicateRecords").value(0));

        verify(dealService, times(2)).saveDeal(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle duplicates in batch creation")
    void shouldHandleDuplicatesInBatchCreation() throws Exception {
        CreateDealRequest request1 = CreateDealRequest.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        CreateDealRequest request2 = CreateDealRequest.builder()
                .dealId("DEAL002")
                .fromCurrency("GBP")
                .toCurrency("JPY")
                .dealTimestamp("2025-01-15T11:00:00")
                .dealAmount("2000.75")
                .build();

        BatchCreateRequest batchRequest = BatchCreateRequest.builder()
                .deals(Arrays.asList(request1, request2))
                .build();

        when(dealService.saveDeal(any(Deal.class)))
                .thenReturn(true)  // First deal succeeds
                .thenReturn(false); // Second deal is duplicate

        mockMvc.perform(post("/api/deals/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulRecords").value(1))
                .andExpect(jsonPath("$.duplicateRecords").value(1))
                .andExpect(jsonPath("$.warnings", hasSize(1)));
    }

    @Test
    @DisplayName("Should get deal by ID successfully")
    void shouldGetDealByIdSuccessfully() throws Exception {
        when(dealService.getDealById("DEAL001")).thenReturn(sampleDeal);

        mockMvc.perform(get("/api/deals/DEAL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value("DEAL001"))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"));

        verify(dealService, times(1)).getDealById("DEAL001");
    }

    @Test
    @DisplayName("Should return 404 when deal not found")
    void shouldReturn404WhenDealNotFound() throws Exception {
        when(dealService.getDealById(anyString()))
                .thenThrow(new EntityNotFoundException("Deal with ID 'DEAL999' not found"));

        mockMvc.perform(get("/api/deals/DEAL999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all deals with pagination")
    void shouldGetAllDealsWithPagination() throws Exception {
        List<Deal> deals = Arrays.asList(sampleDeal);
        Page<Deal> page = new PageImpl<>(deals, PageRequest.of(0, 20), 1);

        when(dealService.getAllDeals(any())).thenReturn(page);

        mockMvc.perform(get("/api/deals")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].dealId").value("DEAL001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get total deals count")
    void shouldGetTotalDealsCount() throws Exception {
        when(dealService.getTotalDealsCount()).thenReturn(42L);

        mockMvc.perform(get("/api/deals/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        verify(dealService, times(1)).getTotalDealsCount();
    }

    @Test
    @DisplayName("Should return health check status")
    void shouldReturnHealthCheckStatus() throws Exception {
        mockMvc.perform(get("/api/deals/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("ClusteredData Warehouse is running"));
    }

    @Test
    @DisplayName("Should handle blacklisted currencies")
    void shouldHandleBlacklistedCurrencies() throws Exception {
        CreateDealRequest invalidRequest = CreateDealRequest.builder()
                .dealId("DEAL006")
                .fromCurrency("XXX")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should trim and uppercase currency codes")
    void shouldTrimAndUppercaseCurrencyCodes() throws Exception {
        CreateDealRequest requestWithLowercase = CreateDealRequest.builder()
                .dealId("DEAL007")
                .fromCurrency(" usd ")
                .toCurrency(" eur ")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .build();

        when(dealService.createDeal(any(Deal.class))).thenReturn(sampleDeal);

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithLowercase)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"));
    }
}