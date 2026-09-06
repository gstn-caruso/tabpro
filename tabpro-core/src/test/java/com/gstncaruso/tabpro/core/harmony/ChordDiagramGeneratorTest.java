package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramGeneratorTest {

    @Test
    void encuentraLaDigitacionEstandarDeLaMenorEnAfinacionEstandar() {
        Chord laMenor = Chord.of(PitchClass.of("La"), ChordType.MINOR);
        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(laMenor, Tuning.standard());

        assertTrue(diagramas.stream().anyMatch(d -> d.frets().equals(List.of(0, 1, 2, 2, 0, -1))));
    }

    @Test
    void ningunDiagramaEstiraMasDeLoPermitido() {
        Chord sol7 = Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH);
        int maxSpan = 3;
        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(sol7, Tuning.standard(), maxSpan);

        assertFalse(diagramas.isEmpty());
        assertTrue(diagramas.stream().allMatch(d -> d.fretSpan() <= maxSpan));
    }

    @Test
    void ningunaCuerdaTocaUnaNotaAjenaAlAcorde() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(doMayor, Tuning.standard());

        for (ChordDiagram diagrama : diagramas) {
            for (int cuerda = 1; cuerda <= diagrama.stringCount(); cuerda++) {
                if (diagrama.isPlayed(cuerda)) {
                    int semitono =
                            (Tuning.standard().pitchOfString(cuerda).midiNumber() + diagrama.fretOfString(cuerda)) % 12;
                    assertTrue(doMayor.formulaSemitones().contains(semitono));
                }
            }
        }
    }

    @Test
    void elBajoIndicadoSuenaSiempreEnLaCuerdaMasGrave() {
        Chord doConMiEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(doConMiEnElBajo, Tuning.standard());

        assertFalse(diagramas.isEmpty());
        for (ChordDiagram diagrama : diagramas) {
            int cuerdaMasGrave = ultimaCuerdaQueSuena(diagrama);
            int semitono = (Tuning.standard().pitchOfString(cuerdaMasGrave).midiNumber()
                            + diagrama.fretOfString(cuerdaMasGrave))
                    % 12;
            assertEquals(PitchClass.of("Mi").semitone(), semitono);
        }
    }

    @Test
    void elFiltroSimpleNuncaDevuelveAcordesConCejilla() {
        Chord fa = Chord.of(PitchClass.of("Fa"), ChordType.MAJOR);
        List<ChordDiagram> simples =
                ChordDiagramGenerator.generate(fa, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.SIMPLE);

        assertTrue(simples.stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void estanOrdenadosPorDificultadCreciente() {
        Chord miMenor = Chord.of(PitchClass.of("Mi"), ChordType.MINOR);
        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(miMenor, Tuning.standard());

        for (int i = 1; i < diagramas.size(); i++) {
            assertTrue(diagramas.get(i - 1).difficultyScore() <= diagramas.get(i).difficultyScore());
        }
    }

    @Test
    void generaDiagramasParaCualquierAfinacion() {
        Chord reMayor = Chord.of(PitchClass.of("Re"), ChordType.MAJOR);
        Tuning dadgad = TuningLibrary.guitars().stream()
                .filter(t -> t.name().equals("DADGAD"))
                .findFirst()
                .orElseThrow();

        List<ChordDiagram> diagramas = ChordDiagramGenerator.generate(reMayor, dadgad);

        assertFalse(diagramas.isEmpty());
    }

    private static int ultimaCuerdaQueSuena(ChordDiagram diagrama) {
        for (int cuerda = diagrama.stringCount(); cuerda >= 1; cuerda--) {
            if (diagrama.isPlayed(cuerda)) {
                return cuerda;
            }
        }
        throw new IllegalStateException("un diagrama valido siempre tiene alguna cuerda sonando");
    }
}
