package com.gstncaruso.tabpro.ui.dialogs.instrument;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.InstrumentPatch;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;

public final class InstrumentDialog {

    private InstrumentDialog() {
    }

    public static void show(Component parent, Editor editor, int trackIndex) {
        show(parent, editor, trackIndex, InstrumentPatch.generalMidi());
    }

    public static void show(Component parent, Editor editor, int trackIndex, InstrumentPatch patch) {
        InstrumentPanel panel = new InstrumentPanel(editor.score().track(trackIndex).channel().program(), patch);

        boolean accepted = DialogShell.ask(parent, "Instrumento", panel);
        if (accepted) {
            editor.setProgram(trackIndex, panel.selectedProgram());
        }
    }
}
