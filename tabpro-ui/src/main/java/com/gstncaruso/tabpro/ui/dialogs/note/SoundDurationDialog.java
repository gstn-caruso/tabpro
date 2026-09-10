package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.Component;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de Nota > Duracion del sonido: cuanto suena la nota respecto de su
 * figura, expresado en porcentaje, sin tocar lo que dice la partitura.
 */
public final class SoundDurationDialog {

    private SoundDurationDialog() {
    }

    public static void show(Component parent, Editor editor) {
        int current = editor.currentNote()
                .map(note -> note.effects().soundDurationPercent())
                .orElse(NoteEffects.FULL_SOUND);
        Fields fields = buildFields(current);

        if (DialogShell.ask(parent, "Duración del sonido", fields.form())) {
            editor.setSoundDuration((Integer) fields.percent().getValue());
        }
    }

    /** Arma el formulario y el campo que hay que releer si se acepta; sin abrir ningun dialogo. */
    static Fields buildFields(int current) {
        JSpinner percent = new JSpinner(new SpinnerNumberModel(current, 1, 200, 5));
        FormPanel form = new FormPanel().addRow("Duración del sonido (%)", percent);
        return new Fields(form, percent);
    }

    record Fields(FormPanel form, JSpinner percent) {
    }
}
