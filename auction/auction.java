package auction;

import java.sql.*;
import java.util.*;

public class auction {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/auction";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Meenu*1234";

    public static class Item {
        private String name;
        private int category;
        private double startingPrice;
        private String certification;
        private double currentBid;
        private String currentBidder;

        public Item(String name, int category, double startingPrice, String certification) {
            this.name = name;
            this.category = category;
            this.startingPrice = startingPrice;
            this.certification = certification;
            this.currentBid = startingPrice;
            this.currentBidder = "";
        }

        public String getName() {
            return name;
        }
    
        public int getCategory() {
            return category;
        }
        
        public double getStartingPrice() {
            return startingPrice;
        }
        
        public String getCertification() {
            return certification;
        }
        
        public double getCurrentBid() {
            return currentBid;
        }
        
        public void setCurrentBid(double currentBid) {
            this.currentBid = currentBid;
        }
        
        public String getCurrentBidder() {
            return currentBidder;
        }
        
        public void setCurrentBidder(String currentBidder) {
            this.currentBidder = currentBidder;
        }

        // Calculate direct buy price for VIP 
        public double getDirectBuyPrice() {
            return startingPrice * 1.1;
        }
        
        // Check if item is certified
        public boolean isCertified() {
            return certification.equals("Certified");
        }
    }
    
    // Initialize database - creates tables if they don't exist
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Create items table if it doesn't exist
                String createItemsTableSQL = "CREATE TABLE IF NOT EXISTS items (" +
                        "name VARCHAR(100) PRIMARY KEY, " +
                        "category INT NOT NULL, " +
                        "startingPrice DOUBLE NOT NULL, " +
                        "certification VARCHAR(20) NOT NULL)";
                stmt.execute(createItemsTableSQL);
                
                // Create bids table if it doesn't exist
                String createBidsTableSQL = "CREATE TABLE IF NOT EXISTS bids (" +
                        "item_name VARCHAR(100) PRIMARY KEY, " +
                        "starting_price DOUBLE NOT NULL, " +
                        "bid_amount DOUBLE NOT NULL, " +
                        "price_sold_at DOUBLE NOT NULL, " +
                        "FOREIGN KEY (item_name) REFERENCES items(name) ON DELETE CASCADE)";
                stmt.execute(createBidsTableSQL);
                
                // Add sample items if the items table is empty
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
                rs.next();
                
                if (rs.getInt(1) == 0) {
                    String[] insertItems = {
                        "INSERT INTO items VALUES ('Vintage Watch', 3, 500.00, 'Certified')",
                        "INSERT INTO items VALUES ('Samsung TV', 1, 800.00, 'Not Certified')",
                        "INSERT INTO items VALUES ('Antique Chair', 10, 350.00, 'Certified')",
                        "INSERT INTO items VALUES ('Mountain Bike', 4, 250.00, 'Not Certified')",
                        "INSERT INTO items VALUES ('Guitar', 6, 400.00, 'Certified')"
                    };
                    
                    for (String query : insertItems) {
                        stmt.execute(query);
                    }
                    
                    System.out.println("Added sample items to the database.");
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
    
    public static ArrayList<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM items")) {
            
            while (rs.next()) {
                Item item = new Item(
                    rs.getString("name"),
                    rs.getInt("category"),
                    rs.getDouble("startingPrice"),
                    rs.getString("certification")
                );
                
                // Get current bid if exists
                double currentBid = getCurrentBid(item.getName());
                if (currentBid > 0) {
                    item.setCurrentBid(currentBid);
                }
                
                items.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all items: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }
    
    // Get items by category
    public static ArrayList<Item> getItemsByCategory(int category) {
        ArrayList<Item> categoryItems = new ArrayList<>();
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE category = ?")) {
            
            pstmt.setInt(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Item item = new Item(
                    rs.getString("name"),
                    rs.getInt("category"),
                    rs.getDouble("startingPrice"),
                    rs.getString("certification")
                );
                
                // Get current bid if exists
                double currentBid = getCurrentBid(item.getName());
                if (currentBid > 0) {
                    item.setCurrentBid(currentBid);
                }
                
                categoryItems.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error getting items by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categoryItems;
    }
    
    // Get items for black market (only certified items)
    public static ArrayList<Item> getBlackMarketItems() {
        ArrayList<Item> certifiedItems = new ArrayList<>();
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE certification = 'Certified'")) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Item item = new Item(
                    rs.getString("name"),
                    rs.getInt("category"),
                    rs.getDouble("startingPrice"),
                    rs.getString("certification")
                );
                
                // Get current bid if exists
                double currentBid = getCurrentBid(item.getName());
                if (currentBid > 0) {
                    item.setCurrentBid(currentBid);
                }
                
                certifiedItems.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error getting black market items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return certifiedItems;
    }
    
    // Place a bid
    public static boolean placeBid(String itemName, double bidAmount, String bidder) {
        if (bidAmount <= 0) {
            return false;
        }
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection()) {
            // Get current bid amount
            double currentBid = getCurrentBid(itemName);
            
            // Check if bid is higher than current bid
            if (bidAmount <= currentBid) {
                return false; // Bid is too low
            }
            
            // Get item's starting price
            double startingPrice = 0;
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT startingPrice FROM items WHERE name = ?")) {
                pstmt.setString(1, itemName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    startingPrice = rs.getDouble("startingPrice");
                } else {
                    return false; // Item not found
                }
            }
            
            // Check if bid exists for this item
            boolean bidExists = false;
            
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM bids WHERE item_name = ?")) {
                checkStmt.setString(1, itemName);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                bidExists = rs.getInt(1) > 0;
            }
            
