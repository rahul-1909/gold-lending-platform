# 🌟 Star Finance - Gold Loan Management System

[![Spring Boot 3.5.6](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Angular 20](https://img.shields.io/badge/Angular-20-red.svg)](https://angular.dev/)
[![MySQL 9.1](https://img.shields.io/badge/MySQL-9.1-blue.svg)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A full-stack, open-source **Gold & Silver Loan Management System** built with **Spring Boot 3.5.6 (Java 17)**, **Angular 20 (Tailwind CSS & Angular Material)**, and **MySQL 9.1**.

The system automates the entire loan lifecycle from customer digital onboarding and KYC verification to gold collateral inspection, LTV-based valuation, custom loan offer disbursement, and gold repossession/redemption workflows.

---

## 🏛️ System Architecture

`mermaid
graph TD
    A[Customer Client - Angular 20 SPA] -->|REST + Bearer JWT| B[API Gateway / Spring Security Filter Chain]
    C[Employee Operations Portal] -->|REST + Bearer JWT| B
    B -->|Stateless Authentication| D[JwtService & JwtAuthenticationFilter]
    B -->|Customer & Auth Endpoints| E[AuthController & CustomerController]
    B -->|Administrative & Workflow Endpoints| F[EmployeeActionsController & KycController]
    E --> G[CustomerService & LoanService]
    F --> G
    G --> H[(MySQL 9.1 Database: star_finance_db)]
    G -->|Purity-based Valuation 75% LTV| I[Gold Rates Registry]
`

### Key Modules
1. **Customer Portal**:
   - Self-service registration & BCrypt credential management.
   - Real-time gold loan valuation calculator with live karat rates (24K, 22K, 18K, 14K, 8K) and statutory 75% LTV ratio.
   - Comprehensive KYC submission with official reference cross-matching.
   - Loan tracking, offer review/decision modal, and fine payment reconciliation.
2. **Employee Operations Portal**:
   - Application queue filtering and KYC verification.
   - Physical gold deposit intake and appraisal inspection.
   - Interactive gold quality index and final offer adjustment modal.
   - Loan sanctioning and instantaneous disbursement logging.
   - Fine-paid gold release and closure management.
3. **Security Engine**:
   - Stateless JWT tokens (HMAC-SHA256).
   - Role-Based Access Control (ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_ADMIN).
   - Secure DTO projections (CustomerResponse, EmployeeResponse) preventing password hash leakage.
   - Centralized GlobalExceptionHandler returning consistent RFC 7807 compliant error payloads.

---

## 📋 Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Spring Boot 3.5.6, Java 17 LTS, Spring Data JPA, Spring Security, Hibernate |
| **Frontend** | Angular 20.3.7, Tailwind CSS 3.4, Angular Material 20.2, Zoneless Change Detection |
| **Database** | MySQL 9.1 Community Server with B-Tree composite indexing |
| **Authentication** | JWT (HMAC-SHA256), BCrypt (cost factor 10), OAuth2 Social Login Ready |
| **Testing** | JUnit 5, Mockito, AssertJ (42 automated unit tests passing) |

---

## 🚀 Getting Started

### Prerequisites
- **Java**: JDK 17 or higher
- **Node.js**: v20.x or higher & npm v10.x
- **MySQL**: 8.0 or 9.1 running on port 3306

---

### 1. Database Setup
1. Create the database and seed initial reference tables, live bullion rates, and initial staff accounts:
   Get-Content database/01_schema.sql -Raw | mysql -u root -p
   Get-Content database/02_seed.sql -Raw | mysql -u root -p

---

### 2. Backend Configuration & Startup
1. Configure environment variables (or copy .env.example):
   DB_URL=jdbc:mysql://localhost:3306/star_finance_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=root
   JWT_SECRET=starfinance-production-grade-256-bit-secure-jwt-signing-key-for-gold-loan-system
   JWT_EXPIRATION=86400000
   PORT=8080
   CORS_ALLOWED_ORIGINS=http://localhost:4200

2. Build and run the backend:
   cd backend
   ./mvnw clean package -DskipTests
   ./mvnw spring-boot:run
   The backend will be live on http://localhost:8080.

---

### 3. Frontend Setup & Startup
1. Install dependencies and start the development server:
   cd frontend
   npm install
   npm start
   The client application will be accessible at http://localhost:4200.

---

## 🔑 Demonstration Accounts

| Role | Username / Identifier | Access Level |
|---|---|---|
| **System Administrator** | admin@starfinance.com | Full Administrative & Employee Capabilities |
| **Loan Evaluation Staff** | staff@starfinance.com | Gold Appraisal, Verification, Disbursement |
| **Demo Customer** | Self-register via /signup | Customer Loan Portal & KYC Management |

> Credentials for demonstration accounts are seeded securely via BCrypt hashes into star_finance_db via database/02_seed.sql.

---

## 🔄 Complete Loan Lifecycle Workflow

1. Customer submits loan application (purity, weight, desired amount). Status: PENDING.
2. Employee verifies customer details & KYC documents. Status: VERIFIED.
3. Customer brings gold to branch for physical appraisal. Status: GOLD_SUBMITTED.
4. Employee inspects gold, inputs quality index, and adjusts final offer amount. Status: EVALUATED.
5. Employee sends formal sanctioned offer to customer. Status: OFFER_MADE.
6. Decision:
   - Accept: Customer accepts offer. Status: OFFER_ACCEPTED -> Employee disburses loan. Status: DISBURSED.
   - Reject: Customer rejects offer. Status: OFFER_REJECTED -> Customer pays appraisal fee (₹500). Status: PAID_FINE -> Staff returns gold. Status: GOLD_COLLECTED.

---

## 🧪 Testing & Verification

### Automated Tests
Backend test suite:
cd backend
./mvnw test
Total tests: 42, Failures: 0, Errors: 0, Skipped: 0 (BUILD SUCCESS).

### Frontend Production Build
cd frontend
npm run build
Built cleanly with 0 errors.

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
