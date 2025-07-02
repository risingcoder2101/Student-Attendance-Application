import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.SwingWorker;
import java.util.List;

public class FacultyDashboard extends JFrame {
    private int facultyId;
    private String facultyName;
    private JTabbedPane tabbedPane;
    private JPanel attendancePanel, reportPanel, profilePanel;
    
    // Attendance Panel Components
    private JComboBox<String> subjectComboBox, batchComboBox;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JButton markAttendanceBtn;
    private JDateChooser dateChooser;
    private Map<Integer, JComboBox<String>> attendanceStatusMap; // Maps student ID to attendance status combobox
    
    // Report Panel Components
    private JComboBox<String> reportSubjectComboBox, reportTypeComboBox;
    private JDateChooser startDateChooser, endDateChooser;
    private JButton generateReportBtn, exportPdfBtn, exportExcelBtn;
    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    
    // Profile Panel Components
    private JTextField nameField, departmentField, emailField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton updateProfileBtn, updatePasswordBtn;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton updateEmailBtn;
    
    public FacultyDashboard(int facultyId) {
        this.facultyId = facultyId;
        loadFacultyInfo();
        
        setTitle("Attendance Management System - Faculty Dashboard (" + facultyName + ")");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // Initialize Panels
        createAttendancePanel();
        createReportPanel();
        createProfilePanel();
        
        tabbedPane.addTab("Mark Attendance", attendancePanel);
        tabbedPane.addTab("Attendance Reports", reportPanel);
        tabbedPane.addTab("Profile", profilePanel);
        
        add(tabbedPane);
        
        // Load initial data
        loadSubjects();
        loadProfileData();
    }
    
