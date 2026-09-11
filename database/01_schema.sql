-- =======================================================
-- Gold Loan Management System - Database Schema
-- Database: star_finance_db
-- =======================================================

CREATE DATABASE IF NOT EXISTS star_finance_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE star_finance_db;

-- 1. Customer Table
CREATE TABLE IF NOT EXISTS customer (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    kn_number           VARCHAR(255) UNIQUE,
    aadhaar             VARCHAR(12) UNIQUE,
    pan_card            VARCHAR(10) UNIQUE,
    kyc_status          TINYINT(1) NOT NULL DEFAULT 0,
    password            VARCHAR(255),
    oauth_provider      VARCHAR(255),
    oauth_id            VARCHAR(255) UNIQUE,
    kyc_verified        TINYINT(1) DEFAULT 0,
    bank_account_number VARCHAR(30),
    city                VARCHAR(100),
    date_of_birth       VARCHAR(255),
    existing_loans      VARCHAR(50),
    full_address        VARCHAR(500),
    gender              VARCHAR(10),
    ifsc_code           VARCHAR(15),
    income              VARCHAR(50),
    mobile_number       VARCHAR(15),
    occupation          VARCHAR(100),
    passport_number     VARCHAR(20),
    pin_code            VARCHAR(10),
    state               VARCHAR(100),
    INDEX idx_customer_email (email),
    INDEX idx_customer_kn (kn_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Employee Table
CREATE TABLE IF NOT EXISTS employee (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    role        ENUM('BANK_ADMIN','BANK_STAFF') NOT NULL DEFAULT 'BANK_STAFF',
    branch_name VARCHAR(255) NOT NULL,
    INDEX idx_employee_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Gold Rates Reference Table
CREATE TABLE IF NOT EXISTS gold_rates (
    id              INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    karat_label     VARCHAR(255) NOT NULL UNIQUE,
    rate_per_gram   DECIMAL(38,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. KYC Verification Authority Reference Data (Simulation)
CREATE TABLE IF NOT EXISTS kyc_reference (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    aadhaar_number  VARCHAR(12) NOT NULL UNIQUE,
    pan_number      VARCHAR(10) NOT NULL UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_kyc_aadhaar_pan (aadhaar_number, pan_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Customer Bank Account Table
CREATE TABLE IF NOT EXISTS bankaccount (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_holder_name VARCHAR(255) NOT NULL,
    joint_account       TINYINT(1) DEFAULT 0,
    account_type        VARCHAR(20) NOT NULL DEFAULT 'SAVINGS',
    account_number      VARCHAR(255) NOT NULL UNIQUE,
    ifsc_code           VARCHAR(50) NOT NULL,
    bank_name           VARCHAR(255) NOT NULL,
    branch_name         VARCHAR(255) NOT NULL,
    customer_id         BIGINT NOT NULL,
    CONSTRAINT fk_bankaccount_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    INDEX idx_bankaccount_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Collateral / Gold Asset Table
CREATE TABLE IF NOT EXISTS asset (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(255) NOT NULL,
    weight          DECIMAL(38,2) NOT NULL,
    purchase_place  VARCHAR(255),
    jeweler_name    VARCHAR(255),
    photo_url       VARCHAR(255),
    customer_id     BIGINT NOT NULL,
    quality_index   DOUBLE,
    CONSTRAINT fk_asset_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    INDEX idx_asset_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Loan Application Table
CREATE TABLE IF NOT EXISTS loanapplication (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rid                 VARCHAR(255) NOT NULL UNIQUE,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    amount              DECIMAL(38,2),
    customer_id         BIGINT NOT NULL,
    asset_id            BIGINT NOT NULL,
    bank_account_id     BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    final_value         DECIMAL(38,2),
    rejection_reason    VARCHAR(500),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_asset FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_bankaccount FOREIGN KEY (bank_account_id) REFERENCES bankaccount(id) ON DELETE SET NULL,
    INDEX idx_loan_status (status),
    INDEX idx_loan_customer (customer_id),
    INDEX idx_loan_rid (rid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
