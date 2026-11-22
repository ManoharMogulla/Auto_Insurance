package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Premium calculation breakdown:
 * basePremium + driverAdjustment + vehicleAdjustment - totalDiscount = finalPremium
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumResponse {

    private String namedInsured;

    private BigDecimal basePremium;
    private BigDecimal driverAdjustment;
    private BigDecimal vehicleAdjustment;
    private BigDecimal totalDiscount;
    private BigDecimal finalPremium;

    private List<DriverPremiumBreakdown> driverBreakdowns;
    private List<VehiclePremiumBreakdown> vehicleBreakdowns;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriverPremiumBreakdown {
        private Long memberId;
        private String driverName;
        private boolean primaryDriver;
        private Integer age;
        private BigDecimal ageFactor;
        private BigDecimal drivingHistoryFactor;
        private BigDecimal insuranceHistoryFactor;
        private BigDecimal creditScoreFactor;
        private BigDecimal totalDriverFactor;
        private BigDecimal driverAdjustment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehiclePremiumBreakdown {
        private String vehicleName;
        private String vehicleType;
        private String usageType;
        private boolean primaryVehicle;
        private BigDecimal basePremium;
        private BigDecimal vehicleAgeFactor;
        private BigDecimal vehicleTypeFactor;
        private BigDecimal usageTypeFactor;
        private BigDecimal vehicleAdjustment;
        private BigDecimal discountAmount;
        private List<String> appliedDiscounts;
        private BigDecimal vehiclePremium;
    }
}
