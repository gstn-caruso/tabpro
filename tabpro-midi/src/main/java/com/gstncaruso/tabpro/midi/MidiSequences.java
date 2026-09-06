package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.MetronomeClick;
import com.gstncaruso.tabpro.core.playback.PitchTrajectory;
import com.gstncaruso.tabpro.core.playback.ScheduledBeat;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.ScheduledParameter;
import com.gstncaruso.tabpro.core.playback.TempoChange;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
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
    private static final int REVERB_CONTROLLER = 91;
    private static final int TREMOLO_CONTROLLER = 92;
    private static final int CHORUS_CONTROLLER = 93;
    private static final int PHASER_CONTROLLER = 95;
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

    /** Cuanto es "mas de un tono", el limite de Limit Pitch Variation del manual. */
    static final double LIMITED_PITCH_VARIATION_SEMITONES = 2.0;

    private MidiSequences() {
    }

    public static Sequence fromTimeline(Timeline timeline) {
        return fromTimeline(timeline, Set.of());
    }

    /**
     * La misma secuencia, pero silenciando la curva de altura de las notas de
     * los puertos que tildaron Limit Pitch Variation cuando esa curva se
     * mueve mas de un tono, tal como pide el manual.
     */
    public static Sequence fromTimeline(Timeline timeline, Set<Integer> limitedPitchVariationPorts) {
        return buildSequence(timeline, indexedTracksOf(timeline, limitedPitchVariationPorts));
    }

    /**
     * Una secuencia por cada puerto de salida que use la partitura, cada una
     * con sus propios canales locales (del 0 al 15), para poder reproducir
     * los cuatro puertos del manual en dispositivos distintos a la vez.
     */
    public static Map<Integer, Sequence> sequencesByPort(Timeline timeline, Set<Integer> limitedPitchVariationPorts) {
        Map<Integer, List<IndexedTrack>> tracksByPort = new TreeMap<>();
        for (IndexedTrack indexed : indexedTracksOf(timeline, limitedPitchVariationPorts)) {
            tracksByPort.computeIfAbsent(indexed.track().port(), port -> new ArrayList<>()).add(indexed);
        }
        Map<Integer, Sequence> sequencesByPort = new LinkedHashMap<>();
        tracksByPort.forEach((port, tracks) -> sequencesByPort.put(port, buildSequence(timeline, tracks)));
        return sequencesByPort;
    }

    private static List<IndexedTrack> indexedTracksOf(Timeline timeline, Set<Integer> limitedPitchVariationPorts) {
        List<IndexedTrack> indexed = new ArrayList<>();
        for (int index = 0; index < timeline.tracks().size(); index++) {
            TrackTimeline trackTimeline = timeline.tracks().get(index);
            boolean limitPitchVariation = limitedPitchVariationPorts.contains(trackTimeline.port());
            indexed.add(new IndexedTrack(index, trackTimeline, limitPitchVariation));
        }
        return indexed;
    }

    /** Una pista con su indice original, para que el marcador de beat siga nombrando la pista real. */
    private record IndexedTrack(int originalIndex, TrackTimeline track, boolean limitPitchVariation) {
    }

    private static Sequence buildSequence(Timeline timeline, List<IndexedTrack> tracks) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, timeline.ticksPerQuarter());
            Track conductor = sequence.createTrack();
            for (TempoChange stretch : timeline.tempo().changes()) {
                conductor.add(tempoEvent(stretch));
            }

            int nonPercussionOrdinal = 0;
            for (IndexedTrack indexed : tracks) {
                TrackTimeline trackTimeline = indexed.track();
                TrackChannels channels = trackTimeline.percussion()
                        ? TrackChannels.percussion()
                        : TrackChannels.ofTheTrackNumber(nonPercussionOrdinal++);
                writeTrack(sequence.createTrack(), indexed.originalIndex(), channels, trackTimeline,
                        indexed.limitPitchVariation());
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Cuanto dura cada click del metronomo. */
    private static final long METRONOME_CLICK_TICKS = 60;

    /** Agrega el metronomo como una pista mas de percusion, si es que tiene algun click para tocar. */
    public static void addMetronomeTrack(Sequence sequence, List<MetronomeClick> clicks) {
        if (clicks.isEmpty()) {
            return;
        }
        try {
            Track track = sequence.createTrack();
            track.add(programChangeEvent(PERCUSSION_CHANNEL, 0));
            for (MetronomeClick click : clicks) {
                ShortMessage on = new ShortMessage(
                        ShortMessage.NOTE_ON, PERCUSSION_CHANNEL, click.sound(), click.velocity());
                ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, PERCUSSION_CHANNEL, click.sound(), 0);
                track.add(new MidiEvent(on, click.tick()));
                track.add(new MidiEvent(off, click.tick() + METRONOME_CLICK_TICKS));
            }
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

    /** El canal MIDI que le toca a la enesima ranura, salteando siempre el 9 (percusion). */
    static int channelFor(int slotNumber) {
        int slot = slotNumber % (CHANNEL_COUNT - 1);
        return slot < PERCUSSION_CHANNEL ? slot : slot + 1;
    }

    /**
     * Los dos canales de una pista, como los reparte Guitar Pro: uno para las
     * notas limpias y el siguiente para las que llevan efecto, asi correrle la
     * altura a una no arrastra a las demas. La percusion toca todo en el suyo.
     */
    private record TrackChannels(int clean, int effects) {

        static TrackChannels percussion() {
            return new TrackChannels(PERCUSSION_CHANNEL, PERCUSSION_CHANNEL);
        }

        static TrackChannels ofTheTrackNumber(int nonPercussionOrdinal) {
            return new TrackChannels(channelFor(2 * nonPercussionOrdinal), channelFor(2 * nonPercussionOrdinal + 1));
        }

        int of(ScheduledNote note) {
            return note.carriesAnEffect() ? effects : clean;
        }

        /** Los canales que hay que preparar: uno solo cuando la pista usa el mismo para todo. */
        List<Integer> toPrepare() {
            return clean == effects ? List.of(clean) : List.of(clean, effects);
        }
    }

    private static void writeTrack(
            Track track, int trackIndex, TrackChannels channels, TrackTimeline trackTimeline,
            boolean limitPitchVariation)
            throws InvalidMidiDataException {
        for (int channel : channels.toPrepare()) {
            track.add(programChangeEvent(channel, trackTimeline.program()));
            writePitchBendRange(track, channel);
            track.add(controlChangeEvent(channel, VOLUME_CONTROLLER, trackTimeline.volume(), 0));
            track.add(controlChangeEvent(channel, PAN_CONTROLLER, trackTimeline.pan(), 0));
        }
        for (ScheduledNote note : trackTimeline.notes()) {
            writeNote(track, channels.of(note), note, limitPitchVariation);
        }
        for (ScheduledBeat beat : trackTimeline.beats()) {
            track.add(markerEvent(trackIndex, beat));
        }
        for (ScheduledParameter parameter : trackTimeline.parameters()) {
            for (int channel : channels.toPrepare()) {
                track.add(parameterEvent(channel, parameter));
            }
        }
    }

    /** Lo que deja un cambio de parametro: el instrumento viaja como program change y el resto como controlador. */
    private static MidiEvent parameterEvent(int channel, ScheduledParameter parameter)
            throws InvalidMidiDataException {
        if (parameter.parameter() == SoundParameter.PROGRAM) {
            ShortMessage message = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, parameter.value(), 0);
            return new MidiEvent(message, parameter.tick());
        }
        return controlChangeEvent(channel, controllerOf(parameter.parameter()), parameter.value(), parameter.tick());
    }

    private static int controllerOf(SoundParameter parameter) {
        return switch (parameter) {
            case VOLUME -> VOLUME_CONTROLLER;
            case PAN -> PAN_CONTROLLER;
            case REVERB -> REVERB_CONTROLLER;
            case TREMOLO -> TREMOLO_CONTROLLER;
            case CHORUS -> CHORUS_CONTROLLER;
            case PHASER -> PHASER_CONTROLLER;
            case PROGRAM, TEMPO -> throw new IllegalArgumentException(
                    parameter.label() + " no viaja como controlador MIDI");
        };
    }

    private static void writeNote(Track track, int channel, ScheduledNote note, boolean limitPitchVariation)
            throws InvalidMidiDataException {
        track.add(noteOnEvent(channel, note));
        boolean playsTheBend = !note.bend().isFlat()
                && (!limitPitchVariation || note.bend().staysWithin(LIMITED_PITCH_VARIATION_SEMITONES));
        if (playsTheBend) {
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

    private static MidiEvent tempoEvent(TempoChange stretch) throws InvalidMidiDataException {
        int microsecondsPerQuarter = 60_000_000 / stretch.bpm();
        byte[] data = {
            (byte) (microsecondsPerQuarter >> 16),
            (byte) (microsecondsPerQuarter >> 8),
            (byte) microsecondsPerQuarter
        };
        return new MidiEvent(new MetaMessage(TEMPO_META_TYPE, data, data.length), stretch.tick());
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
