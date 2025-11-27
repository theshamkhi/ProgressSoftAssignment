package com.progressoft.fxdeals.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealDTO {

    private String dealId;
    private String fromCurrency;
    private String toCurrency;
    private String dealTimestamp;
    private String dealAmount;

    private LocalDateTime parsedTimestamp;
    private BigDecimal parsedAmount;
}