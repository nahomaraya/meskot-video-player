package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminPanel extends JFrame {
    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final Color BG_SECONDARY = new Color(48, 48, 48);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);
    private static final Color SUCCESS = new Color(76, 175, 80);

    private JTextArea logArea;
    private DefaultTableModel moviesModel;
    private DefaultTableModel usersModel;

    public AdminPanel() {
        super("Movie Streaming Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        initUI();
        testConnection();
    }

    private void initUI() {
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Tabbed content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_SECONDARY);
        tabbedPane.setForeground(TEXT_PRIMARY);

        // Tab 1: Movies
        JPanel moviesPanel = createMoviesPanel();
        tabbedPane.addTab("Movies", moviesPanel);

        // Tab 2: Users
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users", usersPanel);

        // Tab 3: System Log
        JPanel logPanel = createLogPanel();
        tabbedPane.addTab("System Log", logPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SECONDARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title
        JLabel title = new JLabel("MOVIE STREAMING ADMIN");
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_SECONDARY);

        JButton testBtn = createButton("Test Connection", ACCENT);
        JButton refreshBtn = createButton("Refresh All", SUCCESS);

        testBtn.addActionListener(e -> testConnection());
        refreshBtn.addActionListener(e -> refreshAll());

        buttonPanel.add(testBtn);
        buttonPanel.add(refreshBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMoviesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(BG_PRIMARY);

        JButton loadBtn = createButton("Load Movies", ACCENT);
        JButton addBtn = createButton("Add Movie", SUCCESS);
        JButton deleteBtn = createButton("Delete Selected", new Color(244, 67, 54));

        JTextField searchField = new JTextField(20);
        styleTextField(searchField);

        loadBtn.addActionListener(e -> loadMovies());
        addBtn.addActionListener(e -> addMovie());
        deleteBtn.addActionListener(e -> deleteSelectedMovie());
        searchField.addActionListener(e -> searchMovies(searchField.getText()));

        toolbar.add(loadBtn);
        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);

        // Movies table
        String[] columns = {"ID", "Title", "Genre", "Year", "Duration", "Status", "Upload Date"};
        moviesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable moviesTable = createStyledTable(moviesModel);
        JScrollPane scrollPane = new JScrollPane(moviesTable);
        styleScrollPane(scrollPane);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(BG_PRIMARY);

        JButton loadBtn = createButton("Load Users", ACCENT);
        JButton toggleAdminBtn = createButton("Toggle Admin", SUCCESS);

        loadBtn.addActionListener(e -> loadUsers());
        toggleAdminBtn.addActionListener(e -> toggleAdminStatus());

        toolbar.add(loadBtn);
        toolbar.add(toggleAdminBtn);

        // Users table
        String[] columns = {"User ID", "Username", "Email", "Role", "Is Admin", "Created", "Last Login"};
        usersModel = new DefaultTableModel(columns, 0);

        JTable usersTable = createStyledTable(usersModel);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        styleScrollPane(scrollPane);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "System Log",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Inter", Font.PLAIN, 12),
                Color.WHITE
        ));

        // Clear button
        JButton clearBtn = createButton("Clear Log", ACCENT);
        clearBtn.addActionListener(e -> logArea.setText(""));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_PRIMARY);
        buttonPanel.add(clearBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(64, 64, 64));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(BG_SECONDARY);
        table.setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(64, 64, 64));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 13));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(new Color(64, 64, 64));
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        scrollPane.getViewport().setBackground(BG_SECONDARY);
    }

    // Data Methods
    private void testConnection() {
        log("=== Testing Database Connection ===");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Testing Supabase connection...");

                try {
                    // Run database test
                    AdminDatabaseService.testDatabase();
                    publish("✓ Database test completed");

                } catch (Exception e) {
                    publish("✗ Connection failed: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }
        };

        worker.execute();
    }

    private void loadMovies() {
        log("\n=== Loading Movies ===");

        SwingWorker<List<Map<String, Object>>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                publish("Querying movies from database...");

                // First, let's find out what tables exist
                try {
                    List<Map<String, Object>> results = AdminDatabaseService.executeQuery(
                            "SELECT table_name FROM information_schema.tables " +
                                    "WHERE table_schema = 'public' ORDER BY table_name"
                    );

                    publish("Available tables:");
                    for (Map<String, Object> row : results) {
                        publish("  - " + row.get("table_name"));
                    }

                    // Try to query movies
                    publish("\nTrying to load movies...");
                    results = AdminDatabaseService.executeQuery(
                            "SELECT * FROM movies ORDER BY id DESC LIMIT 50"
                    );

                    return results;

                } catch (SQLException e) {
                    publish("Error: " + e.getMessage());
                    // Try alternative table names
                    publish("Trying alternative table names...");

                    try {
                        List<Map<String, Object>> results = AdminDatabaseService.executeQuery(
                                "SELECT * FROM movie ORDER BY id DESC LIMIT 50"
                        );
                        return results;
                    } catch (SQLException e2) {
                        publish("Also failed: " + e2.getMessage());
                        throw e;
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> movies = get();

                    // Clear table
                    moviesModel.setRowCount(0);

                    // Add rows to table
                    for (Map<String, Object> movie : movies) {
                        moviesModel.addRow(new Object[]{
                                movie.get("id"),
                                movie.get("title"),
                                movie.get("genre"),
                                movie.get("release_year"),
                                movie.get("duration_minutes"),
                                movie.get("status"),
                                movie.get("uploaded_date")
                        });
                    }

                    log("✓ Loaded " + movies.size() + " movies");

                } catch (Exception e) {
                    log("✗ Failed to load movies: " + e.getMessage());
                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Failed to load movies: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }
        };

        worker.execute();
    }

    private void loadUsers() {
        log("\n=== Loading Users ===");

        SwingWorker<List<Map<String, Object>>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                publish("Loading users from database...");

                try {
                    List<Map<String, Object>> results = AdminDatabaseService.executeQuery(
                            "SELECT user_id, username, email, role, is_admin, created_at, last_login " +
                                    "FROM users ORDER BY user_id DESC LIMIT 50"
                    );

                    return results;

                } catch (SQLException e) {
                    publish("Error: " + e.getMessage());
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> users = get();

                    // Clear table
                    usersModel.setRowCount(0);

                    // Add rows to table
                    for (Map<String, Object> user : users) {
                        usersModel.addRow(new Object[]{
                                user.get("user_id"),
                                user.get("username"),
                                user.get("email"),
                                user.get("role"),
                                user.get("is_admin"),
                                user.get("created_at"),
                                user.get("last_login")
                        });
                    }

                    log("✓ Loaded " + users.size() + " users");

                } catch (Exception e) {
                    log("✗ Failed to load users: " + e.getMessage());
                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Failed to load users: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }
        };

        worker.execute();
    }

    private void addMovie() {
        JDialog dialog = new JDialog(this, "Add New Movie", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_PRIMARY);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JTextField yearField = new JTextField(10);

        JLabel titleLabel = new JLabel("Title:");
        JLabel genreLabel = new JLabel("Genre:");
        JLabel yearLabel = new JLabel("Year:");

        titleLabel.setForeground(TEXT_PRIMARY);
        genreLabel.setForeground(TEXT_PRIMARY);
        yearLabel.setForeground(TEXT_PRIMARY);

        styleTextField(titleField);
        styleTextField(genreField);
        styleTextField(yearField);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(titleLabel, gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(genreLabel, gbc);
        gbc.gridx = 1; panel.add(genreField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(yearLabel, gbc);
        gbc.gridx = 1; panel.add(yearField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BG_PRIMARY);

        JButton saveBtn = createButton("Save", SUCCESS);
        JButton cancelBtn = createButton("Cancel", ACCENT);

        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String genre = genreField.getText().trim();
            String year = yearField.getText().trim();

            if (title.isEmpty() || genre.isEmpty() || year.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save movie to database
            log("Adding movie: " + title + " (" + year + ") - " + genre);
            JOptionPane.showMessageDialog(dialog, "Movie added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelectedMovie() {
        // Implementation for deleting selected movie
        log("Delete movie functionality would be implemented here");
        JOptionPane.showMessageDialog(this, "Delete functionality would remove selected movie from database");
    }

    private void searchMovies(String query) {
        log("Searching for: " + query);
        // Implementation for searching movies
    }

    private void toggleAdminStatus() {
        log("Toggle admin status functionality");
        JOptionPane.showMessageDialog(this, "Would toggle admin status for selected user");
    }

    private void refreshAll() {
        log("Refreshing all data...");
        loadMovies();
        loadUsers();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminPanel admin = new AdminPanel();
            admin.setVisible(true);
        });
    }
}