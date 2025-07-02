import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public LoginWindow() {
        setTitle("Attendance Management System - Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        mainPanel.add(loginButton, gbc);
        
        add(mainPanel);
    }
    
    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username and password are required",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Attempting login for username: " + username);
            
            String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                System.out.println("Login successful. User ID: " + userId + ", Role: " + role);
                
                // Open appropriate window based on role
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    try {
                        switch (role) {
                            case "ADMIN":
                                openAdminDashboard(userId);
                                break;
                            case "FACULTY":
                                openFacultyDashboard(userId);
                                break;
                            case "STUDENT":
                                openStudentDashboard(userId);
                                break;
                            default:
                                throw new SQLException("Invalid user role: " + role);
                        }
                        dispose(); // Close login window
                    } catch (SQLException e) {
                        System.out.println("Error in role switch: " + e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(LoginWindow.this,
                            "Error opening dashboard: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            } else {
                System.out.println("Login failed - invalid credentials");
                JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            // Clear password field for security
            passwordField.setText("");
        }
    }
    
    private void openAdminDashboard(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // No need to query for adminId as we consider userId as the adminId
            AdminDashboard dashboard = new AdminDashboard(userId);
            dashboard.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error opening Admin Dashboard: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openFacultyDashboard(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get faculty id
            String sql = "SELECT id FROM faculty WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int facultyId = rs.getInt("id");
                FacultyDashboard dashboard = new FacultyDashboard(facultyId);
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Faculty profile not found for this user",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error opening Faculty Dashboard: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openStudentDashboard(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== Debugging Student Login ===");
            System.out.println("Attempting to open student dashboard for user ID: " + userId);
            
            // Debug: Print all users
            System.out.println("\nChecking users table:");
            String debugUsersSql = "SELECT id, username, role FROM users";
            Statement debugUsersStmt = conn.createStatement();
            ResultSet debugUsersRs = debugUsersStmt.executeQuery(debugUsersSql);
            while (debugUsersRs.next()) {
                System.out.println("User - ID: " + debugUsersRs.getInt("id") + 
                                 ", Username: " + debugUsersRs.getString("username") + 
                                 ", Role: " + debugUsersRs.getString("role"));
            }
            
            // Debug: Print all students
            System.out.println("\nChecking students table:");
            String debugStudentsSql = "SELECT id, user_id, name FROM students";
            Statement debugStudentsStmt = conn.createStatement();
            ResultSet debugStudentsRs = debugStudentsStmt.executeQuery(debugStudentsSql);
            while (debugStudentsRs.next()) {
                System.out.println("Student - ID: " + debugStudentsRs.getInt("id") + 
                                 ", User ID: " + debugStudentsRs.getInt("user_id") + 
                                 ", Name: " + debugStudentsRs.getString("name"));
            }
            
            // First verify the user exists and is a student
            String userCheckSql = "SELECT username, role FROM users WHERE id = ?";
            PreparedStatement userCheckStmt = conn.prepareStatement(userCheckSql);
            userCheckStmt.setInt(1, userId);
            ResultSet userRs = userCheckStmt.executeQuery();
            
            if (!userRs.next()) {
                System.out.println("\nError: User not found in database");
                JOptionPane.showMessageDialog(this,
                    "User not found in database",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String username = userRs.getString("username");
            String role = userRs.getString("role");
            System.out.println("\nFound user - Username: " + username + ", Role: " + role);
            
            if (!"STUDENT".equals(role)) {
                System.out.println("Error: User is not a student");
                JOptionPane.showMessageDialog(this,
                    "User is not a student. Role: " + role,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get student id
            String sql = "SELECT s.id, s.name FROM students s WHERE s.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            System.out.println("\nChecking student profile for user ID: " + userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int studentId = rs.getInt("id");
                String studentName = rs.getString("name");
                System.out.println("Found student - ID: " + studentId + ", Name: " + studentName);
                
                try {
                    System.out.println("Creating StudentDashboard...");
                    StudentDashboard dashboard = new StudentDashboard(studentId);
                    System.out.println("StudentDashboard created successfully");
                    dashboard.setVisible(true);
                    System.out.println("Student dashboard set to visible");
                    this.dispose(); // Explicitly close the login window
                } catch (Exception e) {
                    System.out.println("Error creating StudentDashboard: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Error creating student dashboard: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("Error: No student profile found for user ID: " + userId);
                JOptionPane.showMessageDialog(this,
                    "Student profile not found. Please contact administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Database error in openStudentDashboard: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set System Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
} 