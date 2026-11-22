@echo off
title LTM Insurance - Start All Services

echo Starting Insurance Microservices...

start "Authentication Service" cmd /k "cd /d C:\LTM_Training\Training_Project\Authentication-Service && mvn spring-boot:run"

start "Customer Service" cmd /k "cd /d C:\LTM_Training\Training_Project\CustomerMicroservices && mvn spring-boot:run"

start "Claims Service" cmd /k "cd /d C:\LTM_Training\Training_Project\Claims-Service && mvn spring-boot:run"

start "Document Service" cmd /k "cd /d C:\LTM_Training\Training_Project\Document-Service && mvn spring-boot:run"

start "Payment Service" cmd /k "cd /d C:\LTM_Training\Training_Project\PaymentService && mvn spring-boot:run"

start "Policy Service" cmd /k "cd /d C:\LTM_Training\Training_Project\Policy-Service && mvn spring-boot:run"

start "Premium Service" cmd /k "cd /d C:\LTM_Training\Training_Project\Premium-Service && mvn spring-boot:run"

start "Eureka Server" cmd /k "cd /d C:\LTM_Training\Training_Project\insurance-infrastructure\eureka-server && mvn spring-boot:run"

start "API Gateway" cmd /k "cd /d C:\LTM_Training\Training_Project\insurance-infrastructure\api-gateway && mvn spring-boot:run"

echo.
echo All microservices started.