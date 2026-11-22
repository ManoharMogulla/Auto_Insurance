package com.ltm.insurance.documentservice.client;

import com.ltm.insurance.documentservice.config.FeignConfiguration;
import com.ltm.insurance.documentservice.dto.PolicyDto.DriverDto;
import com.ltm.insurance.documentservice.dto.PremiumDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "premium-service", url = "${services.premium.url}", configuration = FeignConfiguration.class)
public interface PremiumClient {

    @PostMapping("/calculate")
    PremiumDto calculatePremium(@RequestBody PremiumRequest request);

    record PremiumRequest(List<DriverDto> drivers) { }
}