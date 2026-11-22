package com.ltm.insurance.customermicroservices.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Discounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    private Boolean safePilotDiscount;
    private Boolean goodStudentDiscount;
    private Boolean antiTheftDiscount;
    private Boolean lowMileageDiscount;

    public int enabledDiscountCount() {
        return (Boolean.TRUE.equals(safePilotDiscount) ? 1 : 0)
                + (Boolean.TRUE.equals(goodStudentDiscount) ? 1 : 0)
                + (Boolean.TRUE.equals(antiTheftDiscount) ? 1 : 0)
                + (Boolean.TRUE.equals(lowMileageDiscount) ? 1 : 0);
    }
}
