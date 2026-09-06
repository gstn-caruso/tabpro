package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * El manual permite copiar entre dos sesiones de Guitar Pro ("it is easy to take a
 * track from another file and paste it in your current file"). Tabpro corre un proceso
 * por sesion, asi que "dos sesiones" son dos Editor -cada uno con su propio Clipboard-
 * que comparten solo el lugar donde vive lo copiado: exactamente lo que pasaria entre
 * dos procesos con el portapapeles del sistema operativo en el medio, pero sin salir
 * del proceso (y por eso corre igual en headless).
 */
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
