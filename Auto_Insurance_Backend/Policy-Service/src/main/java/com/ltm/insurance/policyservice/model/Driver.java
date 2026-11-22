package com.ltm.insurance.policyservice.model;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long memberId;
//    private Long customerId;
    private boolean namedInsured;
    private String firstName;
    private String lastName;
    @Temporal(TemporalType.DATE)
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


    private List<Vehicle> vehicles;

    private String drivingHistory;
    private String insuranceHistory;
    private String creditScore;

    @JsonSetter(value = "namedInsured", nulls = Nulls.SET)
    public void setNamedInsured(Boolean namedInsured) {
        this.namedInsured = Boolean.TRUE.equals(namedInsured);
    }

}
