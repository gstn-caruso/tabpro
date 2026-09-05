package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.LoopRange;
import com.gstncaruso.tabpro.core.playback.RelativeTempo;
import com.gstncaruso.tabpro.core.playback.SpeedTrainer;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de "Play looped / Speed Trainer": repetir un rango de compases,
 * ya sea al mismo tempo o subiendolo un poco en cada vuelta.
 */
public final class LoopDialog {

    private LoopDialog() {
    }

    /** Lo que la ventana devuelve: el rango a repetir y, si se pidio, el entrenador. */
    public record Loop(LoopRange range, Optional<SpeedTrainer> trainer) {
    }

    public static Optional<Loop> ask(Component parent, Editor editor, RelativeTempo relativeTempo) {
        int lastMeasure = editor.currentTrack().measureCount();
        int fromDefault = editor.selection().map(selection -> selection.fromMeasure() + 1)
                .orElse(editor.cursor().measure() + 1);
        int toDefault = editor.selection().map(selection -> selection.toMeasure() + 1).orElse(lastMeasure);

        JSpinner from = new JSpinner(new SpinnerNumberModel(fromDefault, 1, lastMeasure, 1));
        JSpinner to = new JSpinner(new SpinnerNumberModel(toDefault, 1, lastMeasure, 1));
        JRadioButton simple = new JRadioButton("Loop simple", true);
        JRadioButton trainer = new JRadioButton("Entrenador de velocidad");
        ButtonGroup mode = new ButtonGroup();
        mode.add(simple);
        mode.add(trainer);

        int tempo = relativeTempo.apply(editor.score().tempo());
        JSpinner startTempo = new JSpinner(new SpinnerNumberModel(tempo, 20, 400, 1));
        JSpinner endTempo = new JSpinner(new SpinnerNumberModel(Math.min(400, tempo + 40), 20, 400, 1));
        JSpinner increment = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Desde el compás"));
        panel.add(from);
        panel.add(new JLabel("Hasta el compás"));
        panel.add(to);
        panel.add(simple);
        panel.add(new JLabel(" "));
        panel.add(trainer);
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Tempo inicial"));
        panel.add(startTempo);
        panel.add(new JLabel("Tempo final"));
        panel.add(endTempo);
        panel.add(new JLabel("Incremento por vuelta"));
        panel.add(increment);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Loop / Entrenador de velocidad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        LoopRange range = new LoopRange(value(from) - 1, value(to) - 1);
        Optional<SpeedTrainer> speedTrainer = trainer.isSelected()
                ? Optional.of(new SpeedTrainer(value(startTempo), value(endTempo), value(increment)))
                : Optional.empty();
        return Optional.of(new Loop(range, speedTrainer));
    }

    private static int value(JSpinner spinner) {
        return (Integer) spinner.getValue();
    }
}
