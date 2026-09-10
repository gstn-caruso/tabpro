package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.AutomaticFingering;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class AutomaticFingeringDialog {

    private AutomaticFingeringDialog() {
    }

    public static void show(Component parent, Editor editor) {
        JPanel content = buildContent(editor.currentTrack().name());

        boolean accepted = DialogShell.ask(parent, "Digitación automática", content, "Aplicar");
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> AutomaticFingering.run(score, trackIndex));
    }

    static JPanel buildContent(String trackName) {
        JPanel content = new JPanel();
        DialogStyle.padded(content);
        content.add(new JLabel("<html>Reubica las notas de \"" + trackName
                + "\" en el diapasón<br>para que la mano viaje lo menos posible.</html>"));
        return content;
    }
}
