package com.ltm.insurance.paymentservice.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @NotNull
    private String policyNumber;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double premiumAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    private LocalDateTime paymentDate;

    public Payment(String policyNumber, Double premiumAmount) {
        this(policyNumber, premiumAmount, PaymentMethod.CARD);
    }

    public Payment(String policyNumber, Double premiumAmount, PaymentMethod paymentMethod) {
        this.policyNumber = policyNumber;
        this.premiumAmount = premiumAmount;
        this.paymentMethod = paymentMethod == null ? PaymentMethod.CARD : paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentDate = LocalDateTime.now();
    }
}
