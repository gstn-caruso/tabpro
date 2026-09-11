package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.RestFiller;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;

public final class RestFillerDialog {

    private RestFillerDialog() {
    }

    public static void show(Component parent, Editor editor) {
        RestFillerPanel panel = new RestFillerPanel(editor.currentTrack().measureCount());

        boolean accepted = DialogShell.ask(
                parent, Texts.get("score_dialogs.RestFillerDialog.title"), panel,
                Texts.get("score_dialogs.RestFillerDialog.accept"));
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        editor.apply(score -> panel.everyTrack()
                ? RestFiller.run(score, panel.toMeasureRange())
                : RestFiller.runOnTrack(score, trackIndex, panel.toMeasureRange()));
    }
}
