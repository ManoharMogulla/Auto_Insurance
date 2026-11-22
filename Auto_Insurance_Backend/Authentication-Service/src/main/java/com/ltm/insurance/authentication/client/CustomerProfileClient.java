package com.ltm.insurance.authentication.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-profile-service", url = "${customer.service.url}",
        configuration = CustomerProfileClientConfiguration.class)
public interface CustomerProfileClient {

    @PostMapping("/customer/profiles")
    CustomerProfileResponse createProfile(@RequestBody CustomerProfileRequest request);
}
