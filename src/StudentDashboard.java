import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class StudentDashboard extends JFrame {
    private int studentId;
    private String studentName;
    private JTabbedPane tabbedPane;
    private JPanel attendancePanel, subjectsPanel, profilePanel, notificationsPanel;
    
    // Attendance Panel Components
    private JComboBox<String> subjectComboBox;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JLabel attendancePercentageLabel;
    private JProgressBar attendanceProgressBar;
    
    // Subjects Panel Components
    private JTable subjectsTable;
    private DefaultTableModel subjectsTableModel;
    
    // Profile Panel Components
    private JTextField usernameField, rollNumberField, courseField, batchField, emailField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton updateProfileBtn, updatePasswordBtn;
    
    // Notifications Panel Components
    private JTable notificationsTable;
    private DefaultTableModel notificationsTableModel;
    
    public StudentDashboard(int studentId) {
        this.studentId = studentId;
        
        // Initialize GUI components on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                loadStudentInfo();
                
                setTitle("Attendance Management System - Student Dashboard (" + studentName + ")");
                setSize(900, 600);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);
                
                tabbedPane = new JTabbedPane();
                
                // Initialize Panels
                createAttendancePanel();
                createSubjectsPanel();
                createProfilePanel();
                createNotificationsPanel();
                
                tabbedPane.addTab("Attendance", attendancePanel);
                tabbedPane.addTab("Subjects", subjectsPanel);
                tabbedPane.addTab("Profile", profilePanel);
                tabbedPane.addTab("Notifications", notificationsPanel);
                
                add(tabbedPane);
                
                // Load initial data in background
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        loadSubjects();
                        loadNotifications();
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(StudentDashboard.this,
                                "Error loading initial data: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
                
                setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error creating student dashboard: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void loadStudentInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM students WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                studentName = rs.getString("name");
            } else {
                studentName = "Unknown Student";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            studentName = "Unknown Student";
        }
    }
    
    private void createAttendancePanel() {
        attendancePanel = new JPanel(new BorderLayout());
        
        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(new JLabel("Subject:"));
        subjectComboBox = new JComboBox<>();
        controlPanel.add(subjectComboBox);
        
        subjectComboBox.addActionListener(e -> loadAttendanceData());
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        attendancePercentageLabel = new JLabel("Attendance: 0.00%");
        attendanceProgressBar = new JProgressBar(0, 100);
        attendanceProgressBar.setStringPainted(true);
        attendanceProgressBar.setPreferredSize(new Dimension(150, 20));
        
        summaryPanel.add(attendancePercentageLabel);
        summaryPanel.add(attendanceProgressBar);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.WEST);
        topPanel.add(summaryPanel, BorderLayout.EAST);
        
        // Table
        String[] columns = {"Date", "Status", "Subject", "Marked By"};
        attendanceTableModel = new DefaultTableModel(columns, 0);
        attendanceTable = new JTable(attendanceTableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        
        // Add components to panel
        attendancePanel.add(topPanel, BorderLayout.NORTH);
        attendancePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createSubjectsPanel() {
        subjectsPanel = new JPanel(new BorderLayout());
        
        // Table
        String[] columns = {"Subject Name", "Subject Code", "Faculty", "Total Classes", "Attended", "Percentage"};
        subjectsTableModel = new DefaultTableModel(columns, 0);
        subjectsTable = new JTable(subjectsTableModel);
        JScrollPane scrollPane = new JScrollPane(subjectsTable);
        
        subjectsPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createProfilePanel() {
        profilePanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        usernameField.setEditable(false);
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Roll Number:"));
        rollNumberField = new JTextField(15);
        rollNumberField.setEditable(false);
        formPanel.add(rollNumberField);

        formPanel.add(new JLabel("Course:"));
        courseField = new JTextField(15);
        courseField.setEditable(false);
        formPanel.add(courseField);

        formPanel.add(new JLabel("Batch:"));
        batchField = new JTextField(15);
        batchField.setEditable(false);
        formPanel.add(batchField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        formPanel.add(emailField);

        formPanel.add(new JLabel("New Password:"));
        newPasswordField = new JPasswordField(15);
        formPanel.add(newPasswordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateProfileBtn = new JButton("Update Email");
        updatePasswordBtn = new JButton("Update Password");
        
        updateProfileBtn.addActionListener(e -> updateEmail());
        updatePasswordBtn.addActionListener(e -> updatePassword());

        buttonPanel.add(updateProfileBtn);
        buttonPanel.add(updatePasswordBtn);

        profilePanel.add(formPanel, BorderLayout.CENTER);
        profilePanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load profile data after panel creation
        loadProfileData();
    }
    
    private void createNotificationsPanel() {
        notificationsPanel = new JPanel(new BorderLayout());
        
        // Table
        String[] columns = {"Date", "Message", "Type", "Status"};
        notificationsTableModel = new DefaultTableModel(columns, 0);
        notificationsTable = new JTable(notificationsTableModel);
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        
        notificationsPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadProfileData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.name, s.roll_number, s.course, s.batch, u.email, u.username " +
                         "FROM students s " +
                         "JOIN users u ON s.user_id = u.id " +
                         "WHERE s.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                usernameField.setText(rs.getString("name"));
                rollNumberField.setText(rs.getString("roll_number"));
                courseField.setText(rs.getString("course"));
                batchField.setText(rs.getString("batch"));
                emailField.setText(rs.getString("email"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading profile data: " + e.getMessage());
        }
    }
    
    private void loadSubjects() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load subjects for combobox
            String subjectSql = "SELECT DISTINCT s.id, s.name FROM subjects s " +
                                "JOIN attendance a ON s.id = a.subject_id " +
                                "WHERE a.student_id = ? ORDER BY s.name";
            PreparedStatement subjectStmt = conn.prepareStatement(subjectSql);
            subjectStmt.setInt(1, studentId);
            ResultSet subjectRs = subjectStmt.executeQuery();
            
            subjectComboBox.removeAllItems();
            
            while (subjectRs.next()) {
                subjectComboBox.addItem(subjectRs.getString("name"));
            }
            
            // Load subjects table with attendance summary
            String summarySQL = "SELECT s.name, s.code, f.name as faculty_name, " +
                                "(SELECT COUNT(*) FROM attendance a WHERE a.subject_id = s.id AND a.student_id = ?) AS total_classes, " +
                                "(SELECT COUNT(*) FROM attendance a WHERE a.subject_id = s.id AND a.student_id = ? AND a.status = 'PRESENT') AS attended_classes " +
                                "FROM subjects s LEFT JOIN faculty f ON s.faculty_id = f.id " +
                                "WHERE s.id IN (SELECT DISTINCT subject_id FROM attendance WHERE student_id = ?) " +
                                "ORDER BY s.name";
            PreparedStatement summaryStmt = conn.prepareStatement(summarySQL);
            summaryStmt.setInt(1, studentId);
            summaryStmt.setInt(2, studentId);
            summaryStmt.setInt(3, studentId);
            ResultSet summaryRs = summaryStmt.executeQuery();
            
            subjectsTableModel.setRowCount(0);
            
            while (summaryRs.next()) {
                String subjectName = summaryRs.getString("name");
                String subjectCode = summaryRs.getString("code");
                String facultyName = summaryRs.getString("faculty_name");
                int totalClasses = summaryRs.getInt("total_classes");
                int attendedClasses = summaryRs.getInt("attended_classes");
                
                double percentage = 0;
                if (totalClasses > 0) {
                    percentage = (attendedClasses * 100.0) / totalClasses;
                }
                
                Object[] row = {
                    subjectName,
                    subjectCode,
                    facultyName != null ? facultyName : "Not Assigned",
                    totalClasses,
                    attendedClasses,
                    String.format("%.2f%%", percentage)
                };
                
                subjectsTableModel.addRow(row);
            }
            
            if (subjectComboBox.getItemCount() > 0) {
                loadAttendanceData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage());
        }
    }
    
    private void loadAttendanceData() {
        if (subjectComboBox.getSelectedItem() == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get subject id
            String subjectIdSQL = "SELECT id FROM subjects WHERE name = ?";
            PreparedStatement subjectIdStmt = conn.prepareStatement(subjectIdSQL);
            subjectIdStmt.setString(1, subjectComboBox.getSelectedItem().toString());
            ResultSet subjectIdRs = subjectIdStmt.executeQuery();
            
            if (subjectIdRs.next()) {
                int subjectId = subjectIdRs.getInt("id");
                
                // Load attendance data
                String attendanceSQL = "SELECT a.date, a.status, s.name as subject_name, f.name as faculty_name " +
                                      "FROM attendance a " +
                                      "JOIN subjects s ON a.subject_id = s.id " +
                                      "JOIN faculty f ON a.marked_by = f.id " +
                                      "WHERE a.student_id = ? AND a.subject_id = ? " +
                                      "ORDER BY a.date DESC";
                PreparedStatement attendanceStmt = conn.prepareStatement(attendanceSQL);
                attendanceStmt.setInt(1, studentId);
                attendanceStmt.setInt(2, subjectId);
                ResultSet attendanceRs = attendanceStmt.executeQuery();
                
                attendanceTableModel.setRowCount(0);
                
                int totalClasses = 0;
                int presentClasses = 0;
                
                while (attendanceRs.next()) {
                    java.sql.Date date = attendanceRs.getDate("date");
                    String status = attendanceRs.getString("status");
                    String subjectName = attendanceRs.getString("subject_name");
                    String facultyName = attendanceRs.getString("faculty_name");
                    
                    totalClasses++;
                    if (status.equals("PRESENT")) {
                        presentClasses++;
                    }
                    
                    // Format date
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = sdf.format(date);
                    
                    Object[] row = {formattedDate, status, subjectName, facultyName};
                    attendanceTableModel.addRow(row);
                }
                
                // Update attendance summary
                double percentage = 0;
                if (totalClasses > 0) {
                    percentage = (presentClasses * 100.0) / totalClasses;
                }
                
                attendancePercentageLabel.setText(String.format("Attendance: %.2f%%", percentage));
                attendanceProgressBar.setValue((int) percentage);
                
                // Set progress bar color based on attendance percentage
                if (percentage < 75) {
                    attendanceProgressBar.setForeground(Color.RED);
                } else {
                    attendanceProgressBar.setForeground(Color.GREEN);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance data: " + e.getMessage());
        }
    }
    
    private void loadNotifications() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT n.created_at, n.message, n.type, n.status FROM notifications n " +
                         "JOIN students s ON n.user_id = s.user_id " +
                         "WHERE s.id = ? ORDER BY n.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            notificationsTableModel.setRowCount(0);
            
            while (rs.next()) {
                Timestamp createdAt = rs.getTimestamp("created_at");
                String message = rs.getString("message");
                String type = rs.getString("type");
                String status = rs.getString("status");
                
                // Format date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sdf.format(createdAt);
                
                Object[] row = {formattedDate, message, type, status};
                notificationsTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage());
        }
    }
    
    private void updateEmail() {
        String newEmail = emailField.getText().trim();
        if (newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a new email address", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    String sql = "UPDATE users SET email = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, newEmail);
                    stmt.setInt(2, studentId);
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        publish("Email updated successfully!");
                    } else {
                        throw new SQLException("Failed to update email");
                    }
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, message);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadProfileData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Error updating email: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void updatePassword() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter and confirm the new password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, User.hashPassword(newPassword));
                    stmt.setInt(2, studentId);
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        publish("Password updated successfully!");
                    } else {
                        throw new SQLException("Failed to update password");
                    }
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, message);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Error updating password: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
} 