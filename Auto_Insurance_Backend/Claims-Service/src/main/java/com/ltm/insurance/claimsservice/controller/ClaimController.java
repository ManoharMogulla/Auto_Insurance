package com.ltm.insurance.claimsservice.controller;

import com.ltm.insurance.claimsservice.dto.ClaimResponse;
import com.ltm.insurance.claimsservice.dto.CreateClaimRequest;
import com.ltm.insurance.claimsservice.dto.UpdateClaimStatusRequest;
import com.ltm.insurance.claimsservice.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping("/health")
    public String health() {
        return "Claims service is up";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse createClaim(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateClaimRequest request) {
        return claimService.createClaim(customerId(jwt), request);
    }

    @GetMapping("/{claimId}")
    public ClaimResponse getById(
            @PathVariable String claimId,
            @AuthenticationPrincipal Jwt jwt) {
        return claimService.getClaimById(claimId, customerId(jwt));
    }

    @GetMapping("/customer/{customerId}")
    public List<ClaimResponse> getByCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal Jwt jwt) {
        return claimService.getClaimsByCustomer(customerId, customerId(jwt));
    }

    @GetMapping("/policy/{policyId}")
    public List<ClaimResponse> getByPolicy(
            @PathVariable String policyId,
            @AuthenticationPrincipal Jwt jwt) {
        return claimService.getClaimsByPolicy(policyId, customerId(jwt));
    }

    @PatchMapping("/{claimId}/status")
    public ClaimResponse updateStatus(
            @PathVariable String claimId,
            @Valid @RequestBody UpdateClaimStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return claimService.updateStatus(claimId, request.status(), customerId(jwt));
    }

    private Long customerId(Jwt jwt) {
        Object value = jwt.getClaims().get("customerId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // handled below
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT does not contain a valid customer ID");
    }
}
