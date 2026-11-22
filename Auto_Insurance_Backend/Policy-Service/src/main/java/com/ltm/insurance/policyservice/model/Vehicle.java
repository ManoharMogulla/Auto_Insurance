package com.ltm.insurance.policyservice.model;

import com.ltm.insurance.policyservice.model.Discounts;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

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

    private List<Discounts> discounts;
    private List<Coverage> coverages;
}