package com.gstncaruso.tabpro.midi;

import java.util.function.IntConsumer;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

/**
 * Escucha un instrumento MIDI externo y avisa cada nota que se toca, para
 * escribir la partitura tocando, como describe "Enter Notes > Using a MIDI
 * Instrument".
 */
public final class MidiCapture implements AutoCloseable {

    /** Cuanto puede tardar la segunda nota de un acorde antes de contar como otro beat. */
    public static final int DEFAULT_SENSITIVITY_MILLIS = 60;

    private final MidiDevice device;
    private final CapturedNotes notes;
    private Transmitter transmitter;

    public MidiCapture(MidiDevice.Info info, CapturedNotes notes) throws MidiUnavailableException {
        this.device = MidiSystem.getMidiDevice(info);
        this.notes = notes;
    }

    /** Que hacer con lo que llega: una nota nueva, o una nota del mismo acorde. */
    public interface CapturedNotes {

        void noteInTheSameChord(int midiNumber, int channel);

        void noteInANewBeat(int midiNumber, int channel);
    }

    public void start() throws MidiUnavailableException {
        device.open();
        transmitter = device.getTransmitter();
        transmitter.setReceiver(new NoteReceiver(DEFAULT_SENSITIVITY_MILLIS));
    }

    @Override
    public void close() {
        if (transmitter != null) {
            transmitter.close();
        }
        device.close();
    }

    /** Junta en un acorde las notas que llegan casi juntas, y abre un beat nuevo con las demas. */
    private final class NoteReceiver implements Receiver {

        private final int sensitivityMillis;
        private long lastNoteAt;

        private NoteReceiver(int sensitivityMillis) {
            this.sensitivityMillis = sensitivityMillis;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (!(message instanceof ShortMessage note) || note.getCommand() != ShortMessage.NOTE_ON) {
                return;
            }
            if (note.getData2() == 0) {
                return;
            }
            long now = System.currentTimeMillis();
            IntConsumer destination = now - lastNoteAt <= sensitivityMillis
                    ? midiNumber -> notes.noteInTheSameChord(midiNumber, note.getChannel())
                    : midiNumber -> notes.noteInANewBeat(midiNumber, note.getChannel());
            lastNoteAt = now;
            destination.accept(note.getData1());
        }

        @Override
        public void close() {
        }
    }
}
