package com.ltm.insurance.documentservice.dto;

import com.ltm.insurance.documentservice.enums.DocumentStatus;
import com.ltm.insurance.documentservice.enums.DocumentType;
import com.ltm.insurance.documentservice.enums.EmailStatus;

import java.time.LocalDateTime;

public record DocumentResponse(
        String documentId,
        String policyId,
        Long customerId,
        DocumentType documentType,
        String fileName,
        LocalDateTime generatedAt,
        String recipientEmail,
        EmailStatus emailStatus,
        LocalDateTime emailedAt,
        DocumentStatus documentStatus
) { }