package com.ltm.insurance.authentication.controller;

import com.ltm.insurance.authentication.dto.AuthResponse;
import com.ltm.insurance.authentication.dto.CustomerIdentityResponse;
import com.ltm.insurance.authentication.dto.LoginRequest;
import com.ltm.insurance.authentication.dto.RegisterRequest;
import com.ltm.insurance.authentication.security.AuthUserPrincipal;
import com.ltm.insurance.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/me")
    public CustomerIdentityResponse me(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return authenticationService.currentUser(principal);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out; discard the bearer token on the client."));
    }
}
