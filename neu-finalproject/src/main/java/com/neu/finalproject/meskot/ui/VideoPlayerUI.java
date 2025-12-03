package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import lombok.Getter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class VideoPlayerUI extends JFrame {

    private PlayerPresenter presenter;
    private final CardLayout mainCardLayout;
    private final JPanel mainPanel;
    private MovieGridPanel libraryGridPanel;

    // Gradient animation
    private float gradientOffset = 0f;
    private Timer gradientTimer;

    public static final String PAGE_SEARCH = "SEARCH";
    public static final String PAGE_LIST = "LIST";
    public static final String PAGE_PLAYER = "PLAYER";
    public static final String PAGE_LIBRARY = "LIBRARY";

    public static final String SOURCE_LOCAL = "Local Storage";
    public static final String SOURCE_SUPABASE = "Supabase";
    public static final String SOURCE_INTERNET_ARCHIVE = "Internet Archive";

    // Two-color scheme: Dark background + Orange accent (VLC-inspired)
    private static final Color BG_PRIMARY = new Color(33, 33, 33);      // Main background
    private static final Color BG_SECONDARY = new Color(48, 48, 48);    // Cards/elevated surfaces
    private static final Color BG_TERTIARY = new Color(64, 64, 64);     // Input fields/hover
    private static final Color BORDER_SUBTLE = new Color(75, 75, 75);   // Subtle borders
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236); // Main text
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156); // Secondary text
    private static final Color ACCENT = new Color(255, 136, 0);         // VLC Orange accent
    private static final Color ACCENT_HOVER = new Color(255, 160, 50);  // Lighter orange for hover

    // Font family - clean, readable
    private static final String FONT_FAMILY = "Inter";
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.PLAIN, 32);
    private static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.PLAIN, 18);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 14);

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JList<MovieDto> playerMovieListUI;
    private final DefaultListModel<MovieDto> playerMovieListModel;
    private final JList<MovieDto> mainMovieListUI;
    private final DefaultListModel<MovieDto> mainMovieListModel;
    private final JList<MovieDto> libraryMovieListUI;
    private final JTextField mainSearchField;
    private final JLabel movieTitleLabel = new JLabel("Select a movie");
    private final JSlider volumeSlider;
    private final JButton downloadButton;

    private final JPanel globalStatusBar;
    private final JComboBox<String> dataSourceCombo;
    private final JProgressBar globalProgressBar;
    private final JLabel progressLabel;
    private final JLabel progressStatusLabel;
    private final JPanel progressPanel;

    private JLabel profileInitialLabel;
    private JPanel profileButton;
    private JPopupMenu profileMenu;

    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    @Getter
    private String currentUser;
    private boolean isLoggedIn = false;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoPlayerUI() {
        super("Meskot Player");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_PRIMARY);

        // Set global UI defaults
        UIManager.put("Panel.background", BG_PRIMARY);
        UIManager.put("OptionPane.background", BG_SECONDARY);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        dataSourceCombo = new JComboBox<>(new String[]{
                SOURCE_LOCAL, SOURCE_SUPABASE, SOURCE_INTERNET_ARCHIVE
        });
        globalProgressBar = new JProgressBar(0, 100);
        progressLabel = new JLabel("");
        progressStatusLabel = new JLabel("");
        progressPanel = new JPanel(new BorderLayout(5, 0));
        globalStatusBar = createGlobalStatusBar();

        setJMenuBar(createMenuBar());

        mainCardLayout = new CardLayout();
        mainPanel = new JPanel(mainCardLayout);
        mainPanel.setBackground(BG_PRIMARY);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        playerMovieListModel = new DefaultListModel<>();
        playerMovieListUI = createStyledList(playerMovieListModel);

        mainMovieListModel = new DefaultListModel<>();
        mainMovieListUI = createStyledList(mainMovieListModel);

        libraryMovieListUI = createStyledList(mainMovieListModel);

        mainSearchField = new JTextField(40);
        volumeSlider = new JSlider(0, 100, 80);
        downloadButton = new JButton("Download");

        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registrationPanel, "REGISTER");

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_PRIMARY);
        contentWrapper.add(globalStatusBar, BorderLayout.NORTH);
        contentWrapper.add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(createSearchPage(), PAGE_SEARCH);
        mainPanel.add(createMovieListPage(), PAGE_LIST);
        mainPanel.add(createPlayerPage(), PAGE_PLAYER);
        mainPanel.add(createLibraryPage(), PAGE_LIBRARY);

        add(contentWrapper);
        showLoginPanel();
    }

    private JList<MovieDto> createStyledList(DefaultListModel<MovieDto> model) {
        JList<MovieDto> list = new JList<>(model);
        list.setCellRenderer(new MovieCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setBackground(BG_SECONDARY);
        list.setForeground(TEXT_PRIMARY);
        list.setSelectionBackground(BG_TERTIARY);
        list.setSelectionForeground(TEXT_PRIMARY);
        list.setFixedCellHeight(60);
        return list;
    }
    private JPanel createLibraryPage() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(BG_PRIMARY);

        // Top bar with title, search, and navigation
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_SECONDARY);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Left side - Title and count
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftPanel.setBackground(BG_SECONDARY);

        // Right side - View toggle and Home button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(BG_SECONDARY);

        // View toggle buttons (Grid / List)
        JPanel viewToggle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        viewToggle.setBackground(BG_TERTIARY);
        viewToggle.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));

        JButton gridViewBtn = createViewToggleButton("⊞", true);
        JButton listViewBtn = createViewToggleButton("☰", false);

        viewToggle.add(gridViewBtn);
        viewToggle.add(listViewBtn);

        JButton homeButton = createSecondaryButton("Home");
        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));

        leftPanel.add(viewToggle);
        leftPanel.add(homeButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Content area with CardLayout to switch between grid and list views
        CardLayout viewCardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(viewCardLayout);
        contentPanel.setBackground(BG_PRIMARY);

        // Grid view
        libraryGridPanel = new MovieGridPanel();
        libraryGridPanel.setOnMovieDoubleClicked(movie -> {
            if (presenter != null) {
                presenter.onMovieSelected(movie);
            }
        });

        JScrollPane gridScrollPane = new JScrollPane(libraryGridPanel);
        gridScrollPane.setBackground(BG_PRIMARY);
        gridScrollPane.getViewport().setBackground(BG_PRIMARY);
        gridScrollPane.setBorder(null);
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        gridScrollPane.getVerticalScrollBar().setBackground(BG_SECONDARY);
        gridScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // List view (existing)
        JScrollPane listScrollPane = new JScrollPane(libraryMovieListUI);
        listScrollPane.getViewport().setBackground(BG_SECONDARY);
        listScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        listScrollPane.getVerticalScrollBar().setBackground(BG_SECONDARY);

        contentPanel.add(gridScrollPane, "GRID");
        contentPanel.add(listScrollPane, "LIST");

        // View toggle actions
        gridViewBtn.addActionListener(e -> {
            viewCardLayout.show(contentPanel, "GRID");
            gridViewBtn.setBackground(ACCENT);
            gridViewBtn.setForeground(BG_PRIMARY);
            listViewBtn.setBackground(BG_TERTIARY);
            listViewBtn.setForeground(TEXT_PRIMARY);
        });

        listViewBtn.addActionListener(e -> {
            viewCardLayout.show(contentPanel, "LIST");
            listViewBtn.setBackground(ACCENT);
            listViewBtn.setForeground(BG_PRIMARY);
            gridViewBtn.setBackground(BG_TERTIARY);
            gridViewBtn.setForeground(TEXT_PRIMARY);
        });

        // Double-click on list view
        libraryMovieListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onMovieSelected(libraryMovieListUI.getSelectedValue());
                }
            }
        });

        page.add(topPanel, BorderLayout.NORTH);
        page.add(contentPanel, BorderLayout.CENTER);

        return page;
    }

    // Add this helper method for view toggle buttons
    private JButton createViewToggleButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(isActive ? BG_PRIMARY : TEXT_PRIMARY);
        button.setBackground(isActive ? ACCENT : BG_TERTIARY);
        button.setPreferredSize(new Dimension(36, 32));
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Update the updateMainMovieList method to also update the grid:
    public void updateMainMovieList(List<MovieDto> movies) {
        mainMovieListModel.clear();
        for (MovieDto m : movies) mainMovieListModel.addElement(m);

        // Also update the grid panel
        if (libraryGridPanel != null) {
            libraryGridPanel.setMovies(movies);
        }
    }

    private JPanel createGlobalStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setBackground(BG_SECONDARY);

        JLabel sourceLabel = new JLabel("Source");
        sourceLabel.setForeground(TEXT_SECONDARY);
        sourceLabel.setFont(FONT_SMALL);

        styleComboBox(dataSourceCombo);
        dataSourceCombo.addActionListener(e -> {
            if (presenter != null) {
                presenter.onDataSourceChanged((String) dataSourceCombo.getSelectedItem());
            }
        });

        leftPanel.add(sourceLabel);
        leftPanel.add(dataSourceCombo);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setBackground(BG_SECONDARY);

        progressPanel.setBackground(BG_SECONDARY);
        progressPanel.setPreferredSize(new Dimension(300, 32));
        progressPanel.setVisible(false);

        JPanel progressInfoPanel = new JPanel(new BorderLayout());
        progressInfoPanel.setBackground(BG_SECONDARY);

        progressLabel.setForeground(TEXT_PRIMARY);
        progressLabel.setFont(FONT_SMALL);
        progressStatusLabel.setForeground(TEXT_SECONDARY);
        progressStatusLabel.setFont(FONT_SMALL);

        progressInfoPanel.add(progressLabel, BorderLayout.WEST);
        progressInfoPanel.add(progressStatusLabel, BorderLayout.EAST);

        globalProgressBar.setPreferredSize(new Dimension(250, 4));
        globalProgressBar.setBackground(BG_TERTIARY);
        globalProgressBar.setForeground(ACCENT);
        globalProgressBar.setBorderPainted(false);

        JButton cancelButton = createIconButton("×");
        cancelButton.addActionListener(e -> {
            if (presenter != null) presenter.onCancelOperation();
        });

        JPanel progressBarWrapper = new JPanel(new BorderLayout(8, 0));
        progressBarWrapper.setBackground(BG_SECONDARY);
        progressBarWrapper.add(globalProgressBar, BorderLayout.CENTER);
        progressBarWrapper.add(cancelButton, BorderLayout.EAST);

        progressPanel.add(progressInfoPanel, BorderLayout.NORTH);
        progressPanel.add(progressBarWrapper, BorderLayout.SOUTH);

        profileButton = createProfileButton();

        rightPanel.add(progressPanel);
        rightPanel.add(profileButton);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);

        return statusBar;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_TERTIARY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_SMALL);
        combo.setPreferredSize(new Dimension(150, 28));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        combo.setFocusable(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : BG_TERTIARY);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 12, 6, 12));
                setFont(FONT_SMALL);
                return this;
            }
        });
    }

    private JPanel createProfileButton() {
        JPanel button = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isLoggedIn ? ACCENT : BG_TERTIARY);
                g2.fillOval(2, 2, 28, 28);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(32, 32));
        button.setBackground(BG_SECONDARY);
        button.setLayout(new GridBagLayout());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        profileInitialLabel = new JLabel("?");
        profileInitialLabel.setFont(FONT_BODY);
        profileInitialLabel.setForeground(TEXT_PRIMARY);
        button.add(profileInitialLabel);

        profileMenu = createProfileMenu();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                profileMenu.show(button, button.getWidth() - 200, button.getHeight() + 5);
            }
        });

        return button;
    }

    private JPopupMenu createProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(BG_SECONDARY);
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(BG_SECONDARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel usernameLabel = new JLabel("Not logged in");
        usernameLabel.setFont(FONT_BODY);
        usernameLabel.setForeground(TEXT_PRIMARY);

        JLabel statusLabel = new JLabel("Click to sign in");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_SECONDARY);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(BG_SECONDARY);
        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(Box.createVerticalStrut(2));
        userInfoPanel.add(statusLabel);

        headerPanel.add(userInfoPanel, BorderLayout.CENTER);
        menu.add(headerPanel);
        menu.addSeparator();

        JMenuItem accountItem = createMenuItem("My Account");
        JMenuItem settingsItem = createMenuItem("Settings");
        JMenuItem adminPanelItem = createMenuItem("Admin Panel");
        JMenuItem loginLogoutItem = createMenuItem("Sign In");

        accountItem.addActionListener(e -> {
            if (!isLoggedIn) showLoginRequired("view account details");
            else showAccountDialog();
        });
        
        adminPanelItem.addActionListener(e -> {
            if (!isLoggedIn) {
                showLoginRequired("access admin panel");
            } else {
                openAdminPanel();
            }
        });

        loginLogoutItem.addActionListener(e -> {
            if (isLoggedIn) performLogout();
            else showLoginPanel();
        });

        menu.add(accountItem);
        menu.add(settingsItem);
        menu.add(adminPanelItem);
        menu.addSeparator();
        menu.add(loginLogoutItem);

        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                usernameLabel.setText(isLoggedIn ? currentUser : "Not logged in");
                statusLabel.setText(isLoggedIn ? "Signed in" : "Click to sign in");
                loginLogoutItem.setText(isLoggedIn ? "Sign Out" : "Sign In");
            }
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        return menu;
    }

    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(BG_SECONDARY);
        item.setForeground(TEXT_PRIMARY);
        item.setFont(FONT_BODY);
        item.setBorder(new EmptyBorder(10, 16, 10, 16));
        item.setOpaque(true);
        return item;
    }

    private void updateProfileButton() {
        String initial = isLoggedIn && currentUser != null ?
                currentUser.substring(0, 1).toUpperCase() : "?";
        profileInitialLabel.setText(initial);
        profileButton.repaint();
    }

    private void showAccountDialog() {
        JDialog dialog = new JDialog(this, "My Account", true);
        dialog.setSize(360, 240);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_SECONDARY);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel nameLabel = new JLabel(currentUser != null ? currentUser : "Unknown");
        nameLabel.setFont(FONT_HEADING);
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Active Account");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ACCENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeButton = createSecondaryButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dialog.dispose());

        panel.add(Box.createVerticalStrut(20));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(40));
        panel.add(closeButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public boolean requireLogin(String action) {
        if (!isLoggedIn) {
            showLoginRequired(action);
            return false;
        }
        return true;
    }

    private void showLoginRequired(String action) {
        int result = JOptionPane.showConfirmDialog(this,
                "You need to sign in to " + action + ".\n\nWould you like to sign in now?",
                "Sign In Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) showLoginPanel();
    }

    public boolean isUserLoggedIn() {
        return isLoggedIn;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_SECONDARY);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(TEXT_PRIMARY);
        fileMenu.setFont(FONT_BODY);

        JMenuItem openItem = createMenuItem("Open Video File...");
        openItem.addActionListener(e -> { if (presenter != null) presenter.onOpenLocalVideo(); });

        JMenuItem uploadItem = createMenuItem("Upload Video...");
        uploadItem.addActionListener(e -> {
            if (requireLogin("upload videos") && presenter != null) presenter.onShowUploadDialog();
        });

        JMenuItem compressItem = createMenuItem("Compress Video...");
        compressItem.addActionListener(e -> { if (presenter != null) presenter.onShowCompressDialog(); });

        JMenuItem exitItem = createMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(uploadItem);
        fileMenu.add(compressItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu navMenu = new JMenu("Navigate");
        navMenu.setForeground(TEXT_PRIMARY);
        navMenu.setFont(FONT_BODY);

        JMenuItem searchItem = createMenuItem("Search Home");
        searchItem.addActionListener(e -> { if (presenter != null) presenter.onNavigate(PAGE_SEARCH); });

        JMenuItem libraryItem = createMenuItem("Full Library");
        libraryItem.addActionListener(e -> { if (presenter != null) presenter.onLoadLibrary(); });

        navMenu.add(searchItem);
        navMenu.add(libraryItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);

        return menuBar;
    }

    private JPanel createSearchPage() {
        JPanel page = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Base background
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 0, w, h);

                // Soft radial gradient blob 1
                float cx1 = (float) (Math.sin(gradientOffset * 0.4) * w * 0.3 + w * 0.3);
                float cy1 = (float) (Math.cos(gradientOffset * 0.3) * h * 0.3 + h * 0.3);
                float radius1 = Math.min(w, h) * 0.8f;

                float[] dist1 = {0f, 0.5f, 1f};
                Color[] colors1 = {
                        new Color(50, 50, 50, 120),
                        new Color(40, 40, 40, 60),
                        new Color(30, 30, 30, 0)
                };
                RadialGradientPaint rgp1 = new RadialGradientPaint(cx1, cy1, radius1, dist1, colors1);
                g2.setPaint(rgp1);
                g2.fillRect(0, 0, w, h);

                // Soft radial gradient blob 2
                float cx2 = (float) (Math.cos(gradientOffset * 0.5) * w * 0.3 + w * 0.7);
                float cy2 = (float) (Math.sin(gradientOffset * 0.4) * h * 0.3 + h * 0.6);
                float radius2 = Math.min(w, h) * 0.7f;

                float[] dist2 = {0f, 0.4f, 1f};
                Color[] colors2 = {
                        new Color(45, 45, 45, 100),
                        new Color(35, 35, 35, 50),
                        new Color(30, 30, 30, 0)
                };
                RadialGradientPaint rgp2 = new RadialGradientPaint(cx2, cy2, radius2, dist2, colors2);
                g2.setPaint(rgp2);
                g2.fillRect(0, 0, w, h);

                // Soft radial gradient blob 3
                float cx3 = (float) (Math.sin(gradientOffset * 0.6) * w * 0.25 + w * 0.5);
                float cy3 = (float) (Math.cos(gradientOffset * 0.35) * h * 0.25 + h * 0.4);
                float radius3 = Math.min(w, h) * 0.6f;

                float[] dist3 = {0f, 0.6f, 1f};
                Color[] colors3 = {
                        new Color(55, 55, 55, 80),
                        new Color(38, 38, 38, 40),
                        new Color(30, 30, 30, 0)
                };
                RadialGradientPaint rgp3 = new RadialGradientPaint(cx3, cy3, radius3, dist3, colors3);
                g2.setPaint(rgp3);
                g2.fillRect(0, 0, w, h);

                g2.dispose();
            }
        };
        page.setOpaque(false);

        // Start gradient animation
        if (gradientTimer == null) {
            gradientTimer = new Timer(50, e -> {
                gradientOffset += 0.02f;
                page.repaint();
            });
            gradientTimer.start();
        }

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setOpaque(false);

