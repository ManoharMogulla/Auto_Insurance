package com.ltm.insurance.documentservice.client;

import com.ltm.insurance.documentservice.config.FeignConfiguration;
import com.ltm.insurance.documentservice.dto.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "payment-service", url = "${services.payment.url}", configuration = FeignConfiguration.class)
public interface PaymentClient {

    @GetMapping("/{policyNumber}")
    List<PaymentDto> getPaymentsForPolicy(@PathVariable String policyNumber);
}