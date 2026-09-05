package com.gstncaruso.tabpro.ui.dialogs.metronome;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.util.Optional;

/** La ventana del Metronomo: activarlo y configurarlo. */
public final class MetronomeDialog {

    private MetronomeDialog() {
    }

    public static Optional<MetronomeSettings> ask(Component parent, Editor editor, MetronomeSettings current) {
        MetronomePanel panel = new MetronomePanel(editor.score().tempo(), current);

        boolean accepted = DialogShell.ask(parent, "Metronomo", panel);
        if (!accepted) {
            return Optional.empty();
        }
        editor.setTempo(panel.toTempo());
        return Optional.of(panel.toSettings());
    }
}
