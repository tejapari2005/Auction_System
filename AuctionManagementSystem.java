import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.sql.*;

// Import the package classes
import userdata.userdata;
import auction.auction;
import auction.auction.Item;

public class AuctionManagementSystem extends JFrame {
    
    // Current logged in user
    private userdata currentUser = null;
    
    // Components for login panel
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    // Components for main panel
    private JPanel mainPanel;
    private JPanel adminPanel;
    private JPanel userPanel;
    private JPanel vipPanel;
    private JTabbedPane mainTabbedPane;
    
    // Database parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/auction";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Meenu*1234";

    public AuctionManagementSystem() {
        setTitle("Auction Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize the database
        initializeDatabase();
        
        // Create login panel
        createLoginPanel();
        
        // Set the login panel as the initial view
        setContentPane(loginPanel);
        
        setVisible(true);
    }
    
    private void initializeDatabase() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Initialize the userdata and auction tables
            userdata.initializeDatabase();
            auction.initializeDatabase();
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL JDBC Driver not found. Please add it to your project.", 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database initialization error: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void createLoginPanel() {
        loginPanel = new JPanel(new BorderLayout());
        
        // North panel with title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Auction System Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        loginPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel with login fields
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        centerPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        centerPanel.add(passwordField, gbc);
        
        loginPanel.add(centerPanel, BorderLayout.CENTER);
        
        // South panel with buttons
        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        loginPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentUser = userdata.authenticateUser(username, password);
        
        if (currentUser != null) {
            // Login successful
            JOptionPane.showMessageDialog(this, "Login successful. Welcome " + currentUser.getUsername() + "!");
            createMainPanel();
            setContentPane(mainPanel);
            revalidate();
            repaint();
        } else {
            // Login failed
            JOptionPane.showMessageDialog(this, "Invalid username or password.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(this, "Register New User", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JLabel roleLabel = new JLabel("Role:");
        
        String[] roles = {"Regular", "VIP"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(registerDialog, "Please enter both username and password.", 
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (userdata.registerUser(username, password, role)) {
                    JOptionPane.showMessageDialog(registerDialog, "Registration successful! You can now login.", 
                        "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    registerDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(registerDialog, "Registration failed. Username may already exist.", 
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerDialog.dispose();
            }
        });
        
        registerDialog.add(panel);
        registerDialog.setVisible(true);
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Create top panel with logout button and user info
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentUser = null;
                setContentPane(loginPanel);
                usernameField.setText("");
                passwordField.setText("");
                revalidate();
                repaint();
            }
        });
        
        JPanel logoutPanel = new JPanel();
        logoutPanel.add(logoutButton);
        topPanel.add(logoutPanel, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for different panels
        mainTabbedPane = new JTabbedPane();
        
        // Create panels based on user role
        if (currentUser.getRole().equals("Admin")) {
            createAdminPanel();
            mainTabbedPane.addTab("User Management", adminPanel);
            
            JPanel itemManagementPanel = createItemManagementPanel();
            mainTabbedPane.addTab("Item Management", itemManagementPanel);
            
            JPanel bidManagementPanel = createBidManagementPanel();
            mainTabbedPane.addTab("Bid Management", bidManagementPanel);
        }
        
        // All users can see the auction items
        createUserPanel();
        mainTabbedPane.addTab("Browse Items", userPanel);
        
        // Only VIP users can see the direct buy panel
        if (currentUser.getRole().equals("VIP")) {
            createVipPanel();
            mainTabbedPane.addTab("VIP Direct Buy", vipPanel);
        }
        
        // Black market items for certified items only
        JPanel blackMarketPanel = createBlackMarketPanel();
        mainTabbedPane.addTab("Certified Items", blackMarketPanel);
        
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void createAdminPanel() {
        adminPanel = new JPanel(new BorderLayout());
        
        // Top panel with new user button
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addUserButton = new JButton("Add New User");
        
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
        
        topButtonPanel.add(addUserButton);
        adminPanel.add(topButtonPanel, BorderLayout.NORTH);
        
        // Create table to display users
        String[] columns = {"Username", "Password", "Role", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only actions column is editable
            }
        };
        
        JTable userTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(userTable);
        adminPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add users to the table
        refreshUserTable(model);
        
        // Add buttons to each row in the table
        Action deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String username = (String) model.getValueAt(modelRow, 0);
                
                if (username.equals("admin")) {
                    JOptionPane.showMessageDialog(adminPanel, "Cannot delete admin user.", 
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (username.equals(currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(adminPanel, "Cannot delete yourself.", 
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(adminPanel, 
                    "Are you sure you want to delete user: " + username + "?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userdata.deleteUser(username)) {
                        refreshUserTable(model);
                    } else {
                        JOptionPane.showMessageDialog(adminPanel, "Failed to delete user.", 
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        
        Action editAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String username = (String) model.getValueAt(modelRow, 0);
                String password = (String) model.getValueAt(modelRow, 1);
                String role = (String) model.getValueAt(modelRow, 2);
                
                showEditUserDialog(username, password, role, model);
            }
        };
        
        // Create button column
        ButtonColumn editButtonColumn = new ButtonColumn(userTable, editAction, 3, "Edit");
        ButtonColumn deleteButtonColumn = new ButtonColumn(userTable, deleteAction, 3, "Delete");
    }
    
    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<userdata> users = userdata.getAllUsers();
        
        for (userdata user : users) {
            model.addRow(new Object[]{
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                "Edit/Delete"
            });
        }
    }
    
    private void showEditUserDialog(String username, String currentPassword, String currentRole, DefaultTableModel model) {
        JDialog editDialog = new JDialog(this, "Edit User: " + username, true);
        editDialog.setSize(400, 250);
        editDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(username);
        usernameField.setEditable(false);
        
        JLabel passwordLabel = new JLabel("New Password:");
        JPasswordField passwordField = new JPasswordField(currentPassword);
        
        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Regular", "VIP", "Admin"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setSelectedItem(currentRole);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = new String(passwordField.getPassword());
                String newRole = (String) roleComboBox.getSelectedItem();
                
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Password cannot be empty.", 
                        "Edit Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (userdata.updateUser(username, newPassword, newRole)) {
                    JOptionPane.showMessageDialog(editDialog, "User updated successfully.", 
                        "Edit Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshUserTable(model);
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Failed to update user.", 
                        "Edit Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editDialog.dispose();
            }
        });
        
        editDialog.add(panel);
        editDialog.setVisible(true);
    }
    
    private JPanel createItemManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with add item button
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItemButton = new JButton("Add New Item");
        
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddItemDialog();
            }
        });
        
