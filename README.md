# 🚗 Auto Insurance Management System

A full-stack Auto Insurance Management System developed using **Java, Spring Boot, Spring Cloud Microservices, and React.js**. The application streamlines end-to-end insurance operations, including customer registration, insurance quotes, premium calculation, policy management, payment processing, document generation, and claims management.

## 📌 Project Overview

The application provides a digital platform for managing auto insurance operations. Customers can register, log in, enter driver and vehicle details, generate insurance quotes, select coverage options, make payments, access policy documents, and submit insurance claims.

The backend follows a **Microservices Architecture**, enabling independent business services and communication through REST APIs.

## 🛠️ Technology Stack

### Backend
- Java
- Spring Boot
- Spring Cloud
- Spring Security
- Spring Data JPA
- RESTful Web Services
- OpenFeign

### Frontend
- React.js
- JavaScript
- HTML5
- CSS3
- Vite

### Microservices & Architecture
- Microservices Architecture
- Spring Cloud API Gateway
- Netflix Eureka Service Discovery
- Inter-service Communication

### Security
- Spring Security
- JWT Authentication and Authorization

### Build Tools
- Maven
- Git

## 🏗️ Microservices Architecture

The application consists of the following independent microservices:

| Microservice | Responsibilities |
|---|---|
| Authentication Service | Customer registration, login, and JWT authentication |
| Customer Microservice | Customer profiles, driver details, and vehicle information |
| Policy Service | Insurance quotes, policy creation, and policy activation |
| Premium Service | Premium calculation based on vehicle, driver, coverage, and discounts |
| Payment Service | Payment processing and payment status management |
| Document Service | Policy document generation, storage, and retrieval |
| Claims Service | Insurance claim creation and claim status tracking |
| API Gateway | Centralized routing of client requests |
| Eureka Server | Service registration and discovery |

## ✨ Key Features

- **Customer Management:** Customer registration, login, and profile management.
- **JWT Authentication:** Secure authentication and authorization.
- **Insurance Quote Generation:** Generate insurance quotes using driver and vehicle information.
- **Premium Calculation:** Calculate premiums based on vehicle details, driver information, coverage, and applicable discounts.
- **Policy Management:** Create and activate insurance policies.
- **Payment Processing:** Process payments and manage payment status.
- **Document Generation:** Automatically generate and retrieve policy documents.
- **Claims Management:** Submit insurance claims and track claim status.
- **API Gateway:** Route requests to appropriate microservices.
- **Service Discovery:** Register and discover microservices using Eureka.

## 🔄 Application Workflow

1. Customer registers and logs into the application.
2. Customer provides driver and vehicle information.
3. Customer selects insurance coverage and applicable discounts.
4. System generates an insurance quote.
5. Premium Service calculates the insurance premium.
6. Customer proceeds with payment.
7. Policy Service activates the policy after successful payment.
8. Document Service generates the policy document.
9. Customer can access policy documents and submit insurance claims.

## 🧩 Architecture Flow

```text
                 React.js Frontend
                         |
                         |
                   API Gateway
                         |
          +--------------+--------------+
          |              |              |
     Authentication  Customer       Policy
       Service       Service        Service
          |              |              |
          +--------------+--------------+
                         |
                 Premium Service
                         |
                 Payment Service
                         |
                 Document Service
                         |
                  Claims Service

              Eureka Service Discovery
```

## 🔐 Security Implementation

- Implemented JWT-based authentication.
- Integrated Spring Security for securing backend endpoints.
- Protected application APIs through authentication and authorization.
- Managed customer access to protected resources.

## ⚙️ Getting Started

### Prerequisites

Make sure the following tools are installed:

- Java JDK
- Maven
- Node.js and npm
- Git
- IDE such as IntelliJ IDEA or VS Code

### Clone the Repository

```bash
git clone https://github.com/ManoharMogulla/Auto_Insurance.git
```

```bash
cd Auto_Insurance
```

### Backend Setup

1. Open the backend project in IntelliJ IDEA.
2. Configure the required application properties.
3. Configure database connectivity as required by the project.
4. Build the application using Maven.
5. Start the Eureka Server and backend microservices in the required order.

```bash
mvn clean install
```

Run each Spring Boot microservice using its main application class.

### Frontend Setup

Navigate to the frontend project directory:

```bash
npm install
```

Start the React application:

```bash
npm run dev
```

Open the local URL displayed in the terminal to access the application.

## 🎯 Learning Outcomes

Through this project, I gained practical experience in:

- Developing REST APIs using Spring Boot.
- Designing and implementing microservices architecture.
- Implementing JWT authentication using Spring Security.
- Integrating independent microservices.
- Implementing API Gateway and Eureka Service Discovery.
- Building frontend components using React.js.
- Developing end-to-end insurance business workflows.

## 👨‍💻 Author

**Manohar Mogulla**

Java Backend Developer | Spring Boot | Microservices

[GitHub Profile](https://github.com/ManoharMogulla)

---

⭐ If you find this project useful, feel free to explore the repository!
