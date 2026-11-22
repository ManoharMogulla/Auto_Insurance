package com.ltm.insurance.documentservice.service;

import com.ltm.insurance.documentservice.dto.PaymentDto;
import com.ltm.insurance.documentservice.dto.PolicyDto;
import com.ltm.insurance.documentservice.dto.PremiumDto;
import com.ltm.insurance.documentservice.util.DocumentValues;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyPdfService {

    public byte[] generate(String documentId, LocalDateTime generatedAt, PolicyDto policy,
                           List<PolicyDto.DriverDto> drivers, PremiumDto premium, PaymentDto payment) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document pdf = new Document();
            PdfWriter writer = PdfWriter.getInstance(pdf, output);
            writer.setPageEvent(new PageNumbers());
            pdf.open();

            Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            pdf.add(new Paragraph("LTM AUTO INSURANCE", heading));
            pdf.add(new Paragraph("Auto Insurance Policy Document", section));
            pdf.add(new Paragraph("Document ID: " + documentId));
            pdf.add(new Paragraph("Generated: " + generatedAt));
            pdf.add(new Paragraph(" "));

            PolicyDto.DriverDto primary = drivers.stream()
                    .filter(PolicyDto.DriverDto::isNamedInsured).findFirst()
                    .orElse(drivers.isEmpty() ? null : drivers.get(0));
            addSection(pdf, "Customer", rows(
                    "Customer ID", primary == null ? null : primary.getCustomerId(),
                    "Full name", fullName(primary),
                    "Email", primary == null ? null : primary.getEmail(),
                    "Phone number", primary == null ? null : primary.getPhoneNumber(),
                    "Address", address(primary)));
            addSection(pdf, "Policy", rows(
                    "Policy ID", policy.getPolicyId(),
                    "Policy number", policy.getPolicyId(),
                    "Policy type", policy.getPolicyType(),
                    "Policy status", policy.getPolicyStatus(),
                    "Start date", policy.getEffectiveDate(),
                    "End date", policy.getExpirationDate(),
                    "Coverage details", policy.getCoverageDetails(),
                    "Created date", policy.getCreatedDate()));

            List<String> vehicleRows = new ArrayList<>();
            int vehicleIndex = 0;
            for (PolicyDto.DriverDto driver : drivers) {
                if (driver.getVehicles() == null) continue;
                for (PolicyDto.VehicleDto vehicle : driver.getVehicles()) {
                    vehicleIndex++;
                    vehicleRows.add("Vehicle " + vehicleIndex);
                    vehicleRows.add("Vehicle ID: " + DocumentValues.value(vehicle.getVehicleId())
                            + " | Registration: " + DocumentValues.value(vehicle.getLicensePlate())
                            + " | VIN: " + DocumentValues.value(vehicle.getVin())
                            + " | Make: " + DocumentValues.value(vehicle.getMake())
                            + " | Model: " + DocumentValues.value(vehicle.getModel())
                            + " | Year: " + DocumentValues.value(vehicle.getYear())
                            + " | Type: " + DocumentValues.value(vehicle.getVehicleType())
                            + " | Coverages: " + coverageSummary(vehicle.getCoverages()));
                }
            }
            addSection(pdf, "Vehicle", vehicleRows);

            List<String> driverRows = new ArrayList<>();
            for (PolicyDto.DriverDto driver : drivers) {
                driverRows.add((driver.isNamedInsured() ? "Named insured / primary driver: " : "Additional driver: ")
                        + DocumentValues.value(fullName(driver)));
            }
            addSection(pdf, "Driver", driverRows);
            addSection(pdf, "Premium", rows(
                    "Base premium", premium == null ? null : premium.getBasePremium(),
                    "Discounts", premium == null ? null : premium.getTotalDiscount(),
                    "Additional charges", additionalCharges(premium),
                    "Final premium", policy.getPremiumAmount()));

            if (payment != null) {
                addSection(pdf, "Payment", rows(
                        "Payment ID", payment.getPaymentId(),
                        "Payment method", payment.getPaymentMethod(),
                        "Payment status", payment.getPaymentStatus(),
                        "Payment date", payment.getPaymentDate(),
                        "Amount paid", payment.getPremiumAmount()));
            }

            addSection(pdf, "Terms and Conditions", List.of(
                    "Coverage is subject to the terms, conditions, exclusions, and limits of the policy.",
                    "Please review the information and contact the insurer to report any inaccuracy."));
            addSection(pdf, "Disclaimer", List.of(
                    "This document is a summary of policy information available at the time of generation."
            ));
            pdf.close();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new com.ltm.insurance.documentservice.exception.DocumentGenerationException(
                    "Unable to generate policy PDF", exception);
        }
    }

    private void addSection(Document pdf, String title, List<String> values) throws Exception {
        pdf.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        for (String value : values) {
            if (value != null && !value.isBlank()) table.addCell(new Phrase(value));
        }
        if (table.size() == 0) table.addCell(new Phrase("Not Available"));
        pdf.add(table);
        pdf.add(new Paragraph(" "));
    }

    private List<String> rows(Object... pairs) {
        List<String> values = new ArrayList<>();
        for (int index = 0; index < pairs.length; index += 2) {
            values.add(pairs[index] + ": " + DocumentValues.value(pairs[index + 1]));
        }
        return values;
    }

    private String fullName(PolicyDto.DriverDto driver) {
        if (driver == null) return null;
        return ((driver.getFirstName() == null ? "" : driver.getFirstName()) + " "
                + (driver.getLastName() == null ? "" : driver.getLastName())).trim();
    }

    private String address(PolicyDto.DriverDto driver) {
        if (driver == null) return null;
        return java.util.Arrays.asList(driver.getAddress(), driver.getCity(), driver.getState(), driver.getZipCode())
                .stream().filter(value -> value != null && !value.isBlank()).reduce((a, b) -> a + ", " + b).orElse(null);
    }

    private String coverageSummary(List<PolicyDto.CoverageDto> coverages) {
        if (coverages == null || coverages.isEmpty()) {
            return "Not available";
        }
        List<String> rows = new ArrayList<>();
        for (PolicyDto.CoverageDto coverage : coverages) {
            if (coverage == null) continue;
            String type = DocumentValues.value(coverage.getCoverageType());
            BigDecimal limit = coverage.getCoverageLimit();
            rows.add(type + " (Limit: " + DocumentValues.value(limit) + ")");
        }
        return rows.isEmpty() ? "Not available" : String.join("; ", rows);
    }

        private java.math.BigDecimal additionalCharges(PremiumDto premium) {
        if (premium == null) return null;
        java.math.BigDecimal driver = premium.getDriverAdjustment() == null
            ? java.math.BigDecimal.ZERO : premium.getDriverAdjustment();
        java.math.BigDecimal vehicle = premium.getVehicleAdjustment() == null
            ? java.math.BigDecimal.ZERO : premium.getVehicleAdjustment();
        return driver.add(vehicle);
        }

    private static class PageNumbers extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase page = new Phrase("Page " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 9));
            com.lowagie.text.pdf.ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    page, document.right(), document.bottom() - 18, 0);
        }
    }
}