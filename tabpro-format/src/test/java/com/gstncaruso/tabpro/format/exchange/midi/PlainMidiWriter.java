package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

/**
 * Armador de archivos MIDI de prueba: escribe una partitura como notacion plana —una pista de
 * tempo y armadura, y una pista por pista audible, con su instrumento, su mezcla y sus notas de
 * corrido— para poder probar el importador sin depender de ningun exportador.
 *
 * <p>Es a proposito lo mas parecido a lo que escribe cualquier otro programa: sin efectos, sin
 * repeticiones y con un solo canal por pista. Eso es justo lo que el importador tiene que saber
 * leer, y no lo que tabpro exporta, que rinde la partitura entera como suena.
 */
final class PlainMidiWriter {

    private static final int SEQUENCE_FORMAT = 1;

    private static final int TRACK_NAME_META = 0x03;
    private static final int END_OF_TRACK_META = 0x2F;
    private static final int TEMPO_META = 0x51;
    private static final int TIME_SIGNATURE_META = 0x58;
    private static final int KEY_SIGNATURE_META = 0x59;

    private static final int VOLUME_CC = 7;
    private static final int PAN_CC = 10;

    private static final int CLOCKS_PER_METRONOME_CLICK = 24;
    private static final int THIRTY_SECONDS_PER_QUARTER = 8;

    private PlainMidiWriter() {
    }

