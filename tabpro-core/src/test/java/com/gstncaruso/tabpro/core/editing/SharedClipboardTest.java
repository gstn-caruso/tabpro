package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SharedClipboardTest {

    @Test
    void copyingInOneEditorAndPastingInAnotherSharesTheMeasures() {
        ClipboardStorage sharedPlace = ClipboardStorage.inMemory();
        Editor first = new Editor(new Score("Origen", 120, List.of(Track.standardGuitar("Guitarra"))), sharedPlace);
        Editor second = new Editor(new Score("Destino", 120, List.of(Track.standardGuitar("Guitarra"))), sharedPlace);

        first.setFret(5);
        first.copy(false);

        second.paste(PasteOptions.replacingOnce());

        assertEquals(
                Optional.of(5),
                second.score().track(0).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
    }

    @Test
    void editorsWithTheirOwnStorageDoNotShareAnything() {
        Editor first = new Editor(new Score("Origen", 120, List.of(Track.standardGuitar("Guitarra"))));
        Editor second = new Editor(new Score("Destino", 120, List.of(Track.standardGuitar("Guitarra"))));

        first.setFret(5);
        first.copy(false);

        second.paste(PasteOptions.replacingOnce());

        assertEquals(
                Optional.empty(),
                second.score().track(0).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
    }
}
