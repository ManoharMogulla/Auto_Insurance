package com.ltm.insurance.documentservice.exception;

public class InvalidPolicyStatusException extends RuntimeException {
    public InvalidPolicyStatusException(String message) { super(message); }
}