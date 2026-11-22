package com.ltm.insurance.authentication.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProfileClientConfiguration {

    @Bean
    RequestInterceptor internalServiceTokenInterceptor(
            @Value("${internal.service.token:}") String internalServiceToken) {
        return template -> {
            if (!internalServiceToken.isBlank()) {
                template.header("X-Internal-Service-Token", internalServiceToken);
            }
        };
    }
}
