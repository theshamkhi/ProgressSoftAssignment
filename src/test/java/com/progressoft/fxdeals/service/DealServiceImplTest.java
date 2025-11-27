package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.exception.DuplicateRecordException;
import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.repository.DealRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Deal Service Tests")
class DealServiceImplTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal sampleDeal;

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
    }

    @Test
    @DisplayName("Should save deal successfully")
    void shouldSaveDealSuccessfully() {
        when(dealRepository.save(any(Deal.class))).thenReturn(sampleDeal);

        boolean result = dealService.saveDeal(sampleDeal);

        assertThat(result).isTrue();
        verify(dealRepository, times(1)).save(sampleDeal);
    }

    @Test
    @DisplayName("Should return false when saving duplicate deal")
    void shouldReturnFalseWhenSavingDuplicateDeal() {
        when(dealRepository.save(any(Deal.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        boolean result = dealService.saveDeal(sampleDeal);

        assertThat(result).isFalse();
        verify(dealRepository, times(1)).save(sampleDeal);
    }

    @Test
    @DisplayName("Should create deal successfully")
    void shouldCreateDealSuccessfully() {
        when(dealRepository.save(any(Deal.class))).thenReturn(sampleDeal);

        Deal result = dealService.createDeal(sampleDeal);

        assertThat(result).isNotNull();
        assertThat(result.getDealId()).isEqualTo("DEAL001");
        assertThat(result.getFromCurrency()).isEqualTo("USD");
        assertThat(result.getToCurrency()).isEqualTo("EUR");
        assertThat(result.getDealAmount()).isEqualByComparingTo(new BigDecimal("1000.50"));
        verify(dealRepository, times(1)).save(sampleDeal);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate deal")
    void shouldThrowExceptionWhenCreatingDuplicateDeal() {
        when(dealRepository.save(any(Deal.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> dealService.createDeal(sampleDeal))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("Deal with ID 'DEAL001' already exists");

        verify(dealRepository, times(1)).save(sampleDeal);
    }

    @Test
    @DisplayName("Should get deal by ID successfully")
    void shouldGetDealByIdSuccessfully() {
        when(dealRepository.findById("DEAL001")).thenReturn(Optional.of(sampleDeal));

        Deal result = dealService.getDealById("DEAL001");

        assertThat(result).isNotNull();
        assertThat(result.getDealId()).isEqualTo("DEAL001");
        verify(dealRepository, times(1)).findById("DEAL001");
    }

    @Test
    @DisplayName("Should throw exception when deal not found")
    void shouldThrowExceptionWhenDealNotFound() {
        when(dealRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.getDealById("DEAL999"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deal with ID 'DEAL999' not found");

        verify(dealRepository, times(1)).findById("DEAL999");
    }

    @Test
    @DisplayName("Should get all deals with pagination")
    void shouldGetAllDealsWithPagination() {
        Deal deal2 = Deal.builder()
                .dealId("DEAL002")
                .fromCurrency("GBP")
                .toCurrency("JPY")
                .dealTimestamp(LocalDateTime.of(2025, 1, 15, 11, 0, 0))
                .dealAmount(new BigDecimal("2000.75"))
                .createdAt(LocalDateTime.now())
                .build();

        List<Deal> deals = Arrays.asList(sampleDeal, deal2);
        Page<Deal> page = new PageImpl<>(deals, PageRequest.of(0, 20), 2);

        when(dealRepository.findAll(any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Deal> result = dealService.getAllDeals(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getDealId()).isEqualTo("DEAL001");
        assertThat(result.getContent().get(1).getDealId()).isEqualTo("DEAL002");
        verify(dealRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no deals exist")
    void shouldReturnEmptyPageWhenNoDealsExist() {
        Page<Deal> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(dealRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Deal> result = dealService.getAllDeals(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(dealRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get total deals count")
    void shouldGetTotalDealsCount() {
        when(dealRepository.count()).thenReturn(42L);

        long count = dealService.getTotalDealsCount();

        assertThat(count).isEqualTo(42L);
        verify(dealRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should return zero when no deals exist")
    void shouldReturnZeroWhenNoDealsExist() {
        when(dealRepository.count()).thenReturn(0L);

        long count = dealService.getTotalDealsCount();

        assertThat(count).isEqualTo(0L);
        verify(dealRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle concurrent save attempts gracefully")
    void shouldHandleConcurrentSaveAttemptsGracefully() {
        // First attempt succeeds, second fails due to duplicate
        when(dealRepository.save(any(Deal.class)))
                .thenReturn(sampleDeal)
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        boolean firstResult = dealService.saveDeal(sampleDeal);
        boolean secondResult = dealService.saveDeal(sampleDeal);

        assertThat(firstResult).isTrue();
        assertThat(secondResult).isFalse();
        verify(dealRepository, times(2)).save(sampleDeal);
    }

    @Test
    @DisplayName("Should preserve deal details when saving")
    void shouldPreserveDealDetailsWhenSaving() {
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Deal dealToSave = Deal.builder()
                .dealId("DEAL003")
                .fromCurrency("CHF")
                .toCurrency("CAD")
                .dealTimestamp(LocalDateTime.of(2025, 1, 20, 14, 45, 30))
                .dealAmount(new BigDecimal("5000.25"))
                .build();

        boolean result = dealService.saveDeal(dealToSave);

        assertThat(result).isTrue();
        verify(dealRepository).save(argThat(deal ->
                deal.getDealId().equals("DEAL003") &&
                        deal.getFromCurrency().equals("CHF") &&
                        deal.getToCurrency().equals("CAD") &&
                        deal.getDealAmount().compareTo(new BigDecimal("5000.25")) == 0
        ));
    }
}