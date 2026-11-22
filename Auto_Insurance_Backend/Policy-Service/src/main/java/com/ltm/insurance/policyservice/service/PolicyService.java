package com.ltm.insurance.policyservice.service;

import com.ltm.insurance.policyservice.client.CustomerClient;
import com.ltm.insurance.policyservice.client.DocumentClient;
import com.ltm.insurance.policyservice.client.PaymentService;
import com.ltm.insurance.policyservice.client.PremiumClient;
import com.ltm.insurance.policyservice.dto.*;
import com.ltm.insurance.policyservice.model.Policy;
import com.ltm.insurance.policyservice.repo.PolicyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final CustomerClient customerClient;
    private final PolicyRepo policyRepo;
    private final PaymentService paymentService;
    private final PremiumClient premiumClient;
    private final DocumentClient documentClient;

    public Policy createPolicy(PolicyDetails policyDetails) {

        Policy policy = new Policy();

        policy.setPolicyType("Quote");
        policy.setPolicyStatus("Pending Payment");
        policy.setDriver(policyDetails.getDrivers());
        policy.setCoverageDetails(policyDetails.getCoverageDetails());
//        policy.setDeductible(policyDetails.getDeductible());

        policy.setPaymentType(
                policyDetails.getPaymentType() != null
                        ? policyDetails.getPaymentType() : PaymentMethod.CARD );

        policy.setExpirationDate(
                new Date(
                        System.currentTimeMillis()
                                + 365L * 24 * 60 * 60 * 1000
                )
        );

        PremiumRequest premiumRequest =
                new PremiumRequest(policy.getDriver());

        System.out.println(
                "Calculating premium with drivers: "
                        + policy.getDriver()
        );

        PremiumResponseDTO premiumResponse = premiumClient.calculatePremium(premiumRequest);

        System.out.println("Premium calculated: " + premiumResponse.finalPremium());

        policy.setPremiumAmount(premiumResponse.finalPremium());
        policy.setBasePremium(premiumResponse.basePremium());
        policy.setDriverAdjustment(premiumResponse.driverAdjustment());
        policy.setVehicleAdjustment(premiumResponse.vehicleAdjustment());
        policy.setTotalDiscount(premiumResponse.totalDiscount());

        Policy savedPolicy = policyRepo.save(policy);

        System.out.println("Policy created successfully with ID: " + savedPolicy.getPolicyId());

        return savedPolicy;
    }

    public String makePayment(String policyId, PaymentRequest request, String authorizationHeader)
    {
        System.out.println("Checking payment request: " + request
                        + " for policyId: " + policyId);
        if (request == null
                || request.amount() == null
                || request.paymentMethod() == null) {

            throw new IllegalArgumentException(
                    "Valid payment amount and payment method are required");
        }
        Policy policy = policyRepo.findByPolicyId(policyId);

        if (policy == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Policy not found: " + policyId
            );
        }

        boolean policyWasAlreadyActive =
                "Active".equalsIgnoreCase(policy.getPolicyStatus());
        String paymentStatus =
                paymentService.makePayment(policyId, request);

        System.out.println(
                "Payment status for policyId "
                        + policyId
                        + ": "
                        + paymentStatus
        );

        if (isPaymentSuccessful(paymentStatus)) {

            policy.setPaymentType(request.paymentMethod());
            policy.setPolicyStatus("Active");
            policy.setPolicyType("Issued");

            policyRepo.save(policy);

            /*
             * Generate and email the policy document only when the
             * policy changes from Pending Payment to Active.
             *
             * This prevents duplicate document generation when the
             * payment endpoint is called more than once.
             */
            if (!policyWasAlreadyActive) {
                generateAndEmailPolicyDocument(
                        policyId,
                        authorizationHeader
                );
            }
        }

        return paymentStatus;
    }

    private boolean isPaymentSuccessful(String paymentStatus) {
        return paymentStatus != null
                && paymentStatus
                .toLowerCase(Locale.ROOT)
                .contains("payment successful");
    }

    private void generateAndEmailPolicyDocument(
            String policyId,
            String authorizationHeader
    ) {
        try {
            DocumentResponse response =
                    documentClient.generateAndEmail(policyId, authorizationHeader);

            System.out.println("Policy document generated and emailed successfully: "
                            + response);

        } catch (Exception exception) {
            /*
             * Do not roll back a successful payment because the
             * document generation or email failed.
             *
             * The user can retry document generation using the
             * Document Service regenerate endpoint.
             */
            System.err.println(
                    "Policy was activated, but document generation failed "
                            + "for policyId "
                            + policyId
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    public Policy getPolicyById(Long policyId) {
        return policyRepo.findById(policyId).orElse(null);
    }

    public Policy getPoliciesByPolicyId(String policyId) {

        Policy policy = policyRepo.findByPolicyId(policyId);

        if (policy == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Policy not found: " + policyId
            );
        }

        policy.setDriver(customerClient.getDriversByPolicyId(policyId));

        return policy;
    }

    public Policy getPolicySummaryByPolicyId(String policyId) {

        Policy policy = policyRepo.findByPolicyId(policyId);

        if (policy == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Policy not found: " + policyId
            );
        }
        return policy;
    }

    public List<Policy> getAllPolicies() {

        List<Policy> policyList = policyRepo.findAll();

        for (Policy policy : policyList) {
            policy.setDriver(
                    customerClient.getDriversByPolicyId(policy.getPolicyId())
            );
        }

        return policyList;
    }

    public String renewPolicy(String policyId) {

        Policy policy = policyRepo.findByPolicyId(policyId);

        if (policy == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Policy not found: " + policyId
            );
        }

        policy.setPolicyStatus("Renewed");

        policy.setExpirationDate(new Date(
                        System.currentTimeMillis()
                                + 365L * 24 * 60 * 60 * 1000));

        policyRepo.save(policy);

        return "Policy renewed successfully";
    }
}