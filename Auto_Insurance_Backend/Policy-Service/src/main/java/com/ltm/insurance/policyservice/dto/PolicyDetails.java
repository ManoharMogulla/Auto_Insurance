package com.ltm.insurance.policyservice.dto;


import com.ltm.insurance.policyservice.model.Driver;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

//
//@Entity
@Getter
@Setter
@NoArgsConstructor
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
    private String policyId;

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

    private PaymentMethod paymentType;
    private String coverageDetails;
    private BigDecimal deductible;

}