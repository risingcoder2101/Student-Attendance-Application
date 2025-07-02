public class Student {
    private int id;
    private String name;
    private String rollNumber;
    private String course;
    
    public Student(int id, String name, String rollNumber, String course) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
        this.course = course;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
} 