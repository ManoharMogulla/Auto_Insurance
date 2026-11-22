package com.ltm.insurance.customermicroservices.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CustomerSecurityConfiguration {

    /**
     * JWT Decoder.
     *
     * Authentication-Service signs the JWT using the same secret.
     * CustomerMicroservices uses the same secret to validate it.
     */
    @Bean
    JwtDecoder customerJwtDecoder(
            @Value("${security.jwt.secret}") String secret) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes for HS256"
            );
        }

        SecretKey secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Spring Security configuration.
     */
    @Bean
    SecurityFilterChain customerSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("customerCorsConfigurationSource")
            CorsConfigurationSource corsConfigurationSource)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize -> authorize

                        // Create policy requires authenticated customer
                        .requestMatchers(
                                HttpMethod.POST,
                                "/customer/addPolicy"
                        )
                        .authenticated()

                        // Get logged-in customer's policies
                        .requestMatchers(
                                HttpMethod.GET,
                                "/customer/policies"
                        )
                        .authenticated()

                        // Allow other existing Customer Service endpoints
                        .anyRequest()
                        .permitAll()
                )

                /*
                 * Customer Service acts as JWT Resource Server.
                 * JWT issued by Authentication-Service is validated here.
                 */
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt -> {
                        })
                );

        return http.build();
    }

    /**
     * CORS configuration for React/Vite frontend.
     */
    @Bean
    CorsConfigurationSource customerCorsConfigurationSource(
            @Value("${cors.allowed-origins:http://localhost:5173}")
            String allowedOrigins) {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Internal-Service-Token"
                )
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}