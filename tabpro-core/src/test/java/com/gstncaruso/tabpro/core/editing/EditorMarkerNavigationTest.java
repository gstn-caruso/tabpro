package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Los marcadores son la forma rapida de moverse por las partes de la partitura. */
class EditorMarkerNavigationTest {

    private Editor editor;

    @BeforeEach
    void scoreWithMarkersOnTheFirstAndFourthMeasure() {
        Measure empty = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track guitar = Track.standardGuitar("Guitarra");
        editor = new Editor(new Score("", 120, List.of(new Track(
                "Guitarra", guitar.tuning(), guitar.channel(), List.of(empty, empty, empty, empty, empty)))));
        editor.moveTo(0, 0, 1);
        editor.setMarker(Marker.named("Intro"));
        editor.moveTo(3, 0, 1);
        editor.setMarker(Marker.named("Estribillo"));
        editor.moveTo(0, 0, 1);
    }

    @Test
    void goesToTheNextMarker() {
        editor.moveToNextMarker();

        assertEquals(3, editor.cursor().measure());
    }

    @Test
    void staysOnTheLastMarkerWhenThereIsNoNextOne() {
        editor.moveTo(3, 0, 1);

        editor.moveToNextMarker();

        assertEquals(3, editor.cursor().measure());
    }

    @Test
    void goesBackToThePreviousMarker() {
        editor.moveTo(4, 0, 1);

        editor.moveToPreviousMarker();

        assertEquals(3, editor.cursor().measure());
    }

    @Test
    void staysWhereItIsWhenThereIsNoMarkerBehind() {
        editor.moveTo(2, 0, 1);

        editor.moveToPreviousMarker();

        assertEquals(0, editor.cursor().measure());
    }

    @Test
    void aScoreWithoutMarkersDoesNotMoveTheCursor() {
        Editor plain = new Editor(Score.blank());

        plain.moveToNextMarker();
        plain.moveToPreviousMarker();

        assertEquals(0, plain.cursor().measure());
    }
}
