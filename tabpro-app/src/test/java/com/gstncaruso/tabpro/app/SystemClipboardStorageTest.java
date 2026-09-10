package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Toolkit.getSystemClipboard() throws HeadlessException in CI and in most environments that run
 * tabpro headless, so there is no real system clipboard to test against here; these tests
 * exercise the Transferable-to-text mechanism and the degradation path separately instead.
 */
class SystemClipboardStorageTest {

    @Test
    void readsTheTextOfAPlainTextTransferable() {
        Transferable plainText = new StringSelection("cualquier texto copiado de otro lado");

        assertEquals(Optional.of("cualquier texto copiado de otro lado"), SystemClipboardStorage.textOf(plainText));
    }

    @Test
    void anImageTransferableHasNoText() {
        assertEquals(Optional.empty(), SystemClipboardStorage.textOf(anImage()));
    }

    @Test
    void pastingWithinTheSameSessionStillWorksWithoutASystemClipboard() {
        // The pom's <argLine> forces -Djava.awt.headless=true: this suite really runs headless,
        // so this test exercises the actual degradation, not a simulation of it.
        Editor editor = new Editor(
                new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"))), new SystemClipboardStorage());
        editor.setFret(5);
        editor.copy(false);
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();

        editor.paste(PasteOptions.replacingOnce());

        assertEquals(
                Optional.of(5), editor.score().track(0).measure(1).beat(0).noteOn(1).map(note -> note.fret()));
    }

    private static Transferable anImage() {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.imageFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
}
