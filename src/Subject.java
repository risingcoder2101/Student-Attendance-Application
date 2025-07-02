public class Subject {
    private int id;
    private String name;
    private String code;
    private int totalClasses;
    
    public Subject(int id, String name, String code, int totalClasses) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalClasses = totalClasses;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
} 