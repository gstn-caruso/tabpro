package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.UiSizing;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

final class HoverNotePainter {

    private HoverNotePainter() {
    }

    static int minimumRadius() {
        return UiSizing.scaled(11);
    }

    static void paint(Graphics2D graphics, String name, int x, int y, int radius, Color background) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(background);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, UiSizing.scaled(10)));
            FontMetrics metrics = g.getFontMetrics();
            g.setColor(Contrast.ratio(background, Color.BLACK) >= Contrast.TEXT_MINIMUM_RATIO
                    ? Color.BLACK : Color.WHITE);
            g.drawString(name, x - metrics.stringWidth(name) / 2,
                    y + (metrics.getAscent() - metrics.getDescent()) / 2);
        } finally {
            g.dispose();
        }
    }
}
