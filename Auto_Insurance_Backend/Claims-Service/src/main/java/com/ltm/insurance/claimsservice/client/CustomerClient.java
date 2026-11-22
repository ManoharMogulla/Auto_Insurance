package com.ltm.insurance.claimsservice.client;

import com.ltm.insurance.claimsservice.dto.DriverInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "CustomerMicroservices", path = "/customer")
public interface CustomerClient {

    @GetMapping("/drivers/{policyId}")
    List<DriverInfo> getDriversByPolicyId(@PathVariable String policyId);
}
