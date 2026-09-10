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

public final class StringOptionsDialog {

    private StringOptionsDialog() {
    }

    public enum Option { LET_RING, PALM_MUTE, DYNAMIC }

    public static void show(Component parent, Editor editor) {
        open(parent, editor, Option.LET_RING);
    }

    public static void showFocusedOnPalmMute(Component parent, Editor editor) {
        open(parent, editor, Option.PALM_MUTE);
    }

    public static void showFocusedOnDynamic(Component parent, Editor editor) {
        open(parent, editor, Option.DYNAMIC);
    }

    private static void open(Component parent, Editor editor, Option option) {
        StringOptionsPanel panel = new StringOptionsPanel(editor.currentTrack().stringCount(), editor.currentTrack().measureCount());

        boolean accepted = DialogShell.ask(parent, titleFor(option), panel, "Aplicar", panel.comboFor(option));
        if (!accepted) {
            return;
        }
        int trackIndex = editor.cursor().track();
        MeasureRange range = panel.toMeasureRange();
        Set<Integer> strings = panel.selectedStrings();

        editor.apply(score -> apply(score, trackIndex, range, strings, panel));
    }

    static String titleFor(Option option) {
        return switch (option) {
            case LET_RING -> "Opciones de let ring";
            case PALM_MUTE -> "Opciones de palm mute";
            case DYNAMIC -> "Opciones de dinámica";
        };
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
