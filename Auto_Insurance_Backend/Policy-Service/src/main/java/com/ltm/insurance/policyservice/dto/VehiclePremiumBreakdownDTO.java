package com.ltm.insurance.policyservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record VehiclePremiumBreakdownDTO(
       String vehicleName,
    String vehicleType,
    String usageType,
    boolean primaryVehicle,
    BigDecimal basePremium,
    BigDecimal vehicleAgeFactor,
    BigDecimal vehicleTypeFactor,
    BigDecimal usageTypeFactor,
    BigDecimal vehicleAdjustment,
    BigDecimal discountAmount,
    List<String> appliedDiscounts,
    BigDecimal vehiclePremium

) {
}
