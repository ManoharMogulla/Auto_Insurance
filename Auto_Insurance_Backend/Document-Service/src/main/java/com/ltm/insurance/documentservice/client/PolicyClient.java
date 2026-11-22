package com.ltm.insurance.documentservice.client;

import com.ltm.insurance.documentservice.config.FeignConfiguration;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policy-service", url = "${services.policy.url}", configuration = FeignConfiguration.class)
public interface PolicyClient {

    @GetMapping("/policies/{policyId}")
    PolicyDto getPolicy(@PathVariable String policyId);
}