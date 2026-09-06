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
 * La CI (y buena parte de los entornos donde corre tabpro) es headless:
 * Toolkit.getSystemClipboard() tira HeadlessException apenas se lo pide, no hay
 * portapapeles del sistema operativo de verdad. Ninguno de estos tests lo toca -serian
 * no confiables e imposibles de correr en CI-; en cambio prueban por separado el
 * mecanismo de extraer texto de un Transferable y el efecto de la degradacion.
 */
class SystemClipboardStorageTest {

    @Test
    void readsTheTextOfAPlainTextTransferable() {
        Transferable textoSuelto = new StringSelection("cualquier texto copiado de otro lado");

        assertEquals(Optional.of("cualquier texto copiado de otro lado"), SystemClipboardStorage.textOf(textoSuelto));
    }

    @Test
    void anImageTransferableHasNoText() {
        assertEquals(Optional.empty(), SystemClipboardStorage.textOf(unaImagen()));
    }

    @Test
    void pastingWithinTheSameSessionStillWorksWithoutASystemClipboard() {
        // El <argLine> del pom fuerza -Djava.awt.headless=true: esta suite corre headless de
        // verdad, asi que este test ejercita la degradacion (no una simulacion de ella).
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

    private static Transferable unaImagen() {
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
