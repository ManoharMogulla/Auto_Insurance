package com.ltm.insurance.authentication.dto;

public record CustomerIdentityResponse(
        Long memberId,
        Long customerId,
        String firstName,
        String lastName,
        String name,
        String email,
        String phoneNumber,
        String role
) {
}
