package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.core.editing.wizards.StringOptions;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.util.Set;

/** El asistente de opciones por cuerda: let ring, palm mute y dinamica sobre un rango de compases. */
public final class StringOptionsDialog {

    private StringOptionsDialog() {
    }

    public static void show(Component parent, Editor editor) {
        StringOptionsPanel panel = new StringOptionsPanel(editor.currentTrack().stringCount(), editor.currentTrack().measureCount());

        boolean accepted = DialogShell.ask(parent, "Opciones por cuerda", panel, "Aplicar");
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        MeasureRange range = panel.toMeasureRange();
        Set<Integer> strings = panel.selectedStrings();

        editor.apply(score -> apply(score, trackIndex, range, strings, panel));
    }

    private static Score apply(Score score, int trackIndex, MeasureRange range, Set<Integer> strings, StringOptionsPanel panel) {
        Score result = score;
        if (panel.letRingChange().isPresent()) {
            result = StringOptions.applyOrnament(result, trackIndex, range, strings, Ornament.LET_RING, panel.letRingChange().get());
        }
        if (panel.palmMuteChange().isPresent()) {
            result = StringOptions.applyOrnament(result, trackIndex, range, strings, Ornament.PALM_MUTE, panel.palmMuteChange().get());
        }
        if (panel.dynamicChange().isPresent()) {
            Dynamic dynamic = panel.dynamicChange().get();
            result = StringOptions.applyDynamic(result, trackIndex, range, strings, dynamic);
        }
        return result;
    }
}
