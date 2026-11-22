package com.ltm.insurance.paymentservice.service;

import com.ltm.insurance.paymentservice.model.Payment;
import com.ltm.insurance.paymentservice.model.PaymentMethod;
import com.ltm.insurance.paymentservice.model.PaymentStatus;
import com.ltm.insurance.paymentservice.repo.PaymentServiceRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String POLICY_NUMBER = "POL12345";
    private static final BigDecimal AMOUNT = new BigDecimal("125.50");
    private static final String SUCCESS_MESSAGE = "Payment successful for policy number: " + POLICY_NUMBER;
    private static final String UNPROCESSED_MESSAGE = "Payment failed for policy number: "
            + POLICY_NUMBER + ". Error: Payment could not be processed.";

    @Mock
    private PaymentServiceRepo paymentServiceRepo;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void paymentConstructorAppliesDefaults() {
        Payment payment = new Payment(POLICY_NUMBER, AMOUNT);

        assertEquals(POLICY_NUMBER, payment.getPolicyNumber());
        assertEquals(AMOUNT, payment.getPremiumAmount());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        assertNotNull(payment.getPaymentDate());
    }

    @ParameterizedTest
    @EnumSource(value = PaymentMethod.class, names = {"NET_BANKING", "WALLET"})
    void completedPaymentIsSavedAndReturnsSuccessMessage(PaymentMethod paymentMethod) {
        assertEquals(SUCCESS_MESSAGE, paymentService.makePayment(POLICY_NUMBER, AMOUNT, paymentMethod));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentServiceRepo).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(POLICY_NUMBER, saved.getPolicyNumber());
        assertEquals(AMOUNT, saved.getPremiumAmount());
        assertEquals(paymentMethod, saved.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, saved.getPaymentStatus());
        assertNotNull(saved.getPaymentDate());
    }

    @ParameterizedTest
    @EnumSource(value = PaymentMethod.class, names = {"CARD", "UPI", "CASH", "BANK_TRANSFER", "CHEQUE"})
    void pendingPaymentReturnsFailureMessageWithoutSaving(PaymentMethod paymentMethod) {
        assertEquals(UNPROCESSED_MESSAGE, paymentService.makePayment(POLICY_NUMBER, AMOUNT, paymentMethod));
        verifyNoInteractions(paymentServiceRepo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999.99", "100000", "100000.01"})
    void upiPaymentsAroundLimitAreNotSaved(String amount) {
        assertEquals(UNPROCESSED_MESSAGE,
                paymentService.makePayment(POLICY_NUMBER, new BigDecimal(amount), PaymentMethod.UPI));
        verifyNoInteractions(paymentServiceRepo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9999.99", "10000", "10000.01"})
    void walletPaymentsAroundLimitComplete(String amount) {
        assertEquals(SUCCESS_MESSAGE,
                paymentService.makePayment(POLICY_NUMBER, new BigDecimal(amount), PaymentMethod.WALLET));
        verify(paymentServiceRepo).save(any(Payment.class));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-10"})
    void invalidAmountReturnsErrorWithoutSaving(String amount) {
        BigDecimal value = amount == null ? null : new BigDecimal(amount);

        assertEquals("Payment failed for policy number: " + POLICY_NUMBER + ". Error: Invalid payment details",
                paymentService.makePayment(POLICY_NUMBER, value, PaymentMethod.CARD));
        verifyNoInteractions(paymentServiceRepo);
    }

    @Test
    void missingPaymentMethodReturnsErrorWithoutSaving() {
        assertEquals("Payment failed for policy number: " + POLICY_NUMBER + ". Error: Invalid payment details",
                paymentService.makePayment(POLICY_NUMBER, AMOUNT, null));
        verifyNoInteractions(paymentServiceRepo);
    }

    @Test
    void repositoryFailureReturnsErrorMessage() {
        when(paymentServiceRepo.save(any(Payment.class))).thenThrow(new IllegalStateException("Database unavailable"));

        assertEquals("Payment failed for policy number: " + POLICY_NUMBER + ". Error: Database unavailable",
                paymentService.makePayment(POLICY_NUMBER, AMOUNT, PaymentMethod.NET_BANKING));
    }

    @Test
    void getPaymentsForPolicyReturnsRepositoryResults() {
        List<Payment> payments = List.of(new Payment(POLICY_NUMBER, AMOUNT));
        when(paymentServiceRepo.findByPolicyNumberOrderByPaymentDateDesc(POLICY_NUMBER)).thenReturn(payments);

        assertSame(payments, paymentService.getPaymentsForPolicy(POLICY_NUMBER));
        verify(paymentServiceRepo).findByPolicyNumberOrderByPaymentDateDesc(POLICY_NUMBER);
    }

    @Test
    void getPaymentsForPolicyWithoutPaymentsReturnsEmptyList() {
        when(paymentServiceRepo.findByPolicyNumberOrderByPaymentDateDesc(POLICY_NUMBER)).thenReturn(List.of());

        assertEquals(List.of(), paymentService.getPaymentsForPolicy(POLICY_NUMBER));
    }
}