            // Insert or update bid
            if (bidExists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE bids SET bid_amount = ?, price_sold_at = ? WHERE item_name = ?")) {
                    
                    updateStmt.setDouble(1, bidAmount);
                    updateStmt.setDouble(2, bidAmount);
                    updateStmt.setString(3, itemName);
                    
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO bids (item_name, starting_price, bid_amount, price_sold_at) VALUES (?, ?, ?, ?)")) {
                    
                    insertStmt.setString(1, itemName);
                    insertStmt.setDouble(2, startingPrice);
                    insertStmt.setDouble(3, bidAmount);
                    insertStmt.setDouble(4, bidAmount);
                    
                    insertStmt.executeUpdate();
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Error placing bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Direct purchase for VIP users (and remove from items list)
    public static boolean directPurchase(String itemName, String buyer) {
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection()) {
            // Check if item exists and get its price
            Item itemToBuy = null;
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE name = ?")) {
                pstmt.setString(1, itemName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    itemToBuy = new Item(
                        rs.getString("name"),
                        rs.getInt("category"),
                        rs.getDouble("startingPrice"),
                        rs.getString("certification")
                    );
                } else {
                    return false; // Item not found
                }
            }
            
            double directBuyPrice = itemToBuy.getDirectBuyPrice();
            
            // Check if bid exists for this item
            boolean bidExists = false;
            
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM bids WHERE item_name = ?")) {
                checkStmt.setString(1, itemName);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                bidExists = rs.getInt(1) > 0;
            }
            
            // Insert or update bid record
            if (bidExists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE bids SET bid_amount = ?, price_sold_at = ? WHERE item_name = ?")) {
                    
                    updateStmt.setDouble(1, directBuyPrice);
                    updateStmt.setDouble(2, directBuyPrice);
                    updateStmt.setString(3, itemName);
                    
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO bids (item_name, starting_price, bid_amount, price_sold_at) VALUES (?, ?, ?, ?)")) {
                    
                    insertStmt.setString(1, itemName);
                    insertStmt.setDouble(2, itemToBuy.getStartingPrice());
                    insertStmt.setDouble(3, directBuyPrice);
                    insertStmt.setDouble(4, directBuyPrice);
                    
                    insertStmt.executeUpdate();
                }
            }
            
            // Remove item from items list
            removeItemFromList(itemName);
            return true;
        } catch (SQLException e) {
            System.out.println("Error with direct purchase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Handle removal after purchase
    public static boolean removeItemAfterPurchase(String itemName) {
        return removeItemFromList(itemName);
    }
    
    private static boolean removeItemFromList(String itemName) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM items WHERE name = ?")) {
            
            pstmt.setString(1, itemName);
            int rowsAffected = pstmt.executeUpdate();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error removing item from list: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get current bid information for an item
    public static double getCurrentBid(String itemName) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT bid_amount FROM bids WHERE item_name = ?")) {
            
            pstmt.setString(1, itemName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("bid_amount");
            }
        } catch (SQLException e) {
            System.out.println("Error getting current bid: " + e.getMessage());
            e.printStackTrace();
        }

        // If no bid found, return starting price
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT startingPrice FROM items WHERE name = ?")) {
            
            pstmt.setString(1, itemName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("startingPrice");
            }
        } catch (SQLException e) {
            System.out.println("Error getting starting price: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    // Get all bids for admin view
    public static ArrayList<String> getAllBids() {
        ArrayList<String> bids = new ArrayList<>();
        
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bids")) {
            
            while (rs.next()) {
                String bidRecord = rs.getString("item_name") + "," + 
                                   rs.getDouble("starting_price") + "," + 
                                   rs.getDouble("bid_amount") + "," +
                                   rs.getDouble("price_sold_at");
                bids.add(bidRecord);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all bids: " + e.getMessage());
            e.printStackTrace();
        }
        
        return bids;
    }
    
    public static boolean addItem(String name, int category, double price, String certification) {
        // Initialize database if needed
        initializeDatabase();
        
        try (Connection conn = getConnection()) {
            // Check if item already exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM items WHERE name = ?")) {
                checkStmt.setString(1, name);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                
                if (rs.getInt(1) > 0) {
                    return false; // Item already exists
                }
            }
            
            // Insert new item
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO items (name, category, startingPrice, certification) VALUES (?, ?, ?, ?)")) {
                
                insertStmt.setString(1, name);
                insertStmt.setInt(2, category);
                insertStmt.setDouble(3, price);
                insertStmt.setString(4, certification);
                
                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error adding item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    } 
    
    // update item for admin
    public static boolean updateItem(String name, int newCategory, double newPrice, String newCertification) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE items SET category = ?, startingPrice = ?, certification = ? WHERE name = ?")) {
            
            pstmt.setInt(1, newCategory);
            pstmt.setDouble(2, newPrice);
            pstmt.setString(3, newCertification);
            pstmt.setString(4, name);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }  
    
    // Delete item (for admin)
    public static boolean deleteItem(String name) {
        return removeItemFromList(name);
    }
}