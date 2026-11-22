package com.ltm.insurance.authentication.client;

public record CustomerProfileResponse(Long memberId, Long customerId, String firstName, String lastName,
                                      String email, String phoneNumber) {
}
