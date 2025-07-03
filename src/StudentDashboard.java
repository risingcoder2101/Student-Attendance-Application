import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private int studentUserId;
    private DefaultTableModel attendanceModel;
    private JTable attendanceTable;

    public StudentDashboard(int studentUserId) {
        this.studentUserId = studentUserId;
        setTitle("Student Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        attendanceModel = new DefaultTableModel(new String[]{"Subject ID", "Date", "Status"}, 0);
        attendanceTable = new JTable(attendanceModel);
        JButton loadBtn = new JButton("Load Attendance");
        add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        add(loadBtn, BorderLayout.SOUTH);
        loadBtn.addActionListener(e -> loadAttendance());
        setVisible(true);
    }

    private void loadAttendance() {
        attendanceModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT subject_id, date, status FROM attendance WHERE student_id = (SELECT id FROM students WHERE user_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendanceModel.addRow(new Object[]{rs.getInt(1), rs.getDate(2), rs.getString(3)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
} 