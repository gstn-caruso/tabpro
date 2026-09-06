package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FingeringMemoryTest {

    /** Cejilla de Fa: forma de Mi (ver ChordDiagram#shape). */
    private static final List<Integer> FORMA_CEJILLA_DE_MI = List.of(1, 1, 2, 3, 3, 1);

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName());
    private final FingeringMemory memory = new FingeringMemory(scratch);

    @AfterEach
    void limpiarElNodoDePrueba() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void unaFormaQueNuncaSeCorrigioNoTieneDigitacionGuardada() {
        assertTrue(memory.fingeringFor(FORMA_CEJILLA_DE_MI).isEmpty());
    }

    @Test
    void recuerdaLaDigitacionQueSeCorrigio() {
        List<Finger> digitacion = Arrays.asList(
                Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX);

        memory.remember(FORMA_CEJILLA_DE_MI, digitacion);

        assertEquals(digitacion, memory.fingeringFor(FORMA_CEJILLA_DE_MI).orElseThrow());
    }

    @Test
    void formasDistintasNoComparteDigitacion() {
        memory.remember(FORMA_CEJILLA_DE_MI, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        assertTrue(memory.fingeringFor(List.of(0, 1, 2, 2, 0, -1)).isEmpty());
    }

    @Test
    void unaDigitacionNuevaReemplazaLaAnterior() {
        memory.remember(FORMA_CEJILLA_DE_MI, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        memory.remember(FORMA_CEJILLA_DE_MI, Arrays.asList(Finger.THUMB, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.THUMB));

        assertEquals(Finger.THUMB, memory.fingeringFor(FORMA_CEJILLA_DE_MI).orElseThrow().get(0));
    }

    @Test
    void unDedoAusenteSeGuardaComoNull() {
        List<Finger> digitacion = Arrays.asList(Finger.INDEX, null, Finger.MIDDLE, null, null, null);

        memory.remember(List.of(1, 0, 2, -1, -1, -1), digitacion);

        assertEquals(digitacion, memory.fingeringFor(List.of(1, 0, 2, -1, -1, -1)).orElseThrow());
    }

    @Test
    void loQueQuedaGuardadoSobreviveAUnaMemoriaNueva() {
        memory.remember(FORMA_CEJILLA_DE_MI, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        FingeringMemory otraInstancia = new FingeringMemory(scratch);

        assertEquals(
                Finger.INDEX,
                otraInstancia.fingeringFor(FORMA_CEJILLA_DE_MI).orElseThrow().get(0));
    }
}