    static Sequence sequenceOf(Score score) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, (int) Duration.TICKS_PER_QUARTER);
            writeConductor(sequence.createTrack(), score);
            for (int index = 0; index < score.trackCount(); index++) {
                Track track = score.track(index);
                if (!track.channel().isSilent()) {
                    writeTrack(sequence.createTrack(), track);
                }
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    static void write(Score score, Path path) {
        write(sequenceOf(score), path);
    }

    static void write(Sequence sequence, Path path) {
        try {
            MidiSystem.write(sequence, SEQUENCE_FORMAT, path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeConductor(javax.sound.midi.Track conductor, Score score) throws InvalidMidiDataException {
        addMeta(conductor, TRACK_NAME_META, score.title().getBytes(StandardCharsets.UTF_8), 0);
        addTempo(conductor, score.tempo());
        Track reference = score.track(0);
        TimeSignature previousSignature = null;
        KeySignature previousKey = null;
        long tick = 0;
        for (int index = 0; index < reference.measureCount(); index++) {
            Measure measure = reference.measure(index);
            TimeSignature signature = measure.timeSignature();
            KeySignature key = measure.attributes().keySignature();
            if (index == 0 || !signature.equals(previousSignature)) {
                addTimeSignature(conductor, tick, signature);
            }
            if (index == 0 || !key.equals(previousKey)) {
                addKeySignature(conductor, tick, key);
            }
            previousSignature = signature;
            previousKey = key;
            tick += signature.ticksPerMeasure();
        }
        addMeta(conductor, END_OF_TRACK_META, new byte[0], tick);
    }

    private static void writeTrack(javax.sound.midi.Track midiTrack, Track track) throws InvalidMidiDataException {
        Channel channel = track.channel();
        int midiChannel = channel.number() - 1;
        addMeta(midiTrack, TRACK_NAME_META, track.name().getBytes(StandardCharsets.UTF_8), 0);
        add(midiTrack, ShortMessage.PROGRAM_CHANGE, midiChannel, channel.program(), 0, 0);
        add(midiTrack, ShortMessage.CONTROL_CHANGE, midiChannel, VOLUME_CC, channel.volume(), 0);
        add(midiTrack, ShortMessage.CONTROL_CHANGE, midiChannel, PAN_CC, channel.pan(), 0);

        long tick = 0;
        Sustains sustains = new Sustains();
        for (int index = 0; index < track.measureCount(); index++) {
            Measure measure = track.measure(index);
            writeVoice(midiTrack, midiChannel, measure.voice(VoicePart.LEAD), tick, track, sustains);
            tick += measure.timeSignature().ticksPerMeasure();
        }
        sustains.closeEverythingAt(midiTrack, midiChannel, tick);
        addMeta(midiTrack, END_OF_TRACK_META, new byte[0], tick);
    }

    private static void writeVoice(
            javax.sound.midi.Track midiTrack, int midiChannel, Voice voice, long measureStart, Track track,
            Sustains sustains) throws InvalidMidiDataException {
        long tick = measureStart;
        for (Beat beat : voice.beats()) {
            for (Note note : beat.notes()) {
                int soundOrPitch = track.isPercussion() ? note.fret() : track.pitchOf(note).midiNumber();
                long end = tick + Math.max(1, Math.round(beat.duration().ticks() * note.soundLength()));
                if (note.tied()) {
                    sustains.extend(note.string(), end);
                    continue;
                }
                sustains.closeAt(midiTrack, midiChannel, note.string(), tick);
                add(midiTrack, ShortMessage.NOTE_ON, midiChannel, soundOrPitch, note.velocity().value(), tick);
                sustains.open(note.string(), soundOrPitch, end);
            }
            tick += beat.duration().ticks();
        }
    }

    /** Lo que sigue sonando en cada cuerda, para poder apagarlo cuando corresponde. */
    private static final class Sustains {

        private record Ringing(int soundOrPitch, long endTick) {
        }

        private final Map<Integer, Ringing> byString = new HashMap<>();

        void open(int string, int soundOrPitch, long endTick) {
            byString.put(string, new Ringing(soundOrPitch, endTick));
        }

        void extend(int string, long endTick) {
            Ringing ringing = byString.get(string);
            if (ringing != null) {
                byString.put(string, new Ringing(ringing.soundOrPitch(), endTick));
            }
        }

        void closeAt(javax.sound.midi.Track midiTrack, int midiChannel, int string, long cutoffTick)
                throws InvalidMidiDataException {
            Ringing ringing = byString.remove(string);
            if (ringing != null) {
                add(midiTrack, ShortMessage.NOTE_OFF, midiChannel, ringing.soundOrPitch(), 0,
                        Math.min(ringing.endTick(), cutoffTick));
            }
        }

        void closeEverythingAt(javax.sound.midi.Track midiTrack, int midiChannel, long tick)
                throws InvalidMidiDataException {
            for (Integer string : java.util.List.copyOf(byString.keySet())) {
                closeAt(midiTrack, midiChannel, string, tick);
            }
        }
    }

    private static void addTempo(javax.sound.midi.Track track, int bpm) throws InvalidMidiDataException {
        int microsecondsPerQuarter = 60_000_000 / bpm;
        byte[] data = {
            (byte) (microsecondsPerQuarter >> 16), (byte) (microsecondsPerQuarter >> 8), (byte) microsecondsPerQuarter
        };
        addMeta(track, TEMPO_META, data, 0);
    }

    private static void addTimeSignature(javax.sound.midi.Track track, long tick, TimeSignature signature)
            throws InvalidMidiDataException {
        byte denominatorPower = (byte) Integer.numberOfTrailingZeros(signature.beatUnit());
        byte[] data = {
            (byte) signature.beats(), denominatorPower, CLOCKS_PER_METRONOME_CLICK, THIRTY_SECONDS_PER_QUARTER
        };
        addMeta(track, TIME_SIGNATURE_META, data, tick);
    }

    private static void addKeySignature(javax.sound.midi.Track track, long tick, KeySignature key)
            throws InvalidMidiDataException {
        byte[] data = {(byte) key.accidentals(), (byte) (key.mode() == Mode.MINOR ? 1 : 0)};
        addMeta(track, KEY_SIGNATURE_META, data, tick);
    }

    private static void addMeta(javax.sound.midi.Track track, int type, byte[] data, long tick)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(new MetaMessage(type, data, data.length), tick));
    }

    private static void add(javax.sound.midi.Track track, int command, int channel, int data1, int data2, long tick)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(new ShortMessage(command, channel, data1, data2), tick));
    }
}
