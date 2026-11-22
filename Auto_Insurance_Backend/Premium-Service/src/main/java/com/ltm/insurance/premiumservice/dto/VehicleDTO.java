package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Mirrors the Vehicle model used by Customer-Service / Policy-Service.
 * The first vehicle of the Primary Driver is the Primary Vehicle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {

    private Long vehicleId;
    private String make;
    private String model;
    private String year;
    private String vin;
    private String licensePlate;
    private String registrationState;
    private String registrationExpirationDate;
    private String vehicleType;
    private String usageType;

    private List<DiscountsDTO> discounts;
    private List<CoverageDTO> coverages;
}
