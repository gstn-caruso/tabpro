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
    void cualquieraAceptaTodo() {
        for (ChordDiagram diagrama : diagramasDeFa()) {
            assertTrue(BarrePreference.ANY.accepts(diagrama));
        }
    }

    @Test
    void forzarCejillaSoloAceptaLosQueLaNecesitan() {
        List<ChordDiagram> diagramas = diagramasDeFa();

        assertTrue(
                diagramas.stream().anyMatch(ChordDiagram::requiresBarre),
                "hace falta al menos un diagrama con cejilla para probar esto");
        for (ChordDiagram diagrama : diagramas) {
            assertEquals(diagrama.requiresBarre(), BarrePreference.FORCE.accepts(diagrama));
        }
    }

    @Test
    void prohibirCejillaSoloAceptaLosQueNoLaNecesitan() {
        for (ChordDiagram diagrama : diagramasDeFa()) {
            assertFalse(BarrePreference.FORBID.accepts(diagrama) && diagrama.requiresBarre());
        }
    }

    private static List<ChordDiagram> diagramasDeFa() {
        Chord fa = Chord.of(PitchClass.of("F"), ChordType.MAJOR);
        return ChordDiagramGenerator.generate(
                fa, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX);
    }
}
