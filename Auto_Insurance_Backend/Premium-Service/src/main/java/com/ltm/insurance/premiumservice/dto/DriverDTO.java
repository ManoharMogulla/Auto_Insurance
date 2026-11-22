package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Mirrors the Driver model used by Customer-Service / Policy-Service.
 * The first driver in the request is the Named Insured / Primary Driver.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {

    private Long memberId;
    private Long customerId;
    private boolean namedInsured;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String licenseNumber;
    private String licenseState;
    private String licenseExpirationDate;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phoneNumber;
    private String email;
    private String gender;
    private String maritalStatus;

    private List<VehicleDTO> vehicles;

    private String drivingHistory;
    private String insuranceHistory;
    private String creditScore;

    private String policyId;
}
