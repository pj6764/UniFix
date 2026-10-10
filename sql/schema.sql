-- UniFix Database Schema
-- Crowdsourced Campus Issue Resolution System

CREATE DATABASE IF NOT EXISTS unifix_db;
USE unifix_db;

-- 1. Users Table
DROP TABLE IF EXISTS issue_upvotes;
DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT'
);

-- 2. Issues Table
CREATE TABLE issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'PENDING',
    upvotes INT DEFAULT 1,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issues_user FOREIGN KEY (created_by) 
        REFERENCES users(user_id) ON DELETE SET NULL
);

-- 3. Issue Upvotes Table (Prevents multiple upvotes by the same user)
CREATE TABLE issue_upvotes (
    upvote_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_upvote_issue FOREIGN KEY (issue_id) 
        REFERENCES issues(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_upvote_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_user_issue UNIQUE (issue_id, user_id)
);

-- =======================================================
-- Sample Seed Data
-- =======================================================

-- Seed Users: 1 Admin, 2 Students
INSERT INTO users (user_id, name, email, password, role) VALUES
(1, 'Campus Administrator', 'admin@college.edu', 'admin123', 'ADMIN'),
(2, 'Alice Sharma', 'alice@college.edu', 'student123', 'STUDENT'),
(3, 'Bob Verma', 'bob@college.edu', 'student123', 'STUDENT');

-- Seed Issues: 3 sample issues across varied categories
INSERT INTO issues (issue_id, title, category, location, description, status, upvotes, created_by, created_at) VALUES
(1, 'Wi-Fi connectivity drop in Block C', 'IT', 'Block C - 3rd Floor Lab', 'The wireless access points continuously disconnect students during class hours, causing disruptions in lab submissions.', 'PENDING', 2, 2, NOW() - INTERVAL 2 DAY),
(2, 'Water cooler leaking and pooling water', 'Sanitation', 'Boys Hostel 2 - Ground Floor', 'The main drinking water cooler has a persistent pipe leak causing water stagnation and slipping hazard near the entrance.', 'IN_PROGRESS', 3, 3, NOW() - INTERVAL 1 DAY),
(3, 'Broken ceiling fan regulator in Mess Hall', 'Hostel', 'Central Mess - Dining Hall B', 'Fan regulator #4 is malfunctioning and stuck at full noise level with intermittent sparks observed.', 'RESOLVED', 1, 2, NOW() - INTERVAL 3 DAY);

-- Seed Issue Upvotes to align with initial upvotes:
-- Issue 1: Upvoted by Alice (creator) and Bob
INSERT INTO issue_upvotes (issue_id, user_id) VALUES
(1, 2),
(1, 3);

-- Issue 2: Upvoted by Bob (creator), Alice, and Admin
INSERT INTO issue_upvotes (issue_id, user_id) VALUES
(2, 3),
(2, 2),
(2, 1);

-- Issue 3: Upvoted by Alice (creator)
INSERT INTO issue_upvotes (issue_id, user_id) VALUES
(3, 2);
