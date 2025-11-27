package com.progressoft.fxdeals.mapper;

import com.progressoft.fxdeals.dto.DealDTO;
import com.progressoft.fxdeals.model.Deal;

/**
 * Mapper for converting between Deal entity and DealDTO.
 */
public class DealMapper {

    private DealMapper() {}

    public static Deal toEntity(DealDTO dealDTO) {
        return Deal.builder()
                .dealId(dealDTO.getDealId())
                .fromCurrency(dealDTO.getFromCurrency())
                .toCurrency(dealDTO.getToCurrency())
                .dealTimestamp(dealDTO.getParsedTimestamp())
                .dealAmount(dealDTO.getParsedAmount())
                .build();
    }
}