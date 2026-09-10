package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeasureNumberPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aCompleteMeasureWritesItsNumberInCoral() {
        RecordingCanvas canvas = paintMeasureNumber(guitarWith(fullMeasure()));

        assertTrue(canvas.drawsColor(ScoreColors.MEASURE_NUMBER));
        assertFalse(canvas.drawsColor(ScoreColors.MUTED_INK));
    }

    @Test
    void anIncompleteMeasureKeepsWarningInsteadOfCoral() {
        RecordingCanvas canvas = paintMeasureNumber(guitarWith(incompleteMeasure()));

        assertTrue(canvas.drawsColor(ScoreColors.WARNING));
        assertFalse(canvas.drawsColor(ScoreColors.MEASURE_NUMBER));
    }

    private static RecordingCanvas paintMeasureNumber(Track track) {
        ScoreLayout layout = ScoreLayout.of(new Score("", 120, List.of(track)), WIDTH);
        RecordingCanvas canvas = new RecordingCanvas();
        TabPainter.paintMeasureNumber(canvas, layout, track, 0, 0);
        return canvas;
    }

    private static Measure fullMeasure() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
    }

    private static Measure incompleteMeasure() {
        return new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
    }

    private static Track guitarWith(Measure measure) {
        return new Track("Guitarra", Tuning.standard(), Channel.playing(Track.GUITAR_PROGRAM), List.of(measure));
    }
}
