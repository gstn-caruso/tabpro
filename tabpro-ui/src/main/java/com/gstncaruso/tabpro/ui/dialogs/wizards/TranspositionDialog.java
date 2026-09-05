package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.Transposition;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;

/** El asistente Transponer del menu Herramientas. */
public final class TranspositionDialog {

    private TranspositionDialog() {
    }

    public static void show(Component parent, Editor editor) {
        TranspositionPanel panel = new TranspositionPanel();

        boolean accepted = DialogShell.ask(parent, "Transponer", panel, "Transponer");
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> panel.everyTrack()
                ? Transposition.transposeEveryTrack(score, panel.semitones())
                : Transposition.transposeTrack(score, trackIndex, panel.semitones()));
    }
}
