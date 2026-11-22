package com.ltm.insurance.policyservice.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Generated;

public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long customerId;

    public String customerFirstName;
    public String customerLastName;


}
