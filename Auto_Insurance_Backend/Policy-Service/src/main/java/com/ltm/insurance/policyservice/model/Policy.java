package com.ltm.insurance.policyservice.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ltm.insurance.policyservice.dto.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@JsonPropertyOrder({
        "memberId",
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
@AllArgsConstructor
@NoArgsConstructor

public class Policy {

    @Column(unique = true, nullable = false)
    private String policyId="POL-" +
            UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date effectiveDate = new Date();

    @Temporal(TemporalType.DATE)
    private Date expirationDate;
    private String policyType;
    private String policyStatus;
//    private BigDecimal premiumAmount;

    @Transient
//    @OneToMany
    private List<Driver> driver;
    private BigDecimal premiumAmount;
    private BigDecimal basePremium;
    private BigDecimal driverAdjustment;
    private BigDecimal vehicleAdjustment;
    private BigDecimal totalDiscount;
    private PaymentMethod paymentType;
    private String coverageDetails;
    private LocalDate createdDate;

    @PrePersist
    void setCreatedDateIfMissing() {
        if (createdDate == null) createdDate = LocalDate.now();
    }

    public Policy(String paymentFailed, String inactive, Object o, Object o1) {


    }
}
