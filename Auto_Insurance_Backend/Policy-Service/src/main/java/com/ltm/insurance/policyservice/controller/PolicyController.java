package com.ltm.insurance.policyservice.controller;

import com.ltm.insurance.policyservice.client.CustomerClient;
import com.ltm.insurance.policyservice.dto.PaymentRequest;
import com.ltm.insurance.policyservice.dto.PolicyDetails;
import com.ltm.insurance.policyservice.model.Policy;
import com.ltm.insurance.policyservice.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//server.port=8082
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {



    private final CustomerClient customerClient;

    private final PolicyService policyService;

    @PostMapping("/createPolicy")
    public Policy createPolicy(@RequestBody PolicyDetails policy)
    {
        try {
            Policy policyDetails = policyService.createPolicy(policy);
            System.out.println("Policy details: " + policyDetails);
            return policyDetails;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create policy",
                    exception
            );
        }
    }

    @PostMapping("/payments/{policyId}")
    public String makePayment(@PathVariable String policyId,
            @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return policyService.makePayment(policyId, request, authorizationHeader);
    }

    @GetMapping("/policies/{policyId}")
    public Policy getPoliciesByMemberId(@PathVariable String policyId) {
        return policyService.getPoliciesByPolicyId(policyId);
    }

    @GetMapping("/policies/summary/{policyId}")
    public Policy getPolicySummaryByPolicyId(@PathVariable String policyId) {
        return policyService.getPolicySummaryByPolicyId(policyId);
    }

    @GetMapping("/allPolicies")
    public List<Policy> getAllPolicies() {
        return policyService.getAllPolicies();
    }


    @PostMapping("/policy/Renewal/{policyId}")
    String renewPolicy(@PathVariable String policyId){


        return policyService.renewPolicy(policyId);
    }




//    @GetMapping("/policy/{policyId}")
//    public List<Policy> getPolicyById(@PathVariable Long policyId) {
////        return policyService.getPolicyById(policyId);
//        return policyService.getPoliciesByMemberId(policyId);
//    }
}
