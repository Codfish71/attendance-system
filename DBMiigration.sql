-- ============================================
-- Database Migration Script
-- Attendance & Payroll System - Major Updates
-- ============================================

USE attendance_payroll_db;

-- ============================================
-- 1. Update Users Table
-- ============================================

-- Add new columns to users table
ALTER TABLE users
ADD COLUMN employee_id VARCHAR(50) UNIQUE AFTER id,
ADD COLUMN date_of_birth DATE AFTER last_name,
ADD COLUMN profile_photo_url VARCHAR(1000) AFTER date_of_birth,
ADD COLUMN reports_to BIGINT AFTER active;

-- Add foreign key for reporting hierarchy
ALTER TABLE users
ADD CONSTRAINT fk_reports_to
FOREIGN KEY (reports_to) REFERENCES users(id) ON DELETE SET NULL;

-- Generate employee IDs for existing users
UPDATE users
SET employee_id = CONCAT('E', LPAD(id, 5, '0'))
WHERE employee_id IS NULL;

-- ============================================
-- 2. Update User Roles
-- ============================================

-- Note: You may need to manually update roles based on your requirements
-- Example: Update specific users to new roles

-- Update admin and HR users (keep as is)
-- Update employees to new role structure
UPDATE user_roles
SET roles = 'ROLE_COORDINATOR'
WHERE roles = 'ROLE_EMPLOYEE';

-- Add sample hierarchy (adjust as needed)
-- Example: Employee 3 reports to Employee 2 (Site Lead)
--          Employee 2 reports to Employee 1 (Project Manager)
-- UPDATE users SET reports_to = 2 WHERE id = 3;
-- UPDATE users SET reports_to = 1 WHERE id = 2;

-- ============================================
-- 3. Update Attendance Table (Optional)
-- ============================================

-- The status change from PENDING/APPROVED/REJECTED to PROCESSING/COMPLETED
-- is handled by the application. Existing data can remain for history.

-- If you want to migrate old data:
UPDATE attendance
SET status = 'COMPLETED'
WHERE status IN ('APPROVED') AND check_out IS NOT NULL;

UPDATE attendance
SET status = 'PROCESSING'
WHERE status = 'PENDING' AND check_out IS NULL;

-- If you want to clean up old columns (CAUTION: This removes data)
-- ALTER TABLE attendance DROP COLUMN approved_by;
-- ALTER TABLE attendance DROP COLUMN approved_at;
-- ALTER TABLE attendance DROP COLUMN rejection_reason;

-- ============================================
-- 4. Create Tasks Table
-- ============================================

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_by BIGINT NOT NULL,
    assigned_to BIGINT NOT NULL,
    task_date_time TIMESTAMP NOT NULL,
    location VARCHAR(500) NOT NULL,
    location_latitude DOUBLE,
    location_longitude DOUBLE,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    completion_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_assigned_by (assigned_by),
    INDEX idx_status (status),
    INDEX idx_task_date (task_date_time)
) ENGINE=InnoDB;

-- ============================================
-- 5. Create Sample Users with New Roles
-- ============================================

-- Insert Project Manager (if not exists)
INSERT IGNORE INTO users (
    employee_id, email, password, first_name, last_name,
    hourly_rate, active, created_at, updated_at
) VALUES (
    'E00101',
    'pm@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'Project',
    'Manager',
    60.0,
    TRUE,
    NOW(),
    NOW()
);

-- Add role for Project Manager
INSERT IGNORE INTO user_roles (user_id, roles)
SELECT id, 'ROLE_PROJECT_MANAGER' FROM users WHERE email = 'pm@company.com';

-- Insert Site Lead (if not exists)
INSERT IGNORE INTO users (
    employee_id, email, password, first_name, last_name,
    hourly_rate, active, reports_to, created_at, updated_at
) VALUES (
    'E00201',
    'sitelead@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'Site',
    'Lead',
    45.0,
    TRUE,
    (SELECT id FROM users WHERE email = 'pm@company.com'),
    NOW(),
    NOW()
);

