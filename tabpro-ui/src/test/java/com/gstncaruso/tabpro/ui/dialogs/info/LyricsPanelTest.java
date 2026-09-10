package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import javax.swing.JTabbedPane;
import org.junit.jupiter.api.Test;

class LyricsPanelTest {

    private final List<String> trackNames = List.of("Guitarra", "Bajo", "Voz");

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new LyricsPanel(trackNames, Lyrics.none()));
    }

    @Test
    void startsOnTheTrackTheLyricsAlreadyPointTo() {
        Lyrics lyrics = Lyrics.none().onTrack(2);

        LyricsPanel panel = new LyricsPanel(trackNames, lyrics);

        assertEquals(2, panel.selectedTrackIndex());
    }

    @Test
    void toLyricsCarriesTheChosenTrack() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());

        panel.selectTrack(1);

        assertEquals(1, panel.toLyrics().trackIndex());
    }

    @Test
    void toLyricsCarriesEachLinesTextAndStartingMeasure() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());

        panel.setLine(0, new LyricLine(3, "a sto-ry"));
        panel.setLine(4, new LyricLine(10, "fi-nal"));

        Lyrics result = panel.toLyrics();
        assertEquals(new LyricLine(3, "a sto-ry"), result.line(0));
        assertEquals(new LyricLine(10, "fi-nal"), result.line(4));
    }

    @Test
    void hasExactlyFiveLines() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());

        assertEquals(LyricLine.MAX_LINES, panel.toLyrics().lines().size());
    }

    @Test
    void lineBreaksSurviveGoingThroughTheDialog() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());
        LyricLine multilinea = new LyricLine(1, "first line\nsecond line");

        panel.setLine(0, multilinea);

        assertEquals(multilinea, panel.line(0));
    }

    @Test
    void eachLineIsItsOwnTabNamedLikeGP5() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());

        JTabbedPane lineTabs = findTabbedPane(panel);

        assertNotNull(lineTabs, "could not find the line tabs");
        assertEquals(LyricLine.MAX_LINES, lineTabs.getTabCount());
        for (int index = 0; index < LyricLine.MAX_LINES; index++) {
            assertEquals("Línea " + (index + 1), lineTabs.getTitleAt(index));
        }
    }

    private static JTabbedPane findTabbedPane(Container root) {
        if (root instanceof JTabbedPane tabs) {
            return tabs;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                JTabbedPane found = findTabbedPane(container);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
