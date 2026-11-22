package com.ltm.insurance.paymentservice.service;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;
import com.ltm.insurance.paymentservice.processor.PaymentProcessor;
import com.ltm.insurance.paymentservice.repo.PaymentServiceRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl {

    private final PaymentServiceRepo paymentServiceRepo;
    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

    public PaymentServiceImpl(PaymentServiceRepo paymentServiceRepo, List<PaymentProcessor> paymentProcessors) {
        this.paymentServiceRepo = paymentServiceRepo;
        for (PaymentProcessor processor : paymentProcessors) {
            PaymentProcessor existing = processors.put(processor.getPaymentMethod(), processor);
            if (existing != null) {
                throw new IllegalStateException("Multiple processors registered for " + processor.getPaymentMethod());
            }
        }
    }

    @Transactional
    public Payment makePayment(String policyNumber, Double amount, PaymentMethod paymentMethod) {
        Payment payment = new Payment(policyNumber, amount, paymentMethod);
        PaymentProcessor processor = processors.get(payment.getPaymentMethod());
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + payment.getPaymentMethod());
        }
        return paymentServiceRepo.save(processor.process(payment));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForPolicy(String policyNumber) {
        return paymentServiceRepo.findByPolicyNumberOrderByPaymentDateDesc(policyNumber);
    }
}
