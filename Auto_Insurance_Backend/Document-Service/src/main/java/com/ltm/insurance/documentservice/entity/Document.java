package com.ltm.insurance.documentservice.entity;

import com.ltm.insurance.documentservice.enums.DocumentStatus;
import com.ltm.insurance.documentservice.enums.DocumentType;
import com.ltm.insurance.documentservice.enums.EmailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_document_policy", columnList = "policy_id"),
        @Index(name = "idx_document_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Document {
    @Id
    @Column(length = 12)
    private String documentId;

    @Column(nullable = false)
    private String policyId;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus emailStatus;

    private LocalDateTime emailedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus documentStatus;
}