package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Optional;

public final class TabPainter {

    public static final Color BACKGROUND = new Color(0x1E1F22);
    public static final Color STRING = new Color(0x5A5D63);
    public static final Color BAR_LINE = new Color(0x8A8D93);
    public static final Color TEXT = new Color(0xDFE1E5);
    public static final Color WARNING = new Color(0xE5A44A);
    public static final Color CURSOR = new Color(0x3574F0);
    public static final Color PLAYING = new Color(0x35, 0x74, 0xF0, 0x40);

    private static final Font FRET_FONT = new Font(Font.MONOSPACED, Font.BOLD, 11);

    private TabPainter() {
    }

    public static void paint(
            Graphics2D g, TabLayout layout, Track track, Cursor cursor, Optional<BeatPosition> playing) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        paintBackground(g);
        for (int m = 0; m < track.measures().size(); m++) {
            paintMeasure(g, layout, track, m);
            paintNotes(g, layout, track.measure(m), m);
        }
        playing.ifPresent(position -> paintPlaying(g, layout, cursor, position));
        paintCursor(g, layout, cursor);
    }

    private static void paintBackground(Graphics2D g) {
        g.setColor(BACKGROUND);
        g.fill(g.getClipBounds());
    }

    private static void paintMeasure(Graphics2D g, TabLayout layout, Track track, int m) {
        Measure measure = track.measure(m);
        Rectangle bounds = layout.measureBounds(m);
        int stringCount = track.tuning().stringCount();

        g.setColor(STRING);
        for (int s = 1; s <= stringCount; s++) {
            int y = layout.stringY(m, s);
            g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
        }

        int top = layout.stringY(m, 1);
        int bottom = layout.stringY(m, stringCount);
        g.setColor(BAR_LINE);
        g.drawLine(bounds.x, top, bounds.x, bottom);
        g.drawLine(bounds.x + bounds.width, top, bounds.x + bounds.width, bottom);

        g.setColor(measure.isComplete() ? TEXT : WARNING);
        g.drawString(String.valueOf(m + 1), bounds.x + 2, bounds.y - 6);
    }

    private static void paintNotes(Graphics2D g, TabLayout layout, Measure measure, int m) {
        for (int b = 0; b < measure.beats().size(); b++) {
            Beat beat = measure.beat(b);
            for (Note note : beat.notes()) {
                paintNote(g, layout, m, b, note);
            }
        }
    }

    private static void paintNote(Graphics2D g, TabLayout layout, int m, int b, Note note) {
        Rectangle beatBounds = layout.beatBounds(m, b);
        int centerX = beatBounds.x + beatBounds.width / 2;
        int y = layout.stringY(m, note.string());
        String fret = String.valueOf(note.fret());

        g.setFont(FRET_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(fret);
        int cutWidth = textWidth + 4;
        int cutHeight = TabLayout.STRING_SPACING - 2;

        g.setColor(BACKGROUND);
        g.fillRect(centerX - cutWidth / 2, y - cutHeight / 2, cutWidth, cutHeight);

        g.setColor(TEXT);
        int textX = centerX - textWidth / 2;
        int textY = y + (metrics.getAscent() - metrics.getDescent()) / 2;
        g.drawString(fret, textX, textY);
    }

    private static void paintPlaying(Graphics2D g, TabLayout layout, Cursor cursor, BeatPosition playing) {
        if (playing.track() != cursor.track()) {
            return;
        }
        Rectangle beat = layout.beatBounds(playing.measure(), playing.beat());
        int half = TabLayout.STRING_SPACING / 2;

        g.setColor(PLAYING);
        g.fillRect(beat.x, beat.y - half, beat.width, beat.height + 2 * half);
    }

    private static void paintCursor(Graphics2D g, TabLayout layout, Cursor cursor) {
        Rectangle beat = layout.beatBounds(cursor.measure(), cursor.beat());
        int x = beat.x + 2;
        int y = layout.stringY(cursor.measure(), cursor.string()) - TabLayout.STRING_SPACING / 2 + 1;
        int w = beat.width - 4;
        int h = TabLayout.STRING_SPACING - 2;

        g.setColor(CURSOR);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, w, h);
    }
}
