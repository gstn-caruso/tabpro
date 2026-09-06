package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * El lector completo: arma un archivo TEF3 minimo a mano (no hay muestras
 * reales de TablEdit disponibles) y confirma que el resultado sea la
 * partitura que ese archivo describe.
 */
class TabEditFileTest {

    private final TabEditFile file = new TabEditFile();

    @Test
    void leeElTituloElTempoYLaEstructuraBasica() {
        Score score = file.read(minimalScore().bytes());

        assertEquals("Cancion de prueba", score.title());
        assertEquals(140, score.tempo());
        assertEquals(1, score.trackCount());
        assertEquals(1, score.measureCount());
    }

    @Test
    void leeLaPistaYSuAfinacion() {
        Score score = file.read(minimalScore().bytes());

        assertEquals("Guitarra", score.track(0).name());
        assertEquals(6, score.track(0).stringCount());
        assertEquals(Tuning.standard().strings(), score.track(0).tuning().strings());
        assertFalse(score.track(0).isPercussion());
    }

    @Test
    void leeLasCuatroNegrasDelCompas() {
        Score score = file.read(minimalScore().bytes());

        Measure measure = score.track(0).measure(0);
        assertEquals(TimeSignature.fourFour(), measure.timeSignature());
        assertEquals(4, measure.beats().size());
        assertTrue(measure.isComplete());
        assertEquals(List.of(3, 5, 7, 8), fretsOf(measure));
    }

    private static List<Integer> fretsOf(Measure measure) {
        List<Integer> frets = new ArrayList<>();
        for (Beat beat : measure.beats()) {
            for (Note note : beat.notes()) {
                frets.add(note.fret());
            }
        }
        return frets;
    }

    @Test
    void unArchivoQueNoEsTablEditFallaConMensajeClaro(@TempDir Path folder) throws Exception {
        Path path = folder.resolve("roto.tef");
        Files.writeString(path, "esto no es un archivo de TablEdit, ni de lejos");

        assertThrows(ScoreFileException.class, () -> file.read(path));
    }

    @Test
    void unArchivoTruncadoFallaConMensajeClaro(@TempDir Path folder) throws Exception {
        Path path = folder.resolve("cortado.tef");
        byte[] whole = minimalScore().bytes();
        Files.write(path, java.util.Arrays.copyOf(whole, whole.length / 2));

        assertThrows(ScoreFileException.class, () -> file.read(path));
    }

    @Test
    void unaPistaDePercusionSeRechazaConMensajeClaro() {
        byte[] bytes = TabEditFixtures.scoreWithPercussionTrack();

        ScoreFileException exception = assertThrows(ScoreFileException.class, () -> file.read(bytes));

        assertTrue(exception.getMessage().contains("percusi"));
    }

    private static TabEditFileWriter minimalScore() {
        return TabEditFixtures.oneTrackOneMeasureScore(
                "Cancion de prueba", 140, "Guitarra", List.of(3, 5, 7, 8));
    }
}
