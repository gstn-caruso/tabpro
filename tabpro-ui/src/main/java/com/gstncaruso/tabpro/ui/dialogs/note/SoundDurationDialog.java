package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class SoundDurationDialog {

    private SoundDurationDialog() {
    }

    public static void show(Component parent, Editor editor) {
        int current = editor.currentNote()
                .map(note -> note.effects().soundDurationPercent())
                .orElse(NoteEffects.FULL_SOUND);
        Fields fields = buildFields(current);

        if (DialogShell.ask(parent, Texts.get("edit_dialogs.SoundDurationDialog.title"), fields.form())) {
            editor.setSoundDuration((Integer) fields.percent().getValue());
        }
    }

    static Fields buildFields(int current) {
        JSpinner percent = new JSpinner(new SpinnerNumberModel(current, 1, 200, 5));
        FormPanel form = new FormPanel().addRow(Texts.get("edit_dialogs.SoundDurationDialog.percent"), percent);
        return new Fields(form, percent);
    }

    record Fields(FormPanel form, JSpinner percent) {
    }
}
