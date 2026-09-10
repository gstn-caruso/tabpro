package com.gstncaruso.tabpro.format.exchange.ascii;

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
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * ASCII tab has no way to notate a rest, so a note followed by a rest looks identical to a
 * longer note: the manual warns about this ambiguity, and there is no way to avoid it by
 * reading the spacing alone. That is why the round trip that is guaranteed lossless is one for
 * a track without rests, with durations that are multiples of an eighth note.
 */
class AsciiTabRoundTripTest {

    private final AsciiTabExporter exporter = new AsciiTabExporter();
    private final AsciiTabImporter importer = new AsciiTabImporter();

    @Test
    void aTrackWithoutRestsRoundTripsExactlyThroughSpacing() {
        Beat chord = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 5), new Note(6, 0));
        Beat first = Beat.of(Duration.of(NoteValue.EIGHTH), new Note(1, 12));
        Beat second = Beat.of(Duration.of(NoteValue.EIGHTH), new Note(2, 3));
        Beat third = Beat.of(Duration.of(NoteValue.QUARTER), new Note(4, 7));
        Beat fourth = Beat.of(Duration.of(NoteValue.QUARTER), new Note(5, 2));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(chord, first, second, third, fourth));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score original = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(original, AsciiTabExportOptions.standard());
        int intervalsPerQuarterNoteMatchingTheExportersColumnWidth = 4;
        Score imported = importer.importScore(
                tab,
                AsciiTabImportOptions.standard()
                        .withRhythm(RhythmStrategy.fromSpacing(intervalsPerQuarterNoteMatchingTheExportersColumnWidth)));

        assertEquals(List.of(chord, first, second, third, fourth), imported.track(0).measure(0).beats());
    }
}
