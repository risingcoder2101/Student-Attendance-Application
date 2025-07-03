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

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
} 