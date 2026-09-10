package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabSymbolLabelPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void theTremoloPickingLabelFitsTheScoreFont() {
        Note note = new Note(1, 0).withEffects(NoteEffects.none().withTremoloPicking(TremoloPicking.at(NoteValue.SIXTEENTH)));

        assertOnlyLabelIsDisplayable(Beat.of(Duration.quarter(), note));
    }

    @Test
    void theWideVibratoLabelFitsTheScoreFont() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(BeatEffects.none().withWideVibrato(true));

        assertOnlyLabelIsDisplayable(beat);
    }

    private static void assertOnlyLabelIsDisplayable(Beat beat) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        TabSymbolPainter.paintMeasure(canvas, layout, track, 0, 0);

        List<RecordingCanvas.DrawnText> drawnTexts = canvas.drawnTexts();
        assertEquals(1, drawnTexts.size(), "the effect has to write exactly one label");
        RecordingCanvas.DrawnText drawnText = drawnTexts.get(0);
        assertEquals(-1, drawnText.font().canDisplayUpTo(drawnText.text()),
                "label \"" + drawnText.text() + "\" has a character that "
                        + drawnText.font().getFontName() + " does not know how to show");
    }
}
