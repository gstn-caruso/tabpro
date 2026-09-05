package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;

public final class TabPainter {

    public static final Color BACKGROUND = new Color(0x1E1F22);
    public static final Color STRING = new Color(0x5A5D63);
    public static final Color BAR_LINE = new Color(0x8A8D93);
    public static final Color TEXT = new Color(0xDFE1E5);
    public static final Color WARNING = new Color(0xE5A44A);
    public static final Color CURSOR = new Color(0x3574F0);
    public static final Color PLAYING = new Color(0x35, 0x74, 0xF0, 0x40);

    private TabPainter() {
    }

    public static void paint(
            Graphics2D g, TabLayout layout, Track track, Cursor cursor, Optional<BeatPosition> playing) {
        paintBackground(g);
    }

    private static void paintBackground(Graphics2D g) {
        g.setColor(BACKGROUND);
        g.fill(g.getClipBounds());
    }
}
