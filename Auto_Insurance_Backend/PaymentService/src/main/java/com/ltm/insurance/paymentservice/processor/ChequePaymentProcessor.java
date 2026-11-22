package com.ltm.insurance.paymentservice.processor;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;
import com.ltm.insurance.paymentservice.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class ChequePaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CHEQUE;
    }

    @Override
    public Payment process(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        return payment;
    }
}
