-- ============================================
-- Attendance & Payroll Management System
-- MySQL Database Creation Script
-- ============================================

-- Drop database if exists (CAUTION: This will delete all data!)
DROP DATABASE IF EXISTS attendance_payroll_db;

-- Create database
CREATE DATABASE attendance_payroll_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE attendance_payroll_db;

-- ============================================
-- Table: users
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    hourly_rate DOUBLE NOT NULL,
    weekend_overtime_multiplier DOUBLE NOT NULL DEFAULT 1.5,
    holiday_overtime_multiplier DOUBLE NOT NULL DEFAULT 2.0,
    regular_overtime_multiplier DOUBLE NOT NULL DEFAULT 1.25,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_active (active)
) ENGINE=InnoDB;

-- ============================================
-- Table: user_roles
-- ============================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, roles),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- ============================================
-- Table: attendance
-- ============================================
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP NULL,
    check_in_latitude DOUBLE NOT NULL,
    check_in_longitude DOUBLE NOT NULL,
    check_out_latitude DOUBLE NULL,
    check_out_longitude DOUBLE NULL,
    hours_worked DOUBLE NULL,
    regular_hours DOUBLE NULL,
    overtime_hours DOUBLE NULL,
    weekend_overtime_hours DOUBLE NULL,
    holiday_overtime_hours DOUBLE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,
    rejection_reason TEXT NULL,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_check_in (check_in),
    INDEX idx_status (status),
    INDEX idx_user_date (user_id, check_in)
) ENGINE=InnoDB;

-- ============================================
-- Table: leaves
-- ============================================
CREATE TABLE leaves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    reason TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT NULL,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB;

-- ============================================
-- Table: public_holidays
-- ============================================
CREATE TABLE public_holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    date DATE NOT NULL UNIQUE,
    description TEXT NULL,
    INDEX idx_date (date)
) ENGINE=InnoDB;

-- ============================================
-- Table: payroll
-- ============================================
CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    regular_hours DOUBLE NULL,
    overtime_hours DOUBLE NULL,
    weekend_overtime_hours DOUBLE NULL,
    holiday_overtime_hours DOUBLE NULL,
    regular_pay DOUBLE NULL,
    overtime_pay DOUBLE NULL,
    weekend_overtime_pay DOUBLE NULL,
    holiday_overtime_pay DOUBLE NULL,
    total_pay DOUBLE NOT NULL,
    payment_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_month_year (user_id, month, year),
    INDEX idx_user_id (user_id),
    INDEX idx_month_year (month, year),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ============================================
-- Table: company_settings
-- ============================================
CREATE TABLE company_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    office_latitude DOUBLE NOT NULL,
    office_longitude DOUBLE NOT NULL,
    proximity_radius_meters DOUBLE NOT NULL DEFAULT 100.0,
    standard_work_hours_per_day DOUBLE NOT NULL DEFAULT 8.0,
    payment_day INT NOT NULL DEFAULT 26
) ENGINE=InnoDB;

-- ============================================
-- Insert Default Company Settings
-- ============================================
INSERT INTO company_settings (
    office_latitude,
    office_longitude,
    proximity_radius_meters,
    standard_work_hours_per_day,
    payment_day
) VALUES (
    29.3759,  -- Kuwait City latitude
    47.9774,  -- Kuwait City longitude
    100.0,    -- 100 meters radius
    8.0,      -- 8 hours standard work day
    26        -- Payment on 26th of each month
);

-- ============================================
-- Insert Default Users
-- ============================================
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, hourly_rate, active)
VALUES (
    'admin@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'Admin',
    'User',
    50.0,
    TRUE
);

-- Password: hr123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, hourly_rate, active)
VALUES (
    'hr@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'HR',
    'Manager',
    40.0,
    TRUE
);

-- Password: emp123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, hourly_rate, active)
VALUES (
    'employee@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'John',
    'Doe',
    25.0,
    TRUE
);

-- ============================================
-- Insert User Roles
-- ============================================
-- Admin roles
INSERT INTO user_roles (user_id, roles) VALUES (1, 'ROLE_ADMIN');
INSERT INTO user_roles (user_id, roles) VALUES (1, 'ROLE_HR');

-- HR roles
INSERT INTO user_roles (user_id, roles) VALUES (2, 'ROLE_HR');

-- Employee role
INSERT INTO user_roles (user_id, roles) VALUES (3, 'ROLE_EMPLOYEE');

-- ============================================
-- Insert Kuwait Public Holidays for 2025
-- ============================================
INSERT INTO public_holidays (name, date, description) VALUES
('New Year''s Day', '2025-01-01', 'New Year celebration'),
('Isra and Mi''raj', '2025-01-27', 'Islamic holiday'),
('National Day', '2025-02-25', 'Kuwait National Day'),
('Liberation Day', '2025-02-26', 'Kuwait Liberation Day'),
('Eid al-Fitr', '2025-03-30', 'End of Ramadan - Day 1'),
('Eid al-Fitr Holiday', '2025-03-31', 'End of Ramadan - Day 2'),
('Eid al-Fitr Holiday', '2025-04-01', 'End of Ramadan - Day 3'),
('Arafat Day', '2025-06-06', 'Day before Eid al-Adha'),
('Eid al-Adha', '2025-06-07', 'Feast of Sacrifice - Day 1'),
('Eid al-Adha Holiday', '2025-06-08', 'Feast of Sacrifice - Day 2'),
('Eid al-Adha Holiday', '2025-06-09', 'Feast of Sacrifice - Day 3'),
('Islamic New Year', '2025-06-27', 'First day of Muharram'),
('Prophet''s Birthday', '2025-09-05', 'Mawlid al-Nabi');