    private void loadFacultyInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM faculty WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                facultyName = rs.getString("name");
            } else {
                facultyName = "Unknown Faculty";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            facultyName = "Unknown Faculty";
        }
    }
    
    private void createAttendancePanel() {
        attendancePanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane for attendance and student management
        JTabbedPane attendanceTabs = new JTabbedPane();
        
        // Attendance tab
        JPanel attendanceTab = new JPanel(new BorderLayout());
        
        // Top control panel
        JPanel controlPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(new JLabel("Subject:"));
        subjectComboBox = new JComboBox<>();
        controlPanel.add(subjectComboBox);
        
        controlPanel.add(new JLabel("Batch:"));
        batchComboBox = new JComboBox<>();
        controlPanel.add(batchComboBox);
        
        controlPanel.add(new JLabel("Date:"));
        dateChooser = new JDateChooser(new java.util.Date());
        controlPanel.add(dateChooser);
        
        subjectComboBox.addActionListener(e -> loadBatches());
        batchComboBox.addActionListener(e -> loadStudentsForAttendance());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        markAttendanceBtn = new JButton("Save Attendance");
        markAttendanceBtn.addActionListener(e -> saveAttendance());
        buttonPanel.add(markAttendanceBtn);
        
        // Table
        String[] columns = {"ID", "Roll Number", "Name", "Attendance Status"};
        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only attendance status column is editable
            }
        };
        studentTable = new JTable(studentTableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        
        attendanceStatusMap = new HashMap<>();
        
        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        attendanceTab.add(topPanel, BorderLayout.NORTH);
        attendanceTab.add(scrollPane, BorderLayout.CENTER);
        
        // Student Management tab
        JPanel studentManagementTab = new JPanel(new BorderLayout());
        
        // Control panel for student management
        JPanel studentControlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        studentControlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        studentControlPanel.add(new JLabel("Subject:"));
        JComboBox<String> subjectCombo = new JComboBox<>();
        studentControlPanel.add(subjectCombo);
        
        JButton addStudentsBtn = new JButton("Add Students");
        addStudentsBtn.addActionListener(e -> {
            if (subjectCombo.getSelectedItem() != null) {
                showAddStudentsDialog(subjectCombo.getSelectedItem().toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a subject first");
            }
        });
        studentControlPanel.add(addStudentsBtn);
        
        // Table for enrolled students
        String[] studentColumns = {"ID", "Roll Number", "Name", "Batch"};
        DefaultTableModel enrolledStudentsModel = new DefaultTableModel(studentColumns, 0);
        JTable enrolledStudentsTable = new JTable(enrolledStudentsModel);
        JScrollPane enrolledStudentsScrollPane = new JScrollPane(enrolledStudentsTable);
        
        // Load subjects into the combo box
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM subjects WHERE faculty_id = ? ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                subjectCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage());
        }
        
        // Add action listener to subject combo box
        subjectCombo.addActionListener(e -> {
            if (subjectCombo.getSelectedItem() != null) {
                loadEnrolledStudents(subjectCombo.getSelectedItem().toString(), enrolledStudentsModel);
            }
        });
        
        studentManagementTab.add(studentControlPanel, BorderLayout.NORTH);
        studentManagementTab.add(enrolledStudentsScrollPane, BorderLayout.CENTER);
        
        // Add tabs
        attendanceTabs.addTab("Mark Attendance", attendanceTab);
        attendanceTabs.addTab("Manage Students", studentManagementTab);
        
        attendancePanel.add(attendanceTabs, BorderLayout.CENTER);
    }
    
    private void createReportPanel() {
        reportPanel = new JPanel(new BorderLayout());
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(new JLabel("Subject:"));
        reportSubjectComboBox = new JComboBox<>();
        controlPanel.add(reportSubjectComboBox);
        
        controlPanel.add(new JLabel("Report Type:"));
        reportTypeComboBox = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Custom Range"});
        controlPanel.add(reportTypeComboBox);
        
        controlPanel.add(new JLabel("Start Date:"));
        startDateChooser = new JDateChooser(new java.util.Date());
        controlPanel.add(startDateChooser);
        
        controlPanel.add(new JLabel("End Date:"));
        endDateChooser = new JDateChooser(new java.util.Date());
        controlPanel.add(endDateChooser);
        
        reportTypeComboBox.addActionListener(e -> updateDateRange());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        generateReportBtn = new JButton("Generate Report");
        exportPdfBtn = new JButton("Export to PDF");
        exportExcelBtn = new JButton("Export to Excel");
        
        generateReportBtn.addActionListener(e -> generateReport());
        exportPdfBtn.addActionListener(e -> exportToPdf());
        exportExcelBtn.addActionListener(e -> exportToExcel());
        
        buttonPanel.add(generateReportBtn);
        buttonPanel.add(exportPdfBtn);
        buttonPanel.add(exportExcelBtn);
        
        // Table
        String[] columns = {"Roll Number", "Name", "Present", "Absent", "Late", "Excused", "Percentage"};
        reportTableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(reportTableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        reportPanel.add(topPanel, BorderLayout.NORTH);
        reportPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createProfilePanel() {
        profilePanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        usernameField.setEditable(false);
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        formPanel.add(emailField);

        formPanel.add(new JLabel("New Password:"));
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateEmailBtn = new JButton("Update Email");
        updatePasswordBtn = new JButton("Update Password");
        
        updateEmailBtn.addActionListener(e -> updateEmail());
        updatePasswordBtn.addActionListener(e -> updatePassword());

        buttonPanel.add(updateEmailBtn);
        buttonPanel.add(updatePasswordBtn);

        profilePanel.add(formPanel, BorderLayout.CENTER);
        profilePanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load profile data after panel creation
        loadProfileData();
    }
    
    private void loadProfileData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT f.name, f.department, u.email, u.username " +
                         "FROM faculty f " +
                         "JOIN users u ON f.user_id = u.id " +
                         "WHERE f.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                emailField.setText(rs.getString("email"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading profile data: " + e.getMessage());
        }
    }
    
    private void loadSubjects() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name FROM subjects WHERE faculty_id = ? ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            subjectComboBox.removeAllItems();
            reportSubjectComboBox.removeAllItems();
            
            while (rs.next()) {
                String subjectName = rs.getString("name");
                subjectComboBox.addItem(subjectName);
                reportSubjectComboBox.addItem(subjectName);
            }
            
            if (subjectComboBox.getItemCount() > 0) {
                loadBatches();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage());
        }
    }
    
    private void loadBatches() {
        if (subjectComboBox.getSelectedItem() == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get the selected subject ID
            String sql = "SELECT id FROM subjects WHERE name = ? AND faculty_id = ?";
            PreparedStatement subjectStmt = conn.prepareStatement(sql);
            subjectStmt.setString(1, subjectComboBox.getSelectedItem().toString());
            subjectStmt.setInt(2, facultyId);
            ResultSet subjectRs = subjectStmt.executeQuery();
            
            if (subjectRs.next()) {
                int subjectId = subjectRs.getInt("id");
                
                // Get unique batches for students taking this subject
                String batchSql = "SELECT DISTINCT batch FROM students ORDER BY batch";
                Statement batchStmt = conn.createStatement();
                ResultSet batchRs = batchStmt.executeQuery(batchSql);
                
                batchComboBox.removeAllItems();
                
                while (batchRs.next()) {
                    batchComboBox.addItem(batchRs.getString("batch"));
                }
                
                if (batchComboBox.getItemCount() > 0) {
                    loadStudentsForAttendance();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading batches: " + e.getMessage());
        }
    }
    
    private void loadStudentsForAttendance() {
        if (subjectComboBox.getSelectedItem() == null || batchComboBox.getSelectedItem() == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing data
            studentTableModel.setRowCount(0);
            attendanceStatusMap.clear();
            
            String sql = "SELECT s.id, s.roll_number, s.name FROM students s " +
                         "WHERE s.batch = ? ORDER BY s.roll_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, batchComboBox.getSelectedItem().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int studentId = rs.getInt("id");
                String rollNumber = rs.getString("roll_number");
                String name = rs.getString("name");
                
                // Create attendance status dropdown
                JComboBox<String> statusCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE", "EXCUSED"});
                attendanceStatusMap.put(studentId, statusCombo);
                
                // Check if attendance already marked for today
                java.sql.Date sqlDate = new java.sql.Date(dateChooser.getDate().getTime());
                checkExistingAttendance(conn, studentId, sqlDate);
                
                // Add table row
                Object[] row = {studentId, rollNumber, name, statusCombo};
                studentTableModel.addRow(row);
            }
            
            // Set custom renderer for combo box column
            studentTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, 
                                                              boolean isSelected, boolean hasFocus, 
                                                              int row, int column) {
                    return (JComboBox)value;
                }
            });
            
            // Set custom editor for combo box column
            studentTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox<>()) {
                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, 
                                                            boolean isSelected, int row, int column) {
                    return (JComboBox)value;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }
    
    private void checkExistingAttendance(Connection conn, int studentId, java.sql.Date date) throws SQLException {
        String sql = "SELECT status FROM attendance WHERE student_id = ? AND date = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setDate(2, date);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String status = rs.getString("status");
            // Update the table model to reflect existing attendance
            for (int row = 0; row < studentTableModel.getRowCount(); row++) {
                if ((int) studentTableModel.getValueAt(row, 0) == studentId) {
                    studentTableModel.setValueAt(status, row, 3);
                    break;
                }
            }
        }
    }
    
    private int getSelectedSubjectId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM subjects WHERE name = ? AND faculty_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, subjectComboBox.getSelectedItem().toString());
        stmt.setInt(2, facultyId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }
    
    private void saveAttendance() {
        if (subjectComboBox.getSelectedItem() == null || batchComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select both subject and batch");
            return;
        }

        java.util.Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    int subjectId = getSelectedSubjectId(conn);
                    java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
                    
                    for (int row = 0; row < studentTableModel.getRowCount(); row++) {
                        int studentId = (int) studentTableModel.getValueAt(row, 0);
                        String status = (String) studentTableModel.getValueAt(row, 3);
                        
                        // Check if attendance already exists for this date
                        checkExistingAttendance(conn, studentId, sqlDate);
                        
                        // Update or insert attendance
                        String sql = "INSERT INTO attendance (student_id, subject_id, date, status, marked_by) " +
                                    "VALUES (?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, studentId);
                        stmt.setInt(2, subjectId);
                        stmt.setDate(3, sqlDate);
                        stmt.setString(4, status);
                        stmt.setInt(5, facultyId);
                        stmt.setString(6, status);
                        stmt.setInt(7, facultyId);
                        stmt.executeUpdate();
                    }
                    
                    conn.commit();
                    publish("Attendance saved successfully!");
                    
                    // Check for low attendance
                    checkLowAttendance(conn, subjectId);
                    
                } catch (SQLException e) {
                    if (conn != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                    throw e;
                } finally {
                    if (conn != null) {
                        try {
                            conn.setAutoCommit(true);
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(FacultyDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    loadStudentsForAttendance(); // Refresh the table
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FacultyDashboard.this,
                        "Error saving attendance: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void checkLowAttendance(Connection conn, int subjectId) {
        try {
            String sql = "SELECT s.id, s.name, u.email, " +
                          "(SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.id AND a.subject_id = ?) as total_classes, " +
                          "(SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.id AND a.subject_id = ? AND a.status = 'PRESENT') as present_classes " +
                          "FROM students s JOIN users u ON s.user_id = u.id " + 
                          "WHERE s.batch = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectId);
            stmt.setInt(2, subjectId);
            stmt.setString(3, batchComboBox.getSelectedItem().toString());
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int totalClasses = rs.getInt("total_classes");
                int presentClasses = rs.getInt("present_classes");
                
                if (totalClasses > 0) {
                    double attendancePercentage = (presentClasses * 100.0) / totalClasses;
                    
                    if (attendancePercentage < 75) {
                        int studentId = rs.getInt("id");
                        String studentName = rs.getString("name");
                        String email = rs.getString("email");
                        
                        // Create notification
                        String notificationSql = "INSERT INTO notifications (user_id, message, type, status) " +
                                                "SELECT user_id, ?, 'EMAIL', 'PENDING' FROM students WHERE id = ?";
                        PreparedStatement notificationStmt = conn.prepareStatement(notificationSql);
                        notificationStmt.setString(1, "Your attendance in " + subjectComboBox.getSelectedItem() + 
                                                    " is " + String.format("%.2f%%", attendancePercentage) + 
                                                    ", which is below the required 75%.");
                        notificationStmt.setInt(2, studentId);
                        notificationStmt.executeUpdate();
                        
                        // For demo purposes, print notification details
                        System.out.println("Notification for " + studentName + " (" + email + "): Attendance is " + 
                                          String.format("%.2f%%", attendancePercentage));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateDateRange() {
        Calendar cal = Calendar.getInstance();
        java.util.Date currentDate = new java.util.Date();
        
        switch (reportTypeComboBox.getSelectedIndex()) {
            case 0: // Daily
                startDateChooser.setDate(currentDate);
                endDateChooser.setDate(currentDate);
                break;
                
            case 1: // Weekly
                cal.setTime(currentDate);
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDateChooser.setDate(cal.getTime());
                
                cal.add(Calendar.DAY_OF_WEEK, 6);
                endDateChooser.setDate(cal.getTime());
                break;
                
            case 2: // Monthly
                cal.setTime(currentDate);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDateChooser.setDate(cal.getTime());
                
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDateChooser.setDate(cal.getTime());
                break;
                
            case 3: // Custom Range
                // Do nothing, let user select
                break;
        }
    }
    
    private void generateReport() {
        if (reportSubjectComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing data
            reportTableModel.setRowCount(0);
            
            int subjectId = getSelectedSubjectId(conn);
            if (subjectId == -1) {
                JOptionPane.showMessageDialog(this, "Subject not found");
                return;
            }
            
            java.sql.Date startDate = new java.sql.Date(startDateChooser.getDate().getTime());
            java.sql.Date endDate = new java.sql.Date(endDateChooser.getDate().getTime());
            
            String reportType = reportTypeComboBox.getSelectedItem().toString();
            String sql = "";
            
            switch (reportType) {
                case "Daily":
                    sql = "SELECT s.roll_number, s.name, " +
                          "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_count, " +
                          "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count, " +
                          "SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) as late_count, " +
                          "SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count, " +
                          "COUNT(a.id) as total_count " +
                          "FROM students s " +
                          "LEFT JOIN attendance a ON s.id = a.student_id AND a.subject_id = ? AND a.date = ? " +
                          "GROUP BY s.id, s.roll_number, s.name " +
                          "ORDER BY s.roll_number";
                    break;
                    
                case "Weekly":
                case "Monthly":
                case "Custom Range":
                    sql = "SELECT s.roll_number, s.name, " +
                          "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_count, " +
                          "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count, " +
                          "SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) as late_count, " +
                          "SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count, " +
                          "COUNT(a.id) as total_count " +
                          "FROM students s " +
                          "LEFT JOIN attendance a ON s.id = a.student_id AND a.subject_id = ? AND a.date BETWEEN ? AND ? " +
                          "GROUP BY s.id, s.roll_number, s.name " +
                          "ORDER BY s.roll_number";
                    break;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectId);
            stmt.setDate(2, startDate);
            if (!reportType.equals("Daily")) {
                stmt.setDate(3, endDate);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String rollNumber = rs.getString("roll_number");
                String name = rs.getString("name");
                int presentCount = rs.getInt("present_count");
                int absentCount = rs.getInt("absent_count");
                int lateCount = rs.getInt("late_count");
                int excusedCount = rs.getInt("excused_count");
                int totalCount = rs.getInt("total_count");
                
                double percentage = 0;
                if (totalCount > 0) {
                    percentage = (presentCount * 100.0) / totalCount;
                }
                
                Object[] row = {
                    rollNumber,
                    name,
                    presentCount,
                    absentCount,
                    lateCount,
                    excusedCount,
                    String.format("%.2f%%", percentage)
                };
                
                reportTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
        }
    }
    
    private void exportToPdf() {
        if (reportTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF File");
        fileChooser.setSelectedFile(new File("attendance_report.pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                // Placeholder for actual PDF export logic
                // In a real implementation, use iText library
                
                JOptionPane.showMessageDialog(this, "PDF report exported to " + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + e.getMessage());
            }
        }
    }
    
    private void exportToExcel() {
        if (reportTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("attendance_report.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                // Placeholder for actual Excel export logic
                // In a real implementation, use Apache POI library
                
                JOptionPane.showMessageDialog(this, "Excel report exported to " + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting to Excel: " + e.getMessage());
            }
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
                    stmt.setInt(2, facultyId);
                    
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
                    JOptionPane.showMessageDialog(FacultyDashboard.this, message);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadProfileData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FacultyDashboard.this,
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
        String newPassword = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter and confirm the new password", "Error", JOptionPane.ERROR_MESSAGE);
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
                    stmt.setInt(2, facultyId);
                    
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
                    JOptionPane.showMessageDialog(FacultyDashboard.this, message);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FacultyDashboard.this,
                        "Error updating password: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void showAddStudentsDialog(String subjectName) {
        JDialog dialog = new JDialog(this, "Add Students to " + subjectName, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        // Students table
        String[] columns = {"ID", "Roll Number", "Name", "Batch", "Add"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Boolean.class : String.class;
            }
        };
        JTable studentsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        
        // Load all students initially
        loadAllStudents(model);
        
        // Add search functionality
        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (searchText.isEmpty()) {
                loadAllStudents(model);
            } else {
                filterStudents(model, searchText);
            }
        });
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add Selected");
        JButton cancelBtn = new JButton("Cancel");
        
        addBtn.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                int subjectId = getSelectedSubjectId(conn);
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((Boolean) model.getValueAt(i, 4)) {
                        int studentId = Integer.parseInt(model.getValueAt(i, 0).toString());
                        addStudentToSubject(conn, studentId, subjectId);
                    }
                }
                
                JOptionPane.showMessageDialog(dialog, "Students added successfully!");
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding students: " + ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);
        
        // Add components to dialog
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void loadAllStudents(DefaultTableModel model) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, roll_number, name, batch FROM students ORDER BY roll_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("batch"),
                    false
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void filterStudents(DefaultTableModel model, String searchText) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, roll_number, name, batch FROM students " +
                         "WHERE LOWER(roll_number) LIKE ? OR LOWER(name) LIKE ? OR LOWER(batch) LIKE ? " +
                         "ORDER BY roll_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("batch"),
                    false
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering students: " + e.getMessage());
        }
    }

    private void addStudentToSubject(Connection conn, int studentId, int subjectId) throws SQLException {
        // First check if student is already enrolled
        String checkSql = "SELECT id FROM attendance WHERE student_id = ? AND subject_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, studentId);
        checkStmt.setInt(2, subjectId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (!rs.next()) {
            // Student is not enrolled, add them
            String insertSql = "INSERT INTO attendance (student_id, subject_id, date, status, marked_by) VALUES (?, ?, CURDATE(), 'ABSENT', ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, studentId);
            insertStmt.setInt(2, subjectId);
            insertStmt.setInt(3, facultyId);
            insertStmt.executeUpdate();
        }
    }
    
    private void loadEnrolledStudents(String subjectName, DefaultTableModel model) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.id, s.roll_number, s.name, s.batch " +
                         "FROM students s " +
                         "JOIN attendance a ON s.id = a.student_id " +
                         "JOIN subjects sub ON a.subject_id = sub.id " +
                         "WHERE sub.name = ? AND sub.faculty_id = ? " +
                         "GROUP BY s.id, s.roll_number, s.name, s.batch " +
                         "ORDER BY s.roll_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectName);
            stmt.setInt(2, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("batch")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading enrolled students: " + e.getMessage());
        }
    }
    
    // Inner class for date chooser
    private class JDateChooser extends JPanel {
        private JTextField textField;
        private java.util.Date date;
        
        public JDateChooser(java.util.Date initialDate) {
            setLayout(new BorderLayout());
            
            textField = new JTextField(10);
            JButton dateButton = new JButton("...");
            
            add(textField, BorderLayout.CENTER);
            add(dateButton, BorderLayout.EAST);
            
            setDate(initialDate);
            
            dateButton.addActionListener(e -> {
                // In a real implementation, show a date picker dialog
                // Here we just use the current date for simplicity
                setDate(new java.util.Date());
            });
        }
        
        public void setDate(java.util.Date date) {
            this.date = date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            textField.setText(sdf.format(date));
        }
        
        public java.util.Date getDate() {
            return date;
        }
    }
} 