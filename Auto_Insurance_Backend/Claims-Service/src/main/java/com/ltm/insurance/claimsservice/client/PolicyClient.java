package com.ltm.insurance.claimsservice.client;

import com.ltm.insurance.claimsservice.dto.PolicySummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Policy-Service", path = "/api/policy")
public interface PolicyClient {

    @GetMapping("/policies/summary/{policyId}")
    PolicySummary getPolicySummary(@PathVariable String policyId);
}
