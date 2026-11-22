package com.ltm.insurance.claimsservice.dto;

public record DriverInfo(
        Long memberId,
        Long customerId,
        boolean namedInsured,
        String policyId
) {
}
