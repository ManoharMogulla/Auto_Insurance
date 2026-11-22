package com.ltm.insurance.premiumservice.service;

import com.ltm.insurance.premiumservice.dto.DiscountsDTO;
import com.ltm.insurance.premiumservice.dto.VehicleDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates discounts from the existing vehicle -> discounts structure.
 * Discount rates are percentages applied to the vehicle subtotal
 * (base premium + driver adjustment + vehicle adjustment).
 */
@Service
public class DiscountCalculationService {

    private static final BigDecimal SAFE_PILOT_RATE = new BigDecimal("0.10");
    private static final BigDecimal GOOD_STUDENT_RATE = new BigDecimal("0.08");
    private static final BigDecimal ANTI_THEFT_RATE = new BigDecimal("0.05");
    private static final BigDecimal LOW_MILEAGE_RATE = new BigDecimal("0.05");
    private static final BigDecimal MULTI_VEHICLE_RATE = new BigDecimal("0.10");
    private static final BigDecimal MAX_DISCOUNT_RATE = new BigDecimal("0.30");

    /**
     * Total discount rate for a vehicle, capped at MAX_DISCOUNT_RATE.
     */
    public BigDecimal discountRate(VehicleDTO vehicle, boolean multiVehiclePolicy) {
        BigDecimal rate = BigDecimal.ZERO;

        if (vehicle.getDiscounts() != null) {
            for (DiscountsDTO discounts : vehicle.getDiscounts()) {
                if (discounts == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(discounts.getSafePilotDiscount())) {
                    rate = rate.add(SAFE_PILOT_RATE);
                }
                if (Boolean.TRUE.equals(discounts.getGoodStudentDiscount())) {
                    rate = rate.add(GOOD_STUDENT_RATE);
                }
                if (Boolean.TRUE.equals(discounts.getAntiTheftDiscount())) {
                    rate = rate.add(ANTI_THEFT_RATE);
                }
                if (Boolean.TRUE.equals(discounts.getLowMileageDiscount())) {
                    rate = rate.add(LOW_MILEAGE_RATE);
                }
            }
        }

        if (multiVehiclePolicy) {
            rate = rate.add(MULTI_VEHICLE_RATE);
        }

        return rate.min(MAX_DISCOUNT_RATE);
    }

    /**
     * Names of the discounts applied to a vehicle, for the premium breakdown.
     */
    public List<String> appliedDiscountNames(VehicleDTO vehicle, boolean multiVehiclePolicy) {
        List<String> names = new ArrayList<>();

        if (vehicle.getDiscounts() != null) {
            for (DiscountsDTO discounts : vehicle.getDiscounts()) {
                if (discounts == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(discounts.getSafePilotDiscount())) {
                    names.add("Safe Pilot Discount");
                }
                if (Boolean.TRUE.equals(discounts.getGoodStudentDiscount())) {
                    names.add("Good Student Discount");
                }
                if (Boolean.TRUE.equals(discounts.getAntiTheftDiscount())) {
                    names.add("Anti-Theft Discount");
                }
                if (Boolean.TRUE.equals(discounts.getLowMileageDiscount())) {
                    names.add("Low Mileage Discount");
                }
            }
        }

        if (multiVehiclePolicy) {
            names.add("Multi-Vehicle Discount");
        }

        return names;
    }
}
