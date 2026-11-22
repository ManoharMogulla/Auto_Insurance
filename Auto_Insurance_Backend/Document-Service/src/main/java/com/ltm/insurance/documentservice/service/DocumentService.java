package com.ltm.insurance.documentservice.service;

import com.ltm.insurance.documentservice.client.CustomerClient;
import com.ltm.insurance.documentservice.client.PaymentClient;
import com.ltm.insurance.documentservice.client.PolicyClient;
import com.ltm.insurance.documentservice.client.PremiumClient;
import com.ltm.insurance.documentservice.dto.DocumentResponse;
import com.ltm.insurance.documentservice.dto.EmailRequest;
import com.ltm.insurance.documentservice.dto.PaymentDto;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import com.ltm.insurance.documentservice.dto.PremiumDto;
import com.ltm.insurance.documentservice.entity.Document;
import com.ltm.insurance.documentservice.enums.DocumentStatus;
import com.ltm.insurance.documentservice.enums.DocumentType;
import com.ltm.insurance.documentservice.enums.EmailStatus;
import com.ltm.insurance.documentservice.exception.CustomerNotFoundException;
import com.ltm.insurance.documentservice.exception.DocumentGenerationException;
import com.ltm.insurance.documentservice.exception.DocumentNotFoundException;
import com.ltm.insurance.documentservice.exception.DownstreamServiceException;
import com.ltm.insurance.documentservice.exception.EmailDeliveryException;
import com.ltm.insurance.documentservice.exception.InvalidPolicyStatusException;
import com.ltm.insurance.documentservice.exception.PolicyNotFoundException;
import com.ltm.insurance.documentservice.exception.UnauthorizedDocumentAccessException;
import com.ltm.insurance.documentservice.repository.DocumentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final PolicyClient policyClient;
    private final CustomerClient customerClient;
    private final PremiumClient premiumClient;
    private final PaymentClient paymentClient;
    private final DocumentRepository documentRepository;
    private final PolicyPdfService pdfService;
    private final StorageService storageService;
    private final DocumentEmailService emailService;

    @Transactional
    public synchronized DocumentResponse generate(String policyId, Jwt jwt, boolean regenerate) {
        long customerId = customerId(jwt);
        PolicyData data = loadPolicyData(policyId, customerId);
        if (!regenerate) {
            var existing = documentRepository.findFirstByPolicyIdAndCustomerIdAndDocumentStatusOrderByGeneratedAtDesc(
                    policyId, customerId, DocumentStatus.GENERATED);
            if (existing.isPresent()) return response(existing.get());
        }
        data = loadCompletePolicyData(data);
        return response(createDocument(data));
    }

    public synchronized DocumentResponse generateAndEmail(String policyId, Jwt jwt) {
        DocumentResponse generated;
        try {
            generated = generate(policyId, jwt, false);
        } catch (RuntimeException exception) {
            log.warn("Automatic policy document generation failed for policy {} ({})",
                    policyId, exception.getClass().getSimpleName());
            throw exception;
        }
        if (generated.emailStatus() == EmailStatus.SENT) return generated;
        if (generated.recipientEmail() == null || generated.recipientEmail().isBlank()) {
            log.warn("No registered email is available for document {}", generated.documentId());
            return generated;
        }
        try {
            return sendEmail(generated.documentId(), new EmailRequest(generated.recipientEmail()), jwt);
        } catch (EmailDeliveryException exception) {
                log.warn("Policy document email failed for document {} ({})",
                    generated.documentId(), exception.getClass().getSimpleName());
            return getDocument(generated.documentId(), jwt);
        }
    }

    public DocumentResponse getDocument(String documentId, Jwt jwt) {
        return response(ownedDocument(documentId, customerId(jwt)));
    }

    public List<DocumentResponse> getPolicyDocuments(String policyId, Jwt jwt) {
        long customerId = customerId(jwt);
        loadPolicyData(policyId, customerId);
        return documentRepository.findByPolicyIdAndCustomerIdOrderByGeneratedAtDesc(policyId, customerId)
                .stream().map(this::response).toList();
    }

    public List<DocumentResponse> getCustomerDocuments(long requestedCustomerId, Jwt jwt) {
        long customerId = customerId(jwt);
        if (requestedCustomerId != customerId) {
            throw new UnauthorizedDocumentAccessException("You cannot view another customer's documents");
        }
        return documentRepository.findByCustomerIdOrderByGeneratedAtDesc(customerId)
                .stream().map(this::response).toList();
    }

    public byte[] download(String documentId, Jwt jwt) {
        return storageService.read(ownedDocument(documentId, customerId(jwt)).getFilePath());
    }

    public synchronized DocumentResponse sendEmail(String documentId, EmailRequest request, Jwt jwt) {
        long customerId = customerId(jwt);
        Document document = ownedDocument(documentId, customerId);
        if (document.getEmailStatus() == EmailStatus.PENDING) {
            throw new InvalidPolicyStatusException("An email is already being sent for this document");
        }
        PolicyData data = loadCompletePolicyData(loadPolicyData(document.getPolicyId(), customerId));
        document.setRecipientEmail(request.recipientEmail().trim());
        document.setEmailStatus(EmailStatus.PENDING);
        documentRepository.save(document);
        try {
            emailService.send(document.getRecipientEmail(), document.getFileName(),
                    storageService.read(document.getFilePath()), data.policy(), data.primaryDriver(), data.payment());
            document.setEmailStatus(EmailStatus.SENT);
            document.setEmailedAt(LocalDateTime.now());
        } catch (RuntimeException exception) {
            document.setEmailStatus(EmailStatus.FAILED);
            documentRepository.save(document);
            throw exception;
        }
        return response(documentRepository.save(document));
    }

    private Document createDocument(PolicyData data) {
        String documentId = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String fileName = safeFileName(data.policy().getPolicyId() + "_" + documentId + ".pdf");
        LocalDateTime generatedAt = LocalDateTime.now();
        try {
            byte[] pdf = pdfService.generate(documentId, generatedAt, data.policy(), data.drivers(), data.premium(), data.payment());
            String filePath = storageService.save(fileName, pdf);
            Document document = new Document();
            document.setDocumentId(documentId);
            document.setPolicyId(data.policy().getPolicyId());
            document.setCustomerId(data.customerId());
            document.setDocumentType(DocumentType.POLICY_DOCUMENT);
            document.setFileName(fileName);
            document.setFilePath(filePath);
            document.setGeneratedAt(generatedAt);
            document.setRecipientEmail(data.primaryDriver().getEmail());
            document.setEmailStatus(EmailStatus.NOT_SENT);
            document.setDocumentStatus(DocumentStatus.GENERATED);
            return documentRepository.save(document);
        } catch (RuntimeException exception) {
            throw exception instanceof DocumentGenerationException
                    ? exception
                    : new DocumentGenerationException("Unable to generate policy document", exception);
        }
    }

    private PolicyData loadPolicyData(String policyId, long customerId) {
        PolicyDto policy;
        try {
            policy = policyClient.getPolicy(policyId);
        } catch (FeignException.NotFound exception) {
            throw new PolicyNotFoundException("Policy not found: " + policyId);
        } catch (FeignException exception) {
            throw new DownstreamServiceException("Policy Service is unavailable", exception);
        }
        if (policy == null) throw new PolicyNotFoundException("Policy not found: " + policyId);

        List<PolicyDto.DriverDto> drivers;
        try {
            drivers = customerClient.getDriversByPolicyId(policyId);
        } catch (FeignException exception) {
            throw new DownstreamServiceException("Customer Service is unavailable", exception);
        }
        if (drivers == null || drivers.isEmpty()) throw new CustomerNotFoundException("No customer is associated with this policy");
        PolicyDto.DriverDto primary = drivers.stream().filter(PolicyDto.DriverDto::isNamedInsured)
                .findFirst().orElse(drivers.get(0));
        boolean belongsToCustomer = drivers.stream().anyMatch(driver -> driver.getCustomerId() != null
                && driver.getCustomerId() == customerId);
        if (!belongsToCustomer) throw new UnauthorizedDocumentAccessException("This policy does not belong to the authenticated customer");
        return new PolicyData(policy, drivers, primary, null, null, customerId);
    }

    private PolicyData loadCompletePolicyData(PolicyData data) {
        String status = data.policy().getPolicyStatus() == null
                ? "" : data.policy().getPolicyStatus().trim().toUpperCase(Locale.ROOT);
        if (!List.of("ACTIVE", "ISSUED", "COMPLETED").contains(status)) {
            throw new InvalidPolicyStatusException("Policy must be ACTIVE, ISSUED, or COMPLETED before document generation");
        }
        if (data.policy().getPremiumAmount() == null) {
            throw new InvalidPolicyStatusException("Premium calculation is incomplete for this policy");
        }
        PremiumDto premium;
        try {
            premium = premiumClient.calculatePremium(new PremiumClient.PremiumRequest(data.drivers()));
        } catch (FeignException exception) {
            throw new DownstreamServiceException("Premium Service is unavailable", exception);
        }
        if (premium == null || premium.getFinalPremium() == null) {
            throw new InvalidPolicyStatusException("Premium calculation is incomplete for this policy");
        }
        PaymentDto payment = null;
        try {
            List<PaymentDto> payments = paymentClient.getPaymentsForPolicy(data.policy().getPolicyId());
            if (payments != null && !payments.isEmpty()) {
                payment = payments.stream()
                        .filter(item -> "COMPLETED".equalsIgnoreCase(item.getPaymentStatus()))
                        .findFirst().orElse(payments.get(0));
            }
        } catch (FeignException exception) {
            throw new DownstreamServiceException("Payment Service is unavailable", exception);
        }
        if (payment == null || payment.getPaymentStatus() == null
                || !payment.getPaymentStatus().equalsIgnoreCase("COMPLETED")) {
            throw new InvalidPolicyStatusException("A successful payment is required before document generation");
        }
        return new PolicyData(data.policy(), data.drivers(), data.primaryDriver(), premium, payment, data.customerId());
    }

    private Document ownedDocument(String documentId, long customerId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        if (!document.getCustomerId().equals(customerId)) {
            throw new UnauthorizedDocumentAccessException("You cannot access this document");
        }
        return document;
    }

    private long customerId(Jwt jwt) {
        Object value = jwt.getClaims().get("customerId");
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                throw new UnauthorizedDocumentAccessException("JWT does not contain a valid customer ID");
            }
        }
        throw new UnauthorizedDocumentAccessException("JWT does not contain a valid customer ID");
    }

    private DocumentResponse response(Document document) {
        return new DocumentResponse(document.getDocumentId(), document.getPolicyId(), document.getCustomerId(),
                document.getDocumentType(), document.getFileName(), document.getGeneratedAt(),
                document.getRecipientEmail(), document.getEmailStatus(), document.getEmailedAt(), document.getDocumentStatus());
    }

    private String safeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private record PolicyData(PolicyDto policy, List<PolicyDto.DriverDto> drivers,
                              PolicyDto.DriverDto primaryDriver, PremiumDto premium,
                              PaymentDto payment, long customerId) { }
}