package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PitchTrajectory;
import com.gstncaruso.tabpro.core.playback.ScheduledBeat;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public final class MidiSequences {

    private static final int TEMPO_META_TYPE = 0x51;
    private static final int MARKER_META_TYPE = 0x06;
    private static final int VOLUME_CONTROLLER = 7;
    private static final int PAN_CONTROLLER = 10;
    private static final int EXPRESSION_CONTROLLER = 11;
    private static final int RPN_MSB_CONTROLLER = 101;
    private static final int RPN_LSB_CONTROLLER = 100;
    private static final int DATA_ENTRY_MSB_CONTROLLER = 6;
    private static final int DATA_ENTRY_LSB_CONTROLLER = 38;
    private static final int PERCUSSION_CHANNEL = 9;
    private static final int CHANNEL_COUNT = 16;

    /** Cuantos semitonos representa el rango completo de pitch bend, para poder pedir bends de varios tonos. */
    private static final int PITCH_BEND_SENSITIVITY_SEMITONES = 24;
    private static final int PITCH_BEND_CENTER = 8192;
    private static final int PITCH_BEND_MAX = 16383;

    /** Cada cuantos ticks se manda un nuevo valor de pitch bend, para que la curva se escuche suave. */
    private static final long PITCH_BEND_STEP_TICKS = 60;

    private static final int FADE_IN_STEPS = 6;
    private static final int FADE_IN_START_EXPRESSION = 20;
    private static final int FULL_EXPRESSION = 127;

    private MidiSequences() {
    }

    public static Sequence fromTimeline(Timeline timeline) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, timeline.ticksPerQuarter());
            sequence.createTrack().add(tempoEvent(timeline.tempoBpm()));

            int nonPercussionOrdinal = 0;
            for (int index = 0; index < timeline.tracks().size(); index++) {
                TrackTimeline trackTimeline = timeline.tracks().get(index);
                int channel = trackTimeline.percussion() ? PERCUSSION_CHANNEL : channelFor(nonPercussionOrdinal++);
                writeTrack(sequence.createTrack(), index, channel, trackTimeline);
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Optional<BeatPosition> beatPositionOf(MetaMessage message) {
        if (message.getType() != MARKER_META_TYPE) {
            return Optional.empty();
        }
        String[] parts = new String(message.getData(), StandardCharsets.US_ASCII).split("/");
        return Optional.of(new BeatPosition(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])));
    }

    /** El canal MIDI que le toca al enesimo instrumento no percusivo, salteando siempre el 9 (percusion). */
    static int channelFor(int nonPercussionOrdinal) {
        int slot = nonPercussionOrdinal % (CHANNEL_COUNT - 1);
        return slot < PERCUSSION_CHANNEL ? slot : slot + 1;
    }

    private static void writeTrack(Track track, int trackIndex, int channel, TrackTimeline trackTimeline)
            throws InvalidMidiDataException {
        track.add(programChangeEvent(channel, trackTimeline.program()));
        writePitchBendRange(track, channel);
        track.add(controlChangeEvent(channel, VOLUME_CONTROLLER, trackTimeline.volume(), 0));
        track.add(controlChangeEvent(channel, PAN_CONTROLLER, trackTimeline.pan(), 0));
        for (ScheduledNote note : trackTimeline.notes()) {
            writeNote(track, channel, note);
        }
        for (ScheduledBeat beat : trackTimeline.beats()) {
            track.add(markerEvent(trackIndex, beat));
        }
    }

    private static void writeNote(Track track, int channel, ScheduledNote note) throws InvalidMidiDataException {
        track.add(noteOnEvent(channel, note));
        if (!note.bend().isFlat()) {
            writePitchBendCurve(track, channel, note);
        }
        if (note.fadeIn()) {
            writeFadeIn(track, channel, note);
        }
        track.add(noteOffEvent(channel, note));
    }

    /** El rango de pitch bend del canal, para poder pedir bends de varios semitonos y no solo dos. */
    private static void writePitchBendRange(Track track, int channel) throws InvalidMidiDataException {
        track.add(controlChangeEvent(channel, RPN_MSB_CONTROLLER, 0, 0));
        track.add(controlChangeEvent(channel, RPN_LSB_CONTROLLER, 0, 0));
        track.add(controlChangeEvent(channel, DATA_ENTRY_MSB_CONTROLLER, PITCH_BEND_SENSITIVITY_SEMITONES, 0));
        track.add(controlChangeEvent(channel, DATA_ENTRY_LSB_CONTROLLER, 0, 0));
    }

    private static void writePitchBendCurve(Track track, int channel, ScheduledNote note)
            throws InvalidMidiDataException {
        PitchTrajectory bend = note.bend();
        for (long offset = 0; offset < note.durationTicks(); offset += PITCH_BEND_STEP_TICKS) {
            track.add(pitchBendEvent(channel, note.startTick() + offset, bend.semitonesAt(offset)));
        }
        // vuelve al centro justo antes de soltar la nota, para no dejar el canal corrido para la que sigue.
        track.add(pitchBendEvent(channel, note.startTick() + note.durationTicks(), 0.0));
    }

    private static void writeFadeIn(Track track, int channel, ScheduledNote note) throws InvalidMidiDataException {
        for (int step = 0; step <= FADE_IN_STEPS; step++) {
            long tick = note.startTick() + (note.durationTicks() * step) / FADE_IN_STEPS;
            int value = FADE_IN_START_EXPRESSION + ((FULL_EXPRESSION - FADE_IN_START_EXPRESSION) * step) / FADE_IN_STEPS;
            track.add(controlChangeEvent(channel, EXPRESSION_CONTROLLER, value, tick));
        }
    }

    private static MidiEvent controlChangeEvent(int channel, int controller, int value, long tick)
            throws InvalidMidiDataException {
        return new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, controller, value), tick);
    }

    private static MidiEvent tempoEvent(int tempoBpm) throws InvalidMidiDataException {
        int microsecondsPerQuarter = 60_000_000 / tempoBpm;
        byte[] data = {
            (byte) (microsecondsPerQuarter >> 16),
            (byte) (microsecondsPerQuarter >> 8),
            (byte) microsecondsPerQuarter
        };
        return new MidiEvent(new MetaMessage(TEMPO_META_TYPE, data, data.length), 0);
    }

    private static MidiEvent programChangeEvent(int channel, int program) throws InvalidMidiDataException {
        return new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0), 0);
    }

    private static MidiEvent noteOnEvent(int channel, ScheduledNote note) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage(
                ShortMessage.NOTE_ON, channel, note.pitch().midiNumber(), note.velocity().value());
        return new MidiEvent(message, note.startTick());
    }

    private static MidiEvent noteOffEvent(int channel, ScheduledNote note) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage(ShortMessage.NOTE_OFF, channel, note.pitch().midiNumber(), 0);
        return new MidiEvent(message, note.startTick() + note.durationTicks());
    }

    private static MidiEvent pitchBendEvent(int channel, long tick, double semitones) throws InvalidMidiDataException {
        int offset = (int) Math.round((semitones / PITCH_BEND_SENSITIVITY_SEMITONES) * PITCH_BEND_CENTER);
        int value = Math.clamp(PITCH_BEND_CENTER + offset, 0, PITCH_BEND_MAX);
        ShortMessage message = new ShortMessage(ShortMessage.PITCH_BEND, channel, value & 0x7F, (value >> 7) & 0x7F);
        return new MidiEvent(message, tick);
    }

    private static MidiEvent markerEvent(int trackIndex, ScheduledBeat beat) throws InvalidMidiDataException {
        String text = trackIndex + "/" + beat.measure() + "/" + beat.beat();
        byte[] data = text.getBytes(StandardCharsets.US_ASCII);
        return new MidiEvent(new MetaMessage(MARKER_META_TYPE, data, data.length), beat.tick());
    }
}
