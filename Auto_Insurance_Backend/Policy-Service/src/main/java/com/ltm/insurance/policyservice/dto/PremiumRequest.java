package com.ltm.insurance.policyservice.dto;

import com.ltm.insurance.policyservice.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PremiumRequest {

    private List<Driver> drivers;
}