package com.ltm.insurance.documentservice.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) { super(message); }
}