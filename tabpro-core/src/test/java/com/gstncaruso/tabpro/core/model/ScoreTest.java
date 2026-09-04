package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreTest {

    @Test
    void aBlankScoreHasOneGuitarTrackAndTempo120() {
        Score score = Score.blank();
        assertEquals("", score.title());
        assertEquals(120, score.tempo());
        assertEquals(1, score.tracks().size());
        assertEquals(Track.standardGuitar("Guitarra"), score.track(0));
    }

    @Test
    void replacesATrack() {
        Score score = Score.blank();
        Track newTrack = Track.standardGuitar("Bajo");
        Score replaced = score.withTrack(0, newTrack);
        assertEquals(newTrack, replaced.track(0));
    }

    @Test
    void changesTempo() {
        Score score = Score.blank();
        Score changed = score.withTempo(140);
        assertEquals(140, changed.tempo());
    }

    @Test
    void changesTitle() {
        Score score = Score.blank();
        Score changed = score.withTitle("Mi cancion");
        assertEquals("Mi cancion", changed.title());
    }

    @Test
    void rejectsANonPositiveTempo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Score("", 0, List.of(Track.standardGuitar("Guitarra"))));
    }

    @Test
    void rejectsAScoreWithoutTracks() {
        assertThrows(IllegalArgumentException.class, () -> new Score("", 120, List.of()));
    }
}
