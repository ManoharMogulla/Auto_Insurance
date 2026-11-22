package com.ltm.insurance.authentication.service;

import com.ltm.insurance.authentication.client.CustomerProfileClient;
import com.ltm.insurance.authentication.client.CustomerProfileRequest;
import com.ltm.insurance.authentication.client.CustomerProfileResponse;
import com.ltm.insurance.authentication.dto.AuthResponse;
import com.ltm.insurance.authentication.dto.CustomerIdentityResponse;
import com.ltm.insurance.authentication.dto.LoginRequest;
import com.ltm.insurance.authentication.dto.RegisterRequest;
import com.ltm.insurance.authentication.model.AuthUser;
import com.ltm.insurance.authentication.model.Role;
import com.ltm.insurance.authentication.repository.AuthUserRepository;
import com.ltm.insurance.authentication.security.AuthUserPrincipal;
import com.ltm.insurance.authentication.security.JwtService;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthenticationService {

    private final AuthUserRepository userRepository;
    private final CustomerProfileClient customerProfileClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthUserRepository userRepository,
                                 CustomerProfileClient customerProfileClient,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.customerProfileClient = customerProfileClient;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String phoneNumber = request.phoneNumber().replaceAll("\\D", "");
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        CustomerProfileResponse profile;
        try {
            profile = customerProfileClient.createProfile(new CustomerProfileRequest(
                    request.firstName().trim(), request.lastName().trim(), email, phoneNumber));
        } catch (FeignException exception) {
            if (exception.status() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Email or phone number is already registered", exception);
            }
            String downstreamBody = exception.contentUTF8().trim();
            if (exception.status() >= 400 && exception.status() < 500) {
            String reason = downstreamBody.isBlank()
                ? "Customer Service rejected profile creation with HTTP " + exception.status()
                : "Customer Service rejected profile creation: " + downstreamBody;
            throw new ResponseStatusException(
                HttpStatusCode.valueOf(exception.status()), reason, exception);
            }
            if (exception.status() >= 500) {
            String reason = downstreamBody.isBlank()
                ? "Customer Service failed profile creation with HTTP " + exception.status()
                : "Customer Service profile creation failed: " + downstreamBody;
            throw new ResponseStatusException(
                HttpStatusCode.valueOf(exception.status()), reason, exception);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to connect to Customer Service at " + exception.request().url()
                    + ": " + exception.getMessage(), exception);
        }
        if (profile == null || profile.memberId() == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer profile service did not return a member ID");
        }

        AuthUser user = userRepository.save(new AuthUser(
                email,
                phoneNumber,
                passwordEncoder.encode(request.password()),
                profile.memberId(),
                profile.customerId() == null ? profile.memberId() : profile.customerId(),
                profile.firstName(),
                profile.lastName(),
                Role.CUSTOMER
        ));
        return authResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(Locale.ROOT), request.password()));
        AuthUserPrincipal principal = (AuthUserPrincipal) authentication.getPrincipal();
        return authResponse(principal.getUser());
    }

    public CustomerIdentityResponse currentUser(AuthUserPrincipal principal) {
        AuthUser user = principal.getUser();
        return new CustomerIdentityResponse(
                user.getMemberId(), user.getCustomerId(), user.getFirstName(), user.getLastName(),
                fullName(user), user.getEmail(), user.getPhoneNumber(), user.getRole().name());
    }

    private AuthResponse authResponse(AuthUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                user.getMemberId(),
                user.getCustomerId(),
                user.getFirstName(),
                user.getLastName(),
                fullName(user),
                user.getEmail(),
                user.getRole().name());
    }

    private String fullName(AuthUser user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
