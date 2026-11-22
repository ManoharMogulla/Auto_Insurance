package com.ltm.insurance.customermicroservices.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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

    private String drivingHistory;
    private String insuranceHistory;
    private String creditScore;

    private String policyId;
}