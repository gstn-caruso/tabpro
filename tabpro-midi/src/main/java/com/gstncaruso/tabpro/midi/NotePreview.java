package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/** Hace sonar una nota suelta y la suelta sola, para escuchar la que se acaba de escribir. */
final class NotePreview implements AutoCloseable {

    /** El ultimo canal, para no pisar el de ninguna pista mientras se escucha. */
    static final int CHANNEL = 15;

    private static final int VELOCITY = 100;
    private static final long RING_MILLIS = 700;
    static final long NOTE_GAP_MILLIS = 350;

    private final Receiver receiver;
    private final Retardo retardo;

    NotePreview(Receiver receiver) {
        this(receiver, new RetardoDelReloj());
    }

    NotePreview(Receiver receiver, Retardo retardo) {
        this.receiver = receiver;
        this.retardo = retardo;
    }

    void play(Pitch pitch, int program) {
        send(ShortMessage.PROGRAM_CHANGE, program, 0);
        send(ShortMessage.NOTE_ON, pitch.midiNumber(), VELOCITY);
        retardo.luegoDe(RING_MILLIS, () -> send(ShortMessage.NOTE_OFF, pitch.midiNumber(), 0));
    }

    /** Hace sonar varias notas, una despues de la otra, para escuchar una escala o un arpegio. */
    void playSequence(List<Pitch> pitches, int program) {
        for (int index = 0; index < pitches.size(); index++) {
            Pitch pitch = pitches.get(index);
            retardo.luegoDe(index * NOTE_GAP_MILLIS, () -> play(pitch, program));
        }
    }

    @Override
    public void close() {
        if (retardo instanceof AutoCloseable closeable) {
            cerrar(closeable);
        }
        receiver.close();
    }

    private static void cerrar(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void send(int command, int data1, int data2) {
        try {
            receiver.send(new ShortMessage(command, CHANNEL, data1, data2), -1);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
