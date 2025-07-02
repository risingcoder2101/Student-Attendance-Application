import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboard extends JFrame {
    private int adminId;
    private JTabbedPane tabbedPane;
    private JPanel studentPanel, facultyPanel, subjectPanel, backupPanel;
    private JPanel subjectEnrollmentTab;
    
    // Student Management Components
    private JTextField studentNameField, studentRollField, studentCourseField, studentBatchField, studentUsernameField, studentPasswordField, studentEmailField;
    private JButton addStudentBtn, updateStudentBtn, deleteStudentBtn;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JComboBox<String> subjectComboBox;
    private JButton addStudentsToSubjectBtn;
    
    // Faculty Management Components
    private JTextField facultyNameField, facultyDeptField, facultyUsernameField, facultyPasswordField, facultyEmailField;
    private JButton addFacultyBtn, updateFacultyBtn, deleteFacultyBtn;
    private JTable facultyTable;
    private DefaultTableModel facultyTableModel;
    
    // Subject Management Components
    private JTextField subjectNameField, subjectCodeField, totalClassesField;
    private JComboBox<String> facultyComboBox;
    private JButton addSubjectBtn, updateSubjectBtn, deleteSubjectBtn;
    private JTable subjectTable;
    private DefaultTableModel subjectTableModel;
    
    // Backup & Restore Components
    private JButton backupBtn, restoreBtn;
    private JTextField backupPathField;
    private JFileChooser fileChooser;
    
    public AdminDashboard(int adminId) {
        this.adminId = adminId;
        setTitle("Attendance Management System - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // Initialize Panels
        createStudentPanel();
        createFacultyPanel();
        createSubjectPanel();
        createBackupPanel();
        
        tabbedPane.addTab("Student Management", studentPanel);
        tabbedPane.addTab("Faculty Management", facultyPanel);
        tabbedPane.addTab("Subject Management", subjectPanel);
        tabbedPane.addTab("Backup & Restore", backupPanel);
        
        add(tabbedPane);
        
        // Load initial data
        loadStudentData();
        loadFacultyData();
        loadSubjectData();
        loadSubjects();
    }
    
    private void createStudentPanel() {
        studentPanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane for student management
        JTabbedPane studentTabs = new JTabbedPane();
        
        // Student Details tab
        JPanel studentDetailsTab = new JPanel(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        formPanel.add(new JLabel("Name:"));
        studentNameField = new JTextField();
        formPanel.add(studentNameField);
        
        formPanel.add(new JLabel("Roll Number:"));
        studentRollField = new JTextField();
        formPanel.add(studentRollField);
        
        formPanel.add(new JLabel("Course:"));
        studentCourseField = new JTextField();
        formPanel.add(studentCourseField);
        
        formPanel.add(new JLabel("Batch:"));
        studentBatchField = new JTextField();
        formPanel.add(studentBatchField);
        
        formPanel.add(new JLabel("Username:"));
        studentUsernameField = new JTextField();
        formPanel.add(studentUsernameField);
        
        formPanel.add(new JLabel("Password:"));
        studentPasswordField = new JTextField();
        formPanel.add(studentPasswordField);
        
        formPanel.add(new JLabel("Email:"));
        studentEmailField = new JTextField();
        formPanel.add(studentEmailField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addStudentBtn = new JButton("Add Student");
        updateStudentBtn = new JButton("Update Student");
        deleteStudentBtn = new JButton("Delete Student");
        
        addStudentBtn.addActionListener(e -> addStudent());
        updateStudentBtn.addActionListener(e -> updateStudent());
        deleteStudentBtn.addActionListener(e -> deleteStudent());
        
        buttonPanel.add(addStudentBtn);
        buttonPanel.add(updateStudentBtn);
        buttonPanel.add(deleteStudentBtn);
        
        // Table
        String[] columns = {"ID", "Name", "Roll Number", "Course", "Batch", "Username", "Email"};
        studentTableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(studentTableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.getSelectedRow();
                if (row >= 0) {
                    studentNameField.setText(studentTable.getValueAt(row, 1).toString());
                    studentRollField.setText(studentTable.getValueAt(row, 2).toString());
                    studentCourseField.setText(studentTable.getValueAt(row, 3).toString());
                    studentBatchField.setText(studentTable.getValueAt(row, 4).toString());
                    studentUsernameField.setText(studentTable.getValueAt(row, 5).toString());
                    studentEmailField.setText(studentTable.getValueAt(row, 6).toString());
                    studentPasswordField.setText("");
                }
            }
        });
        
        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        studentDetailsTab.add(topPanel, BorderLayout.NORTH);
        studentDetailsTab.add(scrollPane, BorderLayout.CENTER);
        
        // Subject Enrollment tab
        subjectEnrollmentTab = new JPanel(new BorderLayout());
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(new JLabel("Subject:"));
        subjectComboBox = new JComboBox<>();
        controlPanel.add(subjectComboBox);
        
        addStudentsToSubjectBtn = new JButton("Add Students to Subject");
        addStudentsToSubjectBtn.addActionListener(e -> showAddStudentsDialog());
        controlPanel.add(addStudentsToSubjectBtn);
        
        // Table for enrolled students
        String[] enrollmentColumns = {"ID", "Roll Number", "Name", "Course", "Batch", "Status"};
        DefaultTableModel enrollmentTableModel = new DefaultTableModel(enrollmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        JTable enrollmentTable = new JTable(enrollmentTableModel);
        JScrollPane enrollmentScrollPane = new JScrollPane(enrollmentTable);
        
        // Add action listener to subject combo box
        subjectComboBox.addActionListener(e -> {
            if (subjectComboBox.getSelectedItem() != null) {
                loadEnrolledStudents(enrollmentTableModel);
            }
        });
        
        subjectEnrollmentTab.add(controlPanel, BorderLayout.NORTH);
        subjectEnrollmentTab.add(enrollmentScrollPane, BorderLayout.CENTER);
        
        // Add tabs
        studentTabs.addTab("Student Details", studentDetailsTab);
        studentTabs.addTab("Subject Enrollment", subjectEnrollmentTab);
        
        studentPanel.add(studentTabs, BorderLayout.CENTER);
        
        // Load initial data
        loadSubjects();
    }
    
    private void createFacultyPanel() {
        facultyPanel = new JPanel(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        formPanel.add(new JLabel("Name:"));
        facultyNameField = new JTextField();
        formPanel.add(facultyNameField);
        
        formPanel.add(new JLabel("Department:"));
        facultyDeptField = new JTextField();
        formPanel.add(facultyDeptField);
        
        formPanel.add(new JLabel("Username:"));
        facultyUsernameField = new JTextField();
        formPanel.add(facultyUsernameField);
        
        formPanel.add(new JLabel("Password:"));
        facultyPasswordField = new JTextField();
        formPanel.add(facultyPasswordField);
        
        formPanel.add(new JLabel("Email:"));
        facultyEmailField = new JTextField();
        formPanel.add(facultyEmailField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addFacultyBtn = new JButton("Add Faculty");
        updateFacultyBtn = new JButton("Update Faculty");
        deleteFacultyBtn = new JButton("Delete Faculty");
        
        addFacultyBtn.addActionListener(e -> addFaculty());
        updateFacultyBtn.addActionListener(e -> updateFaculty());
        deleteFacultyBtn.addActionListener(e -> deleteFaculty());
        
        buttonPanel.add(addFacultyBtn);
        buttonPanel.add(updateFacultyBtn);
        buttonPanel.add(deleteFacultyBtn);
        
        // Table
        String[] columns = {"ID", "Name", "Department", "Username", "Email"};
        facultyTableModel = new DefaultTableModel(columns, 0);
        facultyTable = new JTable(facultyTableModel);
        JScrollPane scrollPane = new JScrollPane(facultyTable);
        
        facultyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = facultyTable.getSelectedRow();
                if (row >= 0) {
                    facultyNameField.setText(facultyTable.getValueAt(row, 1).toString());
                    facultyDeptField.setText(facultyTable.getValueAt(row, 2).toString());
                    facultyUsernameField.setText(facultyTable.getValueAt(row, 3).toString());
                    facultyEmailField.setText(facultyTable.getValueAt(row, 4).toString());
                    facultyPasswordField.setText("");
                }
            }
        });
        
        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        facultyPanel.add(topPanel, BorderLayout.NORTH);
        facultyPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createSubjectPanel() {
        subjectPanel = new JPanel(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        formPanel.add(new JLabel("Subject Name:"));
        subjectNameField = new JTextField();
        formPanel.add(subjectNameField);
        
        formPanel.add(new JLabel("Subject Code:"));
        subjectCodeField = new JTextField();
        formPanel.add(subjectCodeField);
        
        formPanel.add(new JLabel("Total Classes:"));
        totalClassesField = new JTextField();
        formPanel.add(totalClassesField);
        
        formPanel.add(new JLabel("Assigned Faculty:"));
        facultyComboBox = new JComboBox<>();
        formPanel.add(facultyComboBox);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addSubjectBtn = new JButton("Add Subject");
        updateSubjectBtn = new JButton("Update Subject");
        deleteSubjectBtn = new JButton("Delete Subject");
        
        addSubjectBtn.addActionListener(e -> addSubject());
        updateSubjectBtn.addActionListener(e -> updateSubject());
        deleteSubjectBtn.addActionListener(e -> deleteSubject());
        
        buttonPanel.add(addSubjectBtn);
        buttonPanel.add(updateSubjectBtn);
        buttonPanel.add(deleteSubjectBtn);
        
        // Table
        String[] columns = {"ID", "Name", "Code", "Total Classes", "Faculty"};
        subjectTableModel = new DefaultTableModel(columns, 0);
        subjectTable = new JTable(subjectTableModel);
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        
        subjectTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = subjectTable.getSelectedRow();
                if (row >= 0) {
                    subjectNameField.setText(subjectTable.getValueAt(row, 1).toString());
                    subjectCodeField.setText(subjectTable.getValueAt(row, 2).toString());
                    totalClassesField.setText(subjectTable.getValueAt(row, 3).toString());
                    facultyComboBox.setSelectedItem(subjectTable.getValueAt(row, 4).toString());
                }
            }
        });
        
        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        subjectPanel.add(topPanel, BorderLayout.NORTH);
        subjectPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createBackupPanel() {
        backupPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Path field
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Backup/Restore Path:"), gbc);
        
        gbc.gridx = 1;
        backupPathField = new JTextField(30);
        contentPanel.add(backupPathField, gbc);
        
        gbc.gridx = 2;
        JButton browseBtn = new JButton("Browse");
        contentPanel.add(browseBtn, gbc);
        
        fileChooser = new JFileChooser();
        browseBtn.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                backupPathField.setText(file.getAbsolutePath());
            }
        });
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backupBtn = new JButton("Backup Database");
        restoreBtn = new JButton("Restore Database");
        
        backupBtn.addActionListener(e -> backupDatabase());
        restoreBtn.addActionListener(e -> restoreDatabase());
        
        buttonPanel.add(backupBtn);
        buttonPanel.add(restoreBtn);
        
        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        contentPanel.add(buttonPanel, gbc);
        
        backupPanel.add(contentPanel, BorderLayout.NORTH);
    }
    
    private void loadEnrolledStudents(DefaultTableModel model) {
        if (subjectComboBox.getSelectedItem() == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int subjectId = getSelectedSubjectId(conn);
            
            String sql = "SELECT s.id, s.roll_number, s.name, s.course, s.batch, " +
                         "CASE WHEN a.id IS NOT NULL THEN 'Enrolled' ELSE 'Not Enrolled' END as status " +
                         "FROM students s " +
                         "LEFT JOIN attendance a ON s.id = a.student_id AND a.subject_id = ? " +
                         "ORDER BY s.roll_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectId);
            ResultSet rs = stmt.executeQuery();
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("course"),
                    rs.getString("batch"),
                    rs.getString("status")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading enrolled students: " + e.getMessage());
        }
    }
    
    private void showAddStudentsDialog() {
        if (subjectComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject first");
            return;
        }

        JDialog dialog = new JDialog(this, "Add Students to Subject", true);
        dialog.setSize(600, 500);
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
        String[] columns = {"ID", "Roll Number", "Name", "Course", "Batch", "Add"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the "Add" column is editable
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
                int addedCount = 0;
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((Boolean) model.getValueAt(i, 5)) {
                        int studentId = Integer.parseInt(model.getValueAt(i, 0).toString());
                        addStudentToSubject(conn, studentId, subjectId);
                        addedCount++;
                    }
                }
                
                if (addedCount > 0) {
                    JOptionPane.showMessageDialog(dialog, addedCount + " students added successfully!");
                    dialog.dispose();
                    loadEnrolledStudents((DefaultTableModel) ((JTable) ((JScrollPane) ((JPanel) subjectEnrollmentTab.getComponent(1)).getComponent(0)).getViewport().getView()).getModel());
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please select at least one student to add");
                }
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
            String sql = "SELECT id, roll_number, name, course, batch FROM students ORDER BY roll_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("course"),
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
            String sql = "SELECT id, roll_number, name, course, batch FROM students " +
                         "WHERE LOWER(roll_number) LIKE ? OR LOWER(name) LIKE ? OR LOWER(course) LIKE ? OR LOWER(batch) LIKE ? " +
                         "ORDER BY roll_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            ResultSet rs = stmt.executeQuery();
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("course"),
                    rs.getString("batch"),
                    false
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering students: " + e.getMessage());
        }
    }
    
    // Student CRUD Operations
    private void addStudent() {
        // Validate input
        if (studentNameField.getText().trim().isEmpty() ||
            studentRollField.getText().trim().isEmpty() ||
            studentUsernameField.getText().trim().isEmpty() ||
            studentPasswordField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "All required fields must be filled",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            private Connection conn = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    // Check if username already exists
                    String checkSql = "SELECT id FROM users WHERE username = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setString(1, studentUsernameField.getText());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        throw new SQLException("Username already exists");
                    }
                    
                    // First create user
                    String userSql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, 'STUDENT', ?)";
                    PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                    userStmt.setString(1, studentUsernameField.getText());
                    userStmt.setString(2, studentPasswordField.getText());
                    userStmt.setString(3, studentEmailField.getText());
                    userStmt.executeUpdate();
                    
                    // Get the generated user ID
                    ResultSet userRs = userStmt.getGeneratedKeys();
                    if (!userRs.next()) {
                        throw new SQLException("Failed to get generated user ID");
                    }
                    int userId = userRs.getInt(1);
                    
                    // Now create student record
                    String studentSql = "INSERT INTO students (user_id, name, roll_number, course, batch) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement studentStmt = conn.prepareStatement(studentSql);
                    studentStmt.setInt(1, userId);
                    studentStmt.setString(2, studentNameField.getText());
                    studentStmt.setString(3, studentRollField.getText());
                    studentStmt.setString(4, studentCourseField.getText());
                    studentStmt.setString(5, studentBatchField.getText());
                    studentStmt.executeUpdate();
                    
                    conn.commit();
                    publish("Student added successfully!");
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
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearStudentForm();
                    loadStudentData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error adding student: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void updateStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to update");
            return;
        }
        
        int studentId = Integer.parseInt(studentTable.getValueAt(row, 0).toString());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    // Get user_id from student
                    String getUserIdSql = "SELECT user_id FROM students WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, studentId);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        
                        // Update user information
                        String updateUserSql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
                        PreparedStatement updateUserStmt = conn.prepareStatement(updateUserSql);
                        updateUserStmt.setString(1, studentUsernameField.getText());
                        updateUserStmt.setString(2, studentEmailField.getText());
                        updateUserStmt.setInt(3, userId);
                        updateUserStmt.executeUpdate();
                        
                        // Update password if provided
                        if (!studentPasswordField.getText().isEmpty()) {
                            String updatePwdSql = "UPDATE users SET password = ? WHERE id = ?";
                            PreparedStatement updatePwdStmt = conn.prepareStatement(updatePwdSql);
                            updatePwdStmt.setString(1, studentPasswordField.getText());
                            updatePwdStmt.setInt(2, userId);
                            updatePwdStmt.executeUpdate();
                        }
                        
                        // Update student information
                        String updateStudentSql = "UPDATE students SET name = ?, roll_number = ?, course = ?, batch = ? WHERE id = ?";
                        PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentSql);
                        updateStudentStmt.setString(1, studentNameField.getText());
                        updateStudentStmt.setString(2, studentRollField.getText());
                        updateStudentStmt.setString(3, studentCourseField.getText());
                        updateStudentStmt.setString(4, studentBatchField.getText());
                        updateStudentStmt.setInt(5, studentId);
                        updateStudentStmt.executeUpdate();
                        
                        conn.commit();
                        publish("Student updated successfully!");
                    }
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
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearStudentForm();
                    loadStudentData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error updating student: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete");
            return;
        }
        
        int studentId = Integer.parseInt(studentTable.getValueAt(row, 0).toString());
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this student? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Get user_id from student
                    String getUserIdSql = "SELECT user_id FROM students WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, studentId);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        
                        // Delete attendance records first to avoid foreign key constraint issues
                        String deleteAttendanceSql = "DELETE FROM attendance WHERE student_id = ?";
                        PreparedStatement deleteAttendanceStmt = conn.prepareStatement(deleteAttendanceSql);
                        deleteAttendanceStmt.setInt(1, studentId);
                        deleteAttendanceStmt.executeUpdate();
                        
                        // Delete student record
                        String deleteStudentSql = "DELETE FROM students WHERE id = ?";
                        PreparedStatement deleteStudentStmt = conn.prepareStatement(deleteStudentSql);
                        deleteStudentStmt.setInt(1, studentId);
                        deleteStudentStmt.executeUpdate();
                        
                        // Delete user record
                        String deleteUserSql = "DELETE FROM users WHERE id = ?";
                        PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql);
                        deleteUserStmt.setInt(1, userId);
                        deleteUserStmt.executeUpdate();
                        
                        conn.commit();
                        publish("Student deleted successfully!");
                    }
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearStudentForm();
                    loadStudentData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error deleting student: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    // Faculty CRUD Operations
    private void addFaculty() {
        // Validate input
        if (facultyNameField.getText().trim().isEmpty() ||
            facultyDeptField.getText().trim().isEmpty() ||
            facultyUsernameField.getText().trim().isEmpty() ||
            facultyPasswordField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "All required fields must be filled",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            private Connection conn = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    // Check if username already exists
                    String checkSql = "SELECT id FROM users WHERE username = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setString(1, facultyUsernameField.getText());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        throw new SQLException("Username already exists");
                    }
                    
                    // First create user
                    String userSql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, 'FACULTY', ?)";
                    PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                    userStmt.setString(1, facultyUsernameField.getText());
                    userStmt.setString(2, facultyPasswordField.getText());
                    userStmt.setString(3, facultyEmailField.getText());
                    userStmt.executeUpdate();
                    
                    ResultSet userRs = userStmt.getGeneratedKeys();
                    if (userRs.next()) {
                        int userId = userRs.getInt(1);
                        
                        // Now create faculty record
                        String facultySql = "INSERT INTO faculty (user_id, name, department) VALUES (?, ?, ?)";
                        PreparedStatement facultyStmt = conn.prepareStatement(facultySql);
                        facultyStmt.setInt(1, userId);
                        facultyStmt.setString(2, facultyNameField.getText());
                        facultyStmt.setString(3, facultyDeptField.getText());
                        facultyStmt.executeUpdate();
                        
                        conn.commit();
                        publish("Faculty added successfully!");
                    }
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
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearFacultyForm();
                    loadFacultyData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error adding faculty: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void updateFaculty() {
        int row = facultyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty to update");
            return;
        }
        
        int facultyId = Integer.parseInt(facultyTable.getValueAt(row, 0).toString());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Get user_id from faculty
                    String getUserIdSql = "SELECT user_id FROM faculty WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, facultyId);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        
                        // Update user information
                        String updateUserSql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
                        PreparedStatement updateUserStmt = conn.prepareStatement(updateUserSql);
                        updateUserStmt.setString(1, facultyUsernameField.getText());
                        updateUserStmt.setString(2, facultyEmailField.getText());
                        updateUserStmt.setInt(3, userId);
                        updateUserStmt.executeUpdate();
                        
                        // Update password if provided
                        if (!facultyPasswordField.getText().isEmpty()) {
                            String updatePwdSql = "UPDATE users SET password = ? WHERE id = ?";
                            PreparedStatement updatePwdStmt = conn.prepareStatement(updatePwdSql);
                            updatePwdStmt.setString(1, facultyPasswordField.getText());
                            updatePwdStmt.setInt(2, userId);
                            updatePwdStmt.executeUpdate();
                        }
                        
                        // Update faculty information
                        String updateFacultySql = "UPDATE faculty SET name = ?, department = ? WHERE id = ?";
                        PreparedStatement updateFacultyStmt = conn.prepareStatement(updateFacultySql);
                        updateFacultyStmt.setString(1, facultyNameField.getText());
                        updateFacultyStmt.setString(2, facultyDeptField.getText());
                        updateFacultyStmt.setInt(3, facultyId);
                        updateFacultyStmt.executeUpdate();
                        
                        conn.commit();
                        publish("Faculty updated successfully!");
                    }
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearFacultyForm();
                    loadFacultyData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error updating faculty: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void deleteFaculty() {
        int row = facultyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty to delete");
            return;
        }
        
        int facultyId = Integer.parseInt(facultyTable.getValueAt(row, 0).toString());
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this faculty? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Get user_id from faculty
                    String getUserIdSql = "SELECT user_id FROM faculty WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, facultyId);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        
                        // First update subjects to remove faculty assignment
                        String updateSubjectsSql = "UPDATE subjects SET faculty_id = NULL WHERE faculty_id = ?";
                        PreparedStatement updateSubjectsStmt = conn.prepareStatement(updateSubjectsSql);
                        updateSubjectsStmt.setInt(1, facultyId);
                        updateSubjectsStmt.executeUpdate();
                        
                        // Then delete faculty record
                        String deleteFacultySql = "DELETE FROM faculty WHERE id = ?";
                        PreparedStatement deleteFacultyStmt = conn.prepareStatement(deleteFacultySql);
                        deleteFacultyStmt.setInt(1, facultyId);
                        deleteFacultyStmt.executeUpdate();
                        
                        // Finally delete user record
                        String deleteUserSql = "DELETE FROM users WHERE id = ?";
                        PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql);
                        deleteUserStmt.setInt(1, userId);
                        deleteUserStmt.executeUpdate();
                        
                        conn.commit();
                        publish("Faculty deleted successfully!");
                    }
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearFacultyForm();
                    loadFacultyData();
                    loadSubjectData(); // Refresh subject data too as faculty assignments changed
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error deleting faculty: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    // Subject CRUD Operations
    private void addSubject() {
        // Validate input
        if (subjectNameField.getText().trim().isEmpty() ||
            subjectCodeField.getText().trim().isEmpty() ||
            totalClassesField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "All required fields must be filled",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    String subjectSql = "INSERT INTO subjects (name, code, total_classes, faculty_id) VALUES (?, ?, ?, ?)";
                    PreparedStatement subjectStmt = conn.prepareStatement(subjectSql);
                    subjectStmt.setString(1, subjectNameField.getText());
                    subjectStmt.setString(2, subjectCodeField.getText());
                    subjectStmt.setInt(3, Integer.parseInt(totalClassesField.getText()));
                    
                    // Get faculty ID from selected name
                    if (facultyComboBox.getSelectedIndex() != -1) {
                        String selectedFaculty = facultyComboBox.getSelectedItem().toString();
                        String facultySql = "SELECT id FROM faculty WHERE name = ?";
                        PreparedStatement facultyStmt = conn.prepareStatement(facultySql);
                        facultyStmt.setString(1, selectedFaculty);
                        ResultSet rs = facultyStmt.executeQuery();
                        
                        if (rs.next()) {
                            subjectStmt.setInt(4, rs.getInt("id"));
                        } else {
                            subjectStmt.setNull(4, java.sql.Types.INTEGER);
                        }
                    } else {
                        subjectStmt.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    subjectStmt.executeUpdate();
                    conn.commit();
                    publish("Subject added successfully!");
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearSubjectForm();
                    loadSubjectData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error adding subject: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void updateSubject() {
        int row = subjectTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a subject to update");
            return;
        }
        
        int subjectId = Integer.parseInt(subjectTable.getValueAt(row, 0).toString());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    String updateSubjectSql = "UPDATE subjects SET name = ?, code = ?, total_classes = ?, faculty_id = ? WHERE id = ?";
                    PreparedStatement updateSubjectStmt = conn.prepareStatement(updateSubjectSql);
                    updateSubjectStmt.setString(1, subjectNameField.getText());
                    updateSubjectStmt.setString(2, subjectCodeField.getText());
                    updateSubjectStmt.setInt(3, Integer.parseInt(totalClassesField.getText()));
                    
                    // Get faculty ID from selected name
                    if (facultyComboBox.getSelectedIndex() != -1) {
                        String selectedFaculty = facultyComboBox.getSelectedItem().toString();
                        String facultySql = "SELECT id FROM faculty WHERE name = ?";
                        PreparedStatement facultyStmt = conn.prepareStatement(facultySql);
                        facultyStmt.setString(1, selectedFaculty);
                        ResultSet rs = facultyStmt.executeQuery();
                        
                        if (rs.next()) {
                            updateSubjectStmt.setInt(4, rs.getInt("id"));
                        } else {
                            updateSubjectStmt.setNull(4, java.sql.Types.INTEGER);
                        }
                    } else {
                        updateSubjectStmt.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    updateSubjectStmt.setInt(5, subjectId);
                    updateSubjectStmt.executeUpdate();
                    
                    conn.commit();
                    publish("Subject updated successfully!");
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearSubjectForm();
                    loadSubjectData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error updating subject: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    private void deleteSubject() {
        int row = subjectTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete");
            return;
        }
        
        int subjectId = Integer.parseInt(subjectTable.getValueAt(row, 0).toString());
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this subject? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Delete attendance records first to avoid foreign key constraint issues
                    String deleteAttendanceSql = "DELETE FROM attendance WHERE subject_id = ?";
                    PreparedStatement deleteAttendanceStmt = conn.prepareStatement(deleteAttendanceSql);
                    deleteAttendanceStmt.setInt(1, subjectId);
                    deleteAttendanceStmt.executeUpdate();
                    
                    // Delete subject
                    String deleteSubjectSql = "DELETE FROM subjects WHERE id = ?";
                    PreparedStatement deleteSubjectStmt = conn.prepareStatement(deleteSubjectSql);
                    deleteSubjectStmt.setInt(1, subjectId);
                    deleteSubjectStmt.executeUpdate();
                    
                    conn.commit();
                    publish("Subject deleted successfully!");
                } catch (SQLException e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    clearSubjectForm();
                    loadSubjectData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error deleting subject: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    
    // Backup & Restore Operations
    private void backupDatabase() {
        String backupPath = backupPathField.getText();
        if (backupPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify a backup path");
            return;
        }
        
        if (DatabaseConnection.backupDatabase(backupPath)) {
            JOptionPane.showMessageDialog(this, "Database backup created successfully at: " + backupPath);
        } else {
            JOptionPane.showMessageDialog(this, "Error creating database backup", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void restoreDatabase() {
        String backupPath = backupPathField.getText();
        if (backupPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify a backup file path");
            return;
        }
        
        if (DatabaseConnection.restoreDatabase(backupPath)) {
            JOptionPane.showMessageDialog(this, "Database restored successfully from: " + backupPath);
            // Reload all data
            loadStudentData();
            loadFacultyData();
            loadSubjectData();
        } else {
            JOptionPane.showMessageDialog(this, "Error restoring database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Load Data Methods
    private void loadStudentData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    studentTableModel.setRowCount(0); // Clear existing data
                    
                    String sql = "SELECT s.id, s.name, s.roll_number, s.course, s.batch, u.username, u.email " +
                                "FROM students s JOIN users u ON s.user_id = u.id ORDER BY s.id";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("roll_number"),
                            rs.getString("course"),
                            rs.getString("batch"),
                            rs.getString("username"),
                            rs.getString("email")
                        };
                        studentTableModel.addRow(row);
                    }
                    conn.commit();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error loading student data: " + e.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }
    
    private void loadFacultyData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    facultyTableModel.setRowCount(0); // Clear existing data
                    facultyComboBox.removeAllItems(); // Clear faculty dropdown for subjects
                    
                    String sql = "SELECT f.id, f.name, f.department, u.username, u.email " +
                                "FROM faculty f JOIN users u ON f.user_id = u.id ORDER BY f.id";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getString("username"),
                            rs.getString("email")
                        };
                        facultyTableModel.addRow(row);
                        facultyComboBox.addItem(rs.getString("name"));
                    }
                    conn.commit();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error loading faculty data: " + e.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }
    
    private void loadSubjectData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    subjectTableModel.setRowCount(0); // Clear existing data
                    
                    String sql = "SELECT s.id, s.name, s.code, s.total_classes, f.name as faculty_name " +
                                "FROM subjects s LEFT JOIN faculty f ON s.faculty_id = f.id ORDER BY s.id";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("code"),
                            rs.getInt("total_classes"),
                            rs.getString("faculty_name")
                        };
                        subjectTableModel.addRow(row);
                    }
                    conn.commit();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error loading subject data: " + e.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }
    
    // Helper methods to clear forms
    private void clearStudentForm() {
        studentNameField.setText("");
        studentRollField.setText("");
        studentCourseField.setText("");
        studentBatchField.setText("");
        studentUsernameField.setText("");
        studentPasswordField.setText("");
        studentEmailField.setText("");
    }
    
    private void clearFacultyForm() {
        facultyNameField.setText("");
        facultyDeptField.setText("");
        facultyUsernameField.setText("");
        facultyPasswordField.setText("");
        facultyEmailField.setText("");
    }
    
    private void clearSubjectForm() {
        subjectNameField.setText("");
        subjectCodeField.setText("");
        totalClassesField.setText("");
        facultyComboBox.setSelectedIndex(-1);
    }

    private void loadSubjects() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM subjects ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            subjectComboBox.removeAllItems();
            
            while (rs.next()) {
                subjectComboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage());
        }
    }

    private int getSelectedSubjectId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM subjects WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, subjectComboBox.getSelectedItem().toString());
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
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
            insertStmt.setInt(3, adminId); // Using admin ID as marked_by
            insertStmt.executeUpdate();
        }
    }
} 