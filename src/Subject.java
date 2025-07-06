public class Subject {
    private int id;
    private String name;
    private int attendanceLimit;
    
    public Subject(int id, String name, int attendanceLimit) {
        this.id = id;
        this.name = name;
        this.attendanceLimit = attendanceLimit;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getAttendanceLimit() {
        return attendanceLimit;
    }
    
    @Override
    public String toString() {
        return name + " (Limit: " + attendanceLimit + "%)";
    }
} 