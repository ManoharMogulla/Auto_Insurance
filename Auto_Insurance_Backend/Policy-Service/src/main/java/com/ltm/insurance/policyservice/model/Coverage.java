package com.ltm.insurance.policyservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coverage {

    private Long coverageId;
    private String coverageType;
    private BigDecimal coverageLimit;
}