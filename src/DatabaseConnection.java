import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/attendance_db"
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=UTC"
        + "&cachePrepStmts=true"
        + "&useServerPrepStmts=true"
        + "&rewriteBatchedStatements=true"
        + "&prepStmtCacheSize=250"
        + "&prepStmtCacheSqlLimit=2048"
        + "&cacheResultSetMetadata=true"
        + "&cacheServerConfiguration=true"
        + "&elideSetAutoCommits=true"
        + "&maintainTimeStats=false";
    
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static final int MAX_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    private static final BlockingQueue<Connection> connectionPool;
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    
    static {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Initialize connection pool
            connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
            for (int i = 0; i < MAX_POOL_SIZE; i++) {
                try {
                    Connection conn = createConnection();
                    if (conn != null && !conn.isClosed()) {
                        connectionPool.offer(conn);
                    }
                } catch (SQLException e) {
                    System.err.println("Failed to create connection " + i + ": " + e.getMessage());
                }
            }
            
            // Start connection monitor
            startConnectionMonitor();
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }
    
    private static Connection createConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to create database connection: " + e.getMessage());
            throw e;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed() || !conn.isValid(1)) {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
                conn = createConnection();
            }
            activeConnections.incrementAndGet();
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Timeout waiting for database connection", e);
        }
    }
    
    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback(); // Rollback any uncommitted changes
                    if (conn.isValid(1)) {
                        connectionPool.offer(conn);
                    } else {
                        conn.close();
                        connectionPool.offer(createConnection());
                    }
                } else {
                    connectionPool.offer(createConnection());
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
                try {
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error closing invalid connection: " + ex.getMessage());
                }
            } finally {
                activeConnections.decrementAndGet();
            }
        }
    }
    
    private static void startConnectionMonitor() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(30000); // Check every 30 seconds
                        
                        // Refresh idle connections
                        int size = connectionPool.size();
                        for (int i = 0; i < size; i++) {
                            Connection conn = connectionPool.poll();
                            if (conn != null) {
                                try {
                                    if (!conn.isValid(1)) {
                                        conn.close();
                                        conn = createConnection();
                                    }
                                    connectionPool.offer(conn);
                                } catch (SQLException e) {
                                    System.err.println("Error refreshing connection: " + e.getMessage());
                                }
                            }
                        }
                        
                        // Log connection pool status
                        System.out.println("Connection pool status - Available: " + 
                            connectionPool.size() + ", Active: " + activeConnections.get());
                            
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                return null;
            }
        }.execute();
    }
    
    // Backup the database
    public static boolean backupDatabase(String backupPath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows
                pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "mysqldump",
                    "-u", USER,
                    "-p" + PASSWORD,
                    "--add-drop-database",
                    "--databases", "attendance_db",
                    ">", backupPath
                );
            } else {
                // Unix/Linux
                pb = new ProcessBuilder(
                    "mysqldump",
                    "-u", USER,
                    "-p" + PASSWORD,
                    "--add-drop-database",
                    "--databases", "attendance_db",
                    "--result-file=" + backupPath
                );
            }
            
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            System.err.println("Backup failed: " + e.getMessage());
            return false;
        }
    }
    
    // Restore the database
    public static boolean restoreDatabase(String backupPath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows
                pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "mysql",
                    "-u", USER,
                    "-p" + PASSWORD,
                    "attendance_db",
                    "<", backupPath
                );
            } else {
                // Unix/Linux
                pb = new ProcessBuilder(
                    "mysql",
                    "-u", USER,
                    "-p" + PASSWORD,
                    "attendance_db",
                    "<", backupPath
                );
            }
            
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            System.err.println("Restore failed: " + e.getMessage());
            return false;
        }
    }
    
    // Test database connection
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(1);
            return isValid;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }
} 