package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MidiScoreImporterTest {

    private final MidiScoreExporter exporter = new MidiScoreExporter();
    private final MidiScoreImporter importer = new MidiScoreImporter();

    @Test
    void listsTheTracksOfTheFileWithoutTheConductor(@TempDir Path tempDir) {
        Beat note = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(note, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track guitar = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Track bass = new Track("Bajo", Tuning.standardBass(), Channel.playing(33), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(guitar, bass)), tempDir);

        List<MidiTrackSummary> summaries = importer.tracksIn(path);

        assertEquals(2, summaries.size());
        assertEquals("Guitarra", summaries.get(0).name());
        assertEquals("Bajo", summaries.get(1).name());
        assertTrue(summaries.stream().noneMatch(MidiTrackSummary::percussion));
    }

    @Test
    void quickImportRoundTripsATrackWithNaturalFrets() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 0), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(),
                List.of(beat, Beat.rest(Duration.of(NoteValue.QUARTER)), Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score original = new Score("Cancion", 100, List.of(track));
        Path path = export(original, newTempDir());

        Score imported = importer.importQuick(path);

        assertEquals("Cancion", imported.title());
        assertEquals(100, imported.tempo());
        assertEquals(1, imported.trackCount());
        Beat importedBeat = imported.track(0).measure(0).beat(0);
        assertEquals(List.of(new Note(3, 0), new Note(6, 0)), importedBeat.notes());
        assertEquals(Duration.of(NoteValue.QUARTER), importedBeat.duration());
    }

    @Test
    void quickImportGuessesBassTuningFromTheGeneralMidiProgram() {
        Track track = Track.standardBass("Bajo");
        Score original = new Score("Prueba", 120, List.of(track));
        Path path = export(original, newTempDir());

        Score imported = importer.importQuick(path);

        assertEquals(Tuning.standardBass(), imported.track(0).tuning());
    }

    @Test
    void quickImportPutsPercussionOnItsOwnPercussionTrack() {
        Track drums = Track.percussion("Bateria").withMeasure(0,
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Score original = new Score("Prueba", 120, List.of(drums));
        Path path = export(original, newTempDir());

        Score imported = importer.importQuick(path);

        assertTrue(imported.track(0).isPercussion());
        assertEquals(38, imported.track(0).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void quickImportFallsBackToTheFileNameWhenThereIsNoTrackName() throws Exception {
        Path path = newTempDir().resolve("sin-nombre.mid");
        javax.sound.midi.Sequence sequence = exporter.toSequence(Score.blank());
        removeTrackNameEvents(sequence);
        javax.sound.midi.MidiSystem.write(sequence, 1, path.toFile());

        Score imported = importer.importQuick(path);

        assertEquals("sin-nombre", imported.title());
    }

    @Test
    void importsAChosenMidiTrackOntoAnExistingTrackKeepingItsIdentity() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track sourceInScore = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(sourceInScore)), newTempDir());
        Track existing = Track.standardBass("Mi pista");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, midiTrackIndex, false);

        assertEquals("Mi pista", merged.name());
        assertEquals(Tuning.standardBass(), merged.tuning());
        assertEquals(existing.channel(), merged.channel());
        assertEquals(1, merged.measureCount());
    }

    @Test
    void transposesDownAnOctaveWhenAsked() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track source = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(source)), newTempDir());
        Track existing = Track.standardGuitar("Guitarra");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, midiTrackIndex, true);

        assertEquals(0, merged.measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void rejectsAFileThatDoesNotExist() {
        assertThrows(ScoreFileException.class, () -> importer.importQuick(Path.of("no-existe.mid")));
    }

    private Path export(Score score, Path dir) {
        Path path = dir.resolve("prueba.mid");
        exporter.export(score, path);
        return path;
    }

    private static Path newTempDir() {
        try {
            return java.nio.file.Files.createTempDirectory("midi-import-test");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void removeTrackNameEvents(javax.sound.midi.Sequence sequence) {
        for (javax.sound.midi.Track track : sequence.getTracks()) {
            for (int i = track.size() - 1; i >= 0; i--) {
                javax.sound.midi.MidiEvent event = track.get(i);
                if (event.getMessage() instanceof javax.sound.midi.MetaMessage meta && meta.getType() == 0x03) {
                    track.remove(event);
                }
            }
        }
    }
}
