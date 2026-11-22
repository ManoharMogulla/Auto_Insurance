package com.ltm.insurance.documentservice.service;

import com.ltm.insurance.documentservice.client.CustomerClient;
import com.ltm.insurance.documentservice.client.PaymentClient;
import com.ltm.insurance.documentservice.client.PolicyClient;
import com.ltm.insurance.documentservice.client.PremiumClient;
import com.ltm.insurance.documentservice.dto.EmailRequest;
import com.ltm.insurance.documentservice.dto.PaymentDto;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import com.ltm.insurance.documentservice.dto.PremiumDto;
import com.ltm.insurance.documentservice.entity.Document;
import com.ltm.insurance.documentservice.enums.DocumentStatus;
import com.ltm.insurance.documentservice.enums.DocumentType;
import com.ltm.insurance.documentservice.enums.EmailStatus;
import com.ltm.insurance.documentservice.exception.EmailDeliveryException;
import com.ltm.insurance.documentservice.exception.DocumentNotFoundException;
import com.ltm.insurance.documentservice.exception.InvalidPolicyStatusException;
import com.ltm.insurance.documentservice.exception.UnauthorizedDocumentAccessException;
import com.ltm.insurance.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentServiceTest {

    private final PolicyClient policyClient = mock(PolicyClient.class);
    private final CustomerClient customerClient = mock(CustomerClient.class);
    private final PremiumClient premiumClient = mock(PremiumClient.class);
    private final PaymentClient paymentClient = mock(PaymentClient.class);
    private final DocumentRepository repository = mock(DocumentRepository.class);
    private final PolicyPdfService pdfService = mock(PolicyPdfService.class);
    private final StorageService storage = mock(StorageService.class);
    private final DocumentEmailService emailService = mock(DocumentEmailService.class);
    private final Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("customerId", 41L).build();
    private final PolicyDto policy = new PolicyDto();
    private final PolicyDto.DriverDto driver = new PolicyDto.DriverDto();
    private final PremiumDto premium = new PremiumDto();
    private final PaymentDto payment = new PaymentDto();
    private DocumentService service;

    @BeforeEach
    void setUp() {
        policy.setPolicyId("POL-12345678");
        policy.setPolicyStatus("Active");
        policy.setPolicyType("Issued");
        policy.setPremiumAmount(new BigDecimal("900.00"));
        driver.setCustomerId(41L);
        driver.setNamedInsured(true);
        driver.setEmail("jordan@example.com");
        driver.setFirstName("Jordan");
        driver.setLastName("Lee");
        premium.setFinalPremium(new BigDecimal("900.00"));
        payment.setPaymentStatus("COMPLETED");
        when(policyClient.getPolicy(anyString())).thenReturn(policy);
        when(customerClient.getDriversByPolicyId(anyString())).thenReturn(List.of(driver));
        when(premiumClient.calculatePremium(any())).thenReturn(premium);
        when(paymentClient.getPaymentsForPolicy(anyString())).thenReturn(List.of(payment));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pdfService.generate(anyString(), any(), any(), anyList(), any(), any())).thenReturn(new byte[]{1, 2, 3});
        when(storage.save(anyString(), any())).thenReturn("./generated-documents/policy.pdf");
        when(storage.read(anyString())).thenReturn(new byte[]{1, 2, 3});
        service = new DocumentService(policyClient, customerClient, premiumClient, paymentClient,
                repository, pdfService, storage, emailService);
    }

    @Test
    void successfullyGeneratesAndPersistsDocumentMetadata() {
        when(repository.findFirstByPolicyIdAndCustomerIdAndDocumentStatusOrderByGeneratedAtDesc(anyString(), any(), any()))
                .thenReturn(Optional.empty());

        var response = service.generate("POL-12345678", jwt, false);

        assertEquals("POL-12345678", response.policyId());
        assertEquals(DocumentStatus.GENERATED, response.documentStatus());
        assertEquals(EmailStatus.NOT_SENT, response.emailStatus());
        verify(pdfService).generate(anyString(), any(), any(), anyList(), any(), any());
    }

    @Test
    void rejectsInvalidPolicyStatus() {
        policy.setPolicyStatus("Pending Payment");

        assertThrows(InvalidPolicyStatusException.class, () -> service.generate("POL-12345678", jwt, false));
        verify(pdfService, never()).generate(anyString(), any(), any(), anyList(), any(), any());
    }

    @Test
    void reportsDocumentNotFound() {
        when(repository.findById("DOC-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> service.getDocument("DOC-UNKNOWN", jwt));
    }

    @Test
    void returnsExistingDocumentInsteadOfGeneratingDuplicate() {
        Document existing = document();
        when(repository.findFirstByPolicyIdAndCustomerIdAndDocumentStatusOrderByGeneratedAtDesc(anyString(), any(), any()))
                .thenReturn(Optional.of(existing));

        var response = service.generate("POL-12345678", jwt, false);

        assertEquals(existing.getDocumentId(), response.documentId());
        verify(storage, never()).save(anyString(), any());
        verify(pdfService, never()).generate(anyString(), any(), any(), anyList(), any(), any());
    }

    @Test
    void sendsEmailAndUpdatesStatus() {
        Document existing = document();
        when(repository.findById(existing.getDocumentId())).thenReturn(Optional.of(existing));

        var response = service.sendEmail(existing.getDocumentId(), new EmailRequest("new@example.com"), jwt);

        assertEquals(EmailStatus.SENT, response.emailStatus());
        assertNotNull(response.emailedAt());
        verify(emailService).send(anyString(), anyString(), any(), any(), any(), any());
    }

    @Test
    void emailFailurePreservesGeneratedFileAndMarksFailed() {
        Document existing = document();
        when(repository.findById(existing.getDocumentId())).thenReturn(Optional.of(existing));
        doThrow(new EmailDeliveryException("SMTP unavailable", new RuntimeException()))
                .when(emailService).send(anyString(), anyString(), any(), any(), any(), any());

        assertThrows(EmailDeliveryException.class,
                () -> service.sendEmail(existing.getDocumentId(), new EmailRequest("new@example.com"), jwt));

        assertEquals(EmailStatus.FAILED, existing.getEmailStatus());
        verify(storage, never()).delete(anyString());
    }

    @Test
    void deniesDocumentGenerationForAnotherCustomer() {
        driver.setCustomerId(99L);

        assertThrows(UnauthorizedDocumentAccessException.class,
                () -> service.generate("POL-12345678", jwt, false));
        verify(pdfService, never()).generate(anyString(), any(), any(), anyList(), any(), any());
    }

    @Test
    void deniesReadingAnotherCustomersDocument() {
        Document anotherCustomersDocument = document();
        anotherCustomersDocument.setCustomerId(99L);
        when(repository.findById(anotherCustomersDocument.getDocumentId())).thenReturn(Optional.of(anotherCustomersDocument));

        assertThrows(UnauthorizedDocumentAccessException.class,
                () -> service.getDocument(anotherCustomersDocument.getDocumentId(), jwt));
    }

    private Document document() {
        Document document = new Document();
        document.setDocumentId("DOC-A12B34CD");
        document.setPolicyId("POL-12345678");
        document.setCustomerId(41L);
        document.setDocumentType(DocumentType.POLICY_DOCUMENT);
        document.setFileName("policy.pdf");
        document.setFilePath("./generated-documents/policy.pdf");
        document.setGeneratedAt(LocalDateTime.now());
        document.setRecipientEmail("jordan@example.com");
        document.setEmailStatus(EmailStatus.NOT_SENT);
        document.setDocumentStatus(DocumentStatus.GENERATED);
        return document;
    }
}