package com.ltm.insurance.premiumservice.service;

import com.ltm.insurance.premiumservice.dto.DriverDTO;
import com.ltm.insurance.premiumservice.dto.PremiumRequest;
import com.ltm.insurance.premiumservice.dto.PremiumResponse;
import com.ltm.insurance.premiumservice.dto.VehicleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * All premium calculation happens here (backend only).
 *
 * finalPremium = basePremium + driverAdjustment + vehicleAdjustment - totalDiscount
 *
 * The first driver is the Named Insured / Primary Driver.
 * The first vehicle of the Primary Driver is the Primary Vehicle.
 */
@Service
@RequiredArgsConstructor
public class PremiumCalculationService {

    private static final BigDecimal BASE_PREMIUM_PER_VEHICLE = new BigDecimal("500.00");
    private static final int MONEY_SCALE = 2;

    private final DiscountCalculationService discountCalculationService;

    public PremiumResponse calculatePremium(PremiumRequest request) {
        if (request == null || request.getDrivers() == null || request.getDrivers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one driver is required");
        }

        List<DriverDTO> drivers = request.getDrivers();
        DriverDTO primaryDriver = drivers.get(0);

        int totalVehicleCount = drivers.stream()
                .mapToInt(d -> d.getVehicles() == null ? 0 : d.getVehicles().size())
                .sum();
        if (totalVehicleCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one vehicle is required");
        }
        boolean multiVehiclePolicy = totalVehicleCount > 1;

        // ----- Driver factors (averaged across all drivers, applied per vehicle) -----
        List<PremiumResponse.DriverPremiumBreakdown> driverBreakdowns = new ArrayList<>();
        BigDecimal combinedDriverFactor = BigDecimal.ZERO;
        for (DriverDTO driver : drivers) {
            PremiumResponse.DriverPremiumBreakdown breakdown = buildDriverBreakdown(driver, driver == primaryDriver);
            combinedDriverFactor = combinedDriverFactor.add(breakdown.getTotalDriverFactor());
            driverBreakdowns.add(breakdown);
        }
        BigDecimal averageDriverFactor = combinedDriverFactor
                .divide(BigDecimal.valueOf(drivers.size()), 4, RoundingMode.HALF_UP);

        // ----- Per-vehicle premium -----
        List<PremiumResponse.VehiclePremiumBreakdown> vehicleBreakdowns = new ArrayList<>();
        BigDecimal basePremium = BigDecimal.ZERO;
        BigDecimal driverAdjustment = BigDecimal.ZERO;
        BigDecimal vehicleAdjustment = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        VehicleDTO primaryVehicle =
                (primaryDriver.getVehicles() == null || primaryDriver.getVehicles().isEmpty())
                        ? null
                        : primaryDriver.getVehicles().get(0);

        for (DriverDTO driver : drivers) {
            if (driver.getVehicles() == null) {
                continue;
            }
            for (VehicleDTO vehicle : driver.getVehicles()) {
                BigDecimal vehicleAgeFactor = vehicleAgeFactor(vehicle.getYear());
                BigDecimal vehicleTypeFactor = vehicleTypeFactor(vehicle.getVehicleType());
                BigDecimal usageTypeFactor = usageTypeFactor(vehicle.getUsageType());
                BigDecimal totalVehicleFactor = vehicleAgeFactor.add(vehicleTypeFactor).add(usageTypeFactor);

                BigDecimal vehicleBase = BASE_PREMIUM_PER_VEHICLE;
                BigDecimal vehicleDriverAdj = money(vehicleBase.multiply(averageDriverFactor));
                BigDecimal vehicleVehicleAdj = money(vehicleBase.multiply(totalVehicleFactor));
                BigDecimal subtotal = vehicleBase.add(vehicleDriverAdj).add(vehicleVehicleAdj);

                BigDecimal discountRate = discountCalculationService.discountRate(vehicle, multiVehiclePolicy);
                BigDecimal discountAmount = money(subtotal.multiply(discountRate));
                BigDecimal vehiclePremium = subtotal.subtract(discountAmount);

                basePremium = basePremium.add(vehicleBase);
                driverAdjustment = driverAdjustment.add(vehicleDriverAdj);
                vehicleAdjustment = vehicleAdjustment.add(vehicleVehicleAdj);
                totalDiscount = totalDiscount.add(discountAmount);

                vehicleBreakdowns.add(PremiumResponse.VehiclePremiumBreakdown.builder()
                        .vehicleName(buildVehicleName(vehicle))
                        .vehicleType(vehicle.getVehicleType())
                        .usageType(vehicle.getUsageType())
                        .primaryVehicle(vehicle == primaryVehicle)
                        .basePremium(money(vehicleBase))
                        .vehicleAgeFactor(vehicleAgeFactor)
                        .vehicleTypeFactor(vehicleTypeFactor)
                        .usageTypeFactor(usageTypeFactor)
                        .vehicleAdjustment(vehicleVehicleAdj)
                        .discountAmount(discountAmount)
                        .appliedDiscounts(discountCalculationService.appliedDiscountNames(vehicle, multiVehiclePolicy))
                        .vehiclePremium(vehiclePremium)
                        .build());
            }
        }

        BigDecimal finalPremium = basePremium.add(driverAdjustment).add(vehicleAdjustment).subtract(totalDiscount);

        return PremiumResponse.builder()
                .namedInsured(buildDriverName(primaryDriver))
                .basePremium(money(basePremium))
                .driverAdjustment(money(driverAdjustment))
                .vehicleAdjustment(money(vehicleAdjustment))
                .totalDiscount(money(totalDiscount))
                .finalPremium(money(finalPremium))
                .driverBreakdowns(driverBreakdowns)
                .vehicleBreakdowns(vehicleBreakdowns)
                .build();
    }

