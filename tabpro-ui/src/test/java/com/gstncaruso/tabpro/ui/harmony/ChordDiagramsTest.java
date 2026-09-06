package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramsTest {

    @Test
    void unBeatSinNotasQuedaMudoEnTodasLasCuerdas() {
        ChordDiagram diagrama = ChordDiagrams.fromBeat(Beat.rest(Duration.quarter()), Tuning.standard());

        for (int cuerda = 1; cuerda <= 6; cuerda++) {
            assertEquals(ChordDiagram.MUTED, diagrama.fretOfString(cuerda));
        }
    }

    @Test
    void cadaNotaDelBeatQuedaEnSuCuerda() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2), new Note(4, 2));

        ChordDiagram diagrama = ChordDiagrams.fromBeat(beat, Tuning.standard());

        assertEquals(0, diagrama.fretOfString(6));
        assertEquals(2, diagrama.fretOfString(5));
        assertEquals(2, diagrama.fretOfString(4));
        assertEquals(ChordDiagram.MUTED, diagrama.fretOfString(3));
    }

    @Test
    void empiezaEnElTrasteMasBajoQuePisaSiNoHayCuerdasAlAire() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 5), new Note(5, 7));

        ChordDiagram diagrama = ChordDiagrams.fromBeat(beat, Tuning.standard());

        assertEquals(5, diagrama.baseFret());
    }

    @Test
    void cambiaElTrasteBaseSinTocarLosTrastesYaPisados() {
        ChordDiagram diagrama = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        ChordDiagram movido = ChordDiagrams.withBaseFret(diagrama, 5);

        assertEquals(5, movido.baseFret());
        assertEquals(diagrama.frets(), movido.frets());
        assertEquals(diagrama.name(), movido.name());
    }
}
