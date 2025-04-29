package userdata;

import java.sql.*;
import java.util.*;

public class userdata {

    private String username;
    private String password;
    private String role;
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/auction";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Meenu*1234";
    
    public userdata(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getRole() {
        return role;
    }
    
    // Initialize database - creates tables if they don't exist
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create users table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL)";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                
                // Check if admin user exists, if not create it
                String checkAdminSQL = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
                ResultSet rs = stmt.executeQuery(checkAdminSQL);
                rs.next();
                
                if (rs.getInt(1) == 0) {
                    String insertAdminSQL = "INSERT INTO users (username, password, role) VALUES ('admin', 'admin', 'Admin')";
                    stmt.execute(insertAdminSQL);
                    System.out.println("Created default admin user.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Get database connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    // Static user authentication method
    public static userdata authenticateUser(String username, String password) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getString("password").equals(password)) {
                return new userdata(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Register new user
    public static boolean registerUser(String username, String password, String role) {
        // Initialize database if needed
        initializeDatabase();
        
        // Check if user already exists
        try (Connection conn = getConnection();
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            
            if (rs.getInt(1) > 0) {
                return false; // User already exists
            }
            
            // Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
                
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, role);
                
                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all users (for admin)
    public static ArrayList<userdata> getAllUsers() {
        ArrayList<userdata> users = new ArrayList<>();
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            
            while (rs.next()) {
                users.add(new userdata(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    // Delete user (for admin)
    public static boolean deleteUser(String username) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
            
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Update user (for admin)
    public static boolean updateUser(String username, String newPassword, String newRole) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE users SET password = ?, role = ? WHERE username = ?")) {
            
            pstmt.setString(1, newPassword);
            pstmt.setString(2, newRole);
            pstmt.setString(3, username);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}