-- Add role for Site Lead
INSERT IGNORE INTO user_roles (user_id, roles)
SELECT id, 'ROLE_SITE_LEAD' FROM users WHERE email = 'sitelead@company.com';

-- Insert Co-Ordinator (if not exists)
INSERT IGNORE INTO users (
    employee_id, email, password, first_name, last_name,
    hourly_rate, active, reports_to, created_at, updated_at
) VALUES (
    'E00301',
    'coordinator@company.com',
    '$2a$10$xZvhJZ9Q8yXKxFqGJ0P7gu3rH5nLqYVqVqXKqYVqVqXKqYVqVqXKq',
    'John',
    'Coordinator',
    30.0,
    TRUE,
    (SELECT id FROM users WHERE email = 'sitelead@company.com'),
    NOW(),
    NOW()
);

-- Add role for Co-Ordinator
INSERT IGNORE INTO user_roles (user_id, roles)
SELECT id, 'ROLE_COORDINATOR' FROM users WHERE email = 'coordinator@company.com';

-- ============================================
-- 6. Create Sample Tasks (Optional)
-- ============================================

-- Sample task from Project Manager to Site Lead
INSERT INTO tasks (
    title, description, assigned_by, assigned_to,
    task_date_time, location, status, priority
)
SELECT
    'Review Site Construction Progress',
    'Conduct weekly review of construction progress at Site A',
    pm.id,
    sl.id,
    DATE_ADD(NOW(), INTERVAL 1 DAY),
    'Construction Site A, Kuwait',
    'ASSIGNED',
    'HIGH'
FROM
    users pm,
    users sl
WHERE
    pm.email = 'pm@company.com'
    AND sl.email = 'sitelead@company.com'
LIMIT 1;

-- Sample task from Site Lead to Co-Ordinator
INSERT INTO tasks (
    title, description, assigned_by, assigned_to,
    task_date_time, location, status, priority
)
SELECT
    'Material Inventory Check',
    'Verify and document all materials in storage area',
    sl.id,
    co.id,
    DATE_ADD(NOW(), INTERVAL 2 HOUR),
    'Storage Warehouse, Kuwait',
    'ASSIGNED',
    'MEDIUM'
FROM
    users sl,
    users co
WHERE
    sl.email = 'sitelead@company.com'
    AND co.email = 'coordinator@company.com'
LIMIT 1;

-- ============================================
-- 7. Create Views for Reporting
-- ============================================

-- View: User Hierarchy
CREATE OR REPLACE VIEW v_user_hierarchy AS
SELECT
    u.id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) AS full_name,
    u.email,
    GROUP_CONCAT(DISTINCT ur.roles) AS roles,
    CONCAT(m.first_name, ' ', m.last_name) AS reports_to_name,
    m.employee_id AS reports_to_id
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN users m ON u.reports_to = m.id
WHERE u.active = TRUE
GROUP BY u.id, u.employee_id, u.first_name, u.last_name, u.email, m.first_name, m.last_name, m.employee_id;

-- View: Task Summary
CREATE OR REPLACE VIEW v_task_summary AS
SELECT
    t.id,
    t.title,
    t.status,
    t.priority,
    CONCAT(ab.first_name, ' ', ab.last_name) AS assigned_by_name,
    CONCAT(at.first_name, ' ', at.last_name) AS assigned_to_name,
    t.task_date_time,
    t.location,
    CASE
        WHEN t.completed_at IS NOT NULL THEN TIMESTAMPDIFF(HOUR, t.started_at, t.completed_at)
        ELSE NULL
    END AS completion_hours
FROM tasks t
JOIN users ab ON t.assigned_by = ab.id
JOIN users at ON t.assigned_to = at.id;

-- View: Attendance Summary (Updated)
CREATE OR REPLACE VIEW v_attendance_summary AS
SELECT
    u.id AS user_id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) AS full_name,
    DATE(a.check_in) AS attendance_date,
    a.check_in,
    a.check_out,
    a.hours_worked,
    a.status
