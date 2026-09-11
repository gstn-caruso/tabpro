package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;

public final class TrackPropertiesDialog {

    private TrackPropertiesDialog() {
    }

    public static void show(Component parent, Editor editor, int trackIndex, Player player) {
        TrackPropertiesPanel panel = new TrackPropertiesPanel(editor.score().track(trackIndex), player);

        boolean accepted = DialogShell.ask(parent, Texts.get("score_dialogs.TrackPropertiesDialog.title"), panel);
        if (accepted) {
            editor.renameTrack(trackIndex, panel.trackName());
            editor.setTrackSettings(trackIndex, panel.toTrackSettings());
            editor.setTuning(trackIndex, panel.toTuning());
        }
    }
}
