package com.ltm.insurance.customermicroservices.DTO;


import com.ltm.insurance.customermicroservices.model.Driver;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.math.BigDecimal;

//
//@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@JsonPropertyOrder({
//        "policyId",
//        "policyType",
//        "policyStatus",
//        "effectiveDate",
//        "expirationDate",
//        "drivers"
//})

public class PolicyDetails {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long policyId;
//
//    private String policyType;
//    private String policyStatus;
//
//    @Temporal(TemporalType.DATE)
//    private Date effectiveDate = new Date();
//
//    @Temporal(TemporalType.DATE)
//    private Date expirationDate;
    private List<Driver> drivers;

    // Passed through to Policy-Service as the selected payment method
    private String paymentType;
    private String coverageDetails;
    private BigDecimal deductible;
}