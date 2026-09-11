package com.gstncaruso.tabpro.ui.dialogs.wave;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JOptionPane;

public final class WaveExportDialog {

    private WaveExportDialog() {
    }

    public static Optional<AudioQuality> ask(Component parent, AudioQuality defaults) {
        WaveExportPanel panel = new WaveExportPanel(defaults);
        int answer = JOptionPane.showConfirmDialog(
                parent, panel, Texts.get("score_dialogs.WaveExportDialog.title"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return answer == JOptionPane.OK_OPTION ? Optional.of(panel.toAudioQuality()) : Optional.empty();
    }
}
