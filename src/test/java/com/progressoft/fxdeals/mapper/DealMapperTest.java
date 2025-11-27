package com.progressoft.fxdeals.mapper;

import com.progressoft.fxdeals.dto.DealDTO;
import com.progressoft.fxdeals.model.Deal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DealMapper Tests")
class DealMapperTest {

    @Test
    @DisplayName("Should map DealDTO to Deal entity")
    void shouldMapDealDTOToDealEntity() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
        BigDecimal amount = new BigDecimal("1000.50");

        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp("2025-01-15T10:30:00")
                .dealAmount("1000.50")
                .parsedTimestamp(timestamp)
                .parsedAmount(amount)
                .build();

        Deal deal = DealMapper.toEntity(dto);

        assertNotNull(deal);
        assertEquals("DEAL001", deal.getDealId());
        assertEquals("USD", deal.getFromCurrency());
        assertEquals("EUR", deal.getToCurrency());
        assertEquals(timestamp, deal.getDealTimestamp());
        assertEquals(amount, deal.getDealAmount());
    }

    @Test
    @DisplayName("Should trim dealId when mapping to entity")
    void shouldTrimDealIdWhenMappingToEntity() {
        DealDTO dto = DealDTO.builder()
                .dealId("  DEAL001  ")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .parsedTimestamp(LocalDateTime.now())
                .parsedAmount(new BigDecimal("100"))
                .build();

        Deal deal = DealMapper.toEntity(dto);

        assertEquals("DEAL001", deal.getDealId());
    }

    @Test
    @DisplayName("Should map Deal entity to DealDTO")
    void shouldMapDealEntityToDealDTO() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
        BigDecimal amount = new BigDecimal("1000.50");

        Deal deal = Deal.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(timestamp)
                .dealAmount(amount)
                .build();

        DealDTO dto = DealMapper.toDTO(deal);

        assertNotNull(dto);
        assertEquals("DEAL001", dto.getDealId());
        assertEquals("USD", dto.getFromCurrency());
        assertEquals("EUR", dto.getToCurrency());
        assertEquals(timestamp.toString(), dto.getDealTimestamp());
        assertEquals(amount.toString(), dto.getDealAmount());
        assertEquals(timestamp, dto.getParsedTimestamp());
        assertEquals(amount, dto.getParsedAmount());
    }

    @Test
    @DisplayName("Should preserve precision in amount mapping")
    void shouldPreservePrecisionInAmountMapping() {
        BigDecimal preciseAmount = new BigDecimal("123456.7890");

        DealDTO dto = DealDTO.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .parsedTimestamp(LocalDateTime.now())
                .parsedAmount(preciseAmount)
                .build();

        Deal deal = DealMapper.toEntity(dto);

        assertEquals(preciseAmount, deal.getDealAmount());
        assertEquals(0, preciseAmount.compareTo(deal.getDealAmount()));
    }
}