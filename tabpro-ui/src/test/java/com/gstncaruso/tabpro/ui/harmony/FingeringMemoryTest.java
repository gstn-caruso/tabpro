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

    private static final List<Integer> E_BARRE_SHAPE = List.of(1, 1, 2, 3, 3, 1);

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
    private final FingeringMemory memory = new FingeringMemory(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void aShapeThatWasNeverCorrectedHasNoStoredFingering() {
        assertTrue(memory.fingeringFor(E_BARRE_SHAPE).isEmpty());
    }

    @Test
    void remembersTheFingeringThatWasCorrected() {
        List<Finger> fingering = Arrays.asList(
                Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX);

        memory.remember(E_BARRE_SHAPE, fingering);

        assertEquals(fingering, memory.fingeringFor(E_BARRE_SHAPE).orElseThrow());
    }

    @Test
    void differentShapesDoNotShareFingering() {
        memory.remember(E_BARRE_SHAPE, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        assertTrue(memory.fingeringFor(List.of(0, 1, 2, 2, 0, -1)).isEmpty());
    }

    @Test
    void aNewFingeringReplacesThePrevious() {
        memory.remember(E_BARRE_SHAPE, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        memory.remember(E_BARRE_SHAPE, Arrays.asList(Finger.THUMB, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.THUMB));

        assertEquals(Finger.THUMB, memory.fingeringFor(E_BARRE_SHAPE).orElseThrow().get(0));
    }

    @Test
    void aMissingFingerIsStoredAsNull() {
        List<Finger> fingering = Arrays.asList(Finger.INDEX, null, Finger.MIDDLE, null, null, null);

        memory.remember(List.of(1, 0, 2, -1, -1, -1), fingering);

        assertEquals(fingering, memory.fingeringFor(List.of(1, 0, 2, -1, -1, -1)).orElseThrow());
    }

    @Test
    void whatWasStoredSurvivesANewMemory() {
        memory.remember(E_BARRE_SHAPE, Arrays.asList(Finger.INDEX, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE, Finger.INDEX));

        FingeringMemory anotherInstance = new FingeringMemory(scratch);

        assertEquals(
                Finger.INDEX,
                anotherInstance.fingeringFor(E_BARRE_SHAPE).orElseThrow().get(0));
    }
}
