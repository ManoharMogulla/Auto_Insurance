package com.ltm.insurance.paymentservice.service;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;
import com.ltm.insurance.paymentservice.model.PaymentStatus;
import com.ltm.insurance.paymentservice.repo.PaymentServiceRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private static final Double UPI_TRANSACTION_LIMIT = 100000.0;
    private static final Double WALLET_TRANSACTION_LIMIT = 10000.0;

    private final PaymentServiceRepo paymentServiceRepo;

    @Transactional
    public String makePayment(String policyNumber, Double amount, PaymentMethod paymentMethod) {
        try {
            if (amount == null || paymentMethod == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid payment details");
            }

            Payment payment = new Payment(policyNumber, amount, paymentMethod);

            switch (paymentMethod) {
                case CARD -> processCardPayment(payment);
                case UPI -> processUpiPayment(payment);
                case NET_BANKING -> processNetBankingPayment(payment);
                case WALLET -> processWalletPayment(payment);
                case CASH -> processCashPayment(payment);
                case BANK_TRANSFER -> processBankTransferPayment(payment);
                case CHEQUE -> processChequePayment(payment);
                default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            }
            if (payment.getPaymentStatus() == PaymentStatus.FAILED) {
                logger.error("Payment failed for policy number: {}", payment.getPolicyNumber());
                return "Payment failed for policy number: " + policyNumber + ". Error: Payment could not be processed.";
            }
            // COMPLETED (online) and PENDING (offline, awaiting settlement) are both recorded
            logger.info("Payment completed for policy number: {}", payment.getPolicyNumber());
            paymentServiceRepo.save(payment);
            return "Payment successful for policy number: " + payment.getPolicyNumber();


        } catch (Exception e) {
            logger.error("Error processing payment for policy number: {}. Error: {}", policyNumber, e.getMessage());
            return "Payment failed for policy number: " + policyNumber + ". Error: " + e.getMessage();

        }
    }


    // Online methods: simulated instant success (no gateway integration yet).
    private void processCardPayment(Payment payment) {
        System.out.println("Under Card Payment....");
        logger.info("Processing card payment for policy number: {}", payment.getPolicyNumber());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    private void processUpiPayment(Payment payment) {
        logger.info("Processing UPI payment for policy number: {}", payment.getPolicyNumber());
        if (payment.getPremiumAmount().compareTo(UPI_TRANSACTION_LIMIT) > 0) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            return;
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    private void processNetBankingPayment(Payment payment) {
        logger.info("Processing net banking payment for policy number: {}", payment.getPolicyNumber());
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    private void processWalletPayment(Payment payment) {
        if (payment.getPremiumAmount().compareTo(WALLET_TRANSACTION_LIMIT) > 0) {
            logger.info("Processing wallet payment for policy number: {}", payment.getPolicyNumber());
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            return;
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    // Offline methods: recorded as PENDING until settlement is confirmed manually.
    private void processCashPayment(Payment payment) {
        logger.info("Processing cash payment for policy number: {}", payment.getPolicyNumber());
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    private void processBankTransferPayment(Payment payment) {
        logger.info("Processing bank transfer payment for policy number: {}", payment.getPolicyNumber());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    private void processChequePayment(Payment payment) {
        logger.info("Processing cheque payment for policy number: {}", payment.getPolicyNumber());
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForPolicy(String policyNumber) {
        return paymentServiceRepo.findByPolicyNumberOrderByPaymentDateDesc(policyNumber);
    }
}