// Load and scale the logo
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/logo.png"));
        int logoHeight = 360; // Adjust this to match your font size
        int logoWidth = (int) ((double) originalIcon.getIconWidth() / originalIcon.getIconHeight() * logoHeight);
        Image scaledImage = originalIcon.getImage().getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

//        JLabel welcomeLabel = new JLabel("Welcome to ");
//        welcomeLabel.setFont(FONT_TITLE);
//        welcomeLabel.setForeground(TEXT_PRIMARY);

        JLabel logoLabel = new JLabel(scaledIcon);

        JLabel exclamationLabel = new JLabel("!");
        exclamationLabel.setFont(FONT_TITLE);
        exclamationLabel.setForeground(TEXT_PRIMARY);

//        titlePanel.add(welcomeLabel);
        titlePanel.add(logoLabel);
        titlePanel.add(exclamationLabel);
//        JLabel title = new JLabel("Welcome to Meskot!");
//        title.setFont(FONT_TITLE);
//        title.setForeground(TEXT_PRIMARY);

//        JLabel subtitle = new JLabel("Video Player");
//        subtitle.setFont(FONT_BODY);
//        subtitle.setForeground(TEXT_SECONDARY);

        // Search field - Claude-style rounded input
        mainSearchField.setPreferredSize(new Dimension(280, 48));
        mainSearchField.setFont(FONT_BODY);
        mainSearchField.setForeground(TEXT_PRIMARY);
        mainSearchField.setCaretColor(TEXT_PRIMARY);
        mainSearchField.setBackground(BG_SECONDARY);
        mainSearchField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_SUBTLE, 24),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JButton searchButton = createPrimaryButton("SEARCH");
        JButton libraryButton = createSecondaryButton("Library");
        JButton uploadButton = createSecondaryButton("Upload");

        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.gridy = 0;
        page.add(titlePanel, gbc);

