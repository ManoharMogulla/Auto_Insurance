package com.ltm.insurance.claimsservice.service;

import com.ltm.insurance.claimsservice.client.CustomerClient;
import com.ltm.insurance.claimsservice.client.PolicyClient;
import com.ltm.insurance.claimsservice.dto.ClaimResponse;
import com.ltm.insurance.claimsservice.dto.CreateClaimRequest;
import com.ltm.insurance.claimsservice.dto.DriverInfo;
import com.ltm.insurance.claimsservice.dto.PolicySummary;
import com.ltm.insurance.claimsservice.entity.Claim;
import com.ltm.insurance.claimsservice.entity.ClaimStatus;
import com.ltm.insurance.claimsservice.exception.ApiException;
import com.ltm.insurance.claimsservice.repository.ClaimRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final Set<ClaimStatus> ACTIVE_CLAIM_STATUSES = Set.of(
            ClaimStatus.SUBMITTED,
            ClaimStatus.UNDER_REVIEW,
            ClaimStatus.APPROVED
    );

    private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;
    private final CustomerClient customerClient;

    @Transactional
    public ClaimResponse createClaim(Long customerId, CreateClaimRequest request) {
        validateRequest(customerId, request);
        validatePolicyEligibility(customerId, request.policyId());

        if (claimRepository.existsByPolicyIdAndStatusIn(request.policyId(), ACTIVE_CLAIM_STATUSES)) {
            throw new ApiException(HttpStatus.CONFLICT, "An active claim already exists for this policy");
        }

        Claim claim = new Claim();
        claim.setClaimId("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        claim.setCustomerId(customerId);
        claim.setPolicyId(request.policyId());
        claim.setClaimType(request.claimType());
        claim.setIncidentDate(request.incidentDate());
        claim.setIncidentLocation(request.incidentLocation());
        claim.setDescription(request.description());
        claim.setEstimatedDamageAmount(request.estimatedDamageAmount());
        claim.setStatus(ClaimStatus.SUBMITTED);
        LocalDateTime now = LocalDateTime.now();
        claim.setCreatedAt(now);
        claim.setUpdatedAt(now);

        return toResponse(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(String claimId, Long customerId) {
        Claim claim = claimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Claim not found: " + claimId));

        if (!claim.getCustomerId().equals(customerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this claim");
        }

        return toResponse(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByCustomer(Long requestedCustomerId, Long authCustomerId) {
        if (!requestedCustomerId.equals(authCustomerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only view your own claims");
        }

        return claimRepository.findByCustomerIdOrderByCreatedAtDesc(authCustomerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByPolicy(String policyId, Long customerId) {
        validatePolicyOwnership(customerId, policyId);

        return claimRepository.findByCustomerIdAndPolicyIdOrderByCreatedAtDesc(customerId, policyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClaimResponse updateStatus(String claimId, ClaimStatus status, Long customerId) {
        Claim claim = claimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Claim not found: " + claimId));

        if (!claim.getCustomerId().equals(customerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot update this claim");
        }

        claim.setStatus(status);
        return toResponse(claimRepository.save(claim));
    }

    private void validateRequest(Long authenticatedCustomerId, CreateClaimRequest request) {
        if (request.customerId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        if (!request.customerId().equals(authenticatedCustomerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "customerId does not match the logged-in customer");
        }
        if (request.claimType() == null || request.claimType().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "claimType is required");
        }
        if (!"ACCIDENT".equals(request.claimType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported claim type: " + request.claimType());
        }
        if (request.incidentDate() == null || request.incidentDate().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "incidentDate must be today or earlier");
        }
        if (request.estimatedDamageAmount() == null
                || request.estimatedDamageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estimatedDamageAmount must be greater than zero");
        }
    }

    private void validatePolicyEligibility(Long customerId, String policyId) {
        PolicySummary policySummary;
        try {
            policySummary = policyClient.getPolicySummary(policyId);
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Policy not found: " + policyId);
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Policy Service is unavailable");
        }
        if (policySummary == null || !policyId.equals(policySummary.policyId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Policy not found: " + policyId);
        }

        String status = policySummary.policyStatus() == null
                ? "" : policySummary.policyStatus().trim().toUpperCase(Locale.ROOT);

        if (!status.isBlank() && !"ACTIVE".equals(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Claim can be filed only for an active policy");
        }

        validatePolicyOwnership(customerId, policyId);
    }

    private void validatePolicyOwnership(Long customerId, String policyId) {
        List<DriverInfo> drivers;
        try {
            drivers = customerClient.getDriversByPolicyId(policyId);
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Policy does not belong to the logged-in customer");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Customer Service is unavailable");
        }
        if (drivers == null || drivers.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Policy does not belong to the logged-in customer");
        }
        boolean ownsPolicy = drivers.stream().anyMatch(driver ->
                driver.namedInsured() &&
                        driver.customerId() != null &&
                        driver.customerId().equals(customerId)
        );

        if (!ownsPolicy) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Policy does not belong to the logged-in customer");
        }
    }

    private ClaimResponse toResponse(Claim claim) {
        return new ClaimResponse(
                claim.getClaimId(),
                claim.getCustomerId(),
                claim.getPolicyId(),
                claim.getClaimType(),
                claim.getIncidentDate(),
                claim.getIncidentLocation(),
                claim.getDescription(),
                claim.getEstimatedDamageAmount(),
                claim.getStatus(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}
