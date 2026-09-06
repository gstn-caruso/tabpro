package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import java.util.List;
import org.junit.jupiter.api.Test;

class LyricsPanelTest {

    private final List<String> trackNames = List.of("Guitarra", "Bajo", "Voz");

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

        panel.setLine(0, new LyricLine(3, "una can-cion"));
        panel.setLine(4, new LyricLine(10, "fi-nal"));

        Lyrics result = panel.toLyrics();
        assertEquals(new LyricLine(3, "una can-cion"), result.line(0));
        assertEquals(new LyricLine(10, "fi-nal"), result.line(4));
    }

    @Test
    void hasExactlyFiveLines() {
        LyricsPanel panel = new LyricsPanel(trackNames, Lyrics.none());

        assertEquals(LyricLine.MAX_LINES, panel.toLyrics().lines().size());
    }
}
