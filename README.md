# Attendance Management System

A simple Java Swing application for managing student, faculty, subject, and attendance records with MySQL database integration.

---

## Features

- **User Authentication:** Login for Admin, Faculty, and Student roles.
- **Admin Dashboard:**
  - Add, update, and delete students and faculty (with automatic user account creation).
  - Add, update, and delete subjects.
- **Faculty Dashboard:**
  - Mark and view attendance for assigned subjects.
- **Student Dashboard:**
  - View personal attendance records.
- **Relational Database:** MySQL with foreign key constraints for data integrity.

---

## Database Setup

1. **Install MySQL** and ensure it is running.
2. **Create the database and tables:**
   - Run the provided `attendance_db.sql` script in your MySQL client:
     ```sh
     mysql -u root -p < attendance_db.sql
     ```
   - This will create the database, tables, and insert sample data (admin, faculty, students, subjects, attendance).

---

## Application Setup

1. **Clone or download this repository.**
2. **Install Java (JDK 8 or higher).**
3. **Add MySQL Connector/J JAR**  
   - Ensure `mysql-connector-j-9.2.0.jar` is in your project directory.
4. **Configure Database Credentials:**
   - Edit `src/DatabaseConnection.java` and set your MySQL username and password:
     ```java
     private static final String USER = "root";
     private static final String PASSWORD = "your_mysql_password";
     ```
5. **Compile the project:**
   ```sh
   javac -d bin -cp mysql-connector-j-9.2.0.jar src/*.java
   ```
6. **Run the application:**
   ```sh
   java -cp "bin;mysql-connector-j-9.2.0.jar" AttendanceManagementSystem
   ```

---

## Default Users

- **Admin:**  
  - Username: `admin`  
  - Password: `admin123`
- **Sample Faculty:**  
  - Username: `faculty1` / `faculty2`  
  - Password: `faculty123`
- **Sample Students:**  
  - Username: `student1` / `student2` / `student3`  
  - Password: `student123`

---

## Project Structure

```
attendance project/
├── attendance_db.sql
├── mysql-connector-j-9.2.0.jar
├── src/
│   ├── AttendanceManagementSystem.java
│   ├── LoginWindow.java
│   ├── AdminDashboard.java
│   ├── FacultyDashboard.java
│   ├── StudentDashboard.java
│   ├── DatabaseConnection.java
│   ├── User.java
│   ├── Student.java
│   ├── Faculty.java
│   ├── Subject.java
│   └── Attendance.java
└── bin/
```

---

## Notes

- **Foreign Key Constraints:**  
  - Deleting a student will also delete their attendance and user account.
  - Deleting a faculty will unassign them from subjects and delete their user account.
- **Security:**  
  - Passwords are stored as plain text for demo purposes. For production, use password hashing.
- **Extensibility:**  
  - You can add features like notifications, reporting, or backup as needed.

---
# My Linkedin profile - https://www.linkedin.com/in/jaskaran21012004/
## License

This project is for educational/demo purposes.  
Feel free to use and modify as needed! 
