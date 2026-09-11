# Star Finance - Frontend (LoanApp)

This is the Angular frontend client for the **Star Finance - Gold Loan Management System**.

## Tech Stack
- **Framework**: Angular 20 (Zoneless Change Detection, Standalone Components, SSR-ready)
- **Styling**: Tailwind CSS & Angular Material
- **Authentication**: JWT & OAuth2 (Google/Facebook integration)
- **HTTP Client**: Angular `provideHttpClient` with DI interceptors and fetch API

## Development Server
To run the frontend locally:
```bash
npm install
npm start
```
The application will be accessible at `http://localhost:4200/`.

## Production Build
```bash
npm run build
```
Build output is compiled into the `dist/loanapp` directory.

## Core Features
- Customer Onboarding & KYC verification
- Gold Loan Calculator & Real-time Loan Request Submission
- Branch Gold Handover / Deposit tracking
- Employee Operations Portal:
  - Multi-stage Loan Lifecycle Management (Verify -> Evaluate -> Offer -> Disburse)
  - Interactive evaluation modal with quality index and custom loan offer calculations
  - Gold rejection and audit tracking
  - Repossession / Fine payment reconciliation
