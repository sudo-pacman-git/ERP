-- =========================================================
-- 1. Setup Authentication Database
-- =========================================================
CREATE DATABASE IF NOT EXISTS university_auth;
USE university_auth;

CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
    requires_reset BOOLEAN DEFAULT FALSE
);

-- =========================================================
-- 2. Setup ERP Database
-- =========================================================
CREATE DATABASE IF NOT EXISTS university_erp;
USE university_erp;

-- System Settings
CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

-- Insert Default Setting for Maintenance Mode
INSERT IGNORE INTO settings (setting_key, setting_value) VALUES ('maintenance_on', 'false');

-- User Profiles (Linked to users_auth via user_id)
CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(20) UNIQUE NOT NULL,
    program VARCHAR(50),
    current_year INT,
    first_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100),
    full_name VARCHAR(100) NOT NULL
);

-- Academic Core
CREATE TABLE IF NOT EXISTS courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE IF NOT EXISTS sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    instructor_id INT,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT NOT NULL,
    semester VARCHAR(20),
    year INT,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id) ON DELETE SET NULL
);

-- Enrollment and Grading
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    UNIQUE(student_id, section_id), -- Prevents duplicate registrations
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS grades (
    enrollment_id INT NOT NULL,
    component VARCHAR(50) NOT NULL,
    score DOUBLE NOT NULL,
    PRIMARY KEY (enrollment_id, component), -- A student can only have one score per component (e.g., one Quiz score)
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);