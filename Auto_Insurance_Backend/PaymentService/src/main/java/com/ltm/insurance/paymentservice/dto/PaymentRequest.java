package com.ltm.insurance.paymentservice.dto;

import com.ltm.insurance.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        Double amount,
        PaymentMethod paymentMethod
) {

}
