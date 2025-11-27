package com.progressoft.fxdeals.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FX Deal response")
public class DealResponse {

    @Schema(description = "Unique deal identifier", example = "DEAL001")
    private String dealId;

    @Schema(description = "Source currency", example = "USD")
    private String fromCurrency;

    @Schema(description = "Target currency", example = "EUR")
    private String toCurrency;

    @Schema(description = "When the deal occurred", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dealTimestamp;

    @Schema(description = "Deal amount", example = "1000.50")
    private BigDecimal dealAmount;

    @Schema(description = "When the record was created in the system", example = "2025-01-15T10:31:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}