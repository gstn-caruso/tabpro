package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;

/** La ventana de Propiedades de pista [F6]. */
public final class TrackPropertiesDialog {

    private TrackPropertiesDialog() {
    }

    public static void show(Component parent, Editor editor, int trackIndex, Player player) {
        TrackPropertiesPanel panel = new TrackPropertiesPanel(editor.score().track(trackIndex), player);

        boolean accepted = DialogShell.ask(parent, "Propiedades de pista", panel);
        if (accepted) {
            editor.renameTrack(trackIndex, panel.trackName());
            editor.setTrackSettings(trackIndex, panel.toTrackSettings());
            editor.setTuning(trackIndex, panel.toTuning());
        }
    }
}
