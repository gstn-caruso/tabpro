package com.gstncaruso.tabpro.ui.dialogs.wave;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JOptionPane;

/**
 * La ventana de "File &gt; Export &gt; Wave" del manual: le pregunta al usuario la calidad del
 * archivo antes de elegir donde guardarlo.
 */
public final class WaveExportDialog {

    private WaveExportDialog() {
    }

    public static Optional<AudioQuality> ask(Component parent, AudioQuality defaults) {
        WaveExportPanel panel = new WaveExportPanel(defaults);
        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Exportar a WAVE", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return answer == JOptionPane.OK_OPTION ? Optional.of(panel.toAudioQuality()) : Optional.empty();
    }
}
