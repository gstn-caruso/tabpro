package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/** Hace sonar una nota suelta y la suelta sola, para escuchar la que se acaba de escribir. */
final class NotePreview implements AutoCloseable {

    /** El ultimo canal, para no pisar el de ninguna pista mientras se escucha. */
    static final int CHANNEL = 15;

    private static final int VELOCITY = 100;
    private static final long RING_MILLIS = 700;

    private final Receiver receiver;
    private final ScheduledExecutorService releases =
            Executors.newSingleThreadScheduledExecutor(daemonThreads());

    NotePreview(Receiver receiver) {
        this.receiver = receiver;
    }

    void play(Pitch pitch, int program) {
        send(ShortMessage.PROGRAM_CHANGE, program, 0);
        send(ShortMessage.NOTE_ON, pitch.midiNumber(), VELOCITY);
        releases.schedule(
                () -> send(ShortMessage.NOTE_OFF, pitch.midiNumber(), 0), RING_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        releases.shutdownNow();
        receiver.close();
    }

    private void send(int command, int data1, int data2) {
        try {
            receiver.send(new ShortMessage(command, CHANNEL, data1, data2), -1);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ThreadFactory daemonThreads() {
        return runnable -> {
            Thread thread = new Thread(runnable, "midi-note-release");
            thread.setDaemon(true);
            return thread;
        };
    }
}
