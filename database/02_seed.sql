-- =======================================================
-- Gold Loan Management System - Seed Data
-- Database: star_finance_db
-- =======================================================

USE star_finance_db;

-- 1. Seed Bullion / Gold Rates per Gram
INSERT INTO gold_rates (id, karat_label, rate_per_gram) VALUES
(1, '24 Karat', 8000.00),
(2, '22 Karat', 6600.00),
(3, '18 Karat', 5400.00),
(4, '14 Karat', 4200.00),
(5, '8 Karat', 2400.00)
ON DUPLICATE KEY UPDATE rate_per_gram = VALUES(rate_per_gram);

-- 2. Seed Official KYC Reference Data (Simulating External Government / Verification API)
INSERT INTO kyc_reference (aadhaar_number, pan_number, full_name) VALUES
('123456789012', 'ABCDE1234A', 'Ravi Kumar Sharma'),
('234567890123', 'FGHIJ5678B', 'Priya Singh Varma'),
('345678901234', 'KLMNO9012C', 'Amitesh Dasgupta'),
('456789012345', 'PQRST3456D', 'Sanjana Reddy'),
('567890123456', 'UVWXY7890E', 'Mohammad Zafar'),
('987654321098', 'XYZAB9876C', 'Rahul Teja Nalla')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 3. Seed Bank Employees (Admin & Staff)
-- Initial system user accounts
INSERT INTO employee (id, username, password, full_name, role, branch_name) VALUES
(1, 'admin@starfinance.com', '$2a$10$ki6In6Gl07Lvum8Itu7Es.tWiQz59KeDh.zlPjhjrevtwjjfAwoCK', 'System Administrator', 'BANK_ADMIN', 'Head Office - Mumbai'),
(2, 'staff@starfinance.com', '$2a$10$gm16Cbe9L9lxGcZkvQaH9eRvoLJNy1u3yzdrthDYLs8QDBb15WYZm', 'Loan Evaluation Officer', 'BANK_STAFF', 'Branch - Hyderabad Central')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    full_name = VALUES(full_name),
    role = VALUES(role),
    branch_name = VALUES(branch_name);
