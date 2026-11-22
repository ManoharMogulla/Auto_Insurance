package com.ltm.insurance.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyDto {
    private String policyId;
    private String policyType;
    private String policyStatus;
    private BigDecimal premiumAmount;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private LocalDate createdDate;
    private String coverageDetails;
    private BigDecimal deductible;
    private String paymentType;
    private List<DriverDto> driver;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DriverDto {
        private Long memberId;
        private Long customerId;
        private boolean namedInsured;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String licenseNumber;
        private String licenseState;
        private String licenseExpirationDate;
        private String gender;
        private String maritalStatus;
        private String drivingHistory;
        private String insuranceHistory;
        private String creditScore;
        private String policyId;
        private List<VehicleDto> vehicles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VehicleDto {
        private Long vehicleId;
        private String licensePlate;
        private String vin;
        private String make;
        private String model;
        private String year;
        private String vehicleType;
        private String usageType;
        private String registrationState;
        private String registrationExpirationDate;
        private List<Map<String, Object>> discounts;
        private List<CoverageDto> coverages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoverageDto {
        private Long coverageId;
        private String coverageType;
        private BigDecimal coverageLimit;
    }
}