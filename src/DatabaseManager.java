import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String MYSQL_HOST = "localhost";
    private static final String MYSQL_PORT = "3306";
    private static final String MYSQL_USER = "root"; // Change as needed
    private static final String MYSQL_PASSWORD = "root"; // Change as needed
    private static final String DB_NAME = "attendance_db";
    private static final String DB_URL = "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + DB_NAME + "?serverTimezone=UTC";

    public DatabaseManager() {
        // No need to create tables, assume they exist as per database.sql
    }

    public boolean addSubject(String subjectName, int attendanceLimit) {
        String sql = "INSERT INTO subjects (name, attendance_limit) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            pstmt.setInt(2, attendanceLimit);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSubject(int subjectId, String newName, int newLimit) {
        String sql = "UPDATE subjects SET name = ?, attendance_limit = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, newLimit);
            pstmt.setInt(3, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT id, name, attendance_limit FROM subjects";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subjects.add(new Subject(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("attendance_limit")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    public boolean markAttendance(String subjectName, String date, String status) {
        String sql = "INSERT INTO attendance (subject_id, date, status) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int subjectId = getSubjectId(subjectName);
            if (subjectId == -1) return false;
            pstmt.setInt(1, subjectId);
            pstmt.setString(2, date);
            pstmt.setString(3, status);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalLectures(String subjectName) {
        String sql = "SELECT COUNT(*) as total FROM attendance a JOIN subjects s ON a.subject_id = s.id WHERE s.name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAttendedLectures(String subjectName) {
        String sql = "SELECT COUNT(*) as attended FROM attendance a JOIN subjects s ON a.subject_id = s.id WHERE s.name = ? AND a.status = 'PRESENT'";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("attended");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAttendancePercentage(String subjectName) {
        int attended = getAttendedLectures(subjectName);
        int total = getTotalLectures(subjectName);
        return total > 0 ? (double) attended / total * 100 : 0;
    }

    public double getOverallAttendancePercentage() {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as attended FROM attendance";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int attended = rs.getInt("attended");
                return total > 0 ? (double) attended / total * 100 : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAttendanceLimit(String subjectName) {
        String sql = "SELECT attendance_limit FROM subjects WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("attendance_limit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 75; // default limit
    }

    private int getSubjectId(String subjectName) {
        String sql = "SELECT id FROM subjects WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean removeLastLecture(int subjectId) {
        String sql = "DELETE FROM attendance WHERE id = (SELECT id FROM (SELECT id FROM attendance WHERE subject_id = ? ORDER BY date DESC, id DESC LIMIT 1) AS t)";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Overall Attendance Limit ---
    public void ensureOverallSettingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS overall_settings (id INT PRIMARY KEY, attendance_limit INT DEFAULT 75)";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Insert default row if not exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM overall_settings");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO overall_settings (id, attendance_limit) VALUES (1, 75)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getOverallLimit() {
        ensureOverallSettingsTable();
        String sql = "SELECT attendance_limit FROM overall_settings WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("attendance_limit");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 75;
    }

    public boolean setOverallLimit(int limit) {
        ensureOverallSettingsTable();
        String sql = "UPDATE overall_settings SET attendance_limit = ? WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // How many lectures must be attended to reach the limit (if below)
    public int lecturesToReachOverallLimit() {
        int attended = 0, total = 0, limit = getOverallLimit();
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as attended FROM attendance";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                total = rs.getInt("total");
                attended = rs.getInt("attended");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // n = lectures to attend, (attended + n) / (total + n) >= limit/100
        int n = 0;
        while (total + n == 0 || ((double)(attended + n) / (total + n)) * 100 < limit) {
            n++;
            if (n > 1000) break; // prevent infinite loop
        }
        return n;
    }

    // How many lectures can be skipped and still stay above the limit (if above)
    public int lecturesCanSkipOverall() {
        int attended = 0, total = 0, limit = getOverallLimit();
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as attended FROM attendance";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                total = rs.getInt("total");
                attended = rs.getInt("attended");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // k = lectures can skip, (attended) / (total + k) >= limit/100
        int k = 0;
        while (total + k == 0 || ((double)attended / (total + k)) * 100 >= limit) {
            k++;
            if (k > 1000) break;
        }
        return k - 1;
    }

    public boolean removeSubject(int subjectId) {
        String deleteAttendance = "DELETE FROM attendance WHERE subject_id = ?";
        String deleteSubject = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, MYSQL_USER, MYSQL_PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteAttendance);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteSubject)) {
                pstmt1.setInt(1, subjectId);
                pstmt1.executeUpdate();
                pstmt2.setInt(1, subjectId);
                int rows = pstmt2.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 