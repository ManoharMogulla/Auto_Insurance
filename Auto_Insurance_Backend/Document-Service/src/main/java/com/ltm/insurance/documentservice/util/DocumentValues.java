package com.ltm.insurance.documentservice.util;

public final class DocumentValues {
    private DocumentValues() { }

    public static String value(Object value) {
        return value == null || value.toString().isBlank() ? "Not Available" : value.toString();
    }
}