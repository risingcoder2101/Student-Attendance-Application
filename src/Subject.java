public class Subject {
    private int id;
    private String name;
    private String code;
    private int totalClasses;
    private int facultyId;

    public Subject(int id, String name, String code, int totalClasses, int facultyId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalClasses = totalClasses;
        this.facultyId = facultyId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public int getTotalClasses() { return totalClasses; }
    public int getFacultyId() { return facultyId; }
} 