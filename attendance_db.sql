-- Drop existing database if it exists
DROP DATABASE IF EXISTS attendance_db;

-- Create new database
CREATE DATABASE attendance_db;

-- Use the database
USE attendance_db;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'FACULTY', 'STUDENT') NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create students table
CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    course VARCHAR(50) NOT NULL,
    batch VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create faculty table
CREATE TABLE faculty (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create subjects table
CREATE TABLE subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    faculty_id INT,
    total_classes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id)
);

-- Create attendance table
CREATE TABLE attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') NOT NULL DEFAULT 'ABSENT',
    marked_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (marked_by) REFERENCES faculty(id)
);

-- Create notifications table
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    type ENUM('EMAIL', 'SMS') NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert default admin user
INSERT INTO users (username, password, role, email)
VALUES ('admin', 'admin123', 'ADMIN', 'admin@example.com');

-- Insert sample faculty users
INSERT INTO users (username, password, role, email) VALUES
('faculty1', 'faculty123', 'FACULTY', 'faculty1@example.com'),
('faculty2', 'faculty123', 'FACULTY', 'faculty2@example.com');

-- Insert faculty profiles
INSERT INTO faculty (user_id, name, department) VALUES
(2, 'Dr. John Smith', 'Computer Science'),
(3, 'Dr. Jane Doe', 'Mathematics');

-- Insert sample student users
INSERT INTO users (username, password, role, email) VALUES
('student1', 'student123', 'STUDENT', 'student1@example.com'),
('student2', 'student123', 'STUDENT', 'student2@example.com'),
('student3', 'student123', 'STUDENT', 'student3@example.com');

-- Insert student profiles
INSERT INTO students (user_id, name, roll_number, course, batch) VALUES
(4, 'Alice Johnson', 'CS001', 'Computer Science', '2023'),
(5, 'Bob Wilson', 'CS002', 'Computer Science', '2023'),
(6, 'Carol Brown', 'CS003', 'Computer Science', '2023');

-- Insert sample subjects
INSERT INTO subjects (name, code, faculty_id) VALUES
('Java Programming', 'CS101', 1),
('Database Management', 'CS102', 1),
('Calculus', 'MATH101', 2),
('Linear Algebra', 'MATH102', 2);

-- Insert sample attendance records
INSERT INTO attendance (student_id, subject_id, date, status, marked_by) VALUES
(1, 1, CURDATE(), 'PRESENT', 1),
(2, 1, CURDATE(), 'LATE', 1),
(3, 1, CURDATE(), 'ABSENT', 1),
(1, 2, CURDATE(), 'PRESENT', 1),
(2, 2, CURDATE(), 'PRESENT', 1),
(3, 2, CURDATE(), 'EXCUSED', 1); 