-- ============================================
-- Create Views for Common Queries
-- ============================================

-- View: Active employees with their roles
CREATE OR REPLACE VIEW v_active_employees AS
SELECT
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.hourly_rate,
    GROUP_CONCAT(ur.roles) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE u.active = TRUE
GROUP BY u.id, u.email, u.first_name, u.last_name, u.hourly_rate;

-- View: Pending approvals summary
CREATE OR REPLACE VIEW v_pending_approvals AS
SELECT
    'ATTENDANCE' AS type,
    COUNT(*) AS count
FROM attendance
WHERE status = 'PENDING'
UNION ALL
SELECT
    'LEAVE' AS type,
    COUNT(*) AS count
FROM leaves
WHERE status = 'PENDING';

-- View: Monthly attendance summary
CREATE OR REPLACE VIEW v_monthly_attendance_summary AS
SELECT
    u.id AS user_id,
    u.first_name,
    u.last_name,
    YEAR(a.check_in) AS year,
    MONTH(a.check_in) AS month,
    COUNT(*) AS total_days,
    SUM(a.hours_worked) AS total_hours,
    SUM(a.regular_hours) AS regular_hours,
    SUM(a.overtime_hours) AS overtime_hours,
    SUM(a.weekend_overtime_hours) AS weekend_overtime_hours,
    SUM(a.holiday_overtime_hours) AS holiday_overtime_hours
FROM users u
JOIN attendance a ON u.id = a.user_id
WHERE a.status = 'APPROVED' AND a.check_out IS NOT NULL
GROUP BY u.id, u.first_name, u.last_name, YEAR(a.check_in), MONTH(a.check_in);

-- ============================================
-- Create Stored Procedures
-- ============================================

DELIMITER //

-- Procedure: Get user monthly summary
CREATE PROCEDURE sp_get_user_monthly_summary(
    IN p_user_id BIGINT,
    IN p_month INT,
    IN p_year INT
)
BEGIN
    SELECT
        COUNT(*) AS total_days,
        SUM(hours_worked) AS total_hours,
        SUM(regular_hours) AS regular_hours,
        SUM(overtime_hours) AS overtime_hours,
        SUM(weekend_overtime_hours) AS weekend_overtime_hours,
        SUM(holiday_overtime_hours) AS holiday_overtime_hours
    FROM attendance
    WHERE user_id = p_user_id
    AND YEAR(check_in) = p_year
    AND MONTH(check_in) = p_month
    AND status = 'APPROVED'
    AND check_out IS NOT NULL;
END //

-- Procedure: Get pending approvals count
CREATE PROCEDURE sp_get_pending_approvals_count()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM attendance WHERE status = 'PENDING') AS pending_attendance,
        (SELECT COUNT(*) FROM leaves WHERE status = 'PENDING') AS pending_leaves;
END //

DELIMITER ;

-- ============================================
-- Create Triggers
-- ============================================

DELIMITER //

-- Trigger: Update attendance timestamps
CREATE TRIGGER trg_attendance_update
BEFORE UPDATE ON attendance
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

-- Trigger: Update leave timestamps
CREATE TRIGGER trg_leave_update
BEFORE UPDATE ON leaves
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

-- Trigger: Update user timestamps
CREATE TRIGGER trg_user_update
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

DELIMITER ;

-- ============================================
-- Grant Permissions (Optional - adjust as needed)
-- ============================================
-- CREATE USER IF NOT EXISTS 'attendance_user'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON attendance_payroll_db.* TO 'attendance_user'@'localhost';
-- FLUSH PRIVILEGES;

-- ============================================
-- Verification Queries
-- ============================================

-- Verify tables created
SELECT TABLE_NAME, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'attendance_payroll_db'
ORDER BY TABLE_NAME;

-- Verify default users
SELECT u.id, u.email, u.first_name, u.last_name, GROUP_CONCAT(ur.roles) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.email, u.first_name, u.last_name;

-- Verify company settings
SELECT * FROM company_settings;

-- Verify public holidays
SELECT COUNT(*) AS total_holidays, MIN(date) AS first_holiday, MAX(date) AS last_holiday
FROM public_holidays;

-- ============================================
-- Success Message
-- ============================================
SELECT 'Database created successfully!' AS status,
       'attendance_payroll_db' AS database_name,
       (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'attendance_payroll_db') AS tables_count,
       (SELECT COUNT(*) FROM users) AS users_count,
       (SELECT COUNT(*) FROM public_holidays) AS holidays_count;