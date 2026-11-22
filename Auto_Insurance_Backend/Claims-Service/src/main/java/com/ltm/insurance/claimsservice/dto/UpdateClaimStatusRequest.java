package com.ltm.insurance.claimsservice.dto;

import com.ltm.insurance.claimsservice.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateClaimStatusRequest(
        @NotNull ClaimStatus status
) {
}
