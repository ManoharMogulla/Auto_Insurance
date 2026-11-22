package com.ltm.insurance.paymentservice.processor;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;
import com.ltm.insurance.paymentservice.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public Payment process(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        return payment;
    }
}
