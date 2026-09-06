package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
        Score score = new Score("Prueba", 140, List.of(Track.standardGuitar("Guitarra")));

        Sequence sequence = exporter.toSequence(score);

        MetaMessage tempoEvent = onlyMetaOfType(sequence.getTracks()[0], 0x51);
        assertEquals(140, microsecondsPerQuarterToBpm(tempoEvent.getData()));
    }

    @Test
    void writesOneMidiTrackPerAudibleScoreTrack() {
        Track muted = Track.standardGuitar("Silenciada").withChannel(Channel.playing(25).toggledMute());
        Score score = new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"), muted));

        Sequence sequence = exporter.toSequence(score);

        // conductor + una sola pista audible
        assertEquals(2, sequence.getTracks().length);
    }

    @Test
    void writesTheProgramVolumeAndPan() {
        Channel channel = Channel.playing(30).withVolume(90).withPan(20);
        Track track = Track.standardGuitar("Guitarra").withChannel(channel);
        Score score = new Score("Prueba", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        assertEquals(30, onlyShortMessageOf(midiTrack, ShortMessage.PROGRAM_CHANGE).getData1());
        ShortMessage volume = onlyControlChange(midiTrack, 7);
        ShortMessage pan = onlyControlChange(midiTrack, 10);
        assertEquals(90, volume.getData2());
        assertEquals(20, pan.getData2());
    }

    @Test
    void writesNotesWithTheirRealDuration() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(Duration.of(NoteValue.QUARTER)),
                Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

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
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        assertEquals(1, countShortMessagesOf(midiTrack, ShortMessage.NOTE_ON));
        ShortMessage noteOff = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_OFF);
        assertEquals(2 * Duration.of(NoteValue.QUARTER).ticks(), tickOf(midiTrack, noteOff));
    }

    @Test
    void putsPercussionOnChannelTen() {
        Track drums = Track.percussion("Bateria")
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 38)))));
        Score score = new Score("Prueba", 120, List.of(drums));

        Sequence sequence = exporter.toSequence(score);

        javax.sound.midi.Track midiTrack = sequence.getTracks()[1];
        ShortMessage noteOn = onlyShortMessageOf(midiTrack, ShortMessage.NOTE_ON);
        assertEquals(9, noteOn.getChannel());
        assertEquals(38, noteOn.getData1());
    }

    @Test
    void writesATimeSignatureChangeAtItsMeasure() {
        Measure fourFour = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure threeFour = Measure.empty(new TimeSignature(3, 4), Duration.quarter());
        Track track = Track.standardGuitar("Guitarra").withMeasures(List.of(fourFour, threeFour));
        Score score = new Score("Prueba", 120, List.of(track));

        Sequence sequence = exporter.toSequence(score);

        List<MetaMessage> changes = metaEventsOfType(sequence.getTracks()[0], 0x58);
        assertEquals(2, changes.size());
        assertEquals(4, changes.get(0).getData()[0]);
        assertEquals(3, changes.get(1).getData()[0]);
    }

    @Test
    void writesAndReadsBackAFile(@TempDir Path tempDir) throws Exception {
        Score score = Score.blank();
        Path path = tempDir.resolve("prueba.mid");

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
        throw new AssertionError("mensaje no encontrado en la pista");
    }

    private static ShortMessage onlyShortMessageOf(javax.sound.midi.Track track, int command) {
        List<ShortMessage> found = shortMessagesOf(track, command);
        assertEquals(1, found.size(), "se esperaba un solo mensaje " + command);
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

    private static ShortMessage onlyControlChange(javax.sound.midi.Track track, int controller) {
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof ShortMessage message
                    && message.getCommand() == ShortMessage.CONTROL_CHANGE
                    && message.getData1() == controller) {
                return message;
            }
        }
        throw new AssertionError("no se encontro el control change " + controller);
    }

    private static MetaMessage onlyMetaOfType(javax.sound.midi.Track track, int type) {
        List<MetaMessage> found = metaEventsOfType(track, type);
        assertEquals(1, found.size(), "se esperaba un solo meta evento de tipo " + type);
        return found.get(0);
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
