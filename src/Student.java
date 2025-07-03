public class Student {
    private int id;
    private int userId;
    private String name;
    private String rollNumber;
    private String course;
    private String batch;

    public Student(int id, int userId, String name, String rollNumber, String course, String batch) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.course = course;
        this.batch = batch;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public String getCourse() { return course; }
    public String getBatch() { return batch; }
} 