        topButtonPanel.add(addItemButton);
        panel.add(topButtonPanel, BorderLayout.NORTH);
        
        // Create table to display items
        String[] columns = {"Name", "Category", "Starting Price", "Certification", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        JTable itemTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add items to the table
        refreshItemTable(model);
        
        // Add buttons to each row in the table
        Action deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String itemName = (String) model.getValueAt(modelRow, 0);
                
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Are you sure you want to delete item: " + itemName + "?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (auction.deleteItem(itemName)) {
                        refreshItemTable(model);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Failed to delete item.", 
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        
        Action editAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String itemName = (String) model.getValueAt(modelRow, 0);
                int category = Integer.parseInt(model.getValueAt(modelRow, 1).toString());
                double price = Double.parseDouble(model.getValueAt(modelRow, 2).toString());
                String certification = (String) model.getValueAt(modelRow, 3);
                
                showEditItemDialog(itemName, category, price, certification, model);
            }
        };
        
        // Create button column
        ButtonColumn editButtonColumn = new ButtonColumn(itemTable, editAction, 4, "Edit");
        ButtonColumn deleteButtonColumn = new ButtonColumn(itemTable, deleteAction, 4, "Delete");
        
        return panel;
    }
    
    private void refreshItemTable(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<Item> items = auction.getAllItems();
        
        for (Item item : items) {
            model.addRow(new Object[]{
                item.getName(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCertification(),
                "Edit/Delete"
            });
        }
    }
    
    private void showAddItemDialog() {
        JDialog addDialog = new JDialog(this, "Add New Item", true);
        addDialog.setSize(400, 300);
        addDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel nameLabel = new JLabel("Item Name:");
        JTextField nameField = new JTextField(20);
        
        JLabel categoryLabel = new JLabel("Category (1-10):");
        JSpinner categorySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        
        JLabel priceLabel = new JLabel("Starting Price:");
        JTextField priceField = new JTextField(20);
        
        JLabel certificationLabel = new JLabel("Certification:");
        String[] certifications = {"Certified", "Not Certified"};
        JComboBox<String> certificationComboBox = new JComboBox<>(certifications);
        
        JButton addButton = new JButton("Add Item");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(categoryLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(categorySpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(priceLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(priceField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(certificationLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(certificationComboBox, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                int category = (Integer) categorySpinner.getValue();
                String priceText = priceField.getText();
                String certification = (String) certificationComboBox.getSelectedItem();
                
                if (name.isEmpty() || priceText.isEmpty()) {
                    JOptionPane.showMessageDialog(addDialog, "Please fill in all fields.", 
                        "Add Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price;
                try {
                    price = Double.parseDouble(priceText);
                    if (price <= 0) {
                        JOptionPane.showMessageDialog(addDialog, "Price must be greater than zero.", 
                            "Add Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addDialog, "Invalid price format.", 
                        "Add Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (auction.addItem(name, category, price, certification)) {
                    JOptionPane.showMessageDialog(addDialog, "Item added successfully.", 
                        "Add Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh item tables in all relevant panels
                    DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) mainTabbedPane.getComponentAt(mainTabbedPane.indexOfTab("Item Management"))).getViewport().getView()).getModel();
                    refreshItemTable(model);
                    
                    addDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Failed to add item. Name may already exist.", 
                        "Add Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDialog.dispose();
            }
        });
        
        addDialog.add(panel);
        addDialog.setVisible(true);
    }
    
    private void showEditItemDialog(String itemName, int currentCategory, double currentPrice, 
            String currentCertification, DefaultTableModel model) {
        JDialog editDialog = new JDialog(this, "Edit Item: " + itemName, true);
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel nameLabel = new JLabel("Item Name:");
        JTextField nameField = new JTextField(itemName);
        nameField.setEditable(false);
        
        JLabel categoryLabel = new JLabel("Category (1-10):");
        JSpinner categorySpinner = new JSpinner(new SpinnerNumberModel(currentCategory, 1, 10, 1));
        
        JLabel priceLabel = new JLabel("Starting Price:");
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        
        JLabel certificationLabel = new JLabel("Certification:");
        String[] certifications = {"Certified", "Not Certified"};
        JComboBox<String> certificationComboBox = new JComboBox<>(certifications);
        certificationComboBox.setSelectedItem(currentCertification);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(categoryLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(categorySpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(priceLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(priceField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(certificationLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(certificationComboBox, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int newCategory = (Integer) categorySpinner.getValue();
                String priceText = priceField.getText();
                String newCertification = (String) certificationComboBox.getSelectedItem();
                
                if (priceText.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Please fill in all fields.", 
                        "Edit Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double newPrice;
                try {
                    newPrice = Double.parseDouble(priceText);
                    if (newPrice <= 0) {
                        JOptionPane.showMessageDialog(editDialog, "Price must be greater than zero.", 
                            "Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Invalid price format.", 
                        "Edit Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (auction.updateItem(itemName, newCategory, newPrice, newCertification)) {
                    JOptionPane.showMessageDialog(editDialog, "Item updated successfully.", 
                        "Edit Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshItemTable(model);
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Failed to update item.", 
                        "Edit Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editDialog.dispose();
            }
        });
        
        editDialog.add(panel);
        editDialog.setVisible(true);
    }
    
    private JPanel createBidManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table to display bids
        String[] columns = {"Item Name", "Starting Price", "Current Bid", "Final Price"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        JTable bidTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bidTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Bids");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBidTable(model);
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.NORTH);
        
        // Add bids to the table
        refreshBidTable(model);
        
        return panel;
    }
    
    private void refreshBidTable(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<String> bids = auction.getAllBids();
        
        for (String bidRecord : bids) {
            String[] bidData = bidRecord.split(",");
            model.addRow(new Object[]{
                bidData[0],  // Item name
                bidData[1],  // Starting price
                bidData[2],  // Current bid
                bidData[3]   // Final price
            });
        }
    }
    
    private void createUserPanel() {
        userPanel = new JPanel(new BorderLayout());
        
        // Create filters for categories
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter by Category"));
        
        String[] categories = {"All Categories", "1 - Electronics", "2 - Furniture", "3 - Watches", 
                              "4 - Sports", "5 - Books", "6 - Music", "7 - Art", 
                              "8 - Clothing", "9 - Jewelry", "10 - Collectibles"};
        JComboBox<String> categoryComboBox = new JComboBox<>(categories);
        
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryComboBox);
        
        JButton filterButton = new JButton("Apply Filter");
        filterPanel.add(filterButton);
        
        userPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Create table to display items
        String[] columns = {"Name", "Category", "Starting Price", "Current Bid", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        JTable itemTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        userPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add items to the table
        refreshItemTableForBidding(model, 0); // 0 = all categories
        
        // Add bid button to each row
        Action bidAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String itemName = (String) model.getValueAt(modelRow, 0);
                double currentBid = Double.parseDouble(model.getValueAt(modelRow, 3).toString());
                
                showBidDialog(itemName, currentBid, model);
            }
        };
        
        ButtonColumn bidButtonColumn = new ButtonColumn(itemTable, bidAction, 4, "Place Bid");
        
        // Add filter action
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = categoryComboBox.getSelectedIndex();
                refreshItemTableForBidding(model, selectedIndex);
            }
        });
    }
    
    private void refreshItemTableForBidding(DefaultTableModel model, int categoryFilter) {
        model.setRowCount(0);
        ArrayList<Item> items;
        
        if (categoryFilter == 0) {
            // All categories
            items = auction.getAllItems();
        } else {
            // Specific category
            items = auction.getItemsByCategory(categoryFilter);
        }
        
        for (Item item : items) {
            model.addRow(new Object[]{
                item.getName(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCurrentBid(),
                "Bid"
            });
        }
    }
    
    private void showBidDialog(String itemName, double currentBid, DefaultTableModel model) {
        JDialog bidDialog = new JDialog(this, "Place Bid for: " + itemName, true);
        bidDialog.setSize(400, 200);
        bidDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel currentBidLabel = new JLabel("Current Bid: $" + currentBid);
        JLabel newBidLabel = new JLabel("Your Bid:");
        JTextField bidField = new JTextField(20);
        bidField.setText(String.valueOf(currentBid + 10)); // Default to current bid + 10
        
        JButton bidButton = new JButton("Place Bid");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(currentBidLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(newBidLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(bidField, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bidButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bidText = bidField.getText();
                
                if (bidText.isEmpty()) {
                    JOptionPane.showMessageDialog(bidDialog, "Please enter a bid amount.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double bidAmount;
                try {
                    bidAmount = Double.parseDouble(bidText);
                    if (bidAmount <= currentBid) {
                        JOptionPane.showMessageDialog(bidDialog, "Bid must be higher than current bid.", 
                            "Bid Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(bidDialog, "Invalid bid amount.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (auction.placeBid(itemName, bidAmount, currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(bidDialog, "Bid placed successfully.", 
                        "Bid Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshItemTableForBidding(model, 0);
                    bidDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(bidDialog, "Failed to place bid.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bidDialog.dispose();
            }
        });
        
        bidDialog.add(panel);
        bidDialog.setVisible(true);
    }
    
    private void createVipPanel() {
        vipPanel = new JPanel(new BorderLayout());
        
        // Create explanation label
        JLabel explanationLabel = new JLabel("<html><body>As a VIP user, you can directly purchase items at 10% above the starting price.<br>Direct purchase removes the item from the auction immediately.</body></html>");
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        vipPanel.add(explanationLabel, BorderLayout.NORTH);
        
        // Create table to display items
        String[] columns = {"Name", "Category", "Starting Price", "Direct Buy Price", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        JTable itemTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        vipPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add items to the table
        refreshItemTableForDirectBuy(model);
        
        // Add buy button to each row
        Action buyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String itemName = (String) model.getValueAt(modelRow, 0);
                double directBuyPrice = Double.parseDouble(model.getValueAt(modelRow, 3).toString());
                
                int confirm = JOptionPane.showConfirmDialog(vipPanel, 
                    "Are you sure you want to buy " + itemName + " for $" + directBuyPrice + "?", 
                    "Confirm Purchase", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (auction.directPurchase(itemName, currentUser.getUsername())) {
                        JOptionPane.showMessageDialog(vipPanel, "Item purchased successfully!", 
                            "Purchase Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshItemTableForDirectBuy(model);
                        
                        // Also refresh the regular items table
                        DefaultTableModel regularModel = (DefaultTableModel) ((JTable) ((JScrollPane) 
                            userPanel.getComponent(1)).getViewport().getView()).getModel();
                        refreshItemTableForBidding(regularModel, 0);
                    } else {
                        JOptionPane.showMessageDialog(vipPanel, "Failed to purchase item.", 
                            "Purchase Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        
        ButtonColumn buyButtonColumn = new ButtonColumn(itemTable, buyAction, 4, "Buy Now");
    }
    
    private void refreshItemTableForDirectBuy(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<Item> items = auction.getAllItems();
        
        for (Item item : items) {
            model.addRow(new Object[]{
                item.getName(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getDirectBuyPrice(),
                "Buy Now"
            });
        }
    }
    
    private JPanel createBlackMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create explanation label
        JLabel explanationLabel = new JLabel("<html><body>Certified Items Section - These items have been verified for authenticity.</body></html>");
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(explanationLabel, BorderLayout.NORTH);
        
        // Create table to display certified items
        String[] columns = {"Name", "Category", "Starting Price", "Current Bid", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        JTable itemTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add certified items to the table
        refreshCertifiedItemsTable(model);
        
        // Add bid button to each row
        Action bidAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                String itemName = (String) model.getValueAt(modelRow, 0);
                double currentBid = Double.parseDouble(model.getValueAt(modelRow, 3).toString());
                
                showBidDialogCertified(itemName, currentBid, model);
            }
        };
        
        ButtonColumn bidButtonColumn = new ButtonColumn(itemTable, bidAction, 4, "Place Bid");
        
        return panel;
    }
    
    private void refreshCertifiedItemsTable(DefaultTableModel model) {
        model.setRowCount(0);
        ArrayList<Item> items = auction.getBlackMarketItems();
        
        for (Item item : items) {
            model.addRow(new Object[]{
                item.getName(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCurrentBid(),
                "Bid"
            });
        }
    }
    
    private void showBidDialogCertified(String itemName, double currentBid, DefaultTableModel model) {
        JDialog bidDialog = new JDialog(this, "Place Bid for Certified Item: " + itemName, true);
        bidDialog.setSize(400, 200);
        bidDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel currentBidLabel = new JLabel("Current Bid: $" + currentBid);
        JLabel newBidLabel = new JLabel("Your Bid:");
        JTextField bidField = new JTextField(20);
        bidField.setText(String.valueOf(currentBid + 10)); // Default to current bid + 10
        
        JButton bidButton = new JButton("Place Bid");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(currentBidLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(newBidLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(bidField, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bidButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bidText = bidField.getText();
                
                if (bidText.isEmpty()) {
                    JOptionPane.showMessageDialog(bidDialog, "Please enter a bid amount.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double bidAmount;
                try {
                    bidAmount = Double.parseDouble(bidText);
                    if (bidAmount <= currentBid) {
                        JOptionPane.showMessageDialog(bidDialog, "Bid must be higher than current bid.", 
                            "Bid Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(bidDialog, "Invalid bid amount.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (auction.placeBid(itemName, bidAmount, currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(bidDialog, "Bid placed successfully.", 
                        "Bid Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshCertifiedItemsTable(model);
                    bidDialog.dispose();
                    
                    // Also refresh the regular items table
                    DefaultTableModel regularModel = (DefaultTableModel) ((JTable) ((JScrollPane) 
                        userPanel.getComponent(1)).getViewport().getView()).getModel();
                    refreshItemTableForBidding(regularModel, 0);
                } else {
                    JOptionPane.showMessageDialog(bidDialog, "Failed to place bid.", 
                        "Bid Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bidDialog.dispose();
            }
        });
        
        bidDialog.add(panel);
        bidDialog.setVisible(true);
    }
    
    // Helper class for adding buttons to table cells
    class ButtonColumn {
        private JTable table;
        private Action action;
        private int column;
        private String text;
        
        public ButtonColumn(JTable table, Action action, int column, String text) {
            this.table = table;
            this.action = action;
            this.column = column;
            this.text = text;
            
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(new ButtonRenderer());
            columnModel.getColumn(column).setCellEditor(new ButtonEditor());
            
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    
                    if (col == column && row >= 0) {
                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
                    }
                }
            });
        }
        
        class ButtonRenderer extends JButton implements TableCellRenderer {
            public ButtonRenderer() {
                setOpaque(true);
            }
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                setText(text);
                return this;
            }
        }
        
        class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
            private JButton button;
            
            public ButtonEditor() {
                button = new JButton();
                button.setOpaque(true);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int row = table.getSelectedRow();
                        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
                        fireEditingStopped();
                    }
                });
            }
            
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                button.setText(text);
                return button;
            }
            
            @Override
            public Object getCellEditorValue() {
                return text;
            }
        }
    }
    
    // Main method to run the application
    public static void main(String[] args) {
        try {
            // Set look and feel to system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AuctionManagementSystem();
            }
        });
    }
}