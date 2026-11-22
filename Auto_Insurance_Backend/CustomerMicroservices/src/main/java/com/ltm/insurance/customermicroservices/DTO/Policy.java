package com.ltm.insurance.customermicroservices.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ltm.insurance.customermicroservices.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "policyId",
        "policyType",
        "policyStatus",
        "basePremium",
        "driverAdjustment",
        "vehicleAdjustment",
        "totalDiscount",
        "premiumAmount",
        "effectiveDate",
        "expirationDate",
        "driver"
})
public class Policy {

    private String policyId;

    private Date effectiveDate = new Date();

    private Date expirationDate;

    private String policyType;

    private String policyStatus;

    private BigDecimal premiumAmount;  //added by AI
    private BigDecimal basePremium;
    private BigDecimal driverAdjustment;
    private BigDecimal vehicleAdjustment;
    private BigDecimal totalDiscount;
    private String coverageDetails;
    private BigDecimal deductible;
    private LocalDate createdDate;

    private String paymentType;   //added by AI

    private List<Driver> driver;
}
