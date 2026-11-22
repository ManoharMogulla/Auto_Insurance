package com.ltm.insurance.paymentservice.controller;

import com.ltm.insurance.paymentservice.dto.PaymentRequest;
import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


//server.port=8084
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/makePayment/{policyNumber}")
    public String makePayment(
            @PathVariable String policyNumber,
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.makePayment(policyNumber, request.amount(), request.paymentMethod());
    }

    @GetMapping("/{policyNumber}")
    public List<Payment> getPaymentsForPolicy(@PathVariable String policyNumber) {
        return paymentService.getPaymentsForPolicy(policyNumber);
    }
    @GetMapping("/")
    public String paymentHomePage() {
        return "Welcome to the Payment Service!";
    }

}
