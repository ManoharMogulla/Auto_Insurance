package com.ltm.insurance.policyservice.dto;

import lombok.*;

import java.math.BigDecimal;

public record DriverPremiumBreakdownDTO(

    Long memberId,
    String driverName,
    boolean primaryDriver,
    Integer age,
    BigDecimal ageFactor,
         BigDecimal drivingHistoryFactor,
    BigDecimal insuranceHistoryFactor,
    BigDecimal creditScoreFactor,
    BigDecimal totalDriverFactor,
    BigDecimal driverAdjustment
     ){
}
