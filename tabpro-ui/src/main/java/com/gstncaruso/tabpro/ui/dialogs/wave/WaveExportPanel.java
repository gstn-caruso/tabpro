package com.gstncaruso.tabpro.ui.dialogs.wave;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * La calidad del archivo que pide "File &gt; Export &gt; Wave" del manual: frecuencia de
 * muestreo, profundidad de bits y mono o estereo.
 */
public final class WaveExportPanel extends JPanel {

    private static final Integer[] SAMPLE_RATES = {44_100, 48_000};
    private static final Integer[] BIT_DEPTHS = {16, 24};

    private final JComboBox<Integer> sampleRate = new JComboBox<>(SAMPLE_RATES);
    private final JComboBox<Integer> bitDepth = new JComboBox<>(BIT_DEPTHS);
    private final JRadioButton mono = new JRadioButton("Mono");
    private final JRadioButton stereo = new JRadioButton("Estéreo");

    public WaveExportPanel(AudioQuality defaults) {
        super(new GridLayout(0, 2, 8, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sampleRate.setSelectedItem(defaults.sampleRateHz());
        bitDepth.setSelectedItem(defaults.bitDepth());
        ButtonGroup channels = new ButtonGroup();
        channels.add(mono);
        channels.add(stereo);
        (defaults.channels() == 1 ? mono : stereo).setSelected(true);

        add(new JLabel("Frecuencia de muestreo"));
        add(sampleRate);
        add(new JLabel("Profundidad de bits"));
        add(bitDepth);
        add(mono);
        add(stereo);
    }

    public AudioQuality toAudioQuality() {
        return new AudioQuality(
                (Integer) sampleRate.getSelectedItem(), (Integer) bitDepth.getSelectedItem(),
                mono.isSelected() ? 1 : 2);
    }

    void chooseSampleRate(int hz) {
        sampleRate.setSelectedItem(hz);
    }

    void chooseBitDepth(int bits) {
        bitDepth.setSelectedItem(bits);
    }

    void chooseMono() {
        mono.setSelected(true);
    }

    void chooseStereo() {
        stereo.setSelected(true);
    }
}
