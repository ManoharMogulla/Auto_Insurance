package com.ltm.insurance.documentservice.repository;

import com.ltm.insurance.documentservice.entity.Document;
import com.ltm.insurance.documentservice.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {
        Optional<Document> findFirstByPolicyIdAndCustomerIdAndDocumentStatusOrderByGeneratedAtDesc(
            String policyId, Long customerId, DocumentStatus status);
    List<Document> findByPolicyIdAndCustomerIdOrderByGeneratedAtDesc(String policyId, Long customerId);
    List<Document> findByCustomerIdOrderByGeneratedAtDesc(Long customerId);
}