package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import java.util.List;
import org.junit.jupiter.api.Test;

class PitchClassesTest {

    @Test
    void ofreceLasDoceNotasCromaticasEmpezandoEnDo() {
        List<PitchClass> notas = PitchClasses.chromatic();

        assertEquals(12, notas.size());
        assertEquals(PitchClass.of("C"), notas.get(0));
        assertEquals(0, notas.get(0).semitone());
    }

    @Test
    void cadaNotaEstaUnSemitonoMasArribaQueLaAnterior() {
        List<PitchClass> notas = PitchClasses.chromatic();

        for (int i = 0; i < notas.size(); i++) {
            assertEquals(i, notas.get(i).semitone());
        }
    }
}
