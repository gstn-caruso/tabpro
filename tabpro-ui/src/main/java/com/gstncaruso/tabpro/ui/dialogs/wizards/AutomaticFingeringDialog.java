package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.AutomaticFingering;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** El asistente Digitacion automatica, sobre la pista activa. */
public final class AutomaticFingeringDialog {

    private AutomaticFingeringDialog() {
    }

    public static void show(Component parent, Editor editor) {
        JPanel content = new JPanel();
        DialogStyle.padded(content);
        content.add(new JLabel("<html>Reubica las notas de \"" + editor.currentTrack().name()
                + "\" en el diapason<br>para que la mano viaje lo menos posible.</html>"));

        boolean accepted = DialogShell.ask(parent, "Digitacion automatica", content, "Aplicar");
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> AutomaticFingering.run(score, trackIndex));
    }
}
