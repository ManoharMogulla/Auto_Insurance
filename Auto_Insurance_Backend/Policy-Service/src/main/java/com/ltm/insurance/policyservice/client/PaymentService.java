package com.ltm.insurance.policyservice.client;

import com.ltm.insurance.policyservice.dto.PaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;


@FeignClient(name = "payment-service", url = "http://localhost:8084/api/payment")
public interface PaymentService {


    @PostMapping("/makePayment/{policyId}")
     String makePayment(
            @PathVariable String policyId,
            @Valid @RequestBody PaymentRequest request
    );


    @GetMapping("/{policyNumber}")
    List<PaymentRequest> getPaymentsForPolicy(@PathVariable @Positive String policyNumber);
}
