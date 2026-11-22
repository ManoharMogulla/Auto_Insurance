package com.ltm.insurance.customermicroservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CustomerMicroservicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerMicroservicesApplication.class, args);
    }

}
