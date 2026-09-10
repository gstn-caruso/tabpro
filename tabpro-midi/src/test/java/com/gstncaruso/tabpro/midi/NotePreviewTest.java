package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;

class NotePreviewTest {

    @Test
    void soundsTheNoteWithTheInstrumentOfTheTrack() {
        RecordingReceiver receiver = new RecordingReceiver();
        NotePreview preview = new NotePreview(receiver);

        preview.play(new Pitch(60), 25);

        ShortMessage program = receiver.firstOf(ShortMessage.PROGRAM_CHANGE);
        assertEquals(NotePreview.CHANNEL, program.getChannel());
        assertEquals(25, program.getData1());

        ShortMessage noteOn = receiver.firstOf(ShortMessage.NOTE_ON);
        assertEquals(NotePreview.CHANNEL, noteOn.getChannel());
        assertEquals(60, noteOn.getData1());
        assertTrue(noteOn.getData2() > 0, "una nota sin velocity no suena");

        preview.close();
    }

    @Test
    void choosesTheInstrumentBeforeSoundingTheNote() {
        RecordingReceiver receiver = new RecordingReceiver();
        NotePreview preview = new NotePreview(receiver);

        preview.play(new Pitch(60), 25);

        assertEquals(
                List.of(ShortMessage.PROGRAM_CHANGE, ShortMessage.NOTE_ON),
                receiver.firstTwoCommands(),
                "el programa tiene que llegar antes que la nota");

        preview.close();
    }

    @Test
    void releasesTheNoteOnItsOwn() {
        RecordingReceiver receiver = new RecordingReceiver();
        NotePreview preview = new NotePreview(receiver, (millis, action) -> action.run());

        preview.play(new Pitch(60), 25);

        assertEquals(60, receiver.firstOf(ShortMessage.NOTE_OFF).getData1());

        preview.close();
    }

    @Test
    void anEmptySequenceSoundsNothing() {
        RecordingReceiver receiver = new RecordingReceiver();
        NotePreview preview = new NotePreview(receiver, (millis, action) -> action.run());

        preview.playSequence(List.of(), 25);

        assertTrue(receiver.received.isEmpty());

        preview.close();
    }

    @Test
    void playsEachNoteOfTheSequenceInOrderWithGrowingSeparation() {
        RecordingReceiver receiver = new RecordingReceiver();
        List<Long> scheduledGaps = new ArrayList<>();
        NotePreview preview = new NotePreview(receiver, (millis, action) -> {
            scheduledGaps.add(millis);
            action.run();
        });

        preview.playSequence(List.of(new Pitch(60), new Pitch(62), new Pitch(64)), 25);

        assertEquals(List.of(60, 62, 64), receiver.notesOn());
        List<Long> noteScheduleGaps = new ArrayList<>();
        for (int index = 0; index < scheduledGaps.size(); index += 2) {
            noteScheduleGaps.add(scheduledGaps.get(index));
        }
        assertEquals(
                List.of(0L, NotePreview.NOTE_GAP_MILLIS, NotePreview.NOTE_GAP_MILLIS * 2),
                noteScheduleGaps);

        preview.close();
    }

    private static final class RecordingReceiver implements Receiver {

        private final List<ShortMessage> received = new CopyOnWriteArrayList<>();

        @Override
        public void send(MidiMessage message, long timeStamp) {
            received.add((ShortMessage) message);
        }

        @Override
        public void close() {
        }

        List<Integer> firstTwoCommands() {
            return received.stream().map(ShortMessage::getCommand).limit(2).toList();
        }

        ShortMessage firstOf(int command) {
            return received.stream()
                    .filter(message -> message.getCommand() == command)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("no llego ningun mensaje " + command));
        }

        List<Integer> notesOn() {
            return received.stream()
                    .filter(message -> message.getCommand() == ShortMessage.NOTE_ON)
                    .map(ShortMessage::getData1)
                    .toList();
        }
    }
}
