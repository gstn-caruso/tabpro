package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.core.editing.Clipboard.Clipping;
import com.gstncaruso.tabpro.core.editing.ClipboardStorage;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;

/**
 * El ClipboardStorage que cruza el portapapeles del sistema operativo, para que copiar
 * y pegar funcione entre dos sesiones de tabpro como pide el manual. Si no hay
 * portapapeles de sistema disponible -headless, como corre la CI- se degrada al
 * comportamiento de siempre (privado de esta sesion), igual que MidiPlayer se degrada
 * a silencio cuando no hay linea MIDI: nunca revienta por esto.
 */
public final class SystemClipboardStorage implements ClipboardStorage {

    private final ClipboardStorage delegate;

    public SystemClipboardStorage() {
        this.delegate = systemClipboard().<ClipboardStorage>map(RealSystemClipboard::new)
                .orElseGet(ClipboardStorage::inMemory);
    }

    @Override
    public void hold(Clipping clipping) {
        delegate.hold(clipping);
    }

    @Override
    public Clipping content() {
        return delegate.content();
    }

    private static Optional<java.awt.datatransfer.Clipboard> systemClipboard() {
        try {
            return Optional.of(Toolkit.getDefaultToolkit().getSystemClipboard());
        } catch (HeadlessException e) {
            return Optional.empty();
        }
    }

    /** El texto de un Transferable, o vacio si no tiene ninguno (una imagen, por ejemplo). */
    static Optional<String> textOf(Transferable transferable) {
        if (transferable == null || !transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return Optional.empty();
        }
        try {
            return Optional.of((String) transferable.getTransferData(DataFlavor.stringFlavor));
        } catch (UnsupportedFlavorException | IOException e) {
            return Optional.empty();
        }
    }

    private record RealSystemClipboard(java.awt.datatransfer.Clipboard system) implements ClipboardStorage {

        private static final ClippingJson JSON = new ClippingJson();

        @Override
        public void hold(Clipping clipping) {
            system.setContents(new StringSelection(JSON.encode(clipping)), null);
        }

        @Override
        public Clipping content() {
            return textOf(system.getContents(null)).map(JSON::decode).orElse(Clipping.EMPTY);
        }
    }
}
