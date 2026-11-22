package com.ltm.insurance.premiumservice.controller;

import com.ltm.insurance.premiumservice.dto.PremiumRequest;
import com.ltm.insurance.premiumservice.dto.PremiumResponse;
import com.ltm.insurance.premiumservice.service.PremiumCalculationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.sql.Driver;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumCalculationService premiumCalculationService;

    @PostMapping("/calculate")
    public PremiumResponse calculatePremium(@RequestBody PremiumRequest request) {
        return premiumCalculationService.calculatePremium(request);
    }

    @GetMapping("/")
    public String premiumHomePage() {
        return "Welcome to the Premium Service!";
    }
}
