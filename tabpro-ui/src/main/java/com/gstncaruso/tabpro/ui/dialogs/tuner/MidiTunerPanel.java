package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

/**
 * El afinador MIDI: una fila por cuerda, con un boton que la hace sonar en
 * bucle para compararla con la cuerda real. Cada toggle arranca sonando una vez
 * al toque (lo que prueban los tests) y programa las repeticiones con un Timer.
 */
public final class MidiTunerPanel extends JPanel {

    private static final int LOOP_INTERVAL_MS = 900;

    private final Tuning tuning;
    private final int program;
    private final Player player;
    private final Map<Integer, Timer> loops = new HashMap<>();

    public MidiTunerPanel(Tuning tuning, int program, Player player) {
        super(new GridLayout(0, 3, DialogStyle.GAP_S, DialogStyle.GAP_XS));
        this.tuning = tuning;
        this.program = program;
        this.player = player;

        for (int string = 1; string <= tuning.stringCount(); string++) {
            int fixedString = string;
            add(new JLabel("Cuerda " + string));
            add(new JLabel(PitchName.of(tuning.pitchOfString(string)).textWithOctave()));
            JToggleButton listen = new JToggleButton("Escuchar en bucle");
            listen.setFocusPainted(false);
            listen.addActionListener(event -> {
                if (listen.isSelected()) {
                    startLoop(fixedString);
                } else {
                    stopLoop(fixedString);
                }
            });
            add(listen);
        }
    }

    public void startLoop(int string) {
        stopLoop(string);
        playOnce(string);
        Timer timer = new Timer(LOOP_INTERVAL_MS, event -> playOnce(string));
        timer.start();
        loops.put(string, timer);
    }

    public void stopLoop(int string) {
        Timer timer = loops.remove(string);
        if (timer != null) {
            timer.stop();
        }
    }

    public boolean isLooping(int string) {
        return loops.containsKey(string);
    }

    /** Corta todas las cuerdas que hayan quedado sonando en bucle, por ejemplo al cerrar la ventana. */
    public void stopAllLoops() {
        for (int string : List.copyOf(loops.keySet())) {
            stopLoop(string);
        }
    }

    private void playOnce(int string) {
        player.playNote(tuning.pitchOfString(string), program);
    }
}
