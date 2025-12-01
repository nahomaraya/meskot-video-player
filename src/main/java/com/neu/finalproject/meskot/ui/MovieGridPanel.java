package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * YouTube-style responsive grid panel for movie cards
 */
public class MovieGridPanel extends JPanel implements Scrollable {

    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final int CARD_GAP = 16;
    private static final int PADDING = 20;

    private final List<MovieCardPanel> cardPanels = new ArrayList<>();
    private MovieCardPanel selectedCard = null;
    private Consumer<MovieDto> onMovieSelected;
    private Consumer<MovieDto> onMovieDoubleClicked;

    public MovieGridPanel() {
        setBackground(BG_PRIMARY);
        setLayout(null); // Custom layout

        // Re-layout on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutCards();
            }
        });
    }

    public void setOnMovieSelected(Consumer<MovieDto> callback) {
        this.onMovieSelected = callback;
    }

    public void setOnMovieDoubleClicked(Consumer<MovieDto> callback) {
        this.onMovieDoubleClicked = callback;
    }

    public void setMovies(List<MovieDto> movies) {
        // Clear existing cards
        cardPanels.clear();
        removeAll();
        selectedCard = null;

        if (movies == null || movies.isEmpty()) {
            // Show empty state
            JLabel emptyLabel = new JLabel("No movies found");
            emptyLabel.setForeground(new Color(156, 156, 156));
            emptyLabel.setFont(new Font("Inter", Font.PLAIN, 16));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(emptyLabel);
            emptyLabel.setBounds(0, 100, getWidth(), 30);
            revalidate();
            repaint();
            return;
        }

        // Create card for each movie
        for (MovieDto movie : movies) {
            MovieCardPanel card = new MovieCardPanel(movie);
            setupCardListeners(card);
            cardPanels.add(card);
            add(card);
        }

        layoutCards();
        revalidate();
        repaint();
    }

    private void setupCardListeners(MovieCardPanel card) {
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Select this card
                if (selectedCard != null) {
                    selectedCard.setSelected(false);
                }
                selectedCard = card;
                card.setSelected(true);

                if (e.getClickCount() == 1 && onMovieSelected != null) {
                    onMovieSelected.accept(card.getMovie());
                } else if (e.getClickCount() == 2 && onMovieDoubleClicked != null) {
                    onMovieDoubleClicked.accept(card.getMovie());
                }
            }
        });
    }

    private void layoutCards() {
        if (cardPanels.isEmpty()) return;

        int availableWidth = getWidth() - PADDING * 2;
        if (availableWidth <= 0) availableWidth = 800; // Default

        // Calculate number of columns that fit
        int cardWidth = MovieCardPanel.CARD_WIDTH;
        int cardHeight = MovieCardPanel.CARD_HEIGHT;
        int cols = Math.max(1, (availableWidth + CARD_GAP) / (cardWidth + CARD_GAP));

        // Calculate actual gap to distribute space evenly
        int totalCardWidth = cols * cardWidth;
        int totalGaps = cols - 1;
        int actualGap = cols > 1 ? (availableWidth - totalCardWidth) / totalGaps : 0;
        actualGap = Math.max(CARD_GAP, actualGap);

        // Position each card
        int x = PADDING;
        int y = PADDING;
        int col = 0;

        for (MovieCardPanel card : cardPanels) {
            card.setBounds(x, y, cardWidth, cardHeight);

            col++;
            if (col >= cols) {
                col = 0;
                x = PADDING;
                y += cardHeight + CARD_GAP;
            } else {
                x += cardWidth + actualGap;
            }
        }

        // Set preferred size for scrolling
        int rows = (int) Math.ceil((double) cardPanels.size() / cols);
        int totalHeight = PADDING * 2 + rows * cardHeight + (rows - 1) * CARD_GAP;
        setPreferredSize(new Dimension(availableWidth + PADDING * 2, totalHeight));
    }

    public MovieDto getSelectedMovie() {
        return selectedCard != null ? selectedCard.getMovie() : null;
    }

    public void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }

    // Scrollable implementation
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return MovieCardPanel.CARD_HEIGHT + CARD_GAP;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true; // Fill width
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // Allow vertical scrolling
    }
}