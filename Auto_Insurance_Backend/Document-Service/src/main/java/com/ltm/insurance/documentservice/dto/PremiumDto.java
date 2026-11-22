package com.ltm.insurance.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PremiumDto {
    private BigDecimal basePremium;
    private BigDecimal driverAdjustment;
    private BigDecimal vehicleAdjustment;
    private BigDecimal totalDiscount;
    private BigDecimal finalPremium;
}