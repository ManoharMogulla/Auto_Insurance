package com.ltm.insurance.documentservice.exception;

public class DocumentGenerationException extends RuntimeException {
    public DocumentGenerationException(String message, Throwable cause) { super(message, cause); }
    public DocumentGenerationException(String message) { super(message); }
}