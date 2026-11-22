package com.ltm.insurance.documentservice.exception;

public class UnauthorizedDocumentAccessException extends RuntimeException {
    public UnauthorizedDocumentAccessException(String message) { super(message); }
}