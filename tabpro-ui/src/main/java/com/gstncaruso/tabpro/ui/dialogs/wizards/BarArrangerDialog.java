package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.BarArranger;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** El asistente Organizador de compases: reacomoda los beats para que cada compas cierre su medida. */
public final class BarArrangerDialog {

    private BarArrangerDialog() {
    }

    public static void show(Component parent, Editor editor) {
        JPanel content = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(content);
        content.add(new JLabel("<html>Reacomoda los beats para que cada compas sume<br>"
                + "exactamente lo que pide su medida.</html>"), BorderLayout.NORTH);
        TrackScopePanel scope = new TrackScopePanel();
        content.add(scope, BorderLayout.CENTER);

        boolean accepted = DialogShell.ask(parent, "Organizador de compases", content, "Organizar");
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> scope.everyTrackSelected() ? BarArranger.run(score) : BarArranger.runOnTrack(score, trackIndex));
    }
}
