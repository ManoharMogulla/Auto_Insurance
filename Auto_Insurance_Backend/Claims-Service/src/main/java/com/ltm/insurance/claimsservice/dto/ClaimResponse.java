package com.ltm.insurance.claimsservice.dto;

import com.ltm.insurance.claimsservice.entity.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponse(
        String claimId,
        Long customerId,
        String policyId,
        String claimType,
        LocalDate incidentDate,
        String incidentLocation,
        String description,
        BigDecimal estimatedDamageAmount,
        ClaimStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
