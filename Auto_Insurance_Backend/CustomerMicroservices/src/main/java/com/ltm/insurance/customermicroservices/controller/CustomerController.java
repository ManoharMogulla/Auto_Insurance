package com.ltm.insurance.customermicroservices.controller;


import com.ltm.insurance.customermicroservices.DTO.Policy;
import com.ltm.insurance.customermicroservices.DTO.PolicyDetails;
import com.ltm.insurance.customermicroservices.DTO.CustomerProfileRequest;
import com.ltm.insurance.customermicroservices.DTO.CustomerProfileResponse;
import com.ltm.insurance.customermicroservices.model.Driver;
import com.ltm.insurance.customermicroservices.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

//import com.ltm.insurance.customerservice.model.Policy;


@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    @Value("${internal.service.token:}")
    private String internalServiceToken;

    //Create a new policy with drivers
    @PostMapping("/addPolicy")
    public String addPolicy(@RequestBody PolicyDetails policy, @AuthenticationPrincipal Jwt jwt) {
        return customerService.addPolicy(policy, customerId(jwt));
    }

    @PostMapping("/profiles")
    public CustomerProfileResponse createCustomerProfile(
            @RequestHeader(name = "X-Internal-Service-Token", required = false) String serviceToken,
            @Valid @RequestBody CustomerProfileRequest request) {
        if (!isValidInternalToken(serviceToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal service authentication required");
        }
        return customerService.createCustomerProfile(request);
    }

    @GetMapping("/policies")
    public List<Policy> getCustomerPolicies(@AuthenticationPrincipal Jwt jwt) {
        return customerService.getPoliciesForCustomer(customerId(jwt));
    }

//    @GetMapping("/policies/{memberId}")
//    public List<PolicyDetailsDTO> getPoliciesByMemberId(@PathVariable Long memberId) {
//        return customerService.getPoliciesByMemberId(memberId);
//    }

//
//    @GetMapping("/driver/{memberId}")
//    public PolicyDetails getDriver(@PathVariable Long memberId) {
//        return customerService.getPoliciesByMemberId(memberId);
//
//    }
//    @GetMapping("/driver/{memberId}")
//    public Policy getPolicies(@PathVariable Long memberId) {
//        return customerService.getPoliciesByMemberId(memberId);
//
//    }

    @GetMapping("/allDrivers")
    public List<Driver> getAllDrivers() {
        return customerService.getAllDrivers();
    }

    @GetMapping("/drivers/{policyId}")
    public List<Driver> getDriversByPolicyId(
            @PathVariable String policyId) {

        return customerService.getDriversByPolicyId(policyId);
    }


    @PostMapping("policy/Renewal/{policyId}")
    public String renewPolicy(@PathVariable String policyId) {
        return customerService.renewPolicy(policyId);
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
                // Handled by the missing-identity response below.
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT does not contain a valid customer ID");
    }

    private boolean isValidInternalToken(String providedToken) {
        return providedToken != null
                && !internalServiceToken.isBlank()
                && MessageDigest.isEqual(
                        internalServiceToken.getBytes(StandardCharsets.UTF_8),
                        providedToken.getBytes(StandardCharsets.UTF_8));
    }


}
