package com.ltm.insurance.policyservice.client;

import com.ltm.insurance.policyservice.dto.DocumentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @FeignClient(name = "DOCUMENT-SERVICE")
    public interface DocumentClient {

        @PostMapping("/api/documents/policies/{policyId}/generate-and-email")
        DocumentResponse generateAndEmail(
                @PathVariable("policyId") String policyId,
                @RequestHeader("Authorization") String authorizationHeader
        );
    }
//    }
//
//        @PostMapping("/policies/{policyId}/generate")
//        public DocumentResponse generate(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt);
//
//        @PostMapping("/policies/{policyId}/regenerate")
//        public DocumentResponse regenerate(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt) ;
//
//        @PostMapping("/policies/{policyId}/generate-and-email")
//        public DocumentResponse generateAndEmail(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt);
//
//        @GetMapping("/{documentId}")
//        public DocumentResponse getDocument(@PathVariable String documentId, @AuthenticationPrincipal Jwt jwt);
//
//        @GetMapping("/policies/{policyId}")
//        public List<DocumentResponse> getPolicyDocuments(@PathVariable String policyId, @AuthenticationPrincipal Jwt jwt);
//
//        @GetMapping("/customers/{customerId}")
//        public List<DocumentResponse> getCustomerDocuments(@PathVariable long customerId, @AuthenticationPrincipal Jwt jwt);
//
//        @GetMapping("/{documentId}/download")
//        public ResponseEntity<byte[]> download(@PathVariable String documentId,
//                                               @RequestParam(defaultValue = "attachment") String disposition,
//                                               @AuthenticationPrincipal Jwt jwt);
//        @GetMapping("/{documentId}/view")
//        public ResponseEntity<byte[]> view(@PathVariable String documentId, @AuthenticationPrincipal Jwt jwt);
//
//        @PostMapping("/{documentId}/email")
//        public DocumentResponse email(@PathVariable String documentId, @Valid @RequestBody EmailRequest request,
//                                      @AuthenticationPrincipal Jwt jwt);
//    }
//}
