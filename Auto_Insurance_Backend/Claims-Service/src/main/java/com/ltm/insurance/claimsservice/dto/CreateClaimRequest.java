package com.ltm.insurance.claimsservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotNull Long customerId,
        @NotBlank String policyId,
        @NotBlank String claimType,
        @NotNull @PastOrPresent LocalDate incidentDate,
        @NotBlank String incidentLocation,
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal estimatedDamageAmount
) {
}
