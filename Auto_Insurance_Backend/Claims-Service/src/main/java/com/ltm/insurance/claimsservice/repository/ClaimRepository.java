package com.ltm.insurance.claimsservice.repository;

import com.ltm.insurance.claimsservice.entity.Claim;
import com.ltm.insurance.claimsservice.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimId(String claimId);

    List<Claim> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Claim> findByCustomerIdAndPolicyIdOrderByCreatedAtDesc(Long customerId, String policyId);

    List<Claim> findByPolicyIdOrderByCreatedAtDesc(String policyId);

    boolean existsByPolicyIdAndStatusIn(String policyId, Collection<ClaimStatus> statuses);
}