//        gbc.gridy = 1;
//        gbc.insets = new Insets(0, 0, 40, 0);
//        page.add(subtitle, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;  // Changed from HORIZONTAL
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 20, 0, 8);
        page.add(mainSearchField, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 20);
        page.add(searchButton, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(24, 0, 0, 0);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(libraryButton);
        buttonRow.add(uploadButton);
        page.add(buttonRow, gbc);

        searchButton.addActionListener(e -> presenter.onSearch());
        mainSearchField.addActionListener(e -> presenter.onSearch());
        uploadButton.addActionListener(e -> {
            if (requireLogin("upload videos")) presenter.onShowUploadDialog();
        });
        libraryButton.addActionListener(e -> presenter.onLoadLibrary());

        return page;
    }

    private JPanel createPlayerPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG_PRIMARY);

        JScrollPane listScrollPane = new JScrollPane(playerMovieListUI);
        listScrollPane.setPreferredSize(new Dimension(280, 600));
        listScrollPane.getViewport().setBackground(BG_SECONDARY);
        listScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_SUBTLE));

        JPanel nowPlayingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        nowPlayingPanel.setBackground(BG_SECONDARY);
        nowPlayingPanel.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel nowPlayingLabel = new JLabel("Now Playing:");
        nowPlayingLabel.setForeground(TEXT_SECONDARY);
        nowPlayingLabel.setFont(FONT_SMALL);

        movieTitleLabel.setForeground(TEXT_PRIMARY);
        movieTitleLabel.setFont(FONT_BODY);

        nowPlayingPanel.add(nowPlayingLabel);
        nowPlayingPanel.add(movieTitleLabel);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(BG_SECONDARY);
        controlPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(BG_SECONDARY);

        JLabel currentTimeLabel = new JLabel("00:00");
        currentTimeLabel.setForeground(TEXT_SECONDARY);
        currentTimeLabel.setFont(FONT_SMALL);

        JLabel durationLabel = new JLabel("00:00");
        durationLabel.setForeground(TEXT_SECONDARY);
        durationLabel.setFont(FONT_SMALL);

        timePanel.add(currentTimeLabel, BorderLayout.WEST);
        timePanel.add(durationLabel, BorderLayout.EAST);

        JSlider seekSlider = new JSlider(0, 100, 0);
        seekSlider.setBackground(BG_SECONDARY);
        seekSlider.setForeground(ACCENT);

        Timer seekTimer = new Timer(500, e -> {
            if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                long current = presenter.getCurrentTime();
                long duration = presenter.getDuration();
                if (duration > 0) {
                    seekSlider.setValue((int) ((current * 100) / duration));
                    currentTimeLabel.setText(presenter.formatTime(current));
                    durationLabel.setText(presenter.formatTime(duration));
                }
            }
        });
        seekTimer.start();

        seekSlider.addChangeListener(e -> {
            if (seekSlider.getValueIsAdjusting()) {
                long duration = presenter.getDuration();
                if (duration > 0) presenter.onSeek((seekSlider.getValue() * duration) / 100);
            }
        });

        JPanel playbackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        playbackPanel.setBackground(BG_SECONDARY);

        JButton skipBackButton = createControlButton("−10");
        JButton playButton = createControlButton("▶");
