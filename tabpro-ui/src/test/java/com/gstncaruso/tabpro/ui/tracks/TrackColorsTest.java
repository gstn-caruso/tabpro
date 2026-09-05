package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TrackColorsTest {

    @Test
    void givesEveryTrackOfAUsualBandItsOwnColour() {
        Set<Color> colours = new HashSet<>();
        for (int track = 0; track < TrackColors.COUNT; track++) {
            colours.add(TrackColors.of(track));
        }

        assertEquals(TrackColors.COUNT, colours.size());
    }

    @Test
    void startsOverOnceItRunsOutOfColours() {
        assertEquals(TrackColors.of(0), TrackColors.of(TrackColors.COUNT));
        assertEquals(TrackColors.of(1), TrackColors.of(TrackColors.COUNT + 1));
    }

    @Test
    void neighbouringTracksNeverShareAColour() {
        for (int track = 0; track < 3 * TrackColors.COUNT; track++) {
            assertNotEquals(TrackColors.of(track), TrackColors.of(track + 1), "pistas vecinas del " + track);
        }
    }

    @Test
    void noTrackWearsTheColourThatMarksWhatIsSounding() {
        for (int track = 0; track < TrackColors.COUNT; track++) {
            assertNotEquals(
                    ScoreColors.PLAYING_MEASURE,
                    TrackColors.of(track),
                    "el rojo esta reservado para el compas que suena");
        }
    }

    @Test
    void rejectsATrackThatIsNotThere() {
        assertThrows(IllegalArgumentException.class, () -> TrackColors.of(-1));
    }

    @Test
    void everyColourReadsOnTheDarkPanel() {
        for (int track = 0; track < TrackColors.COUNT; track++) {
            assertTrue(
                    brightness(TrackColors.of(track)) > brightness(ScoreColors.SURFACE) + 0.25,
                    "el color de la pista " + track + " no se despega del fondo");
        }
    }

    private double brightness(Color color) {
        return (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
    }
}
