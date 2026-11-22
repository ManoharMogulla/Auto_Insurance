package com.ltm.insurance.documentservice.client;

import com.ltm.insurance.documentservice.config.FeignConfiguration;
import com.ltm.insurance.documentservice.dto.PolicyDto.DriverDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "customer-service", url = "${services.customer.url}", configuration = FeignConfiguration.class)
public interface CustomerClient {

    @GetMapping("/drivers/{policyId}")
    List<DriverDto> getDriversByPolicyId(@PathVariable String policyId);
}