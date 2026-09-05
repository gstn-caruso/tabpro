package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.ScheduledBeat;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import org.junit.jupiter.api.Test;

class MidiSequencesTest {

    @Test
    void usesTheTimelineResolution() {
        Timeline timeline = new Timeline(120, 960, List.of());

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals(Sequence.PPQ, sequence.getDivisionType());
        assertEquals(960, sequence.getResolution());
    }

    @Test
    void writesTheTempoAsAMetaEventAtTickZero() {
        Timeline timeline = new Timeline(120, 960, List.of());

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track tempoTrack = sequence.getTracks()[0];
        MetaMessage tempoEvent = (MetaMessage) firstEventOfType(tempoTrack, 0x51).getMessage();
        assertEquals(0, firstEventOfType(tempoTrack, 0x51).getTick());
        byte[] data = tempoEvent.getData();
        int microsecondsPerQuarter = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
        assertEquals(60_000_000 / 120, microsecondsPerQuarter);
    }

    @Test
    void sendsAProgramChangeBeforeTheFirstNote() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64))), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track track = sequence.getTracks()[1];
        ShortMessage programChange = (ShortMessage) track.get(0).getMessage();
        assertEquals(ShortMessage.PROGRAM_CHANGE, programChange.getCommand());
        assertEquals(25, programChange.getData1());
        assertEquals(0, track.get(0).getTick());
    }

    @Test
    void emitsNoteOnAndNoteOffForASingleNote() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64))), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track track = sequence.getTracks()[1];
        MidiEvent noteOnEvent = firstEventOfCommand(track, ShortMessage.NOTE_ON);
        MidiEvent noteOffEvent = firstEventOfCommand(track, ShortMessage.NOTE_OFF);
        ShortMessage noteOn = (ShortMessage) noteOnEvent.getMessage();
        ShortMessage noteOff = (ShortMessage) noteOffEvent.getMessage();
        assertEquals(64, noteOn.getData1());
        assertEquals(100, noteOn.getData2());
        assertEquals(0, noteOnEvent.getTick());
        assertEquals(64, noteOff.getData1());
        assertEquals(960, noteOffEvent.getTick());
    }

    @Test
    void emitsAllChordNotesAtTheSameTick() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64)), new ScheduledNote(0, 960, new Pitch(67))),
                List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track track = sequence.getTracks()[1];
        List<Integer> notesOnAtZero = notesOn(track, 0);
        assertEquals(List.of(64, 67), notesOnAtZero);
    }

    @Test
    void emitsAMarkerPerBeat() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(),
                List.of(new ScheduledBeat(0, 0, 0), new ScheduledBeat(960, 0, 1)));
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track track = sequence.getTracks()[1];
        List<MetaMessage> markers = markersOf(track);
        assertEquals(2, markers.size());
        assertEquals("0/0/0", new String(markers.get(0).getData()));
        assertEquals("0/0/1", new String(markers.get(1).getData()));
    }

    @Test
    void noteOffPrecedesTheNextNoteOnOfTheSamePitchAtTheSameTick() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64)), new ScheduledNote(960, 960, new Pitch(64))),
                List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        Track track = sequence.getTracks()[1];
        int offIndex = -1;
        int onIndex = -1;
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            if (event.getTick() != 960 || !(event.getMessage() instanceof ShortMessage message)) {
                continue;
            }
            if (message.getCommand() == ShortMessage.NOTE_OFF && offIndex == -1) {
                offIndex = i;
            }
            if (message.getCommand() == ShortMessage.NOTE_ON && onIndex == -1) {
                onIndex = i;
            }
        }
        assertTrue(offIndex < onIndex);
    }

    @Test
    void usesOneChannelPerTrack() {
        TrackTimeline first = new TrackTimeline(25, 100, 64,
                List.of(), List.of());
        TrackTimeline second = new TrackTimeline(30, 100, 64,
                List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(first, second));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        ShortMessage firstProgramChange = (ShortMessage) sequence.getTracks()[1].get(0).getMessage();
        ShortMessage secondProgramChange = (ShortMessage) sequence.getTracks()[2].get(0).getMessage();
        assertEquals(0, firstProgramChange.getChannel());
        assertEquals(1, secondProgramChange.getChannel());
    }

    @Test
    void sendsTheVolumeAndThePanOfTheTrackBeforeItsFirstNote() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 80, 20, List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(80, controllerValue(track, 7));
        assertEquals(20, controllerValue(track, 10));
    }

    @Test
    void aSilentTrackIsSentWithVolumeZero() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 0, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64))), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(0, controllerValue(track, 7));
    }

    @Test
    void skipsThePercussionChannelWhenHandingOutChannels() {
        List<TrackTimeline> tracks = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> new TrackTimeline(25, 100, 64, List.of(), List.of()))
                .toList();
        Timeline timeline = new Timeline(120, 960, tracks);

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals(8, channelOf(sequence.getTracks()[9]));
        assertEquals(10, channelOf(sequence.getTracks()[10]));
        assertEquals(11, channelOf(sequence.getTracks()[11]));
    }

    @Test
    void aMarkerNamesItsTrackRatherThanItsChannel() {
        List<TrackTimeline> tracks = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> new TrackTimeline(25, 100, 64, List.of(), List.of(new ScheduledBeat(0, 0, 0))))
                .toList();
        Timeline timeline = new Timeline(120, 960, tracks);

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals("10/0/0", new String(markersOf(sequence.getTracks()[11]).get(0).getData()));
    }

    @Test
    void parsesABeatPositionFromAMarker() throws Exception {
        MetaMessage marker = new MetaMessage(0x06, "2/1/3".getBytes(), 5);

        Optional<BeatPosition> position = MidiSequences.beatPositionOf(marker);

        assertEquals(Optional.of(new BeatPosition(2, 1, 3)), position);
    }

    private MidiEvent firstEventOfCommand(Track track, int command) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getMessage() instanceof ShortMessage sm && sm.getCommand() == command)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no se encontro un mensaje de comando " + command));
    }

    private int controllerValue(Track track, int controller) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.CONTROL_CHANGE
                        && sm.getData1() == controller)
                .map(message -> ((ShortMessage) message).getData2())
                .findFirst()
                .orElseThrow(() -> new AssertionError("no se encontro el controlador " + controller));
    }

    private int channelOf(Track track) {
        return ((ShortMessage) track.get(0).getMessage()).getChannel();
    }

    private MidiEvent firstEventOfType(Track track, int type) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof MetaMessage meta && meta.getType() == type) {
                return event;
            }
        }
        throw new AssertionError("no se encontró un MetaMessage de tipo " + type);
    }

    private List<MetaMessage> markersOf(Track track) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(i -> track.get(i).getMessage())
                .filter(message -> message instanceof MetaMessage meta && meta.getType() == 0x06)
                .map(message -> (MetaMessage) message)
                .toList();
    }

    private List<Integer> notesOn(Track track, long tick) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getTick() == tick)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.NOTE_ON)
                .map(message -> ((ShortMessage) message).getData1())
                .toList();
    }
}
