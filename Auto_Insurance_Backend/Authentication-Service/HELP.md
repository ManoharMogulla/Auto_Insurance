# Authentication Service

Authentication Service stores customer credentials and JWT account claims only. Customer profile data is created and stored by CustomerMicroservices through the authenticated internal Feign client.

## Required configuration

Set `JWT_SECRET` in the environment for both Authentication Service and CustomerMicroservices. It must be Base64 encoded and decode to at least 32 bytes. Use the same secret in both services. Set `INTERNAL_SERVICE_TOKEN` to the same private value in Authentication Service and CustomerMicroservices; it protects profile creation over Feign.

Optional variables:

- `AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD`: the separate Authentication Service database (defaults to `prj_authenticationdb` on localhost).
- `CUSTOMER_SERVICE_URL`: Customer Service base URL (defaults to `http://localhost:8081`).
- `JWT_EXPIRATION_MS`: token lifetime (defaults to one hour).
- `CORS_ALLOWED_ORIGINS`: comma-separated allowed browser origins (defaults to `http://localhost:5173`).
- `SERVER_PORT`: service port (defaults to `8086`).

Run with `mvn spring-boot:run` from this directory after setting the required environment variables.

## Endpoints

- `POST /api/auth/register`: validate and create credentials, request a profile from Customer Service, and return a JWT and customer identity.
- `POST /api/auth/login`: authenticate with BCrypt and return a JWT and customer identity.
- `GET /api/auth/me`: return the current identity; requires `Authorization: Bearer <JWT>`.
- `POST /api/auth/logout`: acknowledge logout; the stateless token must also be discarded by the client and remains cryptographically valid until expiry.