FROM users u
JOIN attendance a ON u.id = a.user_id
WHERE a.check_out IS NOT NULL
ORDER BY a.check_in DESC;

-- ============================================
-- 8. Add Indexes for Performance
-- ============================================

-- User indexes
CREATE INDEX idx_employee_id ON users(employee_id);
CREATE INDEX idx_reports_to ON users(reports_to);
CREATE INDEX idx_active ON users(active);

-- Task indexes (if not already created)
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_priority ON tasks(priority);
CREATE INDEX idx_task_datetime ON tasks(task_date_time);

-- ============================================
-- 9. Update Stored Procedures
-- ============================================

DELIMITER //

-- Procedure: Get user with hierarchy
CREATE PROCEDURE sp_get_user_with_hierarchy(IN p_user_id BIGINT)
BEGIN
    SELECT
        u.*,
        CONCAT(m.first_name, ' ', m.last_name) AS manager_name,
        m.employee_id AS manager_employee_id,
        GROUP_CONCAT(DISTINCT ur.roles) AS user_roles
    FROM users u
    LEFT JOIN users m ON u.reports_to = m.id
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    WHERE u.id = p_user_id
    GROUP BY u.id, m.first_name, m.last_name, m.employee_id;
END //

-- Procedure: Get user tasks
CREATE PROCEDURE sp_get_user_tasks(IN p_user_id BIGINT)
BEGIN
    SELECT
        t.*,
        CONCAT(ab.first_name, ' ', ab.last_name) AS assigned_by_name,
        ab.employee_id AS assigned_by_emp_id
    FROM tasks t
    JOIN users ab ON t.assigned_by = ab.id
    WHERE t.assigned_to = p_user_id
    ORDER BY t.task_date_time DESC;
END //

-- Procedure: Get task hierarchy
CREATE PROCEDURE sp_get_task_hierarchy(IN p_task_id BIGINT)
BEGIN
    SELECT
        t.*,
        CONCAT(ab.first_name, ' ', ab.last_name) AS assigned_by_name,
        ab.employee_id AS assigned_by_emp_id,
        CONCAT(at.first_name, ' ', at.last_name) AS assigned_to_name,
        at.employee_id AS assigned_to_emp_id
    FROM tasks t
    JOIN users ab ON t.assigned_by = ab.id
    JOIN users at ON t.assigned_to = at.id
    WHERE t.id = p_task_id;
END //

DELIMITER ;

-- ============================================
-- 10. Verification Queries
-- ============================================

-- Check updated users table
SELECT
    id, employee_id, first_name, last_name, email,
    date_of_birth, reports_to, active
FROM users
ORDER BY id;

-- Check user roles
SELECT
    u.id, u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) AS name,
    GROUP_CONCAT(ur.roles) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.employee_id, u.first_name, u.last_name
ORDER BY u.id;

-- Check user hierarchy
SELECT * FROM v_user_hierarchy;

-- Check tasks table
SELECT
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN status = 'CREATED' THEN 1 ELSE 0 END) AS created,
    SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) AS assigned,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress,
    SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done
FROM tasks;

-- Check attendance status migration
SELECT
    status,
    COUNT(*) AS count,
    COUNT(CASE WHEN check_out IS NULL THEN 1 END) AS active_sessions
FROM attendance
GROUP BY status;

-- ============================================
-- Success Message
-- ============================================
SELECT 'Database migration completed successfully!' AS status,
       (SELECT COUNT(*) FROM users) AS total_users,
       (SELECT COUNT(*) FROM tasks) AS total_tasks,
       (SELECT COUNT(*) FROM attendance WHERE status = 'COMPLETED') AS completed_attendances;

-- ============================================
-- IMPORTANT NOTES
-- ============================================
-- 1. Backup your database before running this script
-- 2. Review and adjust sample data as needed
-- 3. Update passwords for sample users
-- 4. Adjust reporting hierarchy based on your organization
-- 5. Test thoroughly before deploying to production
-- ============================================