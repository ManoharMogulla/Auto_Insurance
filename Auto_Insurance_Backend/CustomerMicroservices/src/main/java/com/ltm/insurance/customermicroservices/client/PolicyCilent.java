package com.ltm.insurance.customermicroservices.client;


import com.ltm.insurance.customermicroservices.DTO.Policy;
import com.ltm.insurance.customermicroservices.DTO.PolicyDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "policy-service",
        url = "http://localhost:8082/api/policy"
)
public interface PolicyCilent {

    @PostMapping("/createPolicy")
    Policy createPolicy(@RequestBody PolicyDetails policy);

    @GetMapping("/policies/Member/{memberId}")
    public List<PolicyDetails> getPoliciesByMemberId(@PathVariable Long memberId);

    @GetMapping("/policies/{policyId}")
    Policy getPolicyByPolicyId(@PathVariable String policyId);

    @GetMapping("/policies/summary/{policyId}")
    Policy getPolicySummaryByPolicyId(@PathVariable String policyId);

    @GetMapping("/policies")
    public List<PolicyDetails> getAllPolicies(Long memberId);

    @PostMapping("/policy/Renewal/{policyId}")
    String renewPolicy(@PathVariable String policyId);
}
