package com.progressoft.fxdeals.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealDTO {

    @CsvBindByName(column = "dealId", required = true)
    private String dealId;

    @CsvBindByName(column = "fromCurrency", required = true)
    private String fromCurrency;

    @CsvBindByName(column = "toCurrency", required = true)
    private String toCurrency;

    @CsvBindByName(column = "dealTimestamp", required = true)
    private String dealTimestamp;

    @CsvBindByName(column = "dealAmount", required = true)
    private String dealAmount;

    private LocalDateTime parsedTimestamp;
    private BigDecimal parsedAmount;
}