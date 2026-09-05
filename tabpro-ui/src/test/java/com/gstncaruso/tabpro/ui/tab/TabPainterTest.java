package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TabPainterTest {

    @Test
    void paintsTheBackground() {
        Track track = Track.standardGuitar("Guitarra");
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);

        TabPainter.paint(g, layout, track, new Cursor(0, 0, 0, 1), Optional.empty());

        assertEquals(TabPainter.BACKGROUND.getRGB(), image.getRGB(0, 0));
    }

    @Test
    void paintsStringLinesAcrossTheStaff() {
        Track track = Track.standardGuitar("Guitarra");
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);

        TabPainter.paint(g, layout, track, new Cursor(0, 0, 0, 1), Optional.empty());

        int x = layout.measureBounds(0).x + layout.measureBounds(0).width / 2;
        int y = layout.stringY(0, 3);
        assertEquals(TabPainter.STRING.getRGB(), image.getRGB(x, y));
    }

    @Test
    void outlinesTheCursorCell() {
        Track track = Track.standardGuitar("Guitarra");
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);
        Cursor cursor = new Cursor(0, 0, 0, 1);

        TabPainter.paint(g, layout, track, cursor, Optional.empty());

        Rectangle beat = layout.beatBounds(cursor.measure(), cursor.beat());
        int x = beat.x + 2;
        int y = layout.stringY(cursor.measure(), cursor.string()) - TabLayout.STRING_SPACING / 2 + 1;
        assertEquals(TabPainter.CURSOR.getRGB(), image.getRGB(x + (beat.width - 4) / 2, y));
    }

    @Test
    void highlightsThePlayingBeat() {
        Measure measure = new Measure(
                TimeSignature.fourFour(), List.of(Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(measure));
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);
        Cursor cursor = new Cursor(0, 0, 1, 1);
        BeatPosition playing = new BeatPosition(0, 0, 0);

        TabPainter.paint(g, layout, track, cursor, Optional.of(playing));

        Rectangle beat = layout.beatBounds(0, 0);
        int x = beat.x + beat.width / 2;
        int y = layout.stringY(0, 1) + TabLayout.STRING_SPACING / 2;
        assertNotEquals(TabPainter.BACKGROUND.getRGB(), image.getRGB(x, y));
    }

    @Test
    void cutsTheStringLineWhereANoteIsPlayed() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(3, 5))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(measure));
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);
        Cursor cursor = new Cursor(0, 0, 0, 1);

        TabPainter.paint(g, layout, track, cursor, Optional.empty());

        Rectangle beat = layout.beatBounds(0, 0);
        int x = beat.x + beat.width / 2;
        int y = layout.stringY(0, 3);
        assertNotEquals(TabPainter.STRING.getRGB(), image.getRGB(x, y));
    }

    @Test
    void paintsAScoreWithManyMeasuresWithoutErrors() {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<Beat> beats = List.of(
                    Beat.of(Duration.quarter(), new Note(6, 0)),
                    Beat.of(Duration.quarter(), new Note(5, 2)),
                    Beat.of(Duration.quarter(), new Note(4, 2)),
                    Beat.rest(Duration.quarter()));
            measures.add(new Measure(TimeSignature.fourFour(), beats));
        }
        Track track = new Track("Guitarra", Tuning.standard(), 25, measures);
        TabLayout layout = TabLayout.of(track, 400);
        BufferedImage image = new BufferedImage(400, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);
        Cursor cursor = new Cursor(0, 0, 0, 1);

        assertDoesNotThrow(() -> TabPainter.paint(g, layout, track, cursor, Optional.empty()));
        assertTrue(layout.lineCount() > 1);
    }

    private Graphics2D graphicsOf(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, image.getWidth(), image.getHeight());
        return g;
    }
}
