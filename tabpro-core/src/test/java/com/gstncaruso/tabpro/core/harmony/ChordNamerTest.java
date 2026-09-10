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
    void recognizesTheOpenCMajorChord() {
        ChordDiagram diagram = ChordDiagram.named("?", List.of(0, 1, 0, 2, 3, -1));

        List<Chord> names = ChordNamer.namesFor(diagram, Tuning.standard());

        assertFalse(names.isEmpty());
        assertEquals("C", names.get(0).name());
    }

    @Test
    void recognizesTheOpenAMinorChord() {
        ChordDiagram diagram = ChordDiagram.named("?", List.of(0, 1, 2, 2, 0, -1));

        List<Chord> names = ChordNamer.namesFor(diagram, Tuning.standard());

        assertEquals("Am", names.get(0).name());
    }

    @Test
    void anInversionIsNamedWithTheIndicatedBass() {
        ChordDiagram cWithEInTheBass = ChordDiagram.named("?", List.of(0, 1, 0, 2, -1, -1));

        List<Chord> names = ChordNamer.namesFor(cWithEInTheBass, Tuning.standard());

        assertTrue(names.stream().anyMatch(c -> c.name().equals("C/E")));
    }

    @Test
    void aSymmetricDiagramHasSeveralAlternativeNames() {
        Chord base = Chord.of(PitchClass.of("Do"), ChordType.DIMINISHED_SEVENTH);
        ChordDiagram diagram =
                ChordDiagramGenerator.generate(base, Tuning.standard()).stream().findFirst().orElseThrow();

        List<Chord> names = ChordNamer.namesFor(diagram, Tuning.standard());

        assertTrue(names.size() >= 4, "a diminished 7 has four equally valid names");
    }
}
