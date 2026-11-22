package com.ltm.insurance.policyservice.client;


import com.ltm.insurance.policyservice.dto.PolicyDetails;
import com.ltm.insurance.policyservice.model.Driver;
import com.ltm.insurance.policyservice.model.Policy;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "customer-service", url = "http://localhost:8081/customer")
public interface CustomerClient {

    //Create a new policy with drivers
    @PostMapping("/addPolicy")
    Policy addpolicyDetails(@RequestBody Policy policy);


    @GetMapping("/drivers/{policyId}")
     List<Driver> getDriversByPolicyId(@PathVariable String policyId);
}