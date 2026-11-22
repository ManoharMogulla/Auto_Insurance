package com.ltm.insurance.documentservice.service;

import com.ltm.insurance.documentservice.dto.PaymentDto;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import com.ltm.insurance.documentservice.exception.EmailDeliveryException;
import com.ltm.insurance.documentservice.util.DocumentValues;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentEmailService {

    private final JavaMailSender mailSender;

    public void send(String recipient, String fileName, byte[] pdf, PolicyDto policy,
                     PolicyDto.DriverDto customer, PaymentDto payment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            String customerName = customer == null
                    ? "Customer"
                    : ((customer.getFirstName() == null ? "" : customer.getFirstName()) + " "
                    + (customer.getLastName() == null ? "" : customer.getLastName())).trim();
            helper.setTo(recipient);
            helper.setSubject("Your Auto Insurance Policy Document - " + DocumentValues.value(policy.getPolicyId()));
            helper.setText("Dear " + DocumentValues.value(customerName) + ",\n\n"
                    + "Your auto insurance policy has been successfully issued.\n\n"
                    + "Policy Number: " + DocumentValues.value(policy.getPolicyId()) + "\n"
                    + "Policy Status: " + DocumentValues.value(policy.getPolicyStatus()) + "\n"
                    + "Coverage Start Date: " + DocumentValues.value(policy.getEffectiveDate()) + "\n"
                    + "Coverage End Date: " + DocumentValues.value(policy.getExpirationDate()) + "\n"
                    + "Selected Coverages: " + selectedCoverageSummary(policy) + "\n"
                    + "Final Premium: " + DocumentValues.value(policy.getPremiumAmount()) + "\n\n"
                    + "Your policy document is attached to this email.\n\n"
                    + "Regards,\nAuto Insurance Team");
            helper.addAttachment(fileName, new ByteArrayResource(pdf));
            mailSender.send(message);
        } catch (Exception exception) {
            throw new EmailDeliveryException("Unable to deliver policy document email", exception);
        }
    }

    private String selectedCoverageSummary(PolicyDto policy) {
        if (policy == null || policy.getDriver() == null) return "Not available";

        Set<String> types = new LinkedHashSet<>();
        for (PolicyDto.DriverDto driver : policy.getDriver()) {
            if (driver == null || driver.getVehicles() == null) continue;
            for (PolicyDto.VehicleDto vehicle : driver.getVehicles()) {
                if (vehicle == null || vehicle.getCoverages() == null) continue;
                for (PolicyDto.CoverageDto coverage : vehicle.getCoverages()) {
                    if (coverage == null || coverage.getCoverageType() == null) continue;
                    String type = coverage.getCoverageType().trim();
                    if (!type.isBlank()) {
                        types.add(type);
                    }
                }
            }
        }

        List<String> ordered = new ArrayList<>(types);
        return ordered.isEmpty() ? "Not available" : String.join(", ", ordered);
    }
}