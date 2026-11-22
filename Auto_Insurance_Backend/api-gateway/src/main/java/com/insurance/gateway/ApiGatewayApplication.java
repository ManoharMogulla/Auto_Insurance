package com.insurance.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        System.out.println("ApiGatewayApplication.main() method called"+
                "SpringApplication.run(ApiGatewayApplication.class, args);");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
