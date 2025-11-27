package com.progressoft.fxdeals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a single FX deal")
public class CreateDealRequest {

    @NotBlank(message = "Deal ID is required")
    @Schema(description = "Unique deal identifier", example = "DEAL001", required = true)
    private String dealId;

    @NotBlank(message = "From currency is required")
    @Schema(description = "ISO 4217 currency code (from)", example = "USD", required = true)
    private String fromCurrency;

    @NotBlank(message = "To currency is required")
    @Schema(description = "ISO 4217 currency code (to)", example = "EUR", required = true)
    private String toCurrency;

    @NotBlank(message = "Deal timestamp is required")
    @Schema(description = "Deal timestamp in ISO 8601 format", example = "2025-01-15T10:30:00", required = true)
    private String dealTimestamp;

    @NotBlank(message = "Deal amount is required")
    @Schema(description = "Deal amount (must be positive)", example = "1000.50", required = true)
    private String dealAmount;
}