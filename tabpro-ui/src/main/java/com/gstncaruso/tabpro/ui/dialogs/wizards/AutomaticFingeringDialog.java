package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.AutomaticFingering;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class AutomaticFingeringDialog {

    private AutomaticFingeringDialog() {
    }

    public static void show(Component parent, Editor editor) {
        JPanel content = buildContent(editor.currentTrack().name());

        boolean accepted = DialogShell.ask(
                parent, Texts.get("score_dialogs.AutomaticFingeringDialog.title"), content,
                Texts.get("score_dialogs.shared.apply"));
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> AutomaticFingering.run(score, trackIndex));
    }

    static JPanel buildContent(String trackName) {
        JPanel content = new JPanel();
        DialogStyle.padded(content);
        content.add(new JLabel(Texts.get("score_dialogs.AutomaticFingeringDialog.description", trackName)));
        return content;
    }
}
