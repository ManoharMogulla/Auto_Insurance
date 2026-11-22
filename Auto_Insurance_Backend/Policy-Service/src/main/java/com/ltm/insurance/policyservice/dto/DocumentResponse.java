package com.ltm.insurance.policyservice.dto;


import com.ltm.insurance.policyservice.enums.DocumentStatus;
import com.ltm.insurance.policyservice.enums.DocumentType;
import com.ltm.insurance.policyservice.enums.EmailStatus;

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