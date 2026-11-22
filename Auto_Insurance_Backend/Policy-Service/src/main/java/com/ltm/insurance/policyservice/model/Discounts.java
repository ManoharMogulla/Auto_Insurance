package com.ltm.insurance.policyservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Discounts {


    private Long discountId;

    private Boolean safePilotDiscount;
    private Boolean goodStudentDiscount;
    private Boolean antiTheftDiscount;
    private Boolean lowMileageDiscount;
}
