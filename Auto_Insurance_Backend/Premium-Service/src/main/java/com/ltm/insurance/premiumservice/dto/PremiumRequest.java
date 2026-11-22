package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request payload for premium calculation.
 * Uses the same drivers -> vehicles -> discounts structure as Customer-Service.
 * The first driver is the Named Insured / Primary Driver.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PremiumRequest {

    private List<DriverDTO> drivers;
}
