package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealService Tests")
class DealServiceImplTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal validDeal;

    @BeforeEach
    void setUp() {
        validDeal = Deal.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.of(2025, 1, 15, 10, 30))
                .dealAmount(new BigDecimal("1000.50"))
                .build();
    }

    @Test
    @DisplayName("Should save valid deal successfully")
    void shouldSaveValidDealSuccessfully() {
        when(dealRepository.existsById("DEAL001")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenReturn(validDeal);

        boolean result = dealService.saveDeal(validDeal);

        assertTrue(result);
        verify(dealRepository, times(1)).existsById("DEAL001");
        verify(dealRepository, times(1)).save(validDeal);
    }

    @Test
    @DisplayName("Should return false for duplicate deal ID")
    void shouldReturnFalseForDuplicateDealId() {
        when(dealRepository.existsById("DEAL001")).thenReturn(true);

        boolean result = dealService.saveDeal(validDeal);

        assertFalse(result);
        verify(dealRepository, times(1)).existsById("DEAL001");
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException gracefully")
    void shouldHandleDataIntegrityViolationExceptionGracefully() {
        when(dealRepository.existsById("DEAL001")).thenReturn(false);
        when(dealRepository.save(any(Deal.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        boolean result = dealService.saveDeal(validDeal);

        assertFalse(result);
        verify(dealRepository, times(1)).existsById("DEAL001");
        verify(dealRepository, times(1)).save(validDeal);
    }

    @Test
    @DisplayName("Should handle multiple saves correctly")
    void shouldHandleMultipleSavesCorrectly() {
        Deal deal1 = Deal.builder()
                .dealId("DEAL001")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.now())
                .dealAmount(new BigDecimal("100"))
                .build();

        Deal deal2 = Deal.builder()
                .dealId("DEAL002")
                .fromCurrency("GBP")
                .toCurrency("USD")
                .dealTimestamp(LocalDateTime.now())
                .dealAmount(new BigDecimal("200"))
                .build();

        when(dealRepository.existsById("DEAL001")).thenReturn(false);
        when(dealRepository.existsById("DEAL002")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenReturn(deal1, deal2);

        assertTrue(dealService.saveDeal(deal1));
        assertTrue(dealService.saveDeal(deal2));

        verify(dealRepository, times(2)).save(any(Deal.class));
    }

    @Test
    @DisplayName("Should not save when deal already exists")
    void shouldNotSaveWhenDealAlreadyExists() {
        when(dealRepository.existsById("DEAL001")).thenReturn(true);

        boolean firstResult = dealService.saveDeal(validDeal);
        boolean secondResult = dealService.saveDeal(validDeal);

        assertFalse(firstResult);
        assertFalse(secondResult);
        verify(dealRepository, times(2)).existsById("DEAL001");
        verify(dealRepository, never()).save(any(Deal.class));
    }
}