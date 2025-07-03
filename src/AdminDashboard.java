import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private int adminId;
    private JTabbedPane tabbedPane;
    private DefaultTableModel studentModel, facultyModel, subjectModel;
    private JTable studentTable, facultyTable, subjectTable;

    public AdminDashboard(int adminId) {
        this.adminId = adminId;
        setTitle("Admin Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Students", createStudentPanel());
        tabbedPane.addTab("Faculty", createFacultyPanel());
        tabbedPane.addTab("Subjects", createSubjectPanel());
        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Name", "Roll No", "Course", "Batch"};
        studentModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(studentModel);
        loadStudents();
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        JPanel form = new JPanel(new GridLayout(1, 9));
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField name = new JTextField();
        JTextField roll = new JTextField();
        JTextField course = new JTextField();
        JTextField batch = new JTextField();
        JButton add = new JButton("Add");
        JButton del = new JButton("Delete");
        form.add(new JLabel("Username")); form.add(usernameField);
        form.add(new JLabel("Password")); form.add(passwordField);
        form.add(new JLabel("Email")); form.add(emailField);
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel("Roll No")); form.add(roll);
        form.add(new JLabel("Course")); form.add(course);
        form.add(new JLabel("Batch")); form.add(batch);
        form.add(add); form.add(del);
        panel.add(form, BorderLayout.SOUTH);
        add.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // 1. Insert user
                String userSql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, 'STUDENT', ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, usernameField.getText());
                userStmt.setString(2, passwordField.getText());
                userStmt.setString(3, emailField.getText());
                userStmt.executeUpdate();
                java.sql.ResultSet userRs = userStmt.getGeneratedKeys();
                userRs.next();
                int userId = userRs.getInt(1);
                // 2. Insert student
                String sql = "INSERT INTO students (user_id, name, roll_number, course, batch) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setString(2, name.getText());
                stmt.setString(3, roll.getText());
                stmt.setString(4, course.getText());
                stmt.setString(5, batch.getText());
                stmt.executeUpdate();
                loadStudents();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) studentModel.getValueAt(row, 0);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Get user_id for this student
                    String getUserIdSql = "SELECT user_id FROM students WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, id);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    int userId = -1;
                    if (rs.next()) userId = rs.getInt(1);
                    // Delete student
                    String sql = "DELETE FROM students WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    // Delete user
                    if (userId != -1) {
                        String delUserSql = "DELETE FROM users WHERE id = ?";
                        PreparedStatement delUserStmt = conn.prepareStatement(delUserSql);
                        delUserStmt.setInt(1, userId);
                        delUserStmt.executeUpdate();
                    }
                    loadStudents();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        return panel;
    }

    private JPanel createFacultyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Name", "Department"};
        facultyModel = new DefaultTableModel(columns, 0);
        facultyTable = new JTable(facultyModel);
        loadFaculty();
        panel.add(new JScrollPane(facultyTable), BorderLayout.CENTER);
        JPanel form = new JPanel(new GridLayout(1, 7));
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField name = new JTextField();
        JTextField dept = new JTextField();
        JButton add = new JButton("Add");
        JButton del = new JButton("Delete");
        form.add(new JLabel("Username")); form.add(usernameField);
        form.add(new JLabel("Password")); form.add(passwordField);
        form.add(new JLabel("Email")); form.add(emailField);
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel("Department")); form.add(dept);
        form.add(add); form.add(del);
        panel.add(form, BorderLayout.SOUTH);
        add.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // 1. Insert user
                String userSql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, 'FACULTY', ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, usernameField.getText());
                userStmt.setString(2, passwordField.getText());
                userStmt.setString(3, emailField.getText());
                userStmt.executeUpdate();
                java.sql.ResultSet userRs = userStmt.getGeneratedKeys();
                userRs.next();
                int userId = userRs.getInt(1);
                // 2. Insert faculty
                String sql = "INSERT INTO faculty (user_id, name, department) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setString(2, name.getText());
                stmt.setString(3, dept.getText());
                stmt.executeUpdate();
                loadFaculty();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = facultyTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) facultyModel.getValueAt(row, 0);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Get user_id for this faculty
                    String getUserIdSql = "SELECT user_id FROM faculty WHERE id = ?";
                    PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                    getUserIdStmt.setInt(1, id);
                    ResultSet rs = getUserIdStmt.executeQuery();
                    int userId = -1;
                    if (rs.next()) userId = rs.getInt(1);
                    // Delete faculty
                    String sql = "DELETE FROM faculty WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    // Delete user
                    if (userId != -1) {
                        String delUserSql = "DELETE FROM users WHERE id = ?";
                        PreparedStatement delUserStmt = conn.prepareStatement(delUserSql);
                        delUserStmt.setInt(1, userId);
                        delUserStmt.executeUpdate();
                    }
                    loadFaculty();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        return panel;
    }

    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Name", "Code", "Total Classes", "Faculty ID"};
        subjectModel = new DefaultTableModel(columns, 0);
        subjectTable = new JTable(subjectModel);
        loadSubjects();
        panel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);
        JPanel form = new JPanel(new GridLayout(1, 6));
        JTextField name = new JTextField();
        JTextField code = new JTextField();
        JTextField total = new JTextField();
        JTextField facultyId = new JTextField();
        JButton add = new JButton("Add");
        JButton del = new JButton("Delete");
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel("Code")); form.add(code);
        form.add(new JLabel("Total Classes")); form.add(total);
        form.add(new JLabel("Faculty ID")); form.add(facultyId);
        form.add(add); form.add(del);
        panel.add(form, BorderLayout.SOUTH);
        add.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO subjects (name, code, total_classes, faculty_id) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.getText());
                stmt.setString(2, code.getText());
                stmt.setInt(3, Integer.parseInt(total.getText()));
                stmt.setInt(4, Integer.parseInt(facultyId.getText()));
                stmt.executeUpdate();
                loadSubjects();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        del.addActionListener(e -> {
            int row = subjectTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) subjectModel.getValueAt(row, 0);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "DELETE FROM subjects WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    loadSubjects();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        return panel;
    }

    private void loadStudents() {
        studentModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, roll_number, course, batch FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                studentModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    private void loadFaculty() {
        facultyModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, department FROM faculty";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                facultyModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    private void loadSubjects() {
        subjectModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, code, total_classes, faculty_id FROM subjects";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                subjectModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
} 