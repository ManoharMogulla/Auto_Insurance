package com.ltm.insurance.paymentservice.processor;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;

/**
 * Strategy for handling a single {@link PaymentMethod}. Each implementation is a Spring bean,
 * and {@code PaymentService} routes payments to it by {@link #getPaymentMethod()}.
 */
public interface PaymentProcessor {

    PaymentMethod getPaymentMethod();

    /**
     * Applies method-specific logic (validation, gateway calls, status) to the payment
     * before it is persisted.
     */
    Payment process(Payment payment);
}
