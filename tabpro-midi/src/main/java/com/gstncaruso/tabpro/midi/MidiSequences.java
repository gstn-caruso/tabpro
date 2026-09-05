package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.playback.BeatPosition;
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
    private static final int NOTE_VELOCITY = 100;

    private MidiSequences() {
    }

    public static Sequence fromTimeline(Timeline timeline) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, timeline.ticksPerQuarter());
            sequence.createTrack().add(tempoEvent(timeline.tempoBpm()));

            int channel = 0;
            for (TrackTimeline trackTimeline : timeline.tracks()) {
                writeTrack(sequence.createTrack(), channel, trackTimeline);
                channel++;
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

    private static void writeTrack(Track track, int channel, TrackTimeline trackTimeline)
            throws InvalidMidiDataException {
        track.add(programChangeEvent(channel, trackTimeline.midiProgram()));
        for (ScheduledNote note : trackTimeline.notes()) {
            track.add(noteOnEvent(channel, note));
            track.add(noteOffEvent(channel, note));
        }
        for (ScheduledBeat beat : trackTimeline.beats()) {
            track.add(markerEvent(channel, beat));
        }
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
                ShortMessage.NOTE_ON, channel, note.pitch().midiNumber(), NOTE_VELOCITY);
        return new MidiEvent(message, note.startTick());
    }

    private static MidiEvent noteOffEvent(int channel, ScheduledNote note) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage(ShortMessage.NOTE_OFF, channel, note.pitch().midiNumber(), 0);
        return new MidiEvent(message, note.startTick() + note.durationTicks());
    }

    private static MidiEvent markerEvent(int trackIndex, ScheduledBeat beat) throws InvalidMidiDataException {
        String text = trackIndex + "/" + beat.measure() + "/" + beat.beat();
        byte[] data = text.getBytes(StandardCharsets.US_ASCII);
        return new MidiEvent(new MetaMessage(MARKER_META_TYPE, data, data.length), beat.tick());
    }
}
