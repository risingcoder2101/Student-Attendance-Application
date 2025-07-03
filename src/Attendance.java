import java.sql.Date;

public class Attendance {
    private int id;
    private int studentId;
    private int subjectId;
    private Date date;
    private String status;
    private int markedBy;

    public Attendance(int id, int studentId, int subjectId, Date date, String status, int markedBy) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        this.status = status;
        this.markedBy = markedBy;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public int getSubjectId() { return subjectId; }
    public Date getDate() { return date; }
    public String getStatus() { return status; }
    public int getMarkedBy() { return markedBy; }
} 