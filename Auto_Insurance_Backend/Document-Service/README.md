# Document-Service

Java 17 / Spring Boot service for generating and emailing policy PDFs. It stores PDFs under `./generated-documents` by default and stores document metadata (including the local path) in its own MySQL database. The API never returns that path.

## Configuration

Set these environment variables when using non-default local settings:

```text
JWT_SECRET=my-local-insurance-jwt-secret-key-123456789
DOCUMENT_DB_URL=jdbc:mysql://localhost:3306/prj_documentdb?createDatabaseIfNotExist=true
DOCUMENT_DB_USERNAME=root
DOCUMENT_DB_PASSWORD=your-local-database-password
DOCUMENT_STORAGE_LOCATION=./generated-documents
POLICY_SERVICE_URL=http://localhost:8082/api/policy
CUSTOMER_SERVICE_URL=http://localhost:8081/customer
PREMIUM_SERVICE_URL=http://localhost:8085
PAYMENT_SERVICE_URL=http://localhost:8084/api/payment
MAIL_USERNAME=your-gmail-address@gmail.com
MAIL_APP_PASSWORD=your-16-character-google-app-password
EUREKA_URL=http://localhost:8761/eureka/
```

`MAIL_APP_PASSWORD` is a Google App Password, not the Gmail account password. Configure secrets in the process environment; do not commit them. The service validates the same HS256 JWT secret as Authentication-Service and forwards the bearer token to downstream services.

Claims-Service uses port `8088` by default in this workspace because Document-Service owns the requested port `8087`.

## Run

Start MySQL, Eureka, Policy-Service, CustomerMicroservices, Premium-Service, PaymentService, and API Gateway. Then run:

```powershell
cd Document-Service
mvn spring-boot:run
```

The service listens on `http://localhost:8087`; browser requests should use the API Gateway at `http://localhost:8080/api/documents`.

## API examples

All endpoints require `Authorization: Bearer <JWT>`.

Generate a policy document (returns existing generated metadata when present):

```http
POST /api/documents/policies/POL-12345678/generate
Authorization: Bearer <JWT>
```

Generate and email, view metadata, view/download PDF, or resend email:

```http
POST /api/documents/policies/POL-12345678/generate-and-email
GET  /api/documents/DOC-A12B34CD
GET  /api/documents/DOC-A12B34CD/download?disposition=inline
GET  /api/documents/DOC-A12B34CD/download
POST /api/documents/DOC-A12B34CD/email
Content-Type: application/json

{"recipientEmail":"customer@gmail.com"}
```

List by policy/customer and explicitly regenerate:

```http
GET  /api/documents/policies/POL-12345678
GET  /api/documents/customers/41
POST /api/documents/policies/POL-12345678/regenerate
```

Successful metadata response:

```json
{
  "documentId": "DOC-A12B34CD",
  "policyId": "POL-12345678",
  "customerId": 41,
  "documentType": "POLICY_DOCUMENT",
  "fileName": "POL-12345678_DOC-A12B34CD.pdf",
  "generatedAt": "2026-11-03T12:00:00",
  "recipientEmail": "customer@gmail.com",
  "emailStatus": "SENT",
  "emailedAt": "2026-11-03T12:01:00",
  "documentStatus": "GENERATED"
}
```

Errors use `{ "timestamp", "status", "error", "message", "path" }`. PDFs are returned as `application/pdf`; `disposition=inline` opens in-browser and the default attachment downloads. Policy ownership is checked against the authenticated JWT `customerId`, not a request-supplied identity.