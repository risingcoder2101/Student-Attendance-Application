import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;

public class FacultyDashboard extends JFrame {
    private int facultyUserId;
    private JTabbedPane tabbedPane;
    private DefaultTableModel attendanceModel;
    private JTable attendanceTable;

    public FacultyDashboard(int facultyUserId) {
        this.facultyUserId = facultyUserId;
        setTitle("Faculty Dashboard");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mark Attendance", createMarkAttendancePanel());
        tabbedPane.addTab("View Attendance", createViewAttendancePanel());
        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createMarkAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JComboBox<String> subjectBox = new JComboBox<>();
        loadSubjects(subjectBox);
        JButton loadBtn = new JButton("Load Students");
        JButton markBtn = new JButton("Mark Present");
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Roll No", "Present"}, 0) {
            public Class<?> getColumnClass(int col) { return col == 3 ? Boolean.class : String.class; }
        };
        JTable table = new JTable(model);
        panel.add(subjectBox, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        btnPanel.add(loadBtn); btnPanel.add(markBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            int subjectId = getSubjectId(subjectBox.getSelectedItem().toString());
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT s.id, s.name, s.roll_number FROM students s";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), false});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        markBtn.addActionListener(e -> {
            int subjectId = getSubjectId(subjectBox.getSelectedItem().toString());
            Date today = new Date();
            for (int i = 0; i < model.getRowCount(); i++) {
                boolean present = (Boolean) model.getValueAt(i, 3);
                if (present) {
                    int studentId = (int) model.getValueAt(i, 0);
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "INSERT INTO attendance (student_id, subject_id, date, status, marked_by) VALUES (?, ?, CURDATE(), 'PRESENT', ?)";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, studentId);
                        stmt.setInt(2, subjectId);
                        stmt.setInt(3, facultyUserId);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Attendance marked.");
        });
        return panel;
    }

    private JPanel createViewAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        attendanceModel = new DefaultTableModel(new String[]{"Student ID", "Subject ID", "Date", "Status"}, 0);
        attendanceTable = new JTable(attendanceModel);
        JButton loadBtn = new JButton("Load Attendance");
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        panel.add(loadBtn, BorderLayout.SOUTH);
        loadBtn.addActionListener(e -> loadAttendance());
        return panel;
    }

    private void loadSubjects(JComboBox<String> box) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM subjects WHERE faculty_id = (SELECT id FROM faculty WHERE user_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                box.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private int getSubjectId(String subjectName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM subjects WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        return -1;
    }

    private void loadAttendance() {
        attendanceModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT student_id, subject_id, date, status FROM attendance WHERE marked_by = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendanceModel.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getDate(3), rs.getString(4)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
} 