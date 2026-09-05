package com.gstncaruso.tabpro.format.exchange.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import org.junit.jupiter.api.Test;

class AsciiTabImporterTest {

    private final AsciiTabImporter importer = new AsciiTabImporter();

    @Test
    void ignoresCommentsAroundTheTabAndReadsTwoNotesWithAFixedRhythm() {
        // celda de 8 columnas: nota en la columna 2 (traste 5) y en la columna 5 (traste 0)
        String block = block(6, "--5--0--");
        String text = "Un comentario antes.\n\n" + block + "\nComentario despues.\n";

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        Track track = score.track(0);
        assertEquals(Tuning.standard(), track.tuning());
        Beat first = track.measure(0).beat(0);
        Beat second = track.measure(0).beat(1);
        assertEquals(new Duration(NoteValue.EIGHTH, false), first.duration());
        assertEquals(List.of(new Note(1, 5)), first.notes());
        assertEquals(List.of(new Note(1, 0)), second.notes());
    }

    @Test
    void readsMultiDigitFrets() {
        String text = block(6, "-12------");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(12, track(score).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void infersDurationFromTheSpacingBetweenColumns() {
        String text = block(6, "--5--0--");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard().withRhythm(RhythmStrategy.fromSpacing()));

        List<Beat> beats = track(score).measure(0).beats();
        // hay un silencio de negra antes de la primera nota (arranca en la columna 2 de 8)
        assertEquals(new Duration(NoteValue.QUARTER, false), beats.get(0).duration());
        assertEquals(List.of(new Note(1, 5)), beats.get(1).notes());
        assertEquals(new Duration(NoteValue.QUARTER, true).ticks(), beats.get(1).duration().ticks());
        assertEquals(List.of(new Note(1, 0)), beats.get(2).notes());
    }

    @Test
    void mergesConsecutiveBlocksWithTheSameStringCountIntoOneTrack() {
        String text = block(6, "-5-") + "\n" + block(6, "-0-");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(1, score.trackCount());
        assertEquals(2, score.track(0).measureCount());
    }

    @Test
    void startsANewTrackWhenTheStringCountChanges() {
        String text = block(6, "-5-") + "\n" + block(4, "-3-");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(2, score.trackCount());
        assertEquals(6, score.track(0).stringCount());
        assertEquals(4, score.track(1).stringCount());
        assertEquals(Tuning.standardBass(), score.track(1).tuning());
    }

    @Test
    void rejectsATextWithoutAnyTab() {
        assertThrows(ScoreFileException.class, () -> importer.importScore("no hay tablatura aca", AsciiTabImportOptions.standard()));
    }

    /** Un bloque de stringCount lineas: la primera cuerda lleva firstStringContent entre barras, el resto va vacio. */
    private static String block(int stringCount, String firstStringContent) {
        StringBuilder text = new StringBuilder();
        text.append('|').append(firstStringContent).append('|').append('\n');
        String emptyLine = "|" + "-".repeat(firstStringContent.length()) + "|";
        for (int i = 1; i < stringCount; i++) {
            text.append(emptyLine).append('\n');
        }
        return text.toString();
    }

    private static Track track(Score score) {
        return score.track(0);
    }
}
