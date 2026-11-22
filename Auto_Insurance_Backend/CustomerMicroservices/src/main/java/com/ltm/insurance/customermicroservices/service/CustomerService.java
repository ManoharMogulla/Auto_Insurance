package com.ltm.insurance.customermicroservices.service;

import com.ltm.insurance.customermicroservices.DTO.CustomerProfileRequest;
import com.ltm.insurance.customermicroservices.DTO.CustomerProfileResponse;
import com.ltm.insurance.customermicroservices.DTO.MemberNumber;
import com.ltm.insurance.customermicroservices.DTO.Policy;
import com.ltm.insurance.customermicroservices.DTO.PolicyDetails;
import com.ltm.insurance.customermicroservices.client.PolicyCilent;
import com.ltm.insurance.customermicroservices.model.CustomerProfile;
import com.ltm.insurance.customermicroservices.model.Coverage;
import com.ltm.insurance.customermicroservices.model.Driver;
import com.ltm.insurance.customermicroservices.model.Vehicle;
import com.ltm.insurance.customermicroservices.repo.CustomerProfileRepository;
import com.ltm.insurance.customermicroservices.repo.DriverRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final PolicyCilent policyCilent;
    private final DriverRepository driverRepository;
    private final CustomerProfileRepository customerProfileRepository;

    private final MemberNumber memberNumber = new MemberNumber();
    private static final Set<String> ALLOWED_COVERAGE_TYPES = Set.of(
            "COLLISION",
            "COMPREHENSIVE",
            "PERSONAL_INJURY_PROTECTION",
            "ROADSIDE_ASSISTANCE"
    );

    public CustomerProfileResponse createCustomerProfile(
            CustomerProfileRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String phoneNumber = request.phoneNumber()
                .replaceAll("\\D", "");

        if (customerProfileRepository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }
        if (customerProfileRepository.existsByPhoneNumber(phoneNumber)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered"
            );
        }

        CustomerProfile profile =
                customerProfileRepository.save(
                        new CustomerProfile(
                                memberNumber.generateMemberId(),
                                request.firstName().trim(),
                                request.lastName().trim(),
                                email,
                                phoneNumber
                        )
                );

        return toProfileResponse(profile);
    }


    public CustomerProfileResponse getCustomerProfile(Long customerId) {

        CustomerProfile profile =
                customerProfileRepository.findById(customerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Customer profile not found"
                                )
                        );

        return toProfileResponse(profile);
    }
    public List<Policy> getPoliciesForCustomer(Long customerId) {

        if (customerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer ID is required"
            );
        }

        List<String> policyIds =
                driverRepository
                        .findByCustomerIdAndNamedInsuredTrue(customerId)
                        .stream()
                        .map(Driver::getPolicyId)
                        .filter(policyId ->
                                policyId != null
                                        && !policyId.isBlank()
                        )
                        .distinct()
                        .toList();

        return policyIds.stream()
                .map(this::getPolicyIfAvailable)
                .filter(policy -> policy != null)
                .toList();
    }

    public String addPolicy(PolicyDetails policy, Long customerId) {

        validatePolicyRequest(policy);

        /*
         * One customer can have only one ACTIVE or ISSUED
         * policy at a time.
         */
        validateCustomerCanCreatePolicy(customerId);
        // Get logged-in customer's profile
        CustomerProfileResponse profile =
                getCustomerProfile(customerId);

        List<Driver> requestedDrivers =
                policy.getDrivers();

        /*
         * First driver becomes:
         *
         * customer
         * +
         * primary driver
         * +
         * named insured
         */
        prepareDrivers(requestedDrivers, profile, customerId);
        try {
            System.out.println("Creating policy for customerId: " + customerId);

            Policy policyResponse = policyCilent.createPolicy(policy);
            // Validate Policy Service response
            validatePolicyServiceResponse(policyResponse);

            String policyId = policyResponse.getPolicyId();

            System.out.println("Policy created successfully with ID: " + policyId);

            List<Driver> policyDrivers =
                    policyResponse.getDriver() == null
                            || policyResponse.getDriver().isEmpty()
                            ? requestedDrivers
                            : policyResponse.getDriver();

            prepareDriversForSaving(policyDrivers, requestedDrivers, customerId, policyId);
            // Save drivers in Customer DB
            driverRepository.saveAll(policyDrivers);

            return "Policy Created Successfully with ID: " + policyId;

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (FeignException exception) {
            exception.printStackTrace();
            HttpStatus downstreamStatus =
                    HttpStatus.resolve(exception.status());
            throw new ResponseStatusException(
                    downstreamStatus != null
                            ? downstreamStatus
                            : HttpStatus.SERVICE_UNAVAILABLE,
                    extractFeignMessage(exception),
                    exception
            );
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to create policy",
                    exception
            );
        }
    }


    private void validatePolicyRequest(PolicyDetails policy) {
        if (policy == null || policy.getDrivers() == null || policy.getDrivers().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one driver is required"
            );
        }
        for (Driver driver : policy.getDrivers()) {
            List<Vehicle> vehicles = driver.getVehicles();
            if (vehicles == null || vehicles.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "At least one vehicle is required for each driver"
                );
            }

            for (Vehicle vehicle : vehicles) {
                List<Coverage> coverages = vehicle.getCoverages();
                if (coverages == null || coverages.isEmpty()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "At least one coverage is required for each vehicle"
                    );
                }
                Set<String> coverageTypes = new HashSet<>();
                for (Coverage coverage : coverages) {
                    String coverageType = coverage.getCoverageType() == null
                            ? "" : coverage.getCoverageType().trim();
                    if (coverageType.isBlank()) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Coverage type is required"
                        );
                    }
                    if (!coverageTypes.add(coverageType.toUpperCase(Locale.ROOT))) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Duplicate coverage type is not allowed for the same vehicle"
                        );
                    }
                    if (!ALLOWED_COVERAGE_TYPES.contains(coverageType.toUpperCase(Locale.ROOT))) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Unsupported coverage type: " + coverageType
                        );
                    }
                    if (coverage.getCoverageLimit() == null || coverage.getCoverageLimit().signum() <= 0) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Coverage limit must be greater than zero"
                        );
                    }
                }
            }
        }
    }

    private String extractFeignMessage(FeignException exception) {

        String content = exception.contentUTF8();
        if (content != null && !content.isBlank()) {
            return content;
        }
        return exception.getMessage() != null
                ? exception.getMessage()
                : "Policy Service request failed";
    }

    private void validateCustomerCanCreatePolicy(Long customerId) {

        if (customerId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer ID is required");
        }

        if (hasActiveOrIssuedPolicy(customerId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer already has an active or issued policy");
        }
    }

    private boolean hasActiveOrIssuedPolicy(Long customerId) {

        List<Driver> primaryDrivers =
                driverRepository.findByCustomerIdAndNamedInsuredTrue(customerId);

        if (primaryDrivers == null || primaryDrivers.isEmpty()) {
            return false;
        }

        List<String> policyIds =
                primaryDrivers.stream()
                        .map(Driver::getPolicyId)
                        .filter(policyId -> policyId != null && !policyId.isBlank())
                        .distinct()
                        .toList();

        for (String policyId : policyIds) {
            try {
                Policy existingPolicy = policyCilent.getPolicySummaryByPolicyId(policyId);
                if (existingPolicy == null) {
                    continue;
                }
                String policyStatus =
                        existingPolicy.getPolicyStatus();
                if (policyStatus == null) {
                    continue;
                }
                System.out.println("Existing policy: " + policyId + ", status: " + policyStatus);
                if ("ACTIVE".equalsIgnoreCase(policyStatus)
                        || "ISSUED".equalsIgnoreCase(
                        policyStatus)) {
                    System.out.println(
                            "Active/Issued policy found for customerId "
                                    + customerId
                                    + ": "
                                    + policyId
                    );
                    return true;
                }

            } catch (FeignException.NotFound exception) {
                System.out.println(
                        "Ignoring stale policy reference: " + policyId + " for customerId: " + customerId
                );


            } catch (FeignException exception) {

                exception.printStackTrace();
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to verify existing policy status",
                        exception
                );
            }
        }
        return false;
    }

    private Policy getPolicyIfAvailable(
            String policyId) {
        try {
            return policyCilent
                    .getPolicySummaryByPolicyId(policyId);
        } catch (FeignException.NotFound exception) {
            System.out.println(
                    "Ignoring stale policy reference while "
                            + "loading policies: "
                            + policyId
            );
            return null;
        } catch (FeignException exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to retrieve policies because "
                            + "Policy Service is unavailable",
                    exception
            );
        }
    }
    private void prepareDrivers(
            List<Driver> requestedDrivers,
            CustomerProfileResponse profile,
            Long customerId) {
        for (int index = 0; index < requestedDrivers.size(); index++) {

            Driver driver = requestedDrivers.get(index);
            // Generate driver/member ID
            driver.setMemberId(
                    memberNumber.generateMemberId()
            );
            if (index == 0) {
                /*
                 * First driver = logged-in customer.
                 *
                 * Customer profile is authoritative for these
                 * identity fields.
                 */
                driver.setFirstName(profile.firstName());
                driver.setLastName(profile.lastName());
                driver.setEmail(profile.email());
                driver.setPhoneNumber(profile.phoneNumber());

//                driver.setCustomerId(
//                        customerId
//                );
                driver.setNamedInsured(true);
            } else {
                /*
                 * Additional drivers belong to the policy,
                 * but they are not the logged-in customer.
                 */
//                driver.setCustomerId(null);
                driver.setNamedInsured(false);
            }
        }
    }
    private void prepareDriversForSaving(
            List<Driver> policyDrivers,
            List<Driver> requestedDrivers,
            Long customerId,
            String policyId) {
        for (int index = 0;
             index < policyDrivers.size();
             index++) {
            Driver driver =
                    policyDrivers.get(index);

            // Every driver belongs to created policy
            driver.setPolicyId(policyId);

            if (driver.getMemberId() == null) {

                if (index < requestedDrivers.size()
                        && requestedDrivers
                        .get(index)
                        .getMemberId() != null) {

                    driver.setMemberId(
                            requestedDrivers
                                    .get(index)
                                    .getMemberId()
                    );

                } else {

                    driver.setMemberId(memberNumber.generateMemberId());
                }
            }


            // First driver remains customer/named insured
            if (index == 0) {
                driver.setCustomerId(customerId);
                driver.setNamedInsured(true);
            } else {
                driver.setCustomerId(null);
                driver.setNamedInsured(false);
            }
        }
    }

    private void validatePolicyServiceResponse(Policy policyResponse) {

        if (policyResponse == null || policyResponse.getPolicyId() == null
                || policyResponse.getPolicyId().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Policy Service did not return a valid policy ID"
            );
        }
    }
    public List<PolicyDetails> getAllPolicies(Long memberId) {

        return policyCilent.getAllPolicies(memberId);
    }

    public List<Driver> getAllDrivers() {

        return driverRepository.findAll();
    }

    public List<Driver> getDriversByPolicyId(String policyId) {

        if (policyId == null || policyId.isBlank()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy ID is required");
        }

        System.out.println("Inside getDriversByPolicyId " + "with policyId: " + policyId);

        return driverRepository.findByPolicyId(policyId);
    }

    public String renewPolicy(String policyId) {

        if (policyId == null || policyId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Policy ID is required"
            );
        }
        try {
            return policyCilent.renewPolicy(policyId);
        } catch (FeignException.NotFound exception) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Policy not found: " + policyId,
                    exception
            );


        } catch (FeignException exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Policy Service is unavailable",
                    exception
            );
        }
    }

    private CustomerProfileResponse toProfileResponse(
            CustomerProfile profile) {

        return new CustomerProfileResponse(
                profile.getMemberId(),
                profile.getMemberId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getPhoneNumber()
        );
    }
}