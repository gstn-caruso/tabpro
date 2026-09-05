package com.gstncaruso.tabpro.ui.dialogs.metronome;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Activar el metronomo y ajustar su tempo y su volumen. */
public final class MetronomePanel extends FormPanel {

    private final JCheckBox active = new JCheckBox("Metronomo activado");
    private final JSpinner tempo = new JSpinner(new SpinnerNumberModel(120, 1, 400, 1));
    private final JSlider volume = new JSlider(MetronomeSettings.MIN_VOLUME, MetronomeSettings.MAX_VOLUME, 100);

    public MetronomePanel(int currentTempo, MetronomeSettings settings) {
        addFullWidthRow(active);
        addRow("Tempo (BPM)", tempo);
        addRow("Volumen", volume);
        apply(currentTempo, settings);
    }

    public void apply(int currentTempo, MetronomeSettings settings) {
        tempo.setValue(currentTempo);
        active.setSelected(settings.active());
        volume.setValue(settings.volume());
    }

    public int toTempo() {
        return (Integer) tempo.getValue();
    }

    public MetronomeSettings toSettings() {
        return new MetronomeSettings(active.isSelected(), volume.getValue());
    }
}
