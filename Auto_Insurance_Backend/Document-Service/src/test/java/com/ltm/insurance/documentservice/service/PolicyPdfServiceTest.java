package com.ltm.insurance.documentservice.service;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import com.ltm.insurance.documentservice.dto.PremiumDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyPdfServiceTest {

    @Test
    void generatesPdfWithPolicyTerms() throws Exception {
        PolicyDto policy = new PolicyDto();
        policy.setPolicyId("POL-12345678");
        policy.setPolicyType("Issued");
        policy.setPolicyStatus("Active");
        policy.setCoverageDetails("Liability and collision coverage");
        policy.setDeductible(new BigDecimal("500.00"));
        policy.setCreatedDate(LocalDate.of(2026, 11, 3));
        PolicyDto.DriverDto driver = new PolicyDto.DriverDto();
        driver.setCustomerId(41L);
        driver.setFirstName("Jordan");
        driver.setLastName("Lee");
        PremiumDto premium = new PremiumDto();

        byte[] pdf = new PolicyPdfService().generate("DOC-A12B34CD", LocalDateTime.now(),
                policy, List.of(driver), premium, null);
        String text = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1, true);

        assertTrue(text.contains("Coverage details: Liability and collision coverage"));
        assertTrue(text.contains("Deductible: 500.00"));
        assertTrue(text.contains("Created date: 2026-11-03"));
    }
}