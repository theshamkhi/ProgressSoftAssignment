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
                .dealId(dealDTO.getDealId().trim())
                .fromCurrency(dealDTO.getFromCurrency())
                .toCurrency(dealDTO.getToCurrency())
                .dealTimestamp(dealDTO.getParsedTimestamp())
                .dealAmount(dealDTO.getParsedAmount())
                .build();
    }

    public static DealDTO toDTO(Deal deal) {
        return DealDTO.builder()
                .dealId(deal.getDealId())
                .fromCurrency(deal.getFromCurrency())
                .toCurrency(deal.getToCurrency())
                .dealTimestamp(deal.getDealTimestamp().toString())
                .dealAmount(deal.getDealAmount().toString())
                .parsedTimestamp(deal.getDealTimestamp())
                .parsedAmount(deal.getDealAmount())
                .build();
    }
}