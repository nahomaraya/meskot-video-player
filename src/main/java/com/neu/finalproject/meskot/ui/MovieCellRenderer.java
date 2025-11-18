package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A custom ListCellRenderer to display a movie with a thumbnail and title,
 * inspired by Wikipedia article lists.
 */
public class MovieCellRenderer extends JPanel implements ListCellRenderer<MovieDto> {

    private final JLabel lblThumbnail;
    private final JLabel lblTitle;
    private final JLabel lblDetails;
    private final Icon defaultIcon;

    public MovieCellRenderer() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create a placeholder icon
        defaultIcon = UIManager.getIcon("FileView.fileIcon");

        lblThumbnail = new JLabel();
        lblThumbnail.setOpaque(true);
        lblThumbnail.setHorizontalAlignment(SwingConstants.CENTER);
        lblThumbnail.setIcon(defaultIcon);
        lblThumbnail.setPreferredSize(new Dimension(64, 64)); // Thumbnail size

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        lblTitle = new JLabel();
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblDetails = new JLabel();
        lblDetails.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblDetails.setForeground(Color.GRAY);

        textPanel.add(lblTitle);
        textPanel.add(lblDetails);

        add(lblThumbnail, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MovieDto> list, MovieDto movie, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        lblTitle.setText(movie.getTitle());
        lblDetails.setText(movie.getUploadedDate() != null ? "Uploaded: " + movie.getUploadedDate().toLocalDate() : "No date");

        // Set colors based on selection
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            lblTitle.setForeground(list.getSelectionForeground());
            lblDetails.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            lblTitle.setForeground(list.getForeground());
            lblDetails.setForeground(Color.GRAY);
        }

        return this;
    }
}