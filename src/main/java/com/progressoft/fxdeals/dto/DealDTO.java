package com.progressoft.fxdeals.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealDTO {

    @CsvBindByName(column = "dealId")
    private String dealId;

    @CsvBindByName(column = "fromCurrency")
    private String fromCurrency;

    @CsvBindByName(column = "toCurrency")
    private String toCurrency;

    @CsvBindByName(column = "dealTimestamp")
    private String dealTimestamp;

    @CsvBindByName(column = "dealAmount")
    private String dealAmount;

    private LocalDateTime parsedTimestamp;
    private BigDecimal parsedAmount;
}