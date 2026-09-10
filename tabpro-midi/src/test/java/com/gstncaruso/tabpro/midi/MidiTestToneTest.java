package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;

class MidiTestToneTest {

    @Test
    void sendsTheChosenProgramBeforeTheNote() {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();

        MidiTestTone.play(receiverInto(received), 25, 200);

        assertEquals(ShortMessage.PROGRAM_CHANGE, received.get(0).getCommand());
        assertEquals(25, received.get(0).getData1());
    }

    @Test
    void soundsTheTestPitchRightAway() {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();

        MidiTestTone.play(receiverInto(received), 0, 200);

        ShortMessage noteOn = received.get(1);
        assertEquals(ShortMessage.NOTE_ON, noteOn.getCommand());
        assertEquals(MidiTestTone.TEST_PITCH, noteOn.getData1());
        assertTrue(noteOn.getData2() > 0);
    }

    @Test
    void doesNotTurnTheNoteOffBeforeItsDuration() {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();

        MidiTestTone.play(receiverInto(received), 0, 500, () -> { }, cuandoTodaviaNoPaso());

        assertFalse(received.stream().anyMatch(message -> message.getCommand() == ShortMessage.NOTE_OFF));
    }

    @Test
    void turnsTheNoteOffOnceItsDurationPasses() {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();

        MidiTestTone.play(receiverInto(received), 0, 20, () -> { }, alInstante());

        assertTrue(received.stream().anyMatch(message -> message.getCommand() == ShortMessage.NOTE_OFF));
    }

    @Test
    void runsTheGivenCallbackOnceTheNoteTurnsOff() {
        AtomicBoolean ranAfterward = new AtomicBoolean(false);

        MidiTestTone.play(receiverInto(new CopyOnWriteArrayList<>()), 0, 20, () -> ranAfterward.set(true), alInstante());

        assertTrue(ranAfterward.get());
    }

    private static Retardo cuandoTodaviaNoPaso() {
        return (millis, accion) -> { };
    }

    private static Retardo alInstante() {
        return (millis, accion) -> accion.run();
    }

    private static Receiver receiverInto(List<ShortMessage> received) {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                received.add((ShortMessage) message);
            }

            @Override
            public void close() {
            }
        };
    }
}
