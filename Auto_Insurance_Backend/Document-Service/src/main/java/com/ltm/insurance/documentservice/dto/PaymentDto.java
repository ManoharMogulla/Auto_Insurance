package com.ltm.insurance.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {
    private Long paymentId;
    private String policyNumber;
    private Double premiumAmount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentDate;
}