    // ----- Driver factors -----

    private PremiumResponse.DriverPremiumBreakdown buildDriverBreakdown(DriverDTO driver, boolean primary) {
        Integer age = calculateAge(driver.getDateOfBirth());
        BigDecimal ageFactor = ageFactor(age);
        BigDecimal drivingHistoryFactor = drivingHistoryFactor(driver.getDrivingHistory());
        BigDecimal insuranceHistoryFactor = insuranceHistoryFactor(driver.getInsuranceHistory());
        BigDecimal creditScoreFactor = creditScoreFactor(driver.getCreditScore());
        BigDecimal totalFactor = ageFactor.add(drivingHistoryFactor).add(insuranceHistoryFactor).add(creditScoreFactor);

        return PremiumResponse.DriverPremiumBreakdown.builder()
                .memberId(driver.getMemberId())
                .driverName(buildDriverName(driver))
                .primaryDriver(primary)
                .age(age)
                .ageFactor(ageFactor)
                .drivingHistoryFactor(drivingHistoryFactor)
                .insuranceHistoryFactor(insuranceHistoryFactor)
                .creditScoreFactor(creditScoreFactor)
                .totalDriverFactor(totalFactor)
                .driverAdjustment(money(BASE_PREMIUM_PER_VEHICLE.multiply(totalFactor)))
                .build();
    }

    private Integer calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private BigDecimal ageFactor(Integer age) {
        if (age == null) {
            return new BigDecimal("0.10");
        }
        if (age < 25) {
            return new BigDecimal("0.30");
        }
        if (age > 65) {
            return new BigDecimal("0.15");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal drivingHistoryFactor(String drivingHistory) {
        String history = normalize(drivingHistory);
        if (history.isEmpty() || history.contains("clean") || history.contains("none") || history.contains("good")) {
            return BigDecimal.ZERO;
        }
        if (history.contains("accident") || history.contains("dui") || history.contains("major")) {
            return new BigDecimal("0.25");
        }
        // minor violations / tickets
        return new BigDecimal("0.10");
    }

    private BigDecimal insuranceHistoryFactor(String insuranceHistory) {
        String history = normalize(insuranceHistory);
        if (history.isEmpty() || history.contains("continuous") || history.contains("active")
                || history.contains("insured") && !history.contains("uninsured")) {
            return BigDecimal.ZERO;
        }
        if (history.contains("lapse") || history.contains("cancel") || history.contains("uninsured")
                || history.contains("none") || history.contains("new")) {
            return new BigDecimal("0.10");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal creditScoreFactor(String creditScore) {
        Integer score = parseInteger(creditScore);
        if (score == null) {
            // non-numeric values such as "Good" / "Poor"
            String normalized = normalize(creditScore);
            if (normalized.contains("excellent") || normalized.contains("good")) {
                return new BigDecimal("-0.05");
            }
            if (normalized.contains("poor") || normalized.contains("bad")) {
                return new BigDecimal("0.15");
            }
            return BigDecimal.ZERO;
        }
        if (score >= 750) {
            return new BigDecimal("-0.10");
        }
        if (score < 650) {
            return new BigDecimal("0.15");
        }
        return BigDecimal.ZERO;
    }

    // ----- Vehicle factors -----

    private BigDecimal vehicleAgeFactor(String year) {
        Integer vehicleYear = parseInteger(year);
        if (vehicleYear == null) {
            return BigDecimal.ZERO;
        }
        int vehicleAge = LocalDate.now().getYear() - vehicleYear;
        if (vehicleAge <= 3) {
            return new BigDecimal("0.15");
        }
        if (vehicleAge > 10) {
            return new BigDecimal("-0.05");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal vehicleTypeFactor(String vehicleType) {
        String type = normalize(vehicleType);
        if (type.contains("sport")) {
            return new BigDecimal("0.25");
        }
        if (type.contains("suv") || type.contains("truck") || type.contains("luxury")) {
            return new BigDecimal("0.10");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal usageTypeFactor(String usageType) {
        String usage = normalize(usageType);
        if (usage.contains("commercial") || usage.contains("business") || usage.contains("rideshare")) {
            return new BigDecimal("0.20");
        }
        if (usage.contains("commute") || usage.contains("work")) {
            return new BigDecimal("0.10");
        }
        return BigDecimal.ZERO;
    }

    // ----- Helpers -----

    private String buildDriverName(DriverDTO driver) {
        String firstName = driver.getFirstName() == null ? "" : driver.getFirstName();
        String lastName = driver.getLastName() == null ? "" : driver.getLastName();
        return (firstName + " " + lastName).trim();
    }

    private String buildVehicleName(VehicleDTO vehicle) {
        StringBuilder name = new StringBuilder();
        if (vehicle.getYear() != null) {
            name.append(vehicle.getYear()).append(" ");
        }
        if (vehicle.getMake() != null) {
            name.append(vehicle.getMake()).append(" ");
        }
        if (vehicle.getModel() != null) {
            name.append(vehicle.getModel());
        }
        return name.toString().trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
