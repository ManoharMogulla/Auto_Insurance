package com.ltm.insurance.paymentservice.repo;

import com.ltm.insurance.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentServiceRepo extends JpaRepository<Payment, Long> {

    List<Payment> findByPolicyNumberOrderByPaymentDateDesc(String policyNumber);
}
