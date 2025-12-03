package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.UploadHistory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Admin Panel - Main UI for administrative functions
 * Features:
 * - Dashboard with statistics
 * - Upload history management
 * - Movie management
 * - User management (when backend supports it)
 */
public class AdminPanel extends JFrame {

    private final AdminApiService adminApiService;
    private final MovieApiService movieApiService;
    
    private final CardLayout cardLayout;
    private final JPanel mainContentPanel;
    
    // Dashboard components
    private JLabel totalUploadsLabel;
    private JLabel completedUploadsLabel;
    private JLabel failedUploadsLabel;
    private JLabel pendingUploadsLabel;
    private JLabel totalMoviesLabel;
    
    // Upload History Table
    private JTable uploadHistoryTable;
    private DefaultTableModel uploadHistoryModel;
    
    // Movie Management Table
    private JTable moviesTable;
    private DefaultTableModel moviesModel;
    
    // Color scheme matching VideoPlayerUI
    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final Color BG_SECONDARY = new Color(48, 48, 48);
    private static final Color BG_TERTIARY = new Color(64, 64, 64);
    private static final Color BORDER_SUBTLE = new Color(75, 75, 75);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);
    private static final Color ACCENT_HOVER = new Color(255, 160, 50);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private static final Color ERROR_RED = new Color(244, 67, 54);
    private static final Color WARNING_YELLOW = new Color(255, 193, 7);
    
    private static final String FONT_FAMILY = "Inter";
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 24);
    private static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 18);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);

    public AdminPanel() {
        this.adminApiService = new AdminApiService();
        this.movieApiService = new MovieApiService();
        this.cardLayout = new CardLayout();
        this.mainContentPanel = new JPanel(cardLayout);
        
        initializeUI();
        loadDashboardData();
    }

    private void initializeUI() {
        setTitle("Meskot Admin Panel");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PRIMARY);
        setLayout(new BorderLayout());

        // Create navigation sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Create main content area
        mainContentPanel.setBackground(BG_PRIMARY);
        
        // Add different panels
        mainContentPanel.add(createDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(createUploadHistoryPanel(), "UPLOADS");
        mainContentPanel.add(createMovieManagementPanel(), "MOVIES");
        mainContentPanel.add(createSettingsPanel(), "SETTINGS");
        
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Show dashboard by default
        cardLayout.show(mainContentPanel, "DASHBOARD");
    }

    // =========================================================================
    // SIDEBAR NAVIGATION
    // =========================================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Header
        JLabel headerLabel = new JLabel("Admin Panel");
        headerLabel.setFont(FONT_TITLE);
        headerLabel.setForeground(ACCENT);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        sidebar.add(headerLabel);

        // Navigation buttons
        sidebar.add(createNavButton("Dashboard", "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavButton("Upload History", "UPLOADS"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavButton("Movie Management", "MOVIES"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavButton("Settings", "SETTINGS"));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Refresh button at bottom
        JButton refreshButton = createActionButton("Refresh All Data");
        refreshButton.addActionListener(e -> refreshAllData());
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(refreshButton);

        return sidebar;
    }

    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(BG_TERTIARY);
        button.setMaximumSize(new Dimension(220, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            cardLayout.show(mainContentPanel, panelName);
            if ("UPLOADS".equals(panelName)) {
                loadUploadHistory();
            } else if ("MOVIES".equals(panelName)) {
                loadMovies();
            }
        });
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BG_TERTIARY);
            }
        });
        
        return button;
    }

    // =========================================================================
    // DASHBOARD PANEL
    // =========================================================================

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel title = new JLabel("Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setBackground(BG_PRIMARY);

        totalUploadsLabel = new JLabel("0");
        completedUploadsLabel = new JLabel("0");
        failedUploadsLabel = new JLabel("0");
        pendingUploadsLabel = new JLabel("0");
        totalMoviesLabel = new JLabel("0");

        statsPanel.add(createStatCard("Total Uploads", totalUploadsLabel, ACCENT));
        statsPanel.add(createStatCard("Completed", completedUploadsLabel, SUCCESS_GREEN));
        statsPanel.add(createStatCard("Failed", failedUploadsLabel, ERROR_RED));
        statsPanel.add(createStatCard("Pending", pendingUploadsLabel, WARNING_YELLOW));
        statsPanel.add(createStatCard("Total Movies", totalMoviesLabel, ACCENT));
        statsPanel.add(createStatCard("Active Users", new JLabel("N/A"), TEXT_SECONDARY));

        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        
        valueLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 36));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================================
    // UPLOAD HISTORY PANEL
    // =========================================================================

    private JPanel createUploadHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PRIMARY);
        
        JLabel title = new JLabel("Upload History");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JButton refreshButton = createActionButton("Refresh");
        refreshButton.addActionListener(e -> loadUploadHistory());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Job ID", "User ID", "Title", "Status", "Progress", "Upload Date", "Size"};
        uploadHistoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        uploadHistoryTable = new JTable(uploadHistoryModel);
        styleTable(uploadHistoryTable);
        
        JScrollPane scrollPane = new JScrollPane(uploadHistoryTable);
        scrollPane.setBackground(BG_SECONDARY);
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // MOVIE MANAGEMENT PANEL
    // =========================================================================

    private JPanel createMovieManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PRIMARY);
        
        JLabel title = new JLabel("Movie Management");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_PRIMARY);
        
        JButton refreshButton = createActionButton("Refresh");
        refreshButton.addActionListener(e -> loadMovies());
        buttonPanel.add(refreshButton);
        
        JButton deleteButton = createActionButton("Delete Selected");
        deleteButton.setBackground(ERROR_RED);
        deleteButton.addActionListener(e -> deleteSelectedMovie());
        buttonPanel.add(deleteButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Title", "Source Type", "Status", "Upload Date", "Resolution", "Size (MB)"};
        moviesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        moviesTable = new JTable(moviesModel);
        styleTable(moviesTable);
        
        JScrollPane scrollPane = new JScrollPane(moviesTable);
        scrollPane.setBackground(BG_SECONDARY);
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // SETTINGS PANEL
    // =========================================================================

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Settings");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_PRIMARY);
        
        JLabel infoLabel = new JLabel("<html><body style='width: 400px'>" +
                "<h3>Admin Panel Information</h3>" +
                "<p>This admin panel connects to the backend API to manage:</p>" +
                "<ul>" +
                "<li>Upload history (all users)</li>" +
                "<li>Movie management (delete, update status)</li>" +
                "<li>System statistics</li>" +
                "</ul>" +
                "<br><p><b>Note:</b> User management endpoints are not yet implemented in the backend. " +
                "Users can be managed directly through the PostgreSQL database.</p>" +
                "</body></html>");
        infoLabel.setFont(FONT_BODY);
        infoLabel.setForeground(TEXT_PRIMARY);
        
        contentPanel.add(infoLabel);
        
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // DATA LOADING METHODS
    // =========================================================================

    private void loadDashboardData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return adminApiService.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    
                    totalUploadsLabel.setText(String.valueOf(stats.getOrDefault("totalUploads", 0)));
                    completedUploadsLabel.setText(String.valueOf(stats.getOrDefault("completedUploads", 0)));
                    failedUploadsLabel.setText(String.valueOf(stats.getOrDefault("failedUploads", 0)));
                    pendingUploadsLabel.setText(String.valueOf(stats.getOrDefault("pendingUploads", 0)));
                    
                    // Load movie count
                    loadMovieCount();
                    
                } catch (Exception e) {
                    showError("Failed to load dashboard data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadMovieCount() {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                List<MovieDto> movies = movieApiService.getAllMovies();
                return movies.size();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    totalMoviesLabel.setText(String.valueOf(count));
                } catch (Exception e) {
                    totalMoviesLabel.setText("Error");
                }
            }
        };
        worker.execute();
    }

    private void loadUploadHistory() {
        SwingWorker<List<UploadHistory>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UploadHistory> doInBackground() throws Exception {
                return adminApiService.getAllUploadHistory(100);
            }

            @Override
            protected void done() {
                try {
                    List<UploadHistory> uploads = get();
                    uploadHistoryModel.setRowCount(0);
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    
                    for (UploadHistory upload : uploads) {
                        uploadHistoryModel.addRow(new Object[]{
                            upload.getJobId(),
                            upload.getUserId(),
                            upload.getTitle(),
                            upload.getStatus(),
                            upload.getProgress() + "%",
                            upload.getUploadedAt() != null ? dateFormat.format(upload.getUploadedAt()) : "N/A",
                            //upload.getSizeBytes() != null ? (upload.getSizeBytes() / (1024 * 1024)) + " MB" : "N/A"
                        });
                    }
                    
                    showSuccess("Loaded " + uploads.size() + " upload records");
                    
                } catch (Exception e) {
                    showError("Failed to load upload history: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadMovies() {
        SwingWorker<List<MovieDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return movieApiService.getAllMovies();
            }

            @Override
            protected void done() {
                try {
                    List<MovieDto> movies = get();
                    moviesModel.setRowCount(0);
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    
                    for (MovieDto movie : movies) {
                        moviesModel.addRow(new Object[]{
                            movie.getId(),
                            movie.getTitle(),
                            movie.getSourceType(),
                            "ACTIVE", // Status field not in MovieDto
                            movie.getUploadedDate() != null ? dateFormat.format(movie.getUploadedDate()) : "N/A",
                            movie.getResolution() != null ? movie.getResolution() : "N/A",
                            movie.getSizeInBytes() != null ? (movie.getSizeInBytes() / (1024 * 1024)) + " MB" : "N/A"
                        });
                    }
                    
                    showSuccess("Loaded " + movies.size() + " movies");
                    
                } catch (Exception e) {
                    showError("Failed to load movies: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedMovie() {
        int selectedRow = moviesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a movie to delete");
            return;
        }

        Long movieId = (Long) moviesModel.getValueAt(selectedRow, 0);
        String movieTitle = (String) moviesModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete: " + movieTitle + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return adminApiService.deleteMovie(movieId);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccess("Movie deleted successfully");
                        loadMovies();
                        loadDashboardData();
                    }
                } catch (Exception e) {
                    showError("Failed to delete movie: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshAllData() {
        loadDashboardData();
        loadUploadHistory();
        loadMovies();
        showSuccess("All data refreshed");
    }

    // =========================================================================
    // UI HELPER METHODS
    // =========================================================================

    private void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SECONDARY);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(BG_PRIMARY);
        table.setRowHeight(30);
        table.setGridColor(BORDER_SUBTLE);
        table.getTableHeader().setFont(FONT_BODY);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBackground(BG_TERTIARY);
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(BG_PRIMARY);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT);
            }
        });
        
        return button;
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    // MAIN METHOD FOR TESTING
    // =========================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            AdminPanel adminPanel = new AdminPanel();
            adminPanel.setVisible(true);
        });
    }
}
