package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
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
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MidiScoreExporterTest {

    private final MidiScoreExporter exporter = new MidiScoreExporter();

    @Test
    void writesTheTempoAtTheStart() throws Exception {
        Score score = new Score("Test", 140, List.of(Track.standardGuitar("Guitar")));

        Sequence sequence = exporter.toSequence(score);

        MetaMessage tempoEvent = onlyMetaOfType(sequence.getTracks()[0], 0x51);
        assertEquals(140, microsecondsPerQuarterToBpm(tempoEvent.getData()));
    }

    @Test
    void aMidiFileThatCannotBeWrittenIsReportedWithItsPath(@TempDir Path tempDir) {
        Score score = new Score("Test", 120, List.of(Track.standardGuitar("Guitar")));
        Path path = tempDir.resolve("missing-folder").resolve("test.mid");

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> exporter.export(score, path));

        assertEquals(ScoreFileProblem.CANNOT_WRITE, failure.problem());
        assertEquals(List.of(path), failure.arguments());
    }

    @Test
    void writesOneMidiTrackPerAudibleScoreTrack() {
        Track muted = Track.standardGuitar("Muted").withChannel(Channel.playing(25).toggledMute());
        Score score = new Score("Test", 120, List.of(Track.standardGuitar("Guitar"), muted));

        Sequence sequence = exporter.toSequence(score);

        assertEquals(2, sequence.getTracks().length);
    }

    @Test
    void preparesTheProgramVolumeAndPanOnBothChannelsOfTheTrack() {
        Channel channel = Channel.playing(30).withVolume(90).withPan(20);
        Track track = Track.standardGuitar("Guitar").withChannel(channel);
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        for (int midiChannel : List.of(0, 1)) {
            assertEquals(30, programChangeOn(midiTrack, midiChannel));
            assertEquals(90, controlChangeOn(midiTrack, midiChannel, 7));
            assertEquals(20, controlChangeOn(midiTrack, midiChannel, 10));
        }
    }

    @Test
    void theMixingConsoleEffectsReachTheSynthOnBothChannelsOfTheTrack() {
        Channel channel = Channel.playing(30).withChorus(10).withReverb(40).withPhaser(70).withTremolo(100);
        Track track = Track.standardGuitar("Guitar").withChannel(channel);
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        for (int midiChannel : List.of(0, 1)) {
            assertEquals(10, controlChangeOn(midiTrack, midiChannel, 93), "chorus");
            assertEquals(40, controlChangeOn(midiTrack, midiChannel, 91), "reverb");
            assertEquals(70, controlChangeOn(midiTrack, midiChannel, 95), "phaser");
            assertEquals(100, controlChangeOn(midiTrack, midiChannel, 92), "tremolo");
        }
    }

    @Test
    void twoTracksWithDifferentReverbSoundDifferentInTheGeneratedMidi() {
        Track wetTrack = Track.standardGuitar("With reverb").withChannel(Channel.playing(25).withReverb(100));
        Track dryTrack = Track.standardBass("Without reverb").withChannel(Channel.playing(33).withReverb(0));
        Score score = new Score("Test", 120, List.of(wetTrack, dryTrack));

        Sequence sequence = exporter.toSequence(score);

        assertEquals(100, controlChangeOf(sequence.getTracks()[1], 91));
        assertEquals(0, controlChangeOf(sequence.getTracks()[2], 91));
    }

    @Test
    void sendsTheBentNoteToTheEffectsChannelSoItDoesNotDragTheRest() {
        Note bent = new Note(6, 0).withBend(Bend.of(BendType.BEND, 4));
        Score score = scoreOfOneMeasure(Beat.of(Duration.of(NoteValue.WHOLE), bent, new Note(1, 0)));

        javax.sound.midi.Track midiTrack = exporter.toSequence(score).getTracks()[1];

        assertEquals(1, noteOnOf(midiTrack, 40).getChannel(), "the bent note goes to the effects channel");
        assertEquals(0, noteOnOf(midiTrack, 64).getChannel(), "the clean note stays on the track's channel");
    }

    @Test
    void aTrackPlaysOnTheChannelTheMixingConsoleConfiguredInsteadOfAnAutomaticOne() {
        Channel onChannelFive = Channel.playing(25).withNumber(5).withEffectChannel(6);
        Track track = Track.standardGuitar("Guitar").withChannel(onChannelFive)
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0)))));
        Score score = new Score("Test", 120, List.of(track));

        javax.sound.midi.Track midiTrack = exporter.toSequence(score).getTracks()[1];

        assertEquals(4, onlyShortMessageOf(midiTrack, ShortMessage.NOTE_ON).getChannel());
    }

    @Test
    void aBentNoteGoesToTheConfiguredEffectsChannelNotAnAutomaticOne() {
        Channel onChannelFive = Channel.playing(25).withNumber(5).withEffectChannel(6);
        Note bent = new Note(6, 0).withBend(Bend.of(BendType.BEND, 4));
        Measure measure = new Measure(TimeSignature.fourFour(),
                List.of(Beat.of(Duration.of(NoteValue.WHOLE), bent, new Note(1, 0))));
        Track track = Track.standardGuitar("Guitar").withChannel(onChannelFive).withMeasure(0, measure);
        Score score = new Score("Test", 120, List.of(track));

        javax.sound.midi.Track midiTrack = exporter.toSequence(score).getTracks()[1];

        assertEquals(5, noteOnOf(midiTrack, 40).getChannel(), "the bend travels through the configured effects channel (Ch2)");
        assertEquals(4, noteOnOf(midiTrack, 64).getChannel(), "the clean note stays on the configured channel (Ch)");
    }

    @Test
    void aPercussionTrackAlwaysUsesChannelTenEvenIfItsChannelIsConfiguredOtherwise() {
        Channel misconfigured = Channel.percussion().withNumber(3).withEffectChannel(4);
        Track drums = Track.percussion("Drums").withChannel(misconfigured)
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Score score = new Score("Test", 120, List.of(drums));

        javax.sound.midi.Track midiTrack = exporter.toSequence(score).getTracks()[1];

        assertEquals(9, onlyShortMessageOf(midiTrack, ShortMessage.NOTE_ON).getChannel());
    }

    @Test
    void twoTracksConfiguredOnTheSameChannelBothSoundOnIt() {
        Channel sharedChannel = Channel.playing(25).withNumber(5).withEffectChannel(6);
        Measure measure = new Measure(TimeSignature.fourFour(),
                List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0))));
        Track first = Track.standardGuitar("One").withChannel(sharedChannel).withMeasure(0, measure);
        Track second = Track.standardBass("Two").withChannel(sharedChannel.withProgram(33)).withMeasure(0, measure);
        Score score = new Score("Test", 120, List.of(first, second));

        Sequence sequence = exporter.toSequence(score);

        assertEquals(4, onlyShortMessageOf(sequence.getTracks()[1], ShortMessage.NOTE_ON).getChannel());
        assertEquals(4, onlyShortMessageOf(sequence.getTracks()[2], ShortMessage.NOTE_ON).getChannel());
    }

    @Test
    void aFreshScoreWithThreeTracksSoundsAsThreeDistinctTracksWithoutTouchingTheMixer() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));
        editor.addTrack(Track.standardGuitar("Guitar 2"));

        Sequence sequence = exporter.toSequence(editor.score());

        ShortMessage first = firstProgramChangeOf(sequence.getTracks()[1]);
        ShortMessage second = firstProgramChangeOf(sequence.getTracks()[2]);
        ShortMessage third = firstProgramChangeOf(sequence.getTracks()[3]);
        assertEquals(3, Set.of(first.getChannel(), second.getChannel(), third.getChannel()).size(),
                "the three tracks have to sound on different channels");
        assertEquals(25, first.getData1(), "the first guitar");
        assertEquals(33, second.getData1(), "the bass");
        assertEquals(25, third.getData1(), "the second guitar");
    }

    private ShortMessage firstProgramChangeOf(javax.sound.midi.Track track) {
        return (ShortMessage) track.get(0).getMessage();
    }

    @Test
    void namesTheScoreAndEachTrack() {
        Score score = new Score("Song", 120,
                List.of(Track.standardGuitar("Guitar"), Track.standardBass("Bass")));

        Sequence sequence = exporter.toSequence(score);

        assertEquals("Song", textOf(onlyMetaOfType(sequence.getTracks()[0], 0x03)));
        assertEquals("Guitar", textOf(onlyMetaOfType(sequence.getTracks()[1], 0x03)));
        assertEquals("Bass", textOf(onlyMetaOfType(sequence.getTracks()[2], 0x03)));
    }

    @Test
    void announcesTheTimeSignatureAgainOnEachPassOfARepeat() {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter())
                .withAttributes(MeasureAttributes.plain().withRepeatCount(2));
        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(fourFour, threeFour));
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        assertEquals(4, metaEventsOfType(sequence.getTracks()[0], 0x58).size());
    }

    @Test
    void writesNotesWithTheirRealDuration() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        ShortMessage noteOn = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_ON);
        ShortMessage noteOff = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_OFF);
        assertEquals(40, noteOn.getData1());
        assertEquals(0L, tickOf(midiTrack, noteOn));
        assertEquals(Duration.of(NoteValue.QUARTER).ticks(), tickOf(midiTrack, noteOff));
    }

    @Test
    void mergesATiedNoteIntoTheSustainOfThePrevious() {
        Beat attack = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Beat tied = Beat.of(Duration.of(NoteValue.QUARTER), Note.tiedOn(6));
        Measure measure = new Measure(TimeSignature.fourFour(),
                List.of(attack, tied, Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        assertEquals(1, countShortMessagesOf(midiTrack, ShortMessage.NOTE_ON));
        ShortMessage noteOff = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_OFF);
        assertEquals(2 * Duration.of(NoteValue.QUARTER).ticks(), tickOf(midiTrack, noteOff));
    }

    @Test
    void putsPercussionOnChannelTen() {
        Track drums = Track.percussion("Drums")
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Score score = new Score("Test", 120, List.of(drums));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        ShortMessage noteOn = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_ON);
        assertEquals(9, noteOn.getChannel());
        assertEquals(38, noteOn.getData1());
    }

    @Test
    void writesEveryTempoChangeAtItsMeasure() {
        Beat changingTempo = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0))
                .withEffects(BeatEffects.none().withParameterChange(
                        ParameterChange.nothing().changing(SoundParameter.TEMPO, 90)));
        Measure first = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER))));
        Measure second = new Measure(TimeSignature.fourFour(), List.of(
                changingTempo,
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.QUARTER))));
        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(first, second));
        Score score = new Score("Test", 140, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        List<MidiEvent> tempos = tempoEventsOf(sequence.getTracks()[0]);
        assertEquals(2, tempos.size(), "the score speeds up halfway through: there have to be two tempo events");
        assertEquals(0L, tempos.get(0).getTick());
        assertEquals(140, microsecondsPerQuarterToBpm(((MetaMessage) tempos.get(0).getMessage()).getData()));
        assertEquals(TimeSignature.fourFour().ticksPerMeasure(), tempos.get(1).getTick());
        assertEquals(90, microsecondsPerQuarterToBpm(((MetaMessage) tempos.get(1).getMessage()).getData()));
    }

    @Test
    void writesATimeSignatureChangeAtItsMeasure() {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(fourFour, threeFour));
        Score score = new Score("Test", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        List<MetaMessage> changes = metaEventsOfType(sequence.getTracks()[0], 0x58);
        assertEquals(2, changes.size());
        assertEquals(4, changes.get(0).getData()[0]);
        assertEquals(3, changes.get(1).getData()[0]);
    }

    @Test
    void writesThePitchBendOfABentNote() {
        Note bent = new Note(6, 0).withBend(Bend.of(BendType.BEND, 4));
        Score score = scoreOfOneMeasure(Beat.of(Duration.of(NoteValue.WHOLE), bent));

        Sequence sequence = exporter.toSequence(score);

        assertTrue(countShortMessagesOf(sequence.getTracks()[1], ShortMessage.PITCH_BEND) > 0,
                "the .mid has to carry the bend that is heard");
    }

    @Test
    void repeatsTheMeasuresThatTheScoreRepeats() {
        Score score = scoreOfOneMeasure(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 0)));
        Score repeated = score.withTrack(0, score.track(0).withMeasure(0, score.track(0).measure(0)
                .withAttributes(MeasureAttributes.plain().withRepeatOpen(true).withRepeatCount(2))));

        Sequence sequence = exporter.toSequence(repeated);

        assertEquals(2, countShortMessagesOf(sequence.getTracks()[1], ShortMessage.NOTE_ON),
                "the measure repeats, so its note sounds twice");
    }

    @Test
    void forcingBeamBreaksAndStemOverridesNeverChangesTheGeneratedMidi() throws Exception {
        Duration eighth = new Duration(NoteValue.EIGHTH, false);
        Beat plainBeat = Beat.of(eighth, new Note(6, 0));
        Measure plainMeasure = new Measure(TimeSignature.fourFour(), List.of(
                plainBeat, plainBeat, plainBeat, plainBeat, plainBeat, plainBeat, plainBeat, plainBeat));
        Score plain = scoreOfOneMeasure(plainMeasure);

        Beat forcedBreak = plainBeat.withEffects(BeatEffects.none().withBeamBreak(BeamBreak.FORCED));
        Beat preventedBreak = plainBeat.withEffects(BeatEffects.none().withBeamBreak(BeamBreak.PREVENTED));
        Beat stemUp = plainBeat.withEffects(BeatEffects.none().withStemOverride(StemOverride.UP));
        Beat stemDown = plainBeat.withEffects(BeatEffects.none().withStemOverride(StemOverride.DOWN));
        Measure overriddenMeasure = new Measure(TimeSignature.fourFour(), List.of(
                plainBeat, forcedBreak, preventedBreak, stemUp, stemDown, plainBeat, plainBeat, plainBeat));
        Score overridden = scoreOfOneMeasure(overriddenMeasure);

        assertEquals(bytesOf(exporter.toSequence(plain)), bytesOf(exporter.toSequence(overridden)),
                "the same measure, with or without notation overrides, has to sound exactly the same");
    }

    private static java.util.List<Byte> bytesOf(Sequence sequence) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MidiSystem.write(sequence, 1, out);
        java.util.List<Byte> bytes = new ArrayList<>();
        for (byte value : out.toByteArray()) {
            bytes.add(value);
        }
        return bytes;
    }

    private static Score scoreOfOneMeasure(Measure measure) {
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Test", 120, List.of(track));
    }

    private static Score scoreOfOneMeasure(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Test", 120, List.of(track));
    }

    @Test
    void writesAndReadsBackAFile(@TempDir Path tempDir) throws Exception {
        Score score = Score.blank();
        Path path = tempDir.resolve("test.mid");

        exporter.export(score, path);

        assertTrue(Files.exists(path));
        Sequence roundTripped = MidiSystem.getSequence(path.toFile());
        assertEquals(score.tempo(), microsecondsPerQuarterToBpm(onlyMetaOfType(roundTripped.getTracks()[0], 0x51).getData()));
    }

    private static long tickOf(javax.sound.midi.Track track, ShortMessage message) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            if (event.getMessage() == message) {
                return event.getTick();
            }
        }
        throw new AssertionError("message not found in the track");
    }

    private static ShortMessage onlyShortMessageOf(javax.sound.midi.Track track, int command) {
        List<ShortMessage> found = shortMessagesOf(track, command);
        assertEquals(1, found.size(), "expected a single message " + command);
        return found.get(0);
    }

    private static int countShortMessagesOf(javax.sound.midi.Track track, int command) {
        return shortMessagesOf(track, command).size();
    }

    private static List<ShortMessage> shortMessagesOf(javax.sound.midi.Track track, int command) {
        List<ShortMessage> found = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof ShortMessage message && message.getCommand() == command) {
                found.add(message);
            }
        }
        return found;
    }

    private static ShortMessage noteOnOf(javax.sound.midi.Track track, int pitch) {
        return shortMessagesOf(track, ShortMessage.NOTE_ON).stream()
                .filter(message -> message.getData1() == pitch)
                .findFirst()
                .orElseThrow(() -> new AssertionError("note not found " + pitch));
    }

    private static int programChangeOn(javax.sound.midi.Track track, int channel) {
        return shortMessagesOf(track, ShortMessage.PROGRAM_CHANGE).stream()
                .filter(message -> message.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no instrument found for channel " + channel))
                .getData1();
    }

    private static int controlChangeOn(javax.sound.midi.Track track, int channel, int controller) {
        return shortMessagesOf(track, ShortMessage.CONTROL_CHANGE).stream()
                .filter(message -> message.getChannel() == channel && message.getData1() == controller)
                .findFirst()
                .orElseThrow(() -> new AssertionError("controller not found " + controller))
                .getData2();
    }

    private static int controlChangeOf(javax.sound.midi.Track track, int controller) {
        return shortMessagesOf(track, ShortMessage.CONTROL_CHANGE).stream()
                .filter(message -> message.getData1() == controller)
                .findFirst()
                .orElseThrow(() -> new AssertionError("controller not found " + controller))
                .getData2();
    }

    private static String textOf(MetaMessage message) {
        return new String(message.getData(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private static MetaMessage onlyMetaOfType(javax.sound.midi.Track track, int type) {
        List<MetaMessage> found = metaEventsOfType(track, type);
        assertEquals(1, found.size(), "expected a single meta event of type " + type);
        return found.get(0);
    }

    private static List<MidiEvent> tempoEventsOf(javax.sound.midi.Track track) {
        List<MidiEvent> found = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            if (event.getMessage() instanceof MetaMessage message && message.getType() == 0x51) {
                found.add(event);
            }
        }
        return found;
    }

    private static List<MetaMessage> metaEventsOfType(javax.sound.midi.Track track, int type) {
        List<MetaMessage> found = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof MetaMessage message && message.getType() == type) {
                found.add(message);
            }
        }
        return found;
    }

    private static int microsecondsPerQuarterToBpm(byte[] data) {
        int microsecondsPerQuarter = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
        return 60_000_000 / microsecondsPerQuarter;
    }
}