//        JButton pauseButton = createControlButton("⏸");
        JButton stopButton = createControlButton("⏹");
        JButton skipForwardButton = createControlButton("+10");

        playbackPanel.add(skipBackButton);
        playbackPanel.add(playButton);
//        playbackPanel.add(pauseButton);
        playbackPanel.add(stopButton);
        playbackPanel.add(skipForwardButton);

        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        volumePanel.setBackground(BG_SECONDARY);

        JLabel volumeIcon = new JLabel("🔊");
        volumeIcon.setFont(FONT_SMALL);
        volumeSlider.setBackground(BG_SECONDARY);
        volumeSlider.setPreferredSize(new Dimension(100, 20));

        downloadButton.setText("Download");
        downloadButton.setFont(FONT_BUTTON);
        downloadButton.setForeground(ACCENT);
        downloadButton.setBackground(BG_SECONDARY);
        downloadButton.setBorder(BorderFactory.createLineBorder(ACCENT));
        downloadButton.setPreferredSize(new Dimension(100, 32));
        downloadButton.setFocusPainted(false);
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        volumePanel.add(volumeIcon);
        volumePanel.add(volumeSlider);
        volumePanel.add(Box.createHorizontalStrut(20));
        volumePanel.add(downloadButton);

        controlPanel.add(timePanel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(seekSlider);
        controlPanel.add(Box.createVerticalStrut(12));
        controlPanel.add(playbackPanel);
        controlPanel.add(Box.createVerticalStrut(12));
        controlPanel.add(volumePanel);

        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(nowPlayingPanel, BorderLayout.NORTH);
        playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, playerPanel);
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0);
        splitPane.setBackground(BG_PRIMARY);

        page.add(splitPane, BorderLayout.CENTER);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topBar.setBackground(BG_SECONDARY);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));

        JButton homeButton = createSecondaryButton("Home");
        JButton backButton = createSecondaryButton("❮❮");

        topBar.add(backButton);
        topBar.add(homeButton);

        page.add(topBar, BorderLayout.NORTH);

        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));
        backButton.addActionListener(e -> presenter.onLoadLibrary());
        playButton.addActionListener(e -> {
            if (playButton.getText().equals("▶")) {
                presenter.onPlay();
                playButton.setText("⏸");
            } else {
                mediaPlayerComponent.mediaPlayer().controls().pause();
                playButton.setText("▶");
            }
        });
