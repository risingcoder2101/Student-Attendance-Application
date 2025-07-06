import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static DatabaseManager dbManager;
    private static JComboBox<Subject> subjectComboBox;
    private static JTextArea attendanceArea;
    private static JLabel overallAttendanceLabel;
    private static Font uiFont = new Font("Arial", Font.PLAIN, 16);
    private static JSpinner overallLimitSpinner;
    private static JLabel adviceLabel;

    public static void main(String[] args) {
        dbManager = new DatabaseManager();
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        dbManager.ensureOverallSettingsTable();
        JFrame frame = new JFrame("Attendance Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // Top: Add/Update Subject
        JPanel subjectPanel = new JPanel(new FlowLayout());
        subjectPanel.setFont(uiFont);
        JTextField subjectField = new JTextField(12);
        subjectField.setFont(uiFont);
        JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(75, 0, 100, 5));
        limitSpinner.setFont(uiFont);
        JButton addSubjectBtn = new JButton("Add Subject");
        addSubjectBtn.setFont(uiFont);
        JButton updateSubjectBtn = new JButton("Update Subject");
        updateSubjectBtn.setFont(uiFont);
        JButton removeSubjectBtn = new JButton("Remove Subject");
        removeSubjectBtn.setFont(uiFont);
        subjectPanel.add(labelWithFont("Subject:"));
        subjectPanel.add(subjectField);
        subjectPanel.add(labelWithFont("Limit %:"));
        subjectPanel.add(limitSpinner);
        subjectPanel.add(addSubjectBtn);
        subjectPanel.add(updateSubjectBtn);
        subjectPanel.add(removeSubjectBtn);

        // Middle: Mark Attendance
        JPanel markPanel = new JPanel(new FlowLayout());
        markPanel.setFont(uiFont);
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(uiFont);
        updateSubjectComboBox();
        JButton markPresentBtn = new JButton("Mark Present");
        markPresentBtn.setFont(uiFont);
        JButton markAbsentBtn = new JButton("Mark Absent");
        markAbsentBtn.setFont(uiFont);
        JButton removeLectureBtn = new JButton("Remove Last Lecture");
        removeLectureBtn.setFont(uiFont);
        markPanel.add(labelWithFont("Subject:"));
        markPanel.add(subjectComboBox);
        markPanel.add(markPresentBtn);
        markPanel.add(markAbsentBtn);
        markPanel.add(removeLectureBtn);

        // Overall Limit Panel
        JPanel overallPanel = new JPanel(new FlowLayout());
        overallPanel.setFont(uiFont);
        overallPanel.add(labelWithFont("Overall Limit %:"));
        overallLimitSpinner = new JSpinner(new SpinnerNumberModel(dbManager.getOverallLimit(), 0, 100, 1));
        overallLimitSpinner.setFont(uiFont);
        JButton setOverallBtn = new JButton("Set Overall Limit");
        setOverallBtn.setFont(uiFont);
        overallPanel.add(overallLimitSpinner);
        overallPanel.add(setOverallBtn);

        // Bottom: Attendance Display
        attendanceArea = new JTextArea(12, 55);
        attendanceArea.setFont(uiFont);
        attendanceArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(attendanceArea);
        JButton refreshBtn = new JButton("Refresh Attendance");
        refreshBtn.setFont(uiFont);
        overallAttendanceLabel = new JLabel("Overall Attendance: 0%");
        overallAttendanceLabel.setFont(uiFont);
        adviceLabel = new JLabel("");
        adviceLabel.setFont(uiFont);

        // Add listeners
        addSubjectBtn.addActionListener((ActionEvent e) -> {
            String subject = subjectField.getText().trim();
            int limit = (Integer) limitSpinner.getValue();
            if (!subject.isEmpty()) {
                if (dbManager.addSubject(subject, limit)) {
                    updateSubjectComboBox();
                    subjectField.setText("");
                    JOptionPane.showMessageDialog(frame, "Subject added!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to add subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Enter a subject name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateSubjectBtn.addActionListener((ActionEvent e) -> {
            Subject selected = (Subject) subjectComboBox.getSelectedItem();
            String newName = subjectField.getText().trim();
            int newLimit = (Integer) limitSpinner.getValue();
            if (selected != null && !newName.isEmpty()) {
                if (dbManager.updateSubject(selected.getId(), newName, newLimit)) {
                    updateSubjectComboBox();
                    subjectField.setText("");
                    JOptionPane.showMessageDialog(frame, "Subject updated!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to update subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Select a subject and enter new name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        markPresentBtn.addActionListener((ActionEvent e) -> markAttendance("PRESENT", frame));
        markAbsentBtn.addActionListener((ActionEvent e) -> markAttendance("ABSENT", frame));
        removeLectureBtn.addActionListener((ActionEvent e) -> removeLastLecture(frame));
        refreshBtn.addActionListener(e -> updateAttendanceArea());
        setOverallBtn.addActionListener(e -> {
            int newLimit = (Integer) overallLimitSpinner.getValue();
            if (dbManager.setOverallLimit(newLimit)) {
                JOptionPane.showMessageDialog(frame, "Overall limit updated!");
                updateAttendanceArea();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to update overall limit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        subjectComboBox.addActionListener(e -> {
            Subject selected = (Subject) subjectComboBox.getSelectedItem();
            if (selected != null) {
                subjectField.setText(selected.getName());
                limitSpinner.setValue(selected.getAttendanceLimit());
            }
        });

        removeSubjectBtn.addActionListener((ActionEvent e) -> {
            Subject selected = (Subject) subjectComboBox.getSelectedItem();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete subject '" + selected.getName() + "'? This will remove all its attendance records.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dbManager.removeSubject(selected.getId())) {
                        updateSubjectComboBox();
                        subjectField.setText("");
                        updateAttendanceArea();
                        JOptionPane.showMessageDialog(null, "Subject removed!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to remove subject.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Select a subject to remove.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(subjectPanel);
        topPanel.add(markPanel);
        topPanel.add(overallPanel);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(refreshBtn, BorderLayout.WEST);
        bottomPanel.add(overallAttendanceLabel, BorderLayout.CENTER);
        bottomPanel.add(adviceLabel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        updateAttendanceArea();
        frame.setVisible(true);
    }

    private static JLabel labelWithFont(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont);
        return label;
    }

    private static void markAttendance(String status, JFrame frame) {
        Subject selected = (Subject) subjectComboBox.getSelectedItem();
        if (selected != null) {
            if (dbManager.markAttendance(selected.getName(), LocalDate.now().toString(), status)) {
                JOptionPane.showMessageDialog(frame, "Attendance marked as " + status + "!");
                updateAttendanceArea();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to mark attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a subject.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void removeLastLecture(JFrame frame) {
        Subject selected = (Subject) subjectComboBox.getSelectedItem();
        if (selected != null) {
            boolean success = dbManager.removeLastLecture(selected.getId());
            if (success) {
                JOptionPane.showMessageDialog(frame, "Last lecture removed!");
                updateAttendanceArea();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to remove last lecture.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a subject.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateSubjectComboBox() {
        subjectComboBox.removeAllItems();
        List<Subject> subjects = dbManager.getAllSubjects();
        for (Subject subject : subjects) {
            subjectComboBox.addItem(subject);
        }
    }

    private static void updateAttendanceArea() {
        attendanceArea.setText("");
        List<Subject> subjects = dbManager.getAllSubjects();
        for (Subject subject : subjects) {
            int attended = dbManager.getAttendedLectures(subject.getName());
            int total = dbManager.getTotalLectures(subject.getName());
            double percent = dbManager.getAttendancePercentage(subject.getName());
            int limit = subject.getAttendanceLimit();
            attendanceArea.append(subject.getName() + ": " + attended + "/" + total + " attended, " + String.format("%.1f", percent) + "% (Limit: " + limit + "%)\n");
        }
        double overall = dbManager.getOverallAttendancePercentage();
        overallAttendanceLabel.setText("Overall Attendance: " + String.format("%.1f", overall) + "%");
        int overallLimit = dbManager.getOverallLimit();
        if (overall < overallLimit) {
            int toAttend = dbManager.lecturesToReachOverallLimit();
            adviceLabel.setText("Below limit! Attend next " + toAttend + " lectures");
        } else {
            int canSkip = dbManager.lecturesCanSkipOverall();
            adviceLabel.setText("Above limit! Can skip " + canSkip + " lectures");
        }
    }
}