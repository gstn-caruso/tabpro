package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordNamerTest {

    @Test
    void reconoceElAcordeAbiertoDeDoMayor() {
        ChordDiagram diagrama = ChordDiagram.named("?", List.of(0, 1, 0, 2, 3, -1));

        List<Chord> nombres = ChordNamer.namesFor(diagrama, Tuning.standard());

        assertFalse(nombres.isEmpty());
        assertEquals("C", nombres.get(0).name());
    }

    @Test
    void reconoceElAcordeAbiertoDeLaMenor() {
        ChordDiagram diagrama = ChordDiagram.named("?", List.of(0, 1, 2, 2, 0, -1));

        List<Chord> nombres = ChordNamer.namesFor(diagrama, Tuning.standard());

        assertEquals("Am", nombres.get(0).name());
    }

    @Test
    void unaInversionSeNombraConElBajoIndicado() {
        // Do mayor con Mi en el bajo: cuerda1=0(Mi), cuerda2=1(Do), cuerda3=0(Sol), cuerda4=2(Mi), resto mudas.
        ChordDiagram doConMiEnElBajo = ChordDiagram.named("?", List.of(0, 1, 0, 2, -1, -1));

        List<Chord> nombres = ChordNamer.namesFor(doConMiEnElBajo, Tuning.standard());

        assertTrue(nombres.stream().anyMatch(c -> c.name().equals("C/E")));
    }

    @Test
    void unDiagramaSimetricoTieneVariosNombresAlternativos() {
        // Do disminuido 7 (Do Mib Solb La) es simetrico: tambien es Mib7, Solb7 y La7 disminuidos.
        Chord base = Chord.of(PitchClass.of("Do"), ChordType.DIMINISHED_SEVENTH);
        ChordDiagram diagrama =
                ChordDiagramGenerator.generate(base, Tuning.standard()).stream().findFirst().orElseThrow();

        List<Chord> nombres = ChordNamer.namesFor(diagrama, Tuning.standard());

        assertTrue(nombres.size() >= 4, "un disminuido 7 tiene cuatro nombres igual de validos");
    }
}
