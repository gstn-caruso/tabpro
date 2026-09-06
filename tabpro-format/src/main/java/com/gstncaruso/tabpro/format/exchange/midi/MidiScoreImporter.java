package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.format.exchange.ChordFretting;
import com.gstncaruso.tabpro.format.exchange.DurationTicks;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

/**
 * Trae una partitura desde un archivo MIDI (formato 0 o 1). Ofrece el "import rapido" del
 * manual, una pista de tabpro por cada pista MIDI con la afinacion deducida de su nombre o
 * instrumento, y el "paso a paso", que trae una pista MIDI elegida sobre una pista existente
 * (con su propia afinacion) y permite transportarla una octava abajo. Las notas que no entran
 * en la afinacion elegida se descartan.
 */
public final class MidiScoreImporter {

    private static final int OCTAVE = 12;

    public List<MidiTrackSummary> tracksIn(Path path) {
        return parse(path).tracks().stream().map(MidiTrackSummary::of).toList();
    }

    /** Una pista de tabpro por cada pista del MIDI que tenga notas. */
    public Score importQuick(Path path) {
        ParsedMidiFile file = parse(path);
        if (file.tracks().isEmpty()) {
            throw new ScoreFileException("el archivo " + path + " no tiene pistas con notas para importar");
        }
        List<Track> tracks = file.tracks().stream().map(raw -> quickTrack(raw, file.grid())).toList();
        String title = file.title().orElseGet(() -> titleFromFileName(path));
        return new Score(title, file.tempoBpm(), tracks);
    }

    /** Los compases de una pista MIDI elegida, listos para reemplazar los de una pista propia. */
    public List<Measure> importMeasures(Path path, int midiTrackIndex, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        ParsedMidiFile file = parse(path);
        RawMidiTrack raw = trackAt(file, midiTrackIndex);
        return measuresOf(raw, file.grid(), tuning, fretCount, transposeDownOneOctave);
    }

    /** El "paso a paso" del manual: la pista MIDI elegida reemplaza los compases de target. */
    public Track importInto(Track target, Path path, int midiTrackIndex, boolean transposeDownOneOctave) {
        List<Measure> measures =
                importMeasures(path, midiTrackIndex, target.tuning(), target.settings().fretCount(), transposeDownOneOctave);
        return target.withMeasures(measures);
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

    private static RawMidiTrack trackAt(ParsedMidiFile file, int midiTrackIndex) {
        return file.tracks().stream()
                .filter(raw -> raw.index() == midiTrackIndex)
                .findFirst()
                .orElseThrow(() -> new ScoreFileException("el archivo MIDI no tiene notas en la pista " + midiTrackIndex));
    }

    private static Track quickTrack(RawMidiTrack raw, MeasureGrid grid) {
        Tuning tuning = raw.percussion() ? PercussionKit.tuning() : TrackTuningGuess.forQuickImport(raw.name(), raw.program());
        List<Measure> measures = measuresOf(raw, grid, tuning, TrackSettings.DEFAULT_FRET_COUNT, false);
        TrackSettings settings = raw.percussion()
                ? TrackSettings.percussion(Track.colorFor(raw.index()))
                : TrackSettings.standard(Track.colorFor(raw.index()));
        return new Track(raw.name(), tuning, channelOf(raw), settings, measures);
    }

    private static Channel channelOf(RawMidiTrack raw) {
        return Channel.playing(raw.program())
                .withVolume(raw.volume())
                .withPan(raw.pan())
                .withReverb(raw.reverb())
                .withTremolo(raw.tremolo())
                .withChorus(raw.chorus())
                .withPhaser(raw.phaser())
                .withPort(raw.port())
                .withNumber(raw.channelNumber());
    }

    private static List<Measure> measuresOf(
            RawMidiTrack raw, MeasureGrid grid, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        List<Measure> measures = new ArrayList<>();
        for (int index = 0; index < grid.measureCount(); index++) {
            measures.add(measureAt(raw, grid, index, tuning, fretCount, transposeDownOneOctave));
        }
        return measures;
    }

    private static Measure measureAt(
            RawMidiTrack raw, MeasureGrid grid, int index, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
        long start = grid.startTick(index);
        long end = grid.endTick(index);
        List<Beat> beats = beatsBetween(raw, start, end, tuning, fretCount, transposeDownOneOctave);
        MeasureAttributes attributes = MeasureAttributes.plain().withKeySignature(grid.keySignatureOf(index));
        return new Measure(grid.timeSignatureOf(index), attributes, List.of(new Voice(beats), Voice.unused()));
    }

    private static List<Beat> beatsBetween(
            RawMidiTrack raw, long start, long end, Tuning tuning, int fretCount, boolean transposeDownOneOctave) {
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
            Duration duration = DurationTicks.nearestTo(soundingUntil - tick);
            List<Note> notes = notesFor(raw, chord, tuning, fretCount, transposeDownOneOctave);
            beats.add(new Beat(duration, notes));
            position = tick + duration.ticks();
        }
        if (position < end) {
            beats.addAll(restsBetween(position, end));
        }
        return beats;
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
