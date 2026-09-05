package com.gstncaruso.tabpro.ui.dialogs.instrument;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;

/** La ventana de Instrumento [F7]. */
public final class InstrumentDialog {

    private InstrumentDialog() {
    }

    public static void show(Component parent, Editor editor, int trackIndex) {
        InstrumentPanel panel = new InstrumentPanel(editor.score().track(trackIndex).channel().program());

        boolean accepted = DialogShell.ask(parent, "Instrumento", panel);
        if (accepted) {
            editor.setProgram(trackIndex, panel.selectedProgram());
        }
    }
}
