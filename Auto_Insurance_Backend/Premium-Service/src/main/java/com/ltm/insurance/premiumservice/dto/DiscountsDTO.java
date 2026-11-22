package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mirrors the Discounts model used by Customer-Service / Policy-Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountsDTO {

    private Long discountId;
    private Boolean safePilotDiscount;
    private Boolean goodStudentDiscount;
    private Boolean antiTheftDiscount;
    private Boolean lowMileageDiscount;
}
