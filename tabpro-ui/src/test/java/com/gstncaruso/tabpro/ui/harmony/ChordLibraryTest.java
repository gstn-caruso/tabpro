package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordLibraryTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
    private final ChordLibrary library = new ChordLibrary(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void startsEmpty() {
        assertTrue(library.all().isEmpty());
    }

    @Test
    void addingAChordKeepsIt() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        library.add(am);

        assertEquals(List.of(am), library.all());
    }

    @Test
    void keepsTheFingering() {
        ChordDiagram withFingers = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1))
                .withFingering(java.util.Arrays.asList(null, Finger.INDEX, Finger.MIDDLE, Finger.RING, null, null));

        library.add(withFingers);

        assertEquals(withFingers.fingering(), library.all().get(0).fingering());
    }

    @Test
    void addingSeveralKeepsTheOrderTheyWereAdded() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        ChordDiagram c = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

        library.add(am);
        library.add(c);

        assertEquals(List.of(am, c), library.all());
    }

    @Test
    void removingDeletesTheChosenOne() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        ChordDiagram c = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));
        library.add(am);
        library.add(c);

        library.remove(0);

        assertEquals(List.of(c), library.all());
    }

    @Test
    void updatingReplacesTheChosenOneWithTheNewDiagram() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(am);
        ChordDiagram amBarre = ChordDiagram.named("Am", List.of(5, 5, 5, 7, 7, 5));

        library.update(0, amBarre);

        assertEquals(List.of(amBarre), library.all());
    }

    @Test
    void sortsAlphabeticallyByName() {
        ChordDiagram gChord = ChordDiagram.named("G", List.of(3, 0, 0, 0, 2, 3));
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(gChord);
        library.add(am);

        library.sortByName();

        assertEquals(List.of("Am", "G"), library.all().stream().map(ChordDiagram::name).toList());
    }

    @Test
    void whatWasStoredSurvivesANewLibrary() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(am);

        ChordLibrary anotherInstance = new ChordLibrary(scratch);

        assertEquals(List.of(am), anotherInstance.all());
    }
}
