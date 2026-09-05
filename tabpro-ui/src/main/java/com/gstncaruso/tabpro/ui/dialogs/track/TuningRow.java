package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Una cuerda de la afinacion: su altura en numero MIDI, su nombre y un boton para escucharla. */
final class TuningRow extends JPanel {

    private final JSpinner midiNumber;
    private final JLabel noteName = new JLabel();

    TuningRow(Pitch initial, Runnable onListen, java.util.function.Consumer<Pitch> onChange) {
        super(new BorderLayout(DialogStyle.GAP_S, 0));
        midiNumber = new JSpinner(new SpinnerNumberModel(initial.midiNumber(), 0, 127, 1));
        setOpaque(false);
        add(midiNumber, BorderLayout.WEST);
        add(noteName, BorderLayout.CENTER);
        javax.swing.JButton listen = DialogStyle.flatButton("Escuchar");
        listen.addActionListener(event -> onListen.run());
        add(listen, BorderLayout.EAST);

        updateNoteName();
        // El listener se cuelga despues de fijar el valor inicial, para no avisar un cambio que no hizo el usuario.
        midiNumber.addChangeListener(event -> {
            updateNoteName();
            onChange.accept(toPitch());
        });
    }

    Pitch toPitch() {
        return new Pitch((Integer) midiNumber.getValue());
    }

    private void updateNoteName() {
        noteName.setText(PitchName.of(toPitch()).textWithOctave());
    }
}
