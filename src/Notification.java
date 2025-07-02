import java.util.Date;
import java.util.Properties;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private Date createdAt;
    
    public enum NotificationType {
        EMAIL,
        SMS
    }
    
    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
    
    public Notification(int id, int userId, String message, NotificationType type, NotificationStatus status, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // Send email notification
    public boolean sendEmail(String recipientEmail) {
        try {
            // This is a placeholder for actual email sending logic
            // In a real application, you would use JavaMail or a similar API
            
            System.out.println("Sending email to: " + recipientEmail);
            System.out.println("Subject: Attendance Alert");
            System.out.println("Message: " + this.message);
            
            // Simulate successful sending
            this.status = NotificationStatus.SENT;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            this.status = NotificationStatus.FAILED;
            return false;
        }
    }
    
    // Send SMS notification
    public boolean sendSMS(String phoneNumber) {
        try {
            // This is a placeholder for actual SMS sending logic
            // In a real application, you would use an SMS gateway service
            
            System.out.println("Sending SMS to: " + phoneNumber);
            System.out.println("Message: " + this.message);
            
            // Simulate successful sending
            this.status = NotificationStatus.SENT;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            this.status = NotificationStatus.FAILED;
            return false;
        }
    }
} 