package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.core.playback.RelativeTempo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Hashtable;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * El tempo relativo del manual: un coeficiente de x0.25 a x2 que acelera o
 * frena la reproduccion sin tocar el archivo.
 */
public final class RelativeTempoDialog {

    private static final int STEPS_PER_UNIT = 100;

    private RelativeTempoDialog() {
    }

    public static Optional<RelativeTempo> ask(Component parent, RelativeTempo current) {
        JSlider slider = sliderFor(current);
        JLabel factor = new JLabel(describe(current.factor()));
        slider.addChangeListener(event -> factor.setText(describe(factorOf(slider))));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Velocidad de reproducción"), BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(factor, BorderLayout.SOUTH);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Tempo relativo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return Optional.of(new RelativeTempo(factorOf(slider)));
    }

    private static JSlider sliderFor(RelativeTempo current) {
        JSlider slider = new JSlider(
                (int) (RelativeTempo.MIN * STEPS_PER_UNIT),
                (int) (RelativeTempo.MAX * STEPS_PER_UNIT),
                (int) (current.factor() * STEPS_PER_UNIT));
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setLabelTable(labels());
        return slider;
    }

    private static Hashtable<Integer, JLabel> labels() {
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(25, new JLabel("x0.25"));
        labels.put(50, new JLabel("x0.5"));
        labels.put(100, new JLabel("x1"));
        labels.put(150, new JLabel("x1.5"));
        labels.put(200, new JLabel("x2"));
        return labels;
    }

    private static double factorOf(JSlider slider) {
        return slider.getValue() / (double) STEPS_PER_UNIT;
    }

    private static String describe(double factor) {
        return String.format("x%.2f", factor);
    }
}
