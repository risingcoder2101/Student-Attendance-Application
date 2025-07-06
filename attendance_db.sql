-- Drop existing database if it exists
DROP DATABASE IF EXISTS attendance_db;

-- Create new database
CREATE DATABASE attendance_db;

-- Use the database
USE attendance_db;

-- Create subjects table (simple, for single-student app)
CREATE TABLE subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    attendance_limit INT DEFAULT 75
);

-- Create attendance table (simple, for single-student app)
CREATE TABLE attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    subject_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT') NOT NULL DEFAULT 'PRESENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- Insert sample subjects
INSERT INTO subjects (name, attendance_limit) VALUES
('Java Programming', 75),
('Database Management', 80);

-- Insert sample attendance records
INSERT INTO attendance (subject_id, date, status) VALUES
(1, CURDATE(), 'PRESENT'),
(2, CURDATE(), 'ABSENT');
