# Student Attendance Application

A simple Java Swing application for tracking attendance for a single student across multiple subjects, using a MySQL database.

## Features
- Add, update, and remove subjects
- Set attendance limit for each subject and overall
- Mark attendance as PRESENT or ABSENT for each subject
- Remove the last attendance record for a subject
- View total and attended lectures per subject
- See overall attendance percentage
- Get advice on how many lectures to attend or can skip based on the overall limit
- Modern, large-font, user-friendly interface

## Setup
1. **Install MySQL** and create the database using the provided `database.sql` file.
2. **Edit `DatabaseManager.java`** if needed to set your MySQL username and password.
3. **Compile the project:**
   ```sh
   javac -cp ".;mysql-connector-j-9.2.0.jar" -d bin src/*.java
   ```
4. **Run the project:**
   ```sh
   java -cp "bin;mysql-connector-j-9.2.0.jar" Main
   ```

## Usage
- Add a subject and set its attendance limit.
- Select a subject to update, remove, or mark attendance.
- Use the "Mark Present" or "Mark Absent" buttons to record attendance for today.
- Use "Remove Last Lecture" to undo the most recent attendance entry for a subject.
- Set the overall attendance limit at the top; advice will show how many lectures to attend or can skip.
- Remove a subject with the "Remove Subject" button (removes all its attendance records).

## Requirements
- Java 8 or higher
- MySQL
- MySQL Connector/J JAR file (e.g., `mysql-connector-j-9.2.0.jar`)
