package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

/**
 * Pasa una partitura a un archivo MIDI formato 1: una pista de tempo y armadura, y una pista
 * por cada pista audible de la partitura, con su instrumento, volumen, paneo y las notas con
 * su duracion real. Las pistas silenciadas no se exportan, como avisa el manual.
 */
public final class MidiScoreExporter {

    private static final int SEQUENCE_FORMAT = 1;

    private static final int TRACK_NAME_META = 0x03;
    private static final int MIDI_PORT_META = 0x21;
    private static final int END_OF_TRACK_META = 0x2F;
    private static final int TEMPO_META = 0x51;
    private static final int TIME_SIGNATURE_META = 0x58;
    private static final int KEY_SIGNATURE_META = 0x59;

    private static final int VOLUME_CC = 7;
    private static final int PAN_CC = 10;
    private static final int REVERB_CC = 91;
    private static final int TREMOLO_CC = 92;
    private static final int CHORUS_CC = 93;
    private static final int PHASER_CC = 95;

    private static final int DEFAULT_PORT = 1;
    private static final int CLOCKS_PER_METRONOME_CLICK = 24;
    private static final int THIRTY_SECONDS_PER_QUARTER = 8;

    public Sequence toSequence(Score score) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, (int) Duration.TICKS_PER_QUARTER);
            long[] measureStarts = measureStartTicks(score);
            writeConductor(sequence.createTrack(), score);
            for (int index = 0; index < score.trackCount(); index++) {
                Track track = score.track(index);
                if (!track.channel().isSilent()) {
                    writeTrack(sequence.createTrack(), track, measureStarts);
                }
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    public void export(Score score, Path path) {
        try {
            MidiSystem.write(toSequence(score), SEQUENCE_FORMAT, path.toFile());
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo escribir " + path, e);
        }
    }

    /** El comienzo de cada compas en tics, segun la medida que define la primera pista. */
    private static long[] measureStartTicks(Score score) {
        int measureCount = score.measureCount();
        long[] starts = new long[measureCount + 1];
        Track reference = score.track(0);
        long tick = 0;
        for (int index = 0; index < measureCount; index++) {
            starts[index] = tick;
            TimeSignature signature = index < reference.measureCount()
                    ? reference.measure(index).timeSignature()
                    : TimeSignature.fourFour();
            tick += signature.ticksPerMeasure();
        }
        starts[measureCount] = tick;
        return starts;
    }

    private static void writeConductor(javax.sound.midi.Track conductor, Score score) throws InvalidMidiDataException {
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
        addEndOfTrack(conductor, tick);
    }

    private static void writeTrack(javax.sound.midi.Track midiTrack, Track track, long[] measureStarts)
            throws InvalidMidiDataException {
        Channel channel = track.channel();
        int midiChannel = channel.number() - 1;
        addMetaText(midiTrack, TRACK_NAME_META, track.name(), 0);
        if (channel.port() != DEFAULT_PORT) {
            addPort(midiTrack, channel.port());
        }
        addShortMessage(midiTrack, ShortMessage.PROGRAM_CHANGE, midiChannel, channel.program(), 0, 0);
        addControlChange(midiTrack, midiChannel, VOLUME_CC, channel.volume(), 0);
        addControlChange(midiTrack, midiChannel, PAN_CC, channel.pan(), 0);
        addControlChange(midiTrack, midiChannel, REVERB_CC, channel.reverb(), 0);
        addControlChange(midiTrack, midiChannel, TREMOLO_CC, channel.tremolo(), 0);
        addControlChange(midiTrack, midiChannel, CHORUS_CC, channel.chorus(), 0);
        addControlChange(midiTrack, midiChannel, PHASER_CC, channel.phaser(), 0);

        NoteEventWriter lead = new NoteEventWriter(midiTrack, midiChannel);
        NoteEventWriter bass = new NoteEventWriter(midiTrack, midiChannel);
        for (int index = 0; index < track.measureCount(); index++) {
            Measure measure = track.measure(index);
            writeVoice(lead, measure.voice(VoicePart.LEAD), measureStarts[index], track);
            if (measure.usesTwoVoices()) {
                writeVoice(bass, measure.voice(VoicePart.BASS), measureStarts[index], track);
            }
        }
        long trackEnd = measureStarts[track.measureCount()];
        lead.closeEverythingAt(trackEnd);
        bass.closeEverythingAt(trackEnd);
        addEndOfTrack(midiTrack, trackEnd);
    }

    private static void writeVoice(NoteEventWriter writer, Voice voice, long measureStart, Track track) {
        long tick = measureStart;
        for (Beat beat : voice.beats()) {
            for (Note note : beat.notes()) {
                int soundOrPitch = track.isPercussion() ? note.fret() : track.pitchOf(note).midiNumber();
                writer.attack(tick, beat.duration().ticks(), note, soundOrPitch);
            }
            tick += beat.duration().ticks();
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

    private static void addPort(javax.sound.midi.Track track, int port) throws InvalidMidiDataException {
        addMeta(track, MIDI_PORT_META, new byte[] {(byte) (port - 1)}, 0);
    }

    private static void addEndOfTrack(javax.sound.midi.Track track, long tick) throws InvalidMidiDataException {
        addMeta(track, END_OF_TRACK_META, new byte[0], tick);
    }

    private static void addMetaText(javax.sound.midi.Track track, int type, String text, long tick)
            throws InvalidMidiDataException {
        addMeta(track, type, text.getBytes(StandardCharsets.UTF_8), tick);
    }

    private static void addMeta(javax.sound.midi.Track track, int type, byte[] data, long tick)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(new MetaMessage(type, data, data.length), tick));
    }

    private static void addControlChange(javax.sound.midi.Track track, int channel, int controller, int value, long tick)
            throws InvalidMidiDataException {
        addShortMessage(track, ShortMessage.CONTROL_CHANGE, channel, controller, value, tick);
    }

    private static void addShortMessage(javax.sound.midi.Track track, int command, int channel, int data1, int data2, long tick)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(new ShortMessage(command, channel, data1, data2), tick));
    }
}
