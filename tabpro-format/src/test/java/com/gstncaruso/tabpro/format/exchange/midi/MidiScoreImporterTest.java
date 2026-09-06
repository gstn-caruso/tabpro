package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MidiScoreImporterTest {

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

    /**
     * El manual: "Use 2 channels per track" es el default de la importacion rapida, asi que
     * dos pistas quedan en canales de efectos distintos de los suyos propios.
     */
    @Test
    void quickImportDefaultsToTwoChannelsPerTrack() {
        Track track = Track.standardGuitar("Guitarra");
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty());

        assertEquals(1, imported.track(0).channel().number());
        assertEquals(2, imported.track(0).channel().effectChannel());
    }

    /** La casilla destildada equivale a un solo canal por pista. */
    @Test
    void quickImportCanUseOnlyOneChannelPerTrack() {
        Track track = Track.standardGuitar("Guitarra");
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty(), false);

        assertEquals(1, imported.track(0).channel().number());
        assertEquals(1, imported.track(0).channel().effectChannel());
    }

    /**
     * La percusion tambien tiene que tener el canal de efectos que le toca, no el de
     * arranque de una pista comun: antes se quedaba pegado en el canal 2 aunque la
     * percusion suene siempre en el 10.
     */
    @Test
    void quickImportGivesPercussionItsOwnEffectChannelToo() {
        Track drums = Track.percussion("Bateria").withMeasure(0,
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Path path = export(new Score("Prueba", 120, List.of(drums)), newTempDir());

        Score imported = importer.importQuick(path);

        assertEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().number());
        assertEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().effectChannel());
    }

    /**
     * Channel.effectChannelNextTo(9) devuelve 10, el mismo de la percusion, si no lo evita: una
     * pista melodica en el canal 9 no puede terminar con sus bends sonando como bateria. La
     * regla ya salta la percusion en effectChannelNextTo (ver PR #83); este test lo confirma
     * desde el lado del import, para que las dos puntas (import y reproduccion/exportacion)
     * sigan compartiendo la misma funcion en vez de calcular el par cada una por su lado.
     */
    @Test
    void aMelodicTrackOnChannelNineDoesNotGetThePercussionEffectChannel() {
        Track track = Track.standardGuitar("Guitarra").withChannel(Channel.playing(25).withNumber(9));
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty(), true);

        assertEquals(9, imported.track(0).channel().number());
        assertNotEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().effectChannel());
    }

    @Test
    void quickImportFallsBackToTheFileNameWhenThereIsNoTrackName() throws Exception {
        Path path = newTempDir().resolve("sin-nombre.mid");
        javax.sound.midi.Sequence sequence = PlainMidiWriter.sequenceOf(Score.blank());
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

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), false);

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

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), true);

        assertEquals(0, merged.measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void mergesSeveralMidiTracksIntoOneWhenSeveralIndicesAreGiven() {
        Measure measureA = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER))));
        Measure measureB = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 3)),
                Beat.rest(Duration.of(NoteValue.QUARTER))));
        Track trackA = new Track("Guitarra 1", Tuning.standard(), Channel.playing(25), List.of(measureA));
        Track trackB = new Track("Guitarra 2", Tuning.standard(), Channel.playing(25), List.of(measureB));
        Path path = export(new Score("Prueba", 120, List.of(trackA, trackB)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();
        Track existing = Track.standardGuitar("Guitarra fusionada");

        Track merged = importer.importInto(existing, path, indices, false);

        assertEquals(List.of(new Note(1, 0)), merged.measure(0).beat(0).notes());
        assertEquals(List.of(new Note(1, 3)), merged.measure(0).beat(2).notes());
    }

    @Test
    void quickImportKeepsOnlyTheSelectedTracks() {
        Track guitar = Track.standardGuitar("Guitarra");
        Track bass = Track.standardBass("Bajo");
        Path path = export(new Score("Prueba", 120, List.of(guitar, bass)), newTempDir());
        int bassIndex = importer.tracksIn(path).stream()
                .filter(summary -> summary.name().equals("Bajo"))
                .findFirst()
                .orElseThrow()
                .index();

        Score imported = importer.importQuick(path, List.of(bassIndex), false);

        assertEquals(1, imported.trackCount());
        assertEquals(Tuning.standardBass(), imported.track(0).tuning());
    }

    @Test
    void quickImportCanTransposeDownAnOctave() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        List<Integer> allIndices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, allIndices, true);

        assertEquals(0, imported.track(0).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void quickImportWithNoTracksSelectedThrows() {
        Path path = export(new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"))), newTempDir());

        assertThrows(ScoreFileException.class, () -> importer.importQuick(path, List.of(), false));
    }

    @Test
    void importsTitleTempoAndTimeSignatureChangesOntoTheCurrentScore() {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track fileTrack = new Track("Guitarra", Tuning.standard(), Channel.playing(25),
                List.of(fourFour, fourFour, threeFour));
        Path path = export(new Score("Cancion", 140, List.of(fileTrack)), newTempDir());
        Measure sixEight = Measure.empty(new TimeSignature(6, 8), Duration.quarter());
        Track targetTrack = new Track("Mi pista", Tuning.standardBass(), Channel.playing(33),
                List.of(sixEight, sixEight, sixEight));
        Score target = new Score("Sin título", 120, List.of(targetTrack));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals("Cancion", result.title());
        assertEquals(140, result.tempo());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(0).timeSignature());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(1).timeSignature());
        assertEquals(new TimeSignature(3, 4), result.track(0).measure(2).timeSignature());
        assertEquals("Mi pista", result.track(0).name());
        assertEquals(Tuning.standardBass(), result.track(0).tuning());
    }

    @Test
    void importingTitleAndTimeSignaturesStopsAtTheLastMeasureOfTheCurrentScore() {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track fileTrack = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(fourFour, threeFour));
        Path path = export(new Score("Cancion", 100, List.of(fileTrack)), newTempDir());
        Measure sixEight = Measure.empty(new TimeSignature(6, 8), Duration.quarter());
        Score target = new Score("T", 120, List.of(
                new Track("Pista", Tuning.standard(), Channel.playing(25), List.of(sixEight))));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals(1, result.track(0).measureCount());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(0).timeSignature());
    }

    @Test
    void importingTitleAndTimeSignaturesFallsBackToTheFileNameWhenThereIsNoTrackName() throws Exception {
        Path path = newTempDir().resolve("sin-nombre-2.mid");
        javax.sound.midi.Sequence sequence = PlainMidiWriter.sequenceOf(Score.blank());
        removeTrackNameEvents(sequence);
        javax.sound.midi.MidiSystem.write(sequence, 1, path.toFile());
        Score target = new Score("Original", 120, List.of(Track.standardGuitar("Pista")));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals("sin-nombre-2", result.title());
    }

    @Test
    void aCoarserPrecisionQuantizesTheSameMidiFileToADifferentScore() {
        // una corchea con puntillo (360 tics: 1.5 semicorcheas) es una figura exacta sin
        // restringir la grilla; pidiendo que no sea mas fina que la corchea, esa figura no entra
        // y la nota se redondea a la corchea simple -- la misma entrada MIDI da otra partitura.
        Beat beat = Beat.of(new Duration(NoteValue.SIXTEENTH, true), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score sinRestringir = importer.importQuick(path, indices, false);
        Score conPrecisionDeCorchea = importer.importQuick(path, indices, false, Optional.of(NoteValue.EIGHTH));

        assertEquals(new Duration(NoteValue.SIXTEENTH, true), sinRestringir.track(0).measure(0).beat(0).duration());
        assertEquals(Duration.of(NoteValue.EIGHTH), conPrecisionDeCorchea.track(0).measure(0).beat(0).duration());
    }

    @Test
    void theStepByStepImportAlsoRespectsTheChosenPrecision() {
        Beat beat = Beat.of(new Duration(NoteValue.SIXTEENTH, true), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track source = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(source)), newTempDir());
        Track existing = Track.standardGuitar("Guitarra");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), false, Optional.of(NoteValue.EIGHTH));

        assertEquals(Duration.of(NoteValue.EIGHTH), merged.measure(0).beat(0).duration());
    }

    @Test
    void timelineOfASelectedTrackHasItsNotesReadyToListenBeforeImporting() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 5));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Prueba", 120, List.of(track)), newTempDir());
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Timeline timeline = importer.timelineOf(path, List.of(midiTrackIndex));

        assertEquals(120, timeline.tempoBpm());
        assertEquals(1, timeline.tracks().size());
        List<ScheduledNote> notes = timeline.tracks().get(0).notes();
        assertEquals(1, notes.size());
        assertEquals(Tuning.standard().pitchOf(new Note(1, 5)), notes.get(0).pitch());
        assertEquals(0L, notes.get(0).startTick());
        assertEquals(Duration.of(NoteValue.QUARTER).ticks(), notes.get(0).durationTicks());
        assertEquals(30, timeline.tracks().get(0).program());
    }

    @Test
    void timelineOfMergesTheSelectedTracksToListenToThemTogether() {
        Measure measureA = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0))));
        Measure measureB = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 3))));
        Track trackA = new Track("Guitarra 1", Tuning.standard(), Channel.playing(25), List.of(measureA));
        Track trackB = new Track("Guitarra 2", Tuning.standard(), Channel.playing(25), List.of(measureB));
        Path path = export(new Score("Prueba", 120, List.of(trackA, trackB)), newTempDir());
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Timeline timeline = importer.timelineOf(path, indices);

        assertEquals(1, timeline.tracks().size());
        assertEquals(2, timeline.tracks().get(0).notes().size());
    }

    @Test
    void timelineOfRejectsWhenNoTrackIsSelected() {
        Path path = export(new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"))), newTempDir());

        assertThrows(ScoreFileException.class, () -> importer.timelineOf(path, List.of()));
    }

    @Test
    void rejectsAFileThatDoesNotExist() {
        assertThrows(ScoreFileException.class, () -> importer.importQuick(Path.of("no-existe.mid")));
    }

    @Test
    void importingTitleAndTimeSignaturesRejectsAFileThatDoesNotExist() {
        Score target = Score.blank();
        assertThrows(ScoreFileException.class, () -> importer.importTitleAndTimeSignatures(target, Path.of("no-existe.mid")));
    }

    private static Path export(Score score, Path dir) {
        Path path = dir.resolve("prueba.mid");
        PlainMidiWriter.write(score, path);
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
