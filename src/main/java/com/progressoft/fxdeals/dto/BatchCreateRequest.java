package com.progressoft.fxdeals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create multiple FX deals")
public class BatchCreateRequest {

    @NotEmpty(message = "Deals list cannot be empty")
    @Valid
    @Schema(description = "List of deals to create", required = true)
    private List<CreateDealRequest> deals;
}