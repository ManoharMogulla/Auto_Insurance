package com.ltm.insurance.documentservice.controller;

import com.ltm.insurance.documentservice.dto.DocumentResponse;
import com.ltm.insurance.documentservice.dto.EmailRequest;
import com.ltm.insurance.documentservice.exception.InvalidPolicyStatusException;
import com.ltm.insurance.documentservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/policies/{policyId}/generate")
    public DocumentResponse generate(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.generate(policyId, jwt, false);
    }

    @PostMapping("/policies/{policyId}/regenerate")
    public DocumentResponse regenerate(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.generate(policyId, jwt, true);
    }

    @PostMapping("/policies/{policyId}/generate-and-email")
    public DocumentResponse generateAndEmail(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.generateAndEmail(policyId, jwt);
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(@PathVariable String documentId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.getDocument(documentId, jwt);
    }

    @GetMapping("/policies/{policyId}")
    public List<DocumentResponse> getPolicyDocuments(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.getPolicyDocuments(policyId, jwt);
    }

    @GetMapping("/customers/{customerId}")
    public List<DocumentResponse> getCustomerDocuments(@PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return documentService.getCustomerDocuments(customerId, jwt);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable String documentId,
                                           @RequestParam(defaultValue = "attachment") String disposition,
                                           @AuthenticationPrincipal Jwt jwt) {
        if (!List.of("attachment", "inline").contains(disposition.toLowerCase())) {
            throw new InvalidPolicyStatusException("Disposition must be attachment or inline");
        }
        DocumentResponse metadata = documentService.getDocument(documentId, jwt);
        byte[] contents = documentService.download(documentId, jwt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder(disposition.toLowerCase())
                .filename(metadata.fileName(), StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(contents);
    }

    @GetMapping("/{documentId}/view")
    public ResponseEntity<byte[]> view(@PathVariable String documentId, @AuthenticationPrincipal Jwt jwt) {
        return download(documentId, "inline", jwt);
    }

    @PostMapping("/{documentId}/email")
    public DocumentResponse email(@PathVariable String documentId, @Valid @RequestBody EmailRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return documentService.sendEmail(documentId, request, jwt);
    }
}