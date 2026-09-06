package com.gstncaruso.tabpro.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 * El boton de altavoz de "Configure the Sound &gt; MIDI Setup": prueba un
 * dispositivo tocandole una nota con el programa elegido para ese puerto.
 */
public final class MidiTestTone {

    public static final int TEST_PITCH = 60;
    public static final int TEST_VELOCITY = 100;
    public static final long DEFAULT_DURATION_MILLIS = 500;

    private MidiTestTone() {
    }

    public static void play(Receiver receiver, int program) {
        play(receiver, program, DEFAULT_DURATION_MILLIS);
    }

    /** Manda el programa y la nota de prueba ya, y la corta sola pasado ese tiempo. */
    public static void play(Receiver receiver, int program, long durationMillis) {
        play(receiver, program, durationMillis, () -> { });
    }

    /** Lo mismo, pero avisando cuando termina -por ejemplo, para cerrar el dispositivo de prueba. */
    public static void play(Receiver receiver, int program, long durationMillis, Runnable afterward) {
        send(receiver, ShortMessage.PROGRAM_CHANGE, program, 0);
        send(receiver, ShortMessage.NOTE_ON, TEST_PITCH, TEST_VELOCITY);
        Thread noteOff = new Thread(() -> {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            send(receiver, ShortMessage.NOTE_OFF, TEST_PITCH, 0);
            afterward.run();
        });
        noteOff.setDaemon(true);
        noteOff.start();
    }

    private static void send(Receiver receiver, int command, int data1, int data2) {
        try {
            receiver.send(new ShortMessage(command, 0, data1, data2), -1);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
