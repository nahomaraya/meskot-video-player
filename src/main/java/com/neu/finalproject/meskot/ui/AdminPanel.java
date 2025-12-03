package com.neu.finalproject.meskot.ui;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class AdminPanel extends JFrame {
    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final Color BG_SECONDARY = new Color(48, 48, 48);
    private static final Color BG_TERTIARY = new Color(64, 64, 64);
    private static final Color BORDER_SUBTLE = new Color(75, 75, 75);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color ERROR = new Color(244, 67, 54);

    private static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 20);
    private static final Font FONT_HEADING = new Font("Inter", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Inter", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Inter", Font.PLAIN, 12);
    private static final Font FONT_TABLE = new Font("Inter", Font.PLAIN, 13);

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public AdminPanel() {
        super("Movie Streaming Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
        loadInitialStats();
    }

    private void initUI() {
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_PRIMARY);

        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Main content area with 3 core tabs + 2 additional
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBackground(BG_SECONDARY);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(FONT_BODY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // CORE FUNCTION 1: Movie Management
        createMoviesTab();

        // CORE FUNCTION 2: User Management
        createUsersTab();

        // CORE FUNCTION 3: View Watch History
        createHistoryTab();

        // ADDITIONAL FUNCTION 1: Upload Management
        createUploadsTab();

        // ADDITIONAL FUNCTION 2: System Dashboard
        createDashboardTab();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE));
        statusBar.setPreferredSize(new Dimension(100, 25));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        statusLabel.setFont(FONT_SMALL);

        statusBar.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SECONDARY);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Title
        JLabel title = new JLabel("MOVIE STREAMING ADMIN PANEL");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        // Right: Time and refresh
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(BG_SECONDARY);

        JButton refreshBtn = createStyledButton("Refresh All", ACCENT, 100, 30);
        refreshBtn.addActionListener(e -> refreshAllData());

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(TEXT_SECONDARY);
        timeLabel.setFont(FONT_SMALL);

        Timer timer = new Timer(1000, evt -> {
            timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        timer.start();

        rightPanel.add(refreshBtn);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(timeLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // CORE FUNCTION 1: Movie Management
    private void createMoviesTab() {
        JPanel moviesPanel = new JPanel(new BorderLayout(10, 10));
        moviesPanel.setBackground(BG_PRIMARY);
        moviesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(BG_PRIMARY);

        JButton addMovieBtn = createStyledButton("Add Movie", SUCCESS, 100, 30);
        JButton editMovieBtn = createStyledButton("Edit Movie", ACCENT, 100, 30);
        JButton deleteMovieBtn = createStyledButton("Delete Movie", ERROR, 100, 30);
        JButton refreshBtn = createStyledButton("Refresh", ACCENT, 80, 30);

        JTextField searchField = new JTextField(20);
        styleTextField(searchField);

        toolbar.add(addMovieBtn);
        toolbar.add(editMovieBtn);
        toolbar.add(deleteMovieBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(refreshBtn);

        // Movie table
        String[] columns = {"ID", "Title", "Genre", "Year", "Duration", "Source", "Upload Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable moviesTable = createStyledTable(model);
        JScrollPane scrollPane = new JScrollPane(moviesTable);
        styleScrollPane(scrollPane);

        // Action listeners
        refreshBtn.addActionListener(e -> loadMoviesData(model));
        addMovieBtn.addActionListener(e -> showAddMovieDialog());
        editMovieBtn.addActionListener(e -> {
            int row = moviesTable.getSelectedRow();
            if (row >= 0) {
                showEditMovieDialog(model, row);
            } else {
                showError("Please select a movie to edit");
            }
        });
        deleteMovieBtn.addActionListener(e -> {
            int row = moviesTable.getSelectedRow();
            if (row >= 0) {
                deleteMovie(model, row);
            } else {
                showError("Please select a movie to delete");
            }
        });
        searchField.addActionListener(e -> searchMovies(searchField.getText(), model));

        moviesPanel.add(toolbar, BorderLayout.NORTH);
        moviesPanel.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Movie Management", moviesPanel);
    }

    // CORE FUNCTION 2: User Management
    private void createUsersTab() {
        JPanel usersPanel = new JPanel(new BorderLayout(10, 10));
        usersPanel.setBackground(BG_PRIMARY);
        usersPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(BG_PRIMARY);

        JButton toggleAdminBtn = createStyledButton("Toggle Admin", ACCENT, 120, 30);
        JButton banUserBtn = createStyledButton("Ban User", ERROR, 100, 30);
        JButton refreshBtn = createStyledButton("Refresh", ACCENT, 80, 30);

        toolbar.add(toggleAdminBtn);
        toolbar.add(banUserBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(refreshBtn);

        // Users table
        String[] columns = {"User ID", "Username", "Email", "Role", "Admin", "Created", "Last Login", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable usersTable = createStyledTable(model);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        styleScrollPane(scrollPane);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBackground(BG_PRIMARY);

        JLabel totalLabel = new JLabel("Total Users: 0");
        JLabel adminLabel = new JLabel("Admins: 0");
        JLabel activeLabel = new JLabel("Active Today: 0");

        for (JLabel label : new JLabel[]{totalLabel, adminLabel, activeLabel}) {
            label.setFont(FONT_BODY);
            label.setForeground(TEXT_SECONDARY);
            statsPanel.add(label);
        }

        // Action listeners
        refreshBtn.addActionListener(e -> loadUsersData(model, totalLabel, adminLabel, activeLabel));
        toggleAdminBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row >= 0) {
                toggleAdminStatus(model, row);
            } else {
                showError("Please select a user");
            }
        });
        banUserBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row >= 0) {
                toggleBanStatus(model, row);
            } else {
                showError("Please select a user");
            }
        });

        usersPanel.add(toolbar, BorderLayout.NORTH);
        usersPanel.add(scrollPane, BorderLayout.CENTER);
        usersPanel.add(statsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("User Management", usersPanel);
    }

    // CORE FUNCTION 3: View Watch History
    private void createHistoryTab() {
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBackground(BG_PRIMARY);
        historyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(BG_PRIMARY);

        JComboBox<String> timeFilter = new JComboBox<>(new String[]{"Last 24 hours", "Last 7 days", "Last 30 days", "All time"});
        JComboBox<String> userFilter = new JComboBox<>(new String[]{"All Users"});
        JButton refreshBtn = createStyledButton("Refresh", ACCENT, 80, 30);

        styleComboBox(timeFilter);
        styleComboBox(userFilter);

        filterPanel.add(new JLabel("Time Period:"));
        filterPanel.add(timeFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("User:"));
        filterPanel.add(userFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(refreshBtn);

        // History table
        String[] columns = {"User", "Movie", "Watched At", "Watch Duration", "Completion %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable historyTable = createStyledTable(model);

        // Add progress bar for completion %
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 4 && value instanceof Integer) {
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue((Integer) value);
                    progressBar.setString(value + "%");
                    progressBar.setStringPainted(true);
                    progressBar.setBackground(BG_TERTIARY);
                    progressBar.setForeground(ACCENT);
                    progressBar.setBorderPainted(false);
                    return progressBar;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        styleScrollPane(scrollPane);

        // Stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBackground(BG_PRIMARY);

        JLabel totalLabel = new JLabel("Total Views: 0");
        JLabel avgDurationLabel = new JLabel("Avg Duration: 0 min");
        JLabel uniqueUsersLabel = new JLabel("Unique Users: 0");

        for (JLabel label : new JLabel[]{totalLabel, avgDurationLabel, uniqueUsersLabel}) {
            label.setFont(FONT_BODY);
            label.setForeground(TEXT_SECONDARY);
            statsPanel.add(label);
        }

        refreshBtn.addActionListener(e -> loadWatchHistory(model, totalLabel, avgDurationLabel, uniqueUsersLabel));

        historyPanel.add(filterPanel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(statsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Watch History", historyPanel);
    }

    // ADDITIONAL FUNCTION 1: Upload Management
    private void createUploadsTab() {
        JPanel uploadsPanel = new JPanel(new BorderLayout(10, 10));
        uploadsPanel.setBackground(BG_PRIMARY);
        uploadsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(BG_PRIMARY);

        JButton retryBtn = createStyledButton("Retry Failed", ACCENT, 120, 30);
        JButton cancelBtn = createStyledButton("Cancel Upload", ERROR, 120, 30);
        JButton refreshBtn = createStyledButton("Refresh", ACCENT, 80, 30);

        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "UPLOADING", "ENCODING", "COMPLETED", "FAILED"});
        styleComboBox(statusFilter);

        toolbar.add(retryBtn);
        toolbar.add(cancelBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(new JLabel("Status:"));
        toolbar.add(statusFilter);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(refreshBtn);

        // Uploads table
        String[] columns = {"Job ID", "User", "Movie", "File Size", "Status", "Progress", "Started", "Completed"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable uploadsTable = createStyledTable(model);

        // Progress bar renderer
        uploadsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 5 && value instanceof Integer) {
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue((Integer) value);
                    progressBar.setString(value + "%");
                    progressBar.setStringPainted(true);
                    progressBar.setBackground(BG_TERTIARY);
                    progressBar.setForeground(ACCENT);
                    progressBar.setBorderPainted(false);
                    return progressBar;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane scrollPane = new JScrollPane(uploadsTable);
        styleScrollPane(scrollPane);

        refreshBtn.addActionListener(e -> loadUploadsData(model));
        statusFilter.addActionListener(e -> filterUploadsByStatus((String) statusFilter.getSelectedItem(), model));

        uploadsPanel.add(toolbar, BorderLayout.NORTH);
        uploadsPanel.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Upload Management", uploadsPanel);
    }

    // ADDITIONAL FUNCTION 2: System Dashboard
    private void createDashboardTab() {
        JPanel dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBackground(BG_PRIMARY);
        dashboardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top stats
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setBackground(BG_PRIMARY);
        statsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Create 6 stat cards
        String[] statTitles = {"Total Movies", "Total Users", "Today's Views",
                "Storage Used", "Active Uploads", "Avg Rating"};
        for (String title : statTitles) {
            statsPanel.add(createStatCard(title, "0"));
        }

        // Recent activity
        JPanel activityPanel = new JPanel(new BorderLayout(10, 10));
        activityPanel.setBackground(BG_PRIMARY);

        JLabel activityLabel = new JLabel("Recent Activity");
        activityLabel.setFont(FONT_HEADING);
        activityLabel.setForeground(TEXT_PRIMARY);

        String[] activityColumns = {"Time", "User", "Action", "Details"};
        DefaultTableModel activityModel = new DefaultTableModel(activityColumns, 0);
        JTable activityTable = createStyledTable(activityModel);
        JScrollPane activityScroll = new JScrollPane(activityTable);
        styleScrollPane(activityScroll);

        activityPanel.add(activityLabel, BorderLayout.NORTH);
        activityPanel.add(activityScroll, BorderLayout.CENTER);

        dashboardPanel.add(statsPanel, BorderLayout.NORTH);
        dashboardPanel.add(activityPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Dashboard", dashboardPanel);
    }

    // Helper Methods
    private JButton createStyledButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
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
        table.setFont(FONT_TABLE);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(BG_TERTIARY);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(BG_SECONDARY);
        table.setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(FONT_BODY);
        table.getTableHeader().setBackground(BG_TERTIARY);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        scrollPane.getVerticalScrollBar().setBackground(BG_TERTIARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_TERTIARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(BG_TERTIARY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_HEADING);
        valueLabel.setForeground(TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // Data Loading Methods
    private void loadInitialStats() {
        updateStatus("Loading initial data...");
        SwingUtilities.invokeLater(() -> {
            updateStatus("Admin panel ready");
        });
    }

    private void refreshAllData() {
        updateStatus("Refreshing all data...");
        // Refresh each tab
        int selectedIndex = tabbedPane.getSelectedIndex();
        String title = tabbedPane.getTitleAt(selectedIndex);

        switch (title) {
            case "Movie Management":
                // Refresh movie table
                break;
            case "User Management":
                // Refresh user table
                break;
            case "Watch History":
                // Refresh history
                break;
            case "Upload Management":
                // Refresh uploads
                break;
            case "Dashboard":
                // Refresh dashboard
                break;
        }
        updateStatus("Refresh completed");
    }

    private void loadMoviesData(DefaultTableModel model) {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT id, title, genre, release_year, duration_minutes, source_type, uploaded_date, status " +
                                "FROM movies ORDER BY id DESC LIMIT 100"
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> movies = get();
                    model.setRowCount(0);
                    for (Map<String, Object> movie : movies) {
                        model.addRow(new Object[]{
                                movie.get("id"),
                                movie.get("title"),
                                movie.get("genre"),
                                movie.get("release_year"),
                                movie.get("duration_minutes"),
                                movie.get("source_type"),
                                movie.get("uploaded_date"),
                                movie.get("status")
                        });
                    }
                    updateStatus("Loaded " + movies.size() + " movies");
                } catch (Exception e) {
                    updateStatus("Error loading movies: " + e.getMessage());
                    showError("Failed to load movies: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadUsersData(DefaultTableModel model, JLabel totalLabel, JLabel adminLabel, JLabel activeLabel) {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT user_id, username, email, role, is_admin, created_at, last_login, " +
                                "CASE WHEN is_banned THEN 'Banned' ELSE 'Active' END as status " +
                                "FROM users ORDER BY user_id DESC LIMIT 100"
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> users = get();
                    model.setRowCount(0);
                    int adminCount = 0;
                    for (Map<String, Object> user : users) {
                        model.addRow(new Object[]{
                                user.get("user_id"),
                                user.get("username"),
                                user.get("email"),
                                user.get("role"),
                                user.get("is_admin"),
                                user.get("created_at"),
                                user.get("last_login"),
                                user.get("status")
                        });
                        if ("1".equals(user.get("is_admin").toString())) {
                            adminCount++;
                        }
                    }
                    totalLabel.setText("Total Users: " + users.size());
                    adminLabel.setText("Admins: " + adminCount);
                    updateStatus("Loaded " + users.size() + " users");
                } catch (Exception e) {
                    updateStatus("Error loading users: " + e.getMessage());
                    showError("Failed to load users: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadWatchHistory(DefaultTableModel model, JLabel totalLabel, JLabel avgDurationLabel, JLabel uniqueUsersLabel) {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT u.username, m.title, w.watched_at, w.watch_duration_minutes, " +
                                "ROUND((w.watch_duration_minutes * 100.0 / m.duration_minutes), 0) as completion " +
                                "FROM watch_history w " +
                                "JOIN users u ON w.user_id = u.user_id " +
                                "JOIN movies m ON w.movie_id = m.id " +
                                "ORDER BY w.watched_at DESC LIMIT 100"
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> history = get();
                    model.setRowCount(0);
                    int totalDuration = 0;
                    Set<String> uniqueUsers = new HashSet<>();

                    for (Map<String, Object> record : history) {
                        model.addRow(new Object[]{
                                record.get("username"),
                                record.get("title"),
                                record.get("watched_at"),
                                record.get("watch_duration_minutes"),
                                record.get("completion")
                        });
                        uniqueUsers.add(record.get("username").toString());
                        if (record.get("watch_duration_minutes") != null) {
                            totalDuration += Integer.parseInt(record.get("watch_duration_minutes").toString());
                        }
                    }

                    totalLabel.setText("Total Views: " + history.size());
                    avgDurationLabel.setText("Avg Duration: " + (history.isEmpty() ? "0" : totalDuration / history.size()) + " min");
                    uniqueUsersLabel.setText("Unique Users: " + uniqueUsers.size());
                    updateStatus("Loaded " + history.size() + " history records");
                } catch (Exception e) {
                    updateStatus("Error loading history: " + e.getMessage());
                    showError("Failed to load watch history: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadUploadsData(DefaultTableModel model) {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT uj.id as job_id, u.username, uh.movie_title, " +
                                "ROUND(uh.file_size_bytes / 1048576.0, 1) || ' MB' as file_size, " +
                                "uh.status, uh.progress, uh.uploaded_at, uh.completed_at " +
                                "FROM upload_history uh " +
                                "LEFT JOIN users u ON uh.user_id = u.user_id " +
                                "LEFT JOIN upload_job uj ON uh.job_id = uj.id " +
                                "ORDER BY uh.uploaded_at DESC LIMIT 100"
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> uploads = get();
                    model.setRowCount(0);
                    for (Map<String, Object> upload : uploads) {
                        model.addRow(new Object[]{
                                upload.get("job_id"),
                                upload.get("username"),
                                upload.get("movie_title"),
                                upload.get("file_size"),
                                upload.get("status"),
                                upload.get("progress"),
                                upload.get("uploaded_at"),
                                upload.get("completed_at")
                        });
                    }
                    updateStatus("Loaded " + uploads.size() + " upload records");
                } catch (Exception e) {
                    updateStatus("Error loading uploads: " + e.getMessage());
                    showError("Failed to load uploads: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // Dialog Methods
    private void showAddMovieDialog() {
        JDialog dialog = new JDialog(this, "Add New Movie", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_PRIMARY);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JTextField titleField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JTextField yearField = new JTextField(10);
        JTextField durationField = new JTextField(10);
        JTextField sourceField = new JTextField(20);

        JLabel[] labels = {
                new JLabel("Title:"), new JLabel("Genre:"), new JLabel("Year:"),
                new JLabel("Duration (min):"), new JLabel("Source Type:")
        };
        JTextField[] fields = {titleField, genreField, yearField, durationField, sourceField};

        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(TEXT_PRIMARY);
            labels[i].setFont(FONT_BODY);
            styleTextField(fields[i]);

            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(labels[i], gbc);

            gbc.gridx = 1;
            panel.add(fields[i], gbc);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BG_PRIMARY);

        JButton saveBtn = createStyledButton("Save", SUCCESS, 80, 30);
        JButton cancelBtn = createStyledButton("Cancel", ERROR, 80, 30);

        saveBtn.addActionListener(e -> {
            // Save movie logic here
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditMovieDialog(DefaultTableModel model, int row) {
        JOptionPane.showMessageDialog(this, "Edit movie functionality would be implemented here.");
    }

    private void deleteMovie(DefaultTableModel model, int row) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this movie?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Object movieId = model.getValueAt(row, 0);
            // Delete from database
            updateStatus("Movie deleted: ID " + movieId);
            model.removeRow(row);
        }
    }

    private void searchMovies(String query, DefaultTableModel model) {
        if (query.trim().isEmpty()) {
            loadMoviesData(model);
            return;
        }

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT id, title, genre, release_year, duration_minutes, source_type, uploaded_date, status " +
                                "FROM movies WHERE title ILIKE ? OR genre ILIKE ? ORDER BY id DESC LIMIT 100",
                        "%" + query + "%", "%" + query + "%"
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> movies = get();
                    model.setRowCount(0);
                    for (Map<String, Object> movie : movies) {
                        model.addRow(new Object[]{
                                movie.get("id"),
                                movie.get("title"),
                                movie.get("genre"),
                                movie.get("release_year"),
                                movie.get("duration_minutes"),
                                movie.get("source_type"),
                                movie.get("uploaded_date"),
                                movie.get("status")
                        });
                    }
                    updateStatus("Found " + movies.size() + " movies matching: " + query);
                } catch (Exception e) {
                    updateStatus("Search failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void toggleAdminStatus(DefaultTableModel model, int row) {
        Object userId = model.getValueAt(row, 0);
        Object currentStatus = model.getValueAt(row, 4);
        int newStatus = "1".equals(currentStatus.toString()) ? 0 : 1;

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return AdminDatabaseService.executeUpdate(
                        "UPDATE users SET is_admin = ? WHERE user_id = ?",
                        newStatus, userId
                );
            }

            @Override
            protected void done() {
                try {
                    int rows = get();
                    if (rows > 0) {
                        model.setValueAt(newStatus, row, 4);
                        updateStatus("Admin status updated for user: " + userId);
                    }
                } catch (Exception e) {
                    updateStatus("Failed to update admin status: " + e.getMessage());
                    showError("Failed to update admin status: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void toggleBanStatus(DefaultTableModel model, int row) {
        Object userId = model.getValueAt(row, 0);
        Object currentStatus = model.getValueAt(row, 7);
        boolean isBanned = "Banned".equals(currentStatus.toString());

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return AdminDatabaseService.executeUpdate(
                        "UPDATE users SET is_banned = ? WHERE user_id = ?",
                        !isBanned, userId
                );
            }

            @Override
            protected void done() {
                try {
                    int rows = get();
                    if (rows > 0) {
                        String newStatus = isBanned ? "Active" : "Banned";
                        model.setValueAt(newStatus, row, 7);
                        updateStatus("User " + (isBanned ? "unbanned" : "banned") + ": ID " + userId);
                    }
                } catch (Exception e) {
                    updateStatus("Failed to update ban status: " + e.getMessage());
                    showError("Failed to update ban status: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void filterUploadsByStatus(String status, DefaultTableModel model) {
        if ("All".equals(status)) {
            loadUploadsData(model);
            return;
        }

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return AdminDatabaseService.executeQuery(
                        "SELECT uj.id as job_id, u.username, uh.movie_title, " +
                                "ROUND(uh.file_size_bytes / 1048576.0, 1) || ' MB' as file_size, " +
                                "uh.status, uh.progress, uh.uploaded_at, uh.completed_at " +
                                "FROM upload_history uh " +
                                "LEFT JOIN users u ON uh.user_id = u.user_id " +
                                "LEFT JOIN upload_job uj ON uh.job_id = uj.id " +
                                "WHERE uh.status = ? ORDER BY uh.uploaded_at DESC LIMIT 100",
                        status
                );
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> uploads = get();
                    model.setRowCount(0);
                    for (Map<String, Object> upload : uploads) {
                        model.addRow(new Object[]{
                                upload.get("job_id"),
                                upload.get("username"),
                                upload.get("movie_title"),
                                upload.get("file_size"),
                                upload.get("status"),
                                upload.get("progress"),
                                upload.get("uploaded_at"),
                                upload.get("completed_at")
                        });
                    }
                    updateStatus("Loaded " + uploads.size() + " " + status + " uploads");
                } catch (Exception e) {
                    updateStatus("Error filtering uploads: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Main method to run the admin panel
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminPanel admin = new AdminPanel();
            admin.setVisible(true);
        });
    }
}