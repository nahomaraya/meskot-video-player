package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YouTube-style movie card component with thumbnail support
 */
public class MovieCardPanel extends JPanel {

    // Colors matching VideoPlayerUI
    private static final Color BG_CARD = new Color(48, 48, 48);
    private static final Color BG_CARD_HOVER = new Color(64, 64, 64);
    private static final Color BG_THUMBNAIL = new Color(28, 28, 28);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);

    // Thumbnail cache to avoid reloading images
    private static final ConcurrentHashMap<String, Image> thumbnailCache = new ConcurrentHashMap<>();

    private final MovieDto movie;
    private boolean isHovered = false;
    private boolean isSelected = false;
    private Image thumbnail = null;
    private boolean thumbnailLoading = false;
    private boolean thumbnailFailed = false;

    public static final int CARD_WIDTH = 240;
    public static final int CARD_HEIGHT = 220;
    private static final int THUMBNAIL_HEIGHT = 135;
    private static final int CORNER_RADIUS = 8;

    public MovieCardPanel(MovieDto movie) {
        this.movie = movie;
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setupMouseListeners();

        // Load thumbnail asynchronously
        loadThumbnail();
    }

    private void loadThumbnail() {
        String thumbnailUrl = movie.getThumbnailUrl();

        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            thumbnailFailed = true;
            return;
        }

        // Check cache first
        if (thumbnailCache.containsKey(thumbnailUrl)) {
            thumbnail = thumbnailCache.get(thumbnailUrl);
            return;
        }

        thumbnailLoading = true;

        // Load in background thread
        SwingWorker<Image, Void> loader = new SwingWorker<>() {
            @Override
            protected Image doInBackground() throws Exception {
                try {
                    URL url = new URL(thumbnailUrl);
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        // Scale to fit thumbnail area while maintaining aspect ratio
                        int targetWidth = CARD_WIDTH - 8;
                        int targetHeight = THUMBNAIL_HEIGHT;

                        double scale = Math.max(
                                (double) targetWidth / img.getWidth(),
                                (double) targetHeight / img.getHeight()
                        );

                        int scaledWidth = (int) (img.getWidth() * scale);
                        int scaledHeight = (int) (img.getHeight() * scale);

                        Image scaled = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                        return scaled;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load thumbnail: " + thumbnailUrl + " - " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Image result = get();
                    if (result != null) {
                        thumbnail = result;
                        thumbnailCache.put(thumbnailUrl, result);
                    } else {
                        thumbnailFailed = true;
                    }
                } catch (Exception e) {
                    thumbnailFailed = true;
                }
                thumbnailLoading = false;
                repaint();
            }
        };
        loader.execute();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public MovieDto getMovie() {
        return movie;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        int padding = 4;
        int thumbWidth = w - padding * 2;

        // Create clipping shape for rounded thumbnail
        Shape thumbnailClip = new RoundRectangle2D.Float(padding, padding, thumbWidth, THUMBNAIL_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // Draw thumbnail background
        g2.setColor(BG_THUMBNAIL);
        g2.fill(thumbnailClip);

        // Draw thumbnail image or placeholder
        if (thumbnail != null) {
            // Clip to rounded rectangle
            Shape oldClip = g2.getClip();
            g2.setClip(thumbnailClip);

            // Center the image in the thumbnail area
            int imgW = thumbnail.getWidth(null);
            int imgH = thumbnail.getHeight(null);
            int imgX = padding + (thumbWidth - imgW) / 2;
            int imgY = padding + (THUMBNAIL_HEIGHT - imgH) / 2;

            g2.drawImage(thumbnail, imgX, imgY, null);
            g2.setClip(oldClip);
        } else if (thumbnailLoading) {
            // Show loading indicator
            g2.setColor(new Color(70, 70, 70));
            g2.setFont(new Font("Inter", Font.PLAIN, 12));
            String loadingText = "Loading...";
            FontMetrics fm = g2.getFontMetrics();
            int textX = padding + (thumbWidth - fm.stringWidth(loadingText)) / 2;
            int textY = padding + THUMBNAIL_HEIGHT / 2 + 5;
            g2.drawString(loadingText, textX, textY);
        } else {
            // Show placeholder icon
            g2.setColor(new Color(70, 70, 70));
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            FontMetrics fmIcon = g2.getFontMetrics();
            String icon = "🎬";
            int iconX = padding + (thumbWidth - fmIcon.stringWidth(icon)) / 2;
            int iconY = padding + THUMBNAIL_HEIGHT / 2 + 18;
            g2.drawString(icon, iconX, iconY);
        }

        // Hover overlay with play button
        if (isHovered) {
            // Dark overlay
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fill(thumbnailClip);

            // Play button circle
            int playSize = 56;
            int playX = (w - playSize) / 2;
            int playY = padding + (THUMBNAIL_HEIGHT - playSize) / 2;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillOval(playX, playY, playSize, playSize);

            // Play triangle
            g2.setColor(BG_THUMBNAIL);
            int[] xPoints = {playX + 20, playX + 20, playX + 40};
            int[] yPoints = {playY + 14, playY + 42, playY + 28};
            g2.fillPolygon(xPoints, yPoints, 3);
        }

        // Duration badge (bottom-right of thumbnail)
        String duration = formatDuration(movie.getDurationMinutes());
        if (duration != null) {
            g2.setFont(new Font("Inter", Font.BOLD, 11));
            FontMetrics fmDur = g2.getFontMetrics();
            int durW = fmDur.stringWidth(duration) + 8;
            int durH = 18;
            int durX = w - padding - durW - 6;
            int durY = padding + THUMBNAIL_HEIGHT - durH - 6;

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(durX, durY, durW, durH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(duration, durX + 4, durY + 13);
        }

        // Resolution badge (top-left of thumbnail)
        String resolution = movie.getResolution();
        if (resolution != null && !resolution.isEmpty()) {
            g2.setFont(new Font("Inter", Font.BOLD, 10));
            FontMetrics fmRes = g2.getFontMetrics();
            int resW = fmRes.stringWidth(resolution) + 8;
            int resH = 16;

            g2.setColor(ACCENT);
            g2.fillRoundRect(padding + 6, padding + 6, resW, resH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(resolution, padding + 10, padding + 17);
        }

        // Source type badge (top-right of thumbnail)
        String sourceType = movie.getSourceType();
        if (sourceType != null && !sourceType.isEmpty() && !sourceType.equals("LOCAL")) {
            g2.setFont(new Font("Inter", Font.BOLD, 9));
            FontMetrics fmSrc = g2.getFontMetrics();
            int srcW = fmSrc.stringWidth(sourceType) + 6;
            int srcH = 14;
            int srcX = w - padding - srcW - 6;

            g2.setColor(new Color(50, 50, 50, 200));
            g2.fillRoundRect(srcX, padding + 6, srcW, srcH, 4, 4);
            g2.setColor(TEXT_SECONDARY);
            g2.drawString(sourceType, srcX + 3, padding + 16);
        }

        // Info section below thumbnail
        int infoY = padding + THUMBNAIL_HEIGHT + 12;

        // Movie title (2 lines max)
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("Inter", Font.BOLD, 13));
        String title = movie.getTitle() != null ? movie.getTitle() : "Untitled";
        drawWrappedText(g2, title, padding + 8, infoY, w - padding * 2 - 16, 2);

        // Metadata line (date)
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(new Font("Inter", Font.PLAIN, 11));
        String meta = formatUploadDate(movie.getUploadedDate());
        g2.drawString(meta, padding + 8, infoY + 38);

        // Selection border
        if (isSelected) {
            g2.setColor(ACCENT);
            g2.setStroke(new BasicStroke(2));
            g2.draw(new RoundRectangle2D.Float(padding, padding, w - padding * 2, h - padding * 2, CORNER_RADIUS, CORNER_RADIUS));
        }

        g2.dispose();
    }

    private String formatDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return null; // Don't show badge if no duration
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int maxLines) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineCount = 0;
        int lineHeight = fm.getHeight();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(testLine) > maxWidth) {
                if (lineCount < maxLines - 1) {
                    g2.drawString(line.toString(), x, y + lineCount * lineHeight);
                    line = new StringBuilder(word);
                    lineCount++;
                } else {
                    // Last line - truncate with ellipsis
                    String truncated = truncateWithEllipsis(line.toString(), fm, maxWidth);
                    g2.drawString(truncated, x, y + lineCount * lineHeight);
                    return;
                }
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0 && lineCount < maxLines) {
            g2.drawString(line.toString(), x, y + lineCount * lineHeight);
        }
    }

    private String truncateWithEllipsis(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) + ellipsisWidth > maxWidth) {
            len--;
        }
        return text.substring(0, len) + ellipsis;
    }

    private String formatUploadDate(LocalDateTime date) {
        if (date == null) return "Unknown date";

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(date, now);

        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " week" + (days / 7 > 1 ? "s" : "") + " ago";
        if (days < 365) return (days / 30) + " month" + (days / 30 > 1 ? "s" : "") + " ago";
        return (days / 365) + " year" + (days / 365 > 1 ? "s" : "") + " ago";
    }

    /**
     * Clear the thumbnail cache (useful when refreshing the library)
     */
    public static void clearThumbnailCache() {
        thumbnailCache.clear();
    }

    /**
     * Get the number of cached thumbnails
     */
    public static int getCacheSize() {
        return thumbnailCache.size();
    }
}