//        playButton.addActionListener(e -> presenter.onPlay());
//        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().stop();
            playButton.setText("▶");
        });
        skipForwardButton.addActionListener(e -> presenter.onSkip(10));
        skipBackButton.addActionListener(e -> presenter.onSkip(-10));
        downloadButton.addActionListener(e -> {
            if (requireLogin("download videos")) presenter.onShowDownloadDialog();
        });
        volumeSlider.addChangeListener(e -> mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue()));

        playerMovieListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) presenter.onPlay();
            }
        });

        return page;
    }

    private JPanel createMovieListPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBorder(new EmptyBorder(20, 20, 20, 20));
        page.setBackground(BG_PRIMARY);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        topPanel.setBackground(BG_PRIMARY);

        JLabel resultsLabel = new JLabel("Search Results");
        resultsLabel.setForeground(TEXT_PRIMARY);
        resultsLabel.setFont(FONT_HEADING);

        JButton homeButton = createSecondaryButton("Home");
        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));

        topPanel.add(resultsLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(homeButton);

        JScrollPane scrollPane = new JScrollPane(mainMovieListUI);
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));

        page.add(topPanel, BorderLayout.NORTH);
        page.add(scrollPane, BorderLayout.CENTER);

        mainMovieListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onMovieSelected(mainMovieListUI.getSelectedValue());
                }
            }
        });

        return page;
    }

    // Button factories
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(BG_PRIMARY);
        button.setPreferredSize(new Dimension(100, 48));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_TERTIARY : BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(120, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(BG_TERTIARY);
        button.setPreferredSize(new Dimension(48, 36));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createIconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(TEXT_SECONDARY);
        button.setBackground(BG_SECONDARY);
        button.setBorder(new EmptyBorder(4, 8, 4, 8));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Rounded Border class
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
    }

    // Public progress methods
    public void showGlobalProgress(String type, String filename, int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressPanel.setVisible(true);
            String truncatedName = filename.length() > 25 ? filename.substring(0, 22) + "..." : filename;
            progressLabel.setText(("UPLOAD".equals(type) ? "↑ " : "↓ ") + truncatedName);
            progressStatusLabel.setText(status);
            if (percent < 0) {
                globalProgressBar.setIndeterminate(true);
            } else {
                globalProgressBar.setIndeterminate(false);
                globalProgressBar.setValue(percent);
            }
        });
    }

    public void showGlobalProgressComplete(String message, boolean success) {
        SwingUtilities.invokeLater(() -> {
            progressLabel.setText((success ? "✓ " : "✗ ") + message);
            progressStatusLabel.setText("");
            globalProgressBar.setIndeterminate(false);
            globalProgressBar.setValue(100);
            Timer hideTimer = new Timer(3000, e -> hideGlobalProgress());
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
    }

    public void hideGlobalProgress() {
        SwingUtilities.invokeLater(() -> {
            progressPanel.setVisible(false);
            globalProgressBar.setValue(0);
            progressLabel.setText("");
            progressStatusLabel.setText("");
        });
    }

    public String getSelectedDataSource() { return (String) dataSourceCombo.getSelectedItem(); }
    public void setSelectedDataSource(String source) { dataSourceCombo.setSelectedItem(source); }
    public void attachPresenter(PlayerPresenter presenter) { this.presenter = presenter; }
    public void showPage(String pageName) { mainCardLayout.show(mainPanel, pageName); }
    public void updateNowPlayingLabel(String title) { movieTitleLabel.setText(title); }



    public void updatePlayerMovieList(List<MovieDto> movies) {
        playerMovieListModel.clear();
        if (movies != null) for (MovieDto m : movies) playerMovieListModel.addElement(m);
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public File showLocalVideoDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Video File");
        return fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                ? fileChooser.getSelectedFile() : null;
    }

    public EmbeddedMediaPlayerComponent getMediaPlayer() { return mediaPlayerComponent; }
    public MovieDto getSelectedMovieFromList() { return playerMovieListUI.getSelectedValue(); }
    public String getSearchQuery() { return mainSearchField.getText().trim(); }
    public void release() {
        if (gradientTimer != null) {
            gradientTimer.stop();
        }
        mediaPlayerComponent.release();
    }

    // Auth methods
    private final AuthApiService authApiService = new AuthApiService();
    
    public boolean performLogin(String username, String password) {
        try {
            AuthApiService.AuthResponse response = authApiService.login(username, password);
            if (response != null && response.getUser() != null) {
                currentUser = response.getUser().getUsername();
                isLoggedIn = true;
                updateProfileButton();
                showPage(PAGE_SEARCH);
                System.out.println("Login successful for user: " + currentUser);
                return true;
            }
        } catch (Exception ex) {
            showErrorMessage("Login failed: " + ex.getMessage());
            System.err.println("Login error: " + ex.getMessage());
        }
        return false;
    }

    public boolean performRegistration(String username, String email, String password) {
        try {
            AuthApiService.AuthResponse response = authApiService.register(username, email, password);
            if (response != null && response.getUser() != null) {
                System.out.println("Registration successful for user: " + username);
                return true;
            }
        } catch (Exception ex) {
            showErrorMessage("Registration failed: " + ex.getMessage());
            System.err.println("Registration error: " + ex.getMessage());
            return false;
        }
        return false;
    }

    public void showLoginPanel() { mainCardLayout.show(mainPanel, "LOGIN"); }
    public void showRegistrationPanel() { mainCardLayout.show(mainPanel, "REGISTER"); }

    public void performLogout() {
        try {
            authApiService.logout();
            System.out.println("Logout successful");
        } catch (Exception ex) {
            System.err.println("Logout error: " + ex.getMessage());
        }
        currentUser = null;
        isLoggedIn = false;
        updateProfileButton();
        showLoginPanel();
    }
    
    /**
     * Open Admin Panel in a new window
     * Only accessible after login
     */
    private void openAdminPanel() {
        SwingUtilities.invokeLater(() -> {
            AdminPanel adminPanel = new AdminPanel();
            adminPanel.setVisible(true);
        });
    }
}