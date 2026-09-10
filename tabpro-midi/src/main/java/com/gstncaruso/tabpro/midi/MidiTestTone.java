package com.gstncaruso.tabpro.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public final class MidiTestTone {

    public static final int TEST_PITCH = 60;
    public static final int TEST_VELOCITY = 100;
    public static final long DEFAULT_DURATION_MILLIS = 500;

    private MidiTestTone() {
    }

    public static void play(Receiver receiver, int program) {
        play(receiver, program, DEFAULT_DURATION_MILLIS);
    }

    public static void play(Receiver receiver, int program, long durationMillis) {
        play(receiver, program, durationMillis, () -> { });
    }

    public static void play(Receiver receiver, int program, long durationMillis, Runnable afterward) {
        play(receiver, program, durationMillis, afterward, new ClockDelay());
    }

    static void play(Receiver receiver, int program, long durationMillis, Runnable afterward, Delay delay) {
        send(receiver, ShortMessage.PROGRAM_CHANGE, program, 0);
        send(receiver, ShortMessage.NOTE_ON, TEST_PITCH, TEST_VELOCITY);
        delay.after(durationMillis, () -> {
            send(receiver, ShortMessage.NOTE_OFF, TEST_PITCH, 0);
            afterward.run();
        });
    }

    private static void send(Receiver receiver, int command, int data1, int data2) {
        try {
            receiver.send(new ShortMessage(command, 0, data1, data2), -1);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
