package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.BarArranger;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class BarArrangerDialog {

    private BarArrangerDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Fields fields = buildFields();

        boolean accepted = DialogShell.ask(
                parent, Texts.get("score_dialogs.BarArrangerDialog.title"), fields.content(),
                Texts.get("score_dialogs.BarArrangerDialog.accept"));
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> fields.scope().everyTrackSelected()
                ? BarArranger.run(score)
                : BarArranger.runOnTrack(score, trackIndex));
    }

    static Fields buildFields() {
        JPanel content = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(content);
        content.add(new JLabel(Texts.get("score_dialogs.BarArrangerDialog.description")), BorderLayout.NORTH);
        TrackScopePanel scope = new TrackScopePanel();
        content.add(scope, BorderLayout.CENTER);

        return new Fields(content, scope);
    }

    record Fields(JPanel content, TrackScopePanel scope) {
    }
}
