package com.ltm.insurance.policyservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Premium calculation breakdown:
 * basePremium + driverAdjustment + vehicleAdjustment - totalDiscount = finalPremium
 */

@Builder
public record PremiumResponseDTO (

     String namedInsured,

     BigDecimal basePremium,
     BigDecimal driverAdjustment,
     BigDecimal vehicleAdjustment,
     BigDecimal totalDiscount,
     BigDecimal finalPremium,

     List<DriverPremiumBreakdownDTO> driverBreakdowns,
     List<VehiclePremiumBreakdownDTO> vehicleBreakdowns


)
{

}