package com.ltm.insurance.authentication.client;

public record CustomerProfileRequest(String firstName, String lastName, String email, String phoneNumber) {
}
