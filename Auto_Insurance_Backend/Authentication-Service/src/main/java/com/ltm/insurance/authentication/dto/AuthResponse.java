package com.ltm.insurance.authentication.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long memberId,
        Long customerId,
        String firstName,
        String lastName,
        String name,
        String email,
        String role
) {
}
