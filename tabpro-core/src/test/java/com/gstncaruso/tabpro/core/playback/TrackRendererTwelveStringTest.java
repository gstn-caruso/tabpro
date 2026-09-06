package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Una guitarra de doce cuerdas suena doblada: al unisono las dos cuerdas mas
 * agudas, a la octava las cuatro graves. La tablatura no cambia, asi que esto
 * se ve solo en lo que realmente suena.
 */
class TrackRendererTwelveStringTest {

    private static final Duration QUARTER = Duration.quarter();

    @Test
    void unaGuitarraNormalNoDoblaNingunaCuerda() {
        Score score = scoreWithLeadBeats(false, Beat.of(QUARTER, new Note(1, 0)));

        assertEquals(1, notesOf(score).size());
    }

    @Test
    void lasCuerdasAgudasDeUnaDoceCuerdasSuenanAlUnisono() {
        Score score = scoreWithLeadBeats(true, Beat.of(QUARTER, new Note(1, 3)));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
        assertEquals(notes.get(0).pitch(), notes.get(1).pitch());
    }

    @Test
    void lasCuerdasGravesDeUnaDoceCuerdasSuenanUnaOctavaArriba() {
        Score score = scoreWithLeadBeats(true, Beat.of(QUARTER, new Note(3, 2)));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
        assertEquals(notes.get(0).pitch().midiNumber() + 12, notes.get(1).pitch().midiNumber());
    }

    private List<ScheduledNote> notesOf(Score score) {
        return Timeline.of(score).tracks().get(0).notes();
    }

    private Score scoreWithLeadBeats(boolean twelveString, Beat... beatsInOrder) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beatsInOrder));
        Track track = Track.standardGuitar("Guitarra")
                .withMeasure(0, measure)
                .mappingSettings(settings -> settings.withTwelveString(twelveString));
        return Score.blank().withTrack(0, track);
    }
}
