package com.ltm.insurance.policyservice.client;

import com.ltm.insurance.policyservice.dto.PremiumRequest;
import com.ltm.insurance.policyservice.dto.PremiumResponseDTO;
import com.ltm.insurance.policyservice.model.Driver;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name="premium-service", url = "http://localhost:8085")
public interface PremiumClient {


    @PostMapping("/calculate")
    PremiumResponseDTO calculatePremium(@RequestBody PremiumRequest request);;

    @GetMapping("/")
    String premiumHomePage();
}

