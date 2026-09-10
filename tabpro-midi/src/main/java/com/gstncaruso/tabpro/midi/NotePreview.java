package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

final class NotePreview implements AutoCloseable {

    static final int CHANNEL = 15;

    private static final int VELOCITY = 100;
    private static final long RING_MILLIS = 700;
    static final long NOTE_GAP_MILLIS = 350;

    private final Receiver receiver;
    private final Delay delay;

    NotePreview(Receiver receiver) {
        this(receiver, new ClockDelay());
    }

    NotePreview(Receiver receiver, Delay delay) {
        this.receiver = receiver;
        this.delay = delay;
    }

    void play(Pitch pitch, int program) {
        send(ShortMessage.PROGRAM_CHANGE, program, 0);
        send(ShortMessage.NOTE_ON, pitch.midiNumber(), VELOCITY);
        delay.after(RING_MILLIS, () -> send(ShortMessage.NOTE_OFF, pitch.midiNumber(), 0));
    }

    void playSequence(List<Pitch> pitches, int program) {
        for (int index = 0; index < pitches.size(); index++) {
            Pitch pitch = pitches.get(index);
            delay.after(index * NOTE_GAP_MILLIS, () -> play(pitch, program));
        }
    }

    @Override
    public void close() {
        if (delay instanceof AutoCloseable closeable) {
            closeUnchecked(closeable);
        }
        receiver.close();
    }

    private static void closeUnchecked(AutoCloseable closeable) {
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
