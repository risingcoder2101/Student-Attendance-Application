public class Faculty {
    private int id;
    private int userId;
    private String name;
    private String department;
    
    public Faculty(int id, int userId, String name, String department) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.department = department;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
} 