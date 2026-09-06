package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.ChordFretting;
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

/**
 * Trae una partitura desde un archivo MIDI (formato 0 o 1). Ofrece el "import rapido" del
 * manual, una pista de tabpro por cada pista MIDI con la afinacion deducida de su nombre o
 * instrumento, y el "paso a paso", que trae una o varias pistas MIDI elegidas (fusionadas en
 * una sola si son varias) sobre una pista existente (con su propia afinacion) y permite
 * transportarla una octava abajo. Las notas que no entran en la afinacion elegida se descartan.
 * En los dos modos se puede elegir la precision con la que se cuantiza la posicion y la
 * duracion de las notas -- vacio es sin restringir, como una interpretacion humana no cuantizada.
 */
public final class MidiScoreImporter {

    private static final int OCTAVE = 12;

    public List<MidiTrackSummary> tracksIn(Path path) {
        return parse(path).tracks().stream().map(MidiTrackSummary::of).toList();
    }

    /** Una pista de tabpro por cada pista del MIDI que tenga notas. */
    public Score importQuick(Path path) {
        ParsedMidiFile file = parse(path);
        return importQuick(path, file, file.tracks(), false, Optional.empty(), true);
    }

    /** El import rapido, pero solo con las pistas MIDI elegidas y con transposicion opcional. */
    public Score importQuick(Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave) {
        return importQuick(path, selectedMidiTrackIndices, transposeDownOneOctave, Optional.empty());
    }

    /** Lo mismo, pero cuantizando posicion y duracion con la precision elegida. */
    public Score importQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        return importQuick(path, selectedMidiTrackIndices, transposeDownOneOctave, precision, true);
    }

    /**
     * Lo mismo, pero con la casilla "Use 2 channels per track" del manual: con dos canales el
     * de efectos es el que sigue al de la pista; con uno solo, los dos coinciden.
     */
    public Score importQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision,
            boolean useTwoChannelsPerTrack) {
        ParsedMidiFile file = parse(path);
        List<RawMidiTrack> raws = file.tracks().stream()
                .filter(raw -> selectedMidiTrackIndices.contains(raw.index()))
                .toList();
        return importQuick(path, file, raws, transposeDownOneOctave, precision, useTwoChannelsPerTrack);
    }

    private static Score importQuick(
            Path path, ParsedMidiFile file, List<RawMidiTrack> raws, boolean transposeDownOneOctave, Optional<NoteValue> precision,
            boolean useTwoChannelsPerTrack) {
        if (raws.isEmpty()) {
            throw new ScoreFileException("el archivo " + path + " no tiene pistas con notas para importar");
        }
        List<Track> tracks = raws.stream()
                .map(raw -> quickTrack(raw, file.grid(), transposeDownOneOctave, precision, useTwoChannelsPerTrack))
                .toList();
        String title = file.title().orElseGet(() -> titleFromFileName(path));
        return new Score(title, file.tempoBpm(), tracks);
    }

    /** Los compases de una o varias pistas MIDI elegidas (fusionadas si son varias), listos para reemplazar los de una pista propia. */
    public List<Measure> importMeasures(
            Path path, List<Integer> midiTrackIndices, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        return importMeasures(path, midiTrackIndices, tuning, fretCount, transposeDownOneOctave, Optional.empty());
    }

    /** Lo mismo, pero cuantizando posicion y duracion con la precision elegida. */
    public List<Measure> importMeasures(
            Path path, List<Integer> midiTrackIndices, Tuning tuning, int fretCount, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        ParsedMidiFile file = parse(path);
        RawMidiTrack raw = merge(tracksAt(file, midiTrackIndices));
        return measuresOf(raw, file.grid(), tuning, fretCount, transposeDownOneOctave, precision);
    }

    /** El "paso a paso" del manual: la o las pistas MIDI elegidas reemplazan los compases de target. */
    public Track importInto(Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave) {
        return importInto(target, path, midiTrackIndices, transposeDownOneOctave, Optional.empty());
    }

    /** Lo mismo, pero cuantizando posicion y duracion con la precision elegida. */
    public Track importInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        List<Measure> measures = importMeasures(
                path, midiTrackIndices, target.tuning(), target.settings().fretCount(), transposeDownOneOctave, precision);
        return target.withMeasures(measures);
    }

    /**
     * Lo que hay que reproducir para escuchar la o las pistas elegidas antes de importarlas, tal
     * como suenan en el archivo -- el manual deja escuchar las pistas MIDI antes de traerlas.
     */
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

    /** El boton "importar titulo y cambios de compas" del paso a paso: no toca ninguna pista. */
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

    private static ParsedMidiFile parse(Path path) {
        try {
            Sequence sequence = MidiSystem.getSequence(path.toFile());
            return MidiFileParser.parse(sequence);
        } catch (InvalidMidiDataException e) {
            throw new ScoreFileException("el archivo " + path + " no es un MIDI valido", e);
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo leer " + path, e);
        }
    }

    private static List<RawMidiTrack> tracksAt(ParsedMidiFile file, List<Integer> midiTrackIndices) {
        List<RawMidiTrack> raws = midiTrackIndices.stream().map(index -> trackAt(file, index)).toList();
        if (raws.isEmpty()) {
            throw new ScoreFileException("no se eligio ninguna pista del archivo MIDI para importar");
        }
        return raws;
    }

    private static RawMidiTrack trackAt(ParsedMidiFile file, int midiTrackIndex) {
        return file.tracks().stream()
                .filter(raw -> raw.index() == midiTrackIndex)
                .findFirst()
                .orElseThrow(() -> new ScoreFileException("el archivo MIDI no tiene notas en la pista " + midiTrackIndex));
    }

    /** Junta las notas de varias pistas MIDI en una sola, tick a tick, como pide el "paso a paso" para fusionar pistas. */
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
            RawMidiTrack raw, MeasureGrid grid, boolean transposeDownOneOctave, Optional<NoteValue> precision,
            boolean useTwoChannelsPerTrack) {
        Tuning tuning = raw.percussion() ? PercussionKit.tuning() : TrackTuningGuess.forQuickImport(raw.name(), raw.program());
        List<Measure> measures =
                measuresOf(raw, grid, tuning, TrackSettings.DEFAULT_FRET_COUNT, transposeDownOneOctave, precision);
        TrackSettings settings = raw.percussion()
                ? TrackSettings.percussion(Track.colorFor(raw.index()))
                : TrackSettings.standard(Track.colorFor(raw.index()));
        return new Track(raw.name(), tuning, channelOf(raw, useTwoChannelsPerTrack), settings, measures);
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
