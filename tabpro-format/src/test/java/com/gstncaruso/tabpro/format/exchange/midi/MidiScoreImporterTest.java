package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MidiScoreImporterTest {

    private final MidiScoreImporter importer = new MidiScoreImporter();

    @Test
    void listsTheTracksOfTheFileWithoutTheConductor(@TempDir Path tempDir) {
        Beat note = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(note, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track guitar = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Track bass = new Track("Bass", Tuning.standardBass(), Channel.playing(33), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(guitar, bass)), tempDir);

        List<MidiTrackSummary> summaries = importer.tracksIn(path);

        assertEquals(2, summaries.size());
        assertEquals("Guitar", summaries.get(0).name());
        assertEquals("Bass", summaries.get(1).name());
        assertTrue(summaries.stream().noneMatch(MidiTrackSummary::percussion));
    }

    @Test
    void quickImportRoundTripsATrackWithNaturalFrets(@TempDir Path tempDir) {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 0), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(),
                List.of(beat, Beat.rest(Duration.of(NoteValue.QUARTER)), Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score original = new Score("Song", 100, List.of(track));
        Path path = export(original, tempDir);

        Score imported = importer.importQuick(path);

        assertEquals("Song", imported.title());
        assertEquals(100, imported.tempo());
        assertEquals(1, imported.trackCount());
        Beat importedBeat = imported.track(0).measure(0).beat(0);
        assertEquals(List.of(new Note(3, 0), new Note(6, 0)), importedBeat.notes());
        assertEquals(Duration.of(NoteValue.QUARTER), importedBeat.duration());
    }

    @Test
    void quickImportGuessesBassTuningFromTheGeneralMidiProgram(@TempDir Path tempDir) {
        Track track = Track.standardBass("Bass");
        Score original = new Score("Test", 120, List.of(track));
        Path path = export(original, tempDir);

        Score imported = importer.importQuick(path);

        assertEquals(Tuning.standardBass(), imported.track(0).tuning());
    }

    @Test
    void quickImportPutsPercussionOnItsOwnPercussionTrack(@TempDir Path tempDir) {
        Track drums = Track.percussion("Drums").withMeasure(0,
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Score original = new Score("Test", 120, List.of(drums));
        Path path = export(original, tempDir);

        Score imported = importer.importQuick(path);

        assertTrue(imported.track(0).isPercussion());
        assertEquals(38, imported.track(0).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void quickImportDefaultsToTwoChannelsPerTrack(@TempDir Path tempDir) {
        Track track = Track.standardGuitar("Guitar");
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty());

        assertEquals(1, imported.track(0).channel().number());
        assertEquals(2, imported.track(0).channel().effectChannel());
    }

    @Test
    void quickImportCanUseOnlyOneChannelPerTrack(@TempDir Path tempDir) {
        Track track = Track.standardGuitar("Guitar");
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty(), false);

        assertEquals(1, imported.track(0).channel().number());
        assertEquals(1, imported.track(0).channel().effectChannel());
    }

    @Test
    void quickImportGivesPercussionItsOwnEffectChannelToo(@TempDir Path tempDir) {
        Track drums = Track.percussion("Drums").withMeasure(0,
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Path path = export(new Score("Test", 120, List.of(drums)), tempDir);

        Score imported = importer.importQuick(path);

        assertEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().number());
        assertEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().effectChannel());
    }

    @Test
    void aMelodicTrackOnChannelNineDoesNotGetThePercussionEffectChannel(@TempDir Path tempDir) {
        Track track = Track.standardGuitar("Guitar").withChannel(Channel.playing(25).withNumber(9));
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, indices, false, Optional.empty(), true);

        assertEquals(9, imported.track(0).channel().number());
        assertNotEquals(Channel.PERCUSSION_CHANNEL, imported.track(0).channel().effectChannel());
    }

    @Test
    void quickImportFallsBackToTheFileNameWhenThereIsNoTrackName(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("no-name.mid");
        javax.sound.midi.Sequence sequence = PlainMidiWriter.sequenceOf(Score.blank());
        removeTrackNameEvents(sequence);
        javax.sound.midi.MidiSystem.write(sequence, 1, path.toFile());

        Score imported = importer.importQuick(path);

        assertEquals("no-name", imported.title());
    }

    @Test
    void importsAChosenMidiTrackOntoAnExistingTrackKeepingItsIdentity(@TempDir Path tempDir) {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track sourceInScore = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(sourceInScore)), tempDir);
        Track existing = Track.standardBass("My track");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), false);

        assertEquals("My track", merged.name());
        assertEquals(Tuning.standardBass(), merged.tuning());
        assertEquals(existing.channel(), merged.channel());
        assertEquals(1, merged.measureCount());
    }

    @Test
    void transposesDownAnOctaveWhenAsked(@TempDir Path tempDir) {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track source = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(source)), tempDir);
        Track existing = Track.standardGuitar("Guitar");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), true);

        assertEquals(0, merged.measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void mergesSeveralMidiTracksIntoOneWhenSeveralIndicesAreGiven(@TempDir Path tempDir) {
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
        Track trackA = new Track("Guitar 1", Tuning.standard(), Channel.playing(25), List.of(measureA));
        Track trackB = new Track("Guitar 2", Tuning.standard(), Channel.playing(25), List.of(measureB));
        Path path = export(new Score("Test", 120, List.of(trackA, trackB)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();
        Track existing = Track.standardGuitar("Merged guitar");

        Track merged = importer.importInto(existing, path, indices, false);

        assertEquals(List.of(new Note(1, 0)), merged.measure(0).beat(0).notes());
        assertEquals(List.of(new Note(1, 3)), merged.measure(0).beat(2).notes());
    }

    @Test
    void quickImportKeepsOnlyTheSelectedTracks(@TempDir Path tempDir) {
        Track guitar = Track.standardGuitar("Guitar");
        Track bass = Track.standardBass("Bass");
        Path path = export(new Score("Test", 120, List.of(guitar, bass)), tempDir);
        int bassIndex = importer.tracksIn(path).stream()
                .filter(summary -> summary.name().equals("Bass"))
                .findFirst()
                .orElseThrow()
                .index();

        Score imported = importer.importQuick(path, List.of(bassIndex), false);

        assertEquals(1, imported.trackCount());
        assertEquals(Tuning.standardBass(), imported.track(0).tuning());
    }

    @Test
    void quickImportCanTransposeDownAnOctave(@TempDir Path tempDir) {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
        List<Integer> allIndices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score imported = importer.importQuick(path, allIndices, true);

        assertEquals(0, imported.track(0).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void quickImportWithNoTracksSelectedThrows(@TempDir Path tempDir) {
        Path path = export(new Score("Test", 120, List.of(Track.standardGuitar("Guitar"))), tempDir);

        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> importer.importQuick(path, List.of(), false));

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
    }

    @Test
    void quickImportOfATrackWithoutNotesHasNothingToImport(@TempDir Path tempDir) {
        Path path = export(new Score("Test", 120, List.of(Track.standardGuitar("Guitar"))), tempDir);

        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> importer.importQuick(path, List.of(7), false));

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
    }

    @Test
    void aFileThatIsNotMidiIsNotRecognized(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("not-midi.mid");
        java.nio.file.Files.writeString(path, "this is nowhere close to a MIDI file");

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> importer.importQuick(path));

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("MIDI"), failure.arguments());
    }

    @Test
    void aSmpteTimedFileIsContentThatIsNotSupported() throws Exception {
        Sequence smpte = new Sequence(Sequence.SMPTE_25, 40);

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> MidiFileParser.parse(smpte));

        assertEquals(ScoreFileProblem.UNSUPPORTED_CONTENT, failure.problem());
        assertEquals(List.of(ScoreFeature.SMPTE_TIME_CODE), failure.arguments());
    }

    @Test
    void importsTitleTempoAndTimeSignatureChangesOntoTheCurrentScore(@TempDir Path tempDir) {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track fileTrack = new Track("Guitar", Tuning.standard(), Channel.playing(25),
                List.of(fourFour, fourFour, threeFour));
        Path path = export(new Score("Song", 140, List.of(fileTrack)), tempDir);
        Measure sixEight = Measure.empty(new TimeSignature(6, 8), Duration.quarter());
        Track targetTrack = new Track("My track", Tuning.standardBass(), Channel.playing(33),
                List.of(sixEight, sixEight, sixEight));
        Score target = new Score("Untitled", 120, List.of(targetTrack));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals("Song", result.title());
        assertEquals(140, result.tempo());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(0).timeSignature());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(1).timeSignature());
        assertEquals(new TimeSignature(3, 4), result.track(0).measure(2).timeSignature());
        assertEquals("My track", result.track(0).name());
        assertEquals(Tuning.standardBass(), result.track(0).tuning());
    }

    @Test
    void importingTitleAndTimeSignaturesStopsAtTheLastMeasureOfTheCurrentScore(@TempDir Path tempDir) {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track fileTrack = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(fourFour, threeFour));
        Path path = export(new Score("Song", 100, List.of(fileTrack)), tempDir);
        Measure sixEight = Measure.empty(new TimeSignature(6, 8), Duration.quarter());
        Score target = new Score("T", 120, List.of(
                new Track("Track", Tuning.standard(), Channel.playing(25), List.of(sixEight))));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals(1, result.track(0).measureCount());
        assertEquals(TimeSignature.fourFour(), result.track(0).measure(0).timeSignature());
    }

    @Test
    void importingTitleAndTimeSignaturesFallsBackToTheFileNameWhenThereIsNoTrackName(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("no-name-2.mid");
        javax.sound.midi.Sequence sequence = PlainMidiWriter.sequenceOf(Score.blank());
        removeTrackNameEvents(sequence);
        javax.sound.midi.MidiSystem.write(sequence, 1, path.toFile());
        Score target = new Score("Original", 120, List.of(Track.standardGuitar("Track")));

        Score result = importer.importTitleAndTimeSignatures(target, path);

        assertEquals("no-name-2", result.title());
    }

    @Test
    void aCoarserPrecisionQuantizesTheSameMidiFileToADifferentScore(@TempDir Path tempDir) {
        Beat beat = Beat.of(new Duration(NoteValue.SIXTEENTH, true), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Score withoutQuantizing = importer.importQuick(path, indices, false);
        Score withEighthNotePrecision = importer.importQuick(path, indices, false, Optional.of(NoteValue.EIGHTH));

        assertEquals(new Duration(NoteValue.SIXTEENTH, true), withoutQuantizing.track(0).measure(0).beat(0).duration());
        assertEquals(Duration.of(NoteValue.EIGHTH), withEighthNotePrecision.track(0).measure(0).beat(0).duration());
    }

    @Test
    void theStepByStepImportAlsoRespectsTheChosenPrecision(@TempDir Path tempDir) {
        Beat beat = Beat.of(new Duration(NoteValue.SIXTEENTH, true), new Note(1, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track source = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(source)), tempDir);
        Track existing = Track.standardGuitar("Guitar");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(existing, path, List.of(midiTrackIndex), false, Optional.of(NoteValue.EIGHTH));

        assertEquals(Duration.of(NoteValue.EIGHTH), merged.measure(0).beat(0).duration());
    }

    @Test
    void timelineOfASelectedTrackHasItsNotesReadyToListenBeforeImporting(@TempDir Path tempDir) {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 5));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Solo", Tuning.standard(), Channel.playing(30), List.of(measure));
        Path path = export(new Score("Test", 120, List.of(track)), tempDir);
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
    void timelineOfMergesTheSelectedTracksToListenToThemTogether(@TempDir Path tempDir) {
        Measure measureA = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0))));
        Measure measureB = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 3))));
        Track trackA = new Track("Guitar 1", Tuning.standard(), Channel.playing(25), List.of(measureA));
        Track trackB = new Track("Guitar 2", Tuning.standard(), Channel.playing(25), List.of(measureB));
        Path path = export(new Score("Test", 120, List.of(trackA, trackB)), tempDir);
        List<Integer> indices = importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();

        Timeline timeline = importer.timelineOf(path, indices);

        assertEquals(1, timeline.tracks().size());
        assertEquals(2, timeline.tracks().get(0).notes().size());
    }

    @Test
    void timelineOfRejectsWhenNoTrackIsSelected(@TempDir Path tempDir) {
        Path path = export(new Score("Test", 120, List.of(Track.standardGuitar("Guitar"))), tempDir);

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> importer.timelineOf(path, List.of()));

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
    }

    @Test
    void rejectsAFileThatDoesNotExist() {
        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> importer.importQuick(Path.of("does-not-exist.mid")));

        assertEquals(ScoreFileProblem.CANNOT_READ, failure.problem());
        assertEquals(List.of(Path.of("does-not-exist.mid")), failure.arguments());
    }

    @Test
    void importingTitleAndTimeSignaturesRejectsAFileThatDoesNotExist() {
        Score target = Score.blank();
        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> importer.importTitleAndTimeSignatures(target, Path.of("does-not-exist.mid")));

        assertEquals(ScoreFileProblem.CANNOT_READ, failure.problem());
    }

    @Test
    void chordPositionQuantizeWithAQuarterNoteGridSnapsANoteCloseToTheDownbeat(@TempDir Path tempDir) throws Exception {
        Path path = rawMidiFile(tempDir, "near-the-beat.mid", new long[] {240, 64, 100});

        Score imported = importer.importQuick(
                path, indicesOf(path), false, Optional.of(NoteValue.QUARTER), Optional.empty(), true);

        Track track = imported.track(0);
        assertEquals(1, track.measure(0).beat(0).notes().size());
        assertEquals(64, track.pitchOf(track.measure(0).beat(0).notes().get(0)).midiNumber());
    }

    @Test
    void chordPositionQuantizeWithASixteenthNoteGridLeavesAnAlreadyAlignedNoteWhereItIs(@TempDir Path tempDir) throws Exception {
        Path path = rawMidiFile(tempDir, "already-aligned.mid", new long[] {240, 64, 100});

        Score imported = importer.importQuick(
                path, indicesOf(path), false, Optional.of(NoteValue.SIXTEENTH), Optional.empty(), true);

        Track track = imported.track(0);
        assertTrue(track.measure(0).beat(0).notes().isEmpty(), "there is still a rest before: the note did not move");
        assertEquals(Duration.of(NoteValue.SIXTEENTH), track.measure(0).beat(0).duration());
        assertEquals(1, track.measure(0).beat(1).notes().size());
    }

    @Test
    void chordPositionQuantizeMergesTwoNearbyAttacksIntoAChord(@TempDir Path tempDir) throws Exception {
        Path path = rawMidiFile(tempDir, "almost-simultaneous.mid", new long[] {900, 60, 100}, new long[] {1020, 64, 100});

        Score imported = importer.importQuick(
                path, indicesOf(path), false, Optional.of(NoteValue.QUARTER), Optional.empty(), true);

        Track track = imported.track(0);
        long beatsWithNotes = track.measure(0).beats().stream().filter(beat -> !beat.notes().isEmpty()).count();
        assertEquals(1, beatsWithNotes, "the two near-simultaneous notes must land in a single beat");
        Beat chord = track.measure(0).beats().stream().filter(beat -> !beat.notes().isEmpty()).findFirst().orElseThrow();
        assertEquals(2, chord.notes().size());
    }

    @Test
    void chordPositionQuantizeCanPushANoteIntoTheNextMeasureAtTheEdgeOfAMeasure(@TempDir Path tempDir) throws Exception {
        Path path = rawMidiFile(tempDir, "measure-edge.mid", new long[] {3800, 64, 60});

        Score imported = importer.importQuick(
                path, indicesOf(path), false, Optional.of(NoteValue.QUARTER), Optional.empty(), true);

        Track track = imported.track(0);
        assertEquals(2, track.measureCount(), "the file must carry a second measure for the note to move there");
        boolean firstMeasureHasNotes = track.measure(0).beats().stream().anyMatch(beat -> !beat.notes().isEmpty());
        assertFalse(firstMeasureHasNotes, "the quantized note moved, so the first measure is left silent");
        assertEquals(1, track.measure(1).beat(0).notes().size());
    }

    @Test
    void theStepByStepImportAlsoQuantizesTheChordPosition(@TempDir Path tempDir) throws Exception {
        Path path = rawMidiFile(tempDir, "step-by-step.mid", new long[] {240, 64, 100});
        Track existing = Track.standardGuitar("Guitar");
        int midiTrackIndex = importer.tracksIn(path).get(0).index();

        Track merged = importer.importInto(
                existing, path, List.of(midiTrackIndex), false, Optional.of(NoteValue.QUARTER), Optional.empty());

        assertEquals(1, merged.measure(0).beat(0).notes().size(),
                "the note at 240 ticks quantizes to time 1 with a quarter-note grid: it must land in the first beat");
    }

    private static Path rawMidiFile(Path dir, String fileName, long[]... notes) throws Exception {
        Sequence sequence = new Sequence(Sequence.PPQ, (int) Duration.TICKS_PER_QUARTER);
        sequence.createTrack();
        javax.sound.midi.Track track = sequence.createTrack();
        byte[] trackName = "Guitar".getBytes(StandardCharsets.UTF_8);
        track.add(new MidiEvent(new MetaMessage(0x03, trackName, trackName.length), 0));
        track.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, 25, 0), 0));
        long lastTick = 0;
        for (long[] note : notes) {
            long tick = note[0];
            int pitch = (int) note[1];
            long duration = note[2];
            track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, 100), tick));
            track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, 0), tick + duration));
            lastTick = Math.max(lastTick, tick + duration);
        }
        track.add(new MidiEvent(new MetaMessage(0x2F, new byte[0], 0), lastTick));
        Path path = dir.resolve(fileName);
        PlainMidiWriter.write(sequence, path);
        return path;
    }

    private List<Integer> indicesOf(Path path) {
        return importer.tracksIn(path).stream().map(MidiTrackSummary::index).toList();
    }

    private static Path export(Score score, Path dir) {
        Path path = dir.resolve("test.mid");
        PlainMidiWriter.write(score, path);
        return path;
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
