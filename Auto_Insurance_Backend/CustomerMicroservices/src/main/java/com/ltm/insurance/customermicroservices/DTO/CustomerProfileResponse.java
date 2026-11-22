package com.ltm.insurance.customermicroservices.DTO;

public record CustomerProfileResponse(Long memberId, Long customerId, String firstName, String lastName,
                                      String email, String phoneNumber) {
}
