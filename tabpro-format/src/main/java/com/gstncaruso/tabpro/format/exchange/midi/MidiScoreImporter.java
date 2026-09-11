package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.ChordFretting;
import com.gstncaruso.tabpro.core.model.DefaultNames;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import com.gstncaruso.tabpro.format.exchange.DurationTicks;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

public final class MidiScoreImporter {

    private static final int OCTAVE = 12;

    private final DefaultNames names;

    public MidiScoreImporter(DefaultNames names) {
        this.names = names;
    }

    public List<MidiTrackSummary> tracksIn(Path path) {
        return parse(path).tracks().stream().map(MidiTrackSummary::of).toList();
    }

    public Score importQuick(Path path) {
        ParsedMidiFile file = parse(path);
        return importQuick(path, file, file.tracks(), false, Optional.empty(), Optional.empty(), true);
    }

    public Score importQuick(Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave) {
        return importQuick(path, selectedMidiTrackIndices, transposeDownOneOctave, Optional.empty());
    }

    public Score importQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        return importQuick(path, selectedMidiTrackIndices, transposeDownOneOctave, precision, true);
    }

    public Score importQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision,
            boolean useTwoChannelsPerTrack) {
        return importQuick(
                path, selectedMidiTrackIndices, transposeDownOneOctave, Optional.empty(), precision, useTwoChannelsPerTrack);
    }

    public Score importQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
            boolean useTwoChannelsPerTrack) {
        ParsedMidiFile file = parse(path);
        List<RawMidiTrack> raws = file.tracks().stream()
                .filter(raw -> selectedMidiTrackIndices.contains(raw.index()))
                .toList();
        return importQuick(path, file, raws, transposeDownOneOctave, chordPositionQuantize, noteDurationQuantize, useTwoChannelsPerTrack);
    }

    private static Score importQuick(
            Path path, ParsedMidiFile file, List<RawMidiTrack> raws, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
            boolean useTwoChannelsPerTrack) {
        if (raws.isEmpty()) {
            throw ScoreFileException.nothingToImport("no tracks with notes in " + path);
        }
        List<Track> tracks = raws.stream()
                .map(raw -> quickTrack(
                        raw, file.grid(), transposeDownOneOctave, chordPositionQuantize, noteDurationQuantize,
                        useTwoChannelsPerTrack))
                .toList();
        String title = file.title().orElseGet(() -> titleFromFileName(path));
        return new Score(title, file.tempoBpm(), tracks);
    }

    public List<Measure> importMeasures(
            Path path, List<Integer> midiTrackIndices, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        return importMeasures(path, midiTrackIndices, tuning, fretCount, transposeDownOneOctave, Optional.empty());
    }

    public List<Measure> importMeasures(
            Path path, List<Integer> midiTrackIndices, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        return importMeasures(
                path, midiTrackIndices, tuning, fretCount, transposeDownOneOctave, Optional.empty(), precision);
    }

    public List<Measure> importMeasures(
            Path path, List<Integer> midiTrackIndices, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
        ParsedMidiFile file = parse(path);
        RawMidiTrack raw = merge(tracksAt(file, midiTrackIndices)).withPositionsQuantizedTo(chordPositionQuantize);
        return measuresOf(raw, file.grid(), tuning, fretCount, transposeDownOneOctave, noteDurationQuantize);
    }

    public Track importInto(Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave) {
        return importInto(target, path, midiTrackIndices, transposeDownOneOctave, Optional.empty());
    }

    public Track importInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        return importInto(target, path, midiTrackIndices, transposeDownOneOctave, Optional.empty(), precision);
    }

    public Track importInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
        List<Measure> measures = importMeasures(
                path, midiTrackIndices, target.tuning(), target.settings().fretCount(), transposeDownOneOctave,
                chordPositionQuantize, noteDurationQuantize);
        return target.withMeasures(measures);
    }

    public Timeline timelineOf(Path path, List<Integer> midiTrackIndices) {
        ParsedMidiFile file = parse(path);
        RawMidiTrack raw = merge(tracksAt(file, midiTrackIndices));
        return timelineOf(raw, file.tempoBpm());
    }

    private static Timeline timelineOf(RawMidiTrack raw, int tempoBpm) {
        List<ScheduledNote> notes = new ArrayList<>();
        raw.notesByTick().forEach((tick, chord) -> chord.forEach(
                note -> notes.add(new ScheduledNote(tick, note.durationTicks(), new Pitch(note.number())))));
        TrackTimeline track = new TrackTimeline(raw.program(), raw.volume(), raw.pan(), raw.percussion(), notes, List.of());
        return new Timeline(tempoBpm, Duration.TICKS_PER_QUARTER, List.of(track));
    }

    public Score importTitleAndTimeSignatures(Score target, Path path) {
        ParsedMidiFile file = parse(path);
        String title = file.title().orElseGet(() -> titleFromFileName(path));
        Score result = target.withTitle(title).withTempo(file.tempoBpm());
        int measureCount = Math.min(result.measureCount(), file.grid().measureCount());
        for (int index = 0; index < measureCount; index++) {
            result = result.withTimeSignatureFrom(index, file.grid().timeSignatureOf(index));
        }
        return result;
    }

    private ParsedMidiFile parse(Path path) {
        try {
            Sequence sequence = MidiSystem.getSequence(path.toFile());
            return MidiFileParser.parse(sequence, names);
        } catch (InvalidMidiDataException e) {
            throw ScoreFileException.notRecognized("MIDI", "invalid MIDI data: " + path, e);
        } catch (IOException e) {
            throw ScoreFileException.cannotRead(path, e);
        }
    }

    private static List<RawMidiTrack> tracksAt(ParsedMidiFile file, List<Integer> midiTrackIndices) {
        List<RawMidiTrack> raws = midiTrackIndices.stream().map(index -> trackAt(file, index)).toList();
        if (raws.isEmpty()) {
            throw ScoreFileException.nothingToImport("no MIDI track was chosen");
        }
        return raws;
    }

    private static RawMidiTrack trackAt(ParsedMidiFile file, int midiTrackIndex) {
        return file.tracks().stream()
                .filter(raw -> raw.index() == midiTrackIndex)
                .findFirst()
                .orElseThrow(() -> ScoreFileException.nothingToImport("MIDI track " + midiTrackIndex + " has no notes"));
    }

    private static RawMidiTrack merge(List<RawMidiTrack> raws) {
        if (raws.size() == 1) {
            return raws.getFirst();
        }
        SortedMap<Long, List<RawNote>> notesByTick = new TreeMap<>();
        for (RawMidiTrack raw : raws) {
            raw.notesByTick().forEach((tick, notes) -> notesByTick.computeIfAbsent(tick, key -> new ArrayList<>()).addAll(notes));
        }
        boolean percussion = raws.stream().allMatch(RawMidiTrack::percussion);
        return new RawMidiTrack(
                raws.getFirst().index(), "", 0, 1, 1, Channel.DEFAULT_VOLUME, Channel.CENTER_PAN, 0, 0, 0, 0, percussion, notesByTick);
    }

    private static Track quickTrack(
            RawMidiTrack raw, MeasureGrid grid, boolean transposeDownOneOctave, Optional<NoteValue> chordPositionQuantize,
            Optional<NoteValue> noteDurationQuantize, boolean useTwoChannelsPerTrack) {
        RawMidiTrack quantized = raw.withPositionsQuantizedTo(chordPositionQuantize);
        Tuning tuning = quantized.percussion()
                ? PercussionKit.tuning() : TrackTuningGuess.forQuickImport(quantized.name(), quantized.program());
        List<Measure> measures = measuresOf(
                quantized, grid, tuning, TrackSettings.DEFAULT_FRET_COUNT, transposeDownOneOctave, noteDurationQuantize);
        TrackSettings settings = quantized.percussion()
                ? TrackSettings.percussion(Track.colorFor(quantized.index()))
                : TrackSettings.standard(Track.colorFor(quantized.index()));
        return new Track(quantized.name(), tuning, channelOf(quantized, useTwoChannelsPerTrack), settings, measures);
    }

    private static Channel channelOf(RawMidiTrack raw, boolean useTwoChannelsPerTrack) {
        return Channel.playing(raw.program())
                .withVolume(raw.volume())
                .withPan(raw.pan())
                .withReverb(raw.reverb())
                .withTremolo(raw.tremolo())
                .withChorus(raw.chorus())
                .withPhaser(raw.phaser())
                .withPort(raw.port())
                .withNumber(raw.channelNumber())
                .withEffectChannel(Channel.effectChannelFor(raw.channelNumber(), useTwoChannelsPerTrack));
    }

    private static List<Measure> measuresOf(
            RawMidiTrack raw, MeasureGrid grid, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        List<Measure> measures = new ArrayList<>();
        for (int index = 0; index < grid.measureCount(); index++) {
            measures.add(measureAt(raw, grid, index, tuning, fretCount, transposeDownOneOctave, precision));
        }
        return measures;
    }

    private static Measure measureAt(
            RawMidiTrack raw, MeasureGrid grid, int index, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        long start = grid.startTick(index);
        long end = grid.endTick(index);
        List<Beat> beats = beatsBetween(raw, start, end, tuning, fretCount, transposeDownOneOctave, precision);
        MeasureAttributes attributes = MeasureAttributes.plain().withKeySignature(grid.keySignatureOf(index));
        return new Measure(grid.timeSignatureOf(index), attributes, List.of(new Voice(beats), Voice.unused()));
    }

    private static List<Beat> beatsBetween(
            RawMidiTrack raw, long start, long end, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        SortedMap<Long, List<RawNote>> attacks = raw.notesByTick().subMap(start, end);
        if (attacks.isEmpty()) {
            return restsBetween(start, end);
        }
        List<Long> ticks = new ArrayList<>(attacks.keySet());
        List<Beat> beats = new ArrayList<>();
        long position = start;
        for (int i = 0; i < ticks.size(); i++) {
            long tick = ticks.get(i);
            if (tick > position) {
                beats.addAll(restsBetween(position, tick));
            }
            long nextAttack = i + 1 < ticks.size() ? ticks.get(i + 1) : end;
            List<RawNote> chord = attacks.get(tick);
            long sustain = chord.stream().mapToLong(RawNote::durationTicks).max().orElse(DurationTicks.GRID_TICKS);
            long soundingUntil = Math.min(tick + sustain, nextAttack);
            Duration duration = quantized(soundingUntil - tick, precision);
            List<Note> notes = notesFor(raw, chord, tuning, fretCount, transposeDownOneOctave);
            beats.add(new Beat(duration, notes));
            position = tick + duration.ticks();
        }
        if (position < end) {
            beats.addAll(restsBetween(position, end));
        }
        return beats;
    }

    private static Duration quantized(long ticks, Optional<NoteValue> precision) {
        return precision.map(finestGrid -> DurationTicks.nearestTo(ticks, finestGrid)).orElseGet(() -> DurationTicks.nearestTo(ticks));
    }

    private static List<Note> notesFor(
            RawMidiTrack raw, List<RawNote> chord, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        if (raw.percussion()) {
            return PercussionChord.notesFor(chord.stream().map(RawNote::number).toList());
        }
        List<Pitch> pitches = chord.stream()
                .map(RawNote::number)
                .map(sound -> transposeDownOneOctave ? Math.max(0, sound - OCTAVE) : sound)
                .map(Pitch::new)
                .toList();
        return ChordFretting.assign(tuning, fretCount, pitches);
    }

    private static List<Beat> restsBetween(long start, long end) {
        return DurationTicks.decompose(end - start).stream().map(Beat::rest).toList();
    }

    private static String titleFromFileName(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
