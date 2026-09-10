package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordDiagramGenerator;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class BarrePreferenceTest {

    @Test
    void anyAcceptsEverything() {
        for (ChordDiagram diagram : fDiagrams()) {
            assertTrue(BarrePreference.ANY.accepts(diagram));
        }
    }

    @Test
    void forcingBarreOnlyAcceptsTheOnesThatNeedIt() {
        List<ChordDiagram> diagrams = fDiagrams();

        assertTrue(
                diagrams.stream().anyMatch(ChordDiagram::requiresBarre),
                "hace falta al menos un diagrama con cejilla para probar esto");
        for (ChordDiagram diagram : diagrams) {
            assertEquals(diagram.requiresBarre(), BarrePreference.FORCE.accepts(diagram));
        }
    }

    @Test
    void forbiddingBarreOnlyAcceptsTheOnesThatDoNotNeedIt() {
        for (ChordDiagram diagram : fDiagrams()) {
            assertFalse(BarrePreference.FORBID.accepts(diagram) && diagram.requiresBarre());
        }
    }

    private static List<ChordDiagram> fDiagrams() {
        Chord fChord = Chord.of(PitchClass.of("F"), ChordType.MAJOR);
        return ChordDiagramGenerator.generate(
                fChord, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX);
    }
}
