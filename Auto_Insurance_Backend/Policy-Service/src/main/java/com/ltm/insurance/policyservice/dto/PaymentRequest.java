package com.ltm.insurance.policyservice.dto;


import java.math.BigDecimal;

//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
public record PaymentRequest(
        BigDecimal amount ,
        PaymentMethod paymentMethod
) {

}
//) {}
//    @NotNull
//    private Long policyNumber;
//
//    @NotNull
//    @DecimalMin(value = "0.0", inclusive = false)
//    private BigDecimal premiumAmount;
//
//    private LocalDateTime paymentDate;
//
//    private PaymentMethod paymentType;
//
//}
