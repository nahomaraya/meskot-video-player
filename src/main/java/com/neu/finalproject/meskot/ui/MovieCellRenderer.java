package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.swing.*;
import java.awt.*;

public class MovieCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof MovieDto movie) {
            setText("🎬 " + movie.getTitle());
        }
        return this;
    }
}
