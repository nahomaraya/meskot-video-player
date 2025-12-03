package com.neu.finalproject.meskot.ui;

import javax.swing.*;

/**
 * Main entry point for the Movie Streaming Application UI
 * Connects the Swing UI components to the backend REST API
 */
public class MovieStreamingApp {

    public static void main(String[] args) {
        // Set system look and feel properties before creating any UI
        setupSystemProperties();
        
        // Use SwingUtilities.invokeLater to ensure UI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the UI theme/look and feel
                setupLookAndFeel();
                
                // Create and display the main window
                VideoPlayerUI mainWindow = new VideoPlayerUI();
                mainWindow.setVisible(true);
                
                System.out.println("Movie Streaming Application started successfully");
                System.out.println("Backend API: http://localhost:8080/api");
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Set up system properties for better UI rendering
     */
    private static void setupSystemProperties() {
        // Enable anti-aliasing for text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Better font rendering
        System.setProperty("sun.java2d.opengl", "true");
        
        // macOS specific settings
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Meskot Player");
    }

    /**
     * Configure the Swing Look and Feel
     */
    private static void setupLookAndFeel() {
        try {
            // Try to use the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If system L&F fails, use cross-platform (Metal)
            System.err.println("Could not set system look and feel, using default");
        }
    }
}
