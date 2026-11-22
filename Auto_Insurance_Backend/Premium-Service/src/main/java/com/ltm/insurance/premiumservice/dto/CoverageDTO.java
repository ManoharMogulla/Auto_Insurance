package com.ltm.insurance.premiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoverageDTO {

    private Long coverageId;
    private String coverageType;
    private BigDecimal coverageLimit;
}