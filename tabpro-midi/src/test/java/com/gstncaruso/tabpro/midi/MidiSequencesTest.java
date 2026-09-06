package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Velocity;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.MetronomeClick;
import com.gstncaruso.tabpro.core.playback.PitchTrajectory;
import com.gstncaruso.tabpro.core.playback.ScheduledBeat;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.ScheduledParameter;
import com.gstncaruso.tabpro.core.playback.TempoMap;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        assertEquals(60_000_000 / 120, microsecondsPerQuarterOf(tempoEvent));
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
    void handsOutTwoConsecutiveChannelsToEachTrack() {
        TrackTimeline first = new TrackTimeline(25, 100, 64,
                List.of(), List.of());
        TrackTimeline second = new TrackTimeline(30, 100, 64,
                List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(first, second));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals(List.of(0, 1), channelsSetUpIn(sequence.getTracks()[1]));
        assertEquals(List.of(2, 3), channelsSetUpIn(sequence.getTracks()[2]));
    }

    @Test
    void setsUpTheInstrumentTheVolumeAndThePanOnBothChannels() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 80, 20, List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        for (int channel : List.of(0, 1)) {
            assertEquals(25, programChangeOn(track, channel));
            assertEquals(80, controllerValueOn(track, channel, 7));
            assertEquals(20, controllerValueOn(track, channel, 10));
        }
    }

    @Test
    void playsANoteWithAnEffectOnTheSecondChannelOfTheTrack() {
        ScheduledNote limpia = new ScheduledNote(0, 960, new Pitch(64));
        ScheduledNote conBend = new ScheduledNote(960, 960, new Pitch(67), new Velocity(100),
                PitchTrajectory.ramp(0, 0, 960, 2), false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, List.of(limpia, conBend), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(List.of(0, 1), notesOnByChannel(track));
    }

    @Test
    void theBendOfANoteTravelsOnTheChannelWhereThatNoteSounds() {
        ScheduledNote conBend = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100),
                PitchTrajectory.ramp(0, 0, 960, 2), false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, List.of(conBend), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        List<ShortMessage> bends = messagesOfCommand(track, ShortMessage.PITCH_BEND);
        assertFalse(bends.isEmpty());
        assertTrue(bends.stream().allMatch(bend -> bend.getChannel() == 1));
    }

    @Test
    void aPercussionTrackPlaysItsEffectsOnTheTenthChannelToo() {
        ScheduledNote conBend = new ScheduledNote(0, 960, new Pitch(38), new Velocity(100),
                PitchTrajectory.ramp(0, 0, 960, 2), false);
        TrackTimeline percussion = new TrackTimeline(0, 100, 64, true, List.of(conBend), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(percussion));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(List.of(9), channelsSetUpIn(track));
        assertEquals(List.of(9), notesOnByChannel(track));
    }

    @Test
    void aChangeOfParameterReachesBothChannelsOfTheTrack() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(), List.of(),
                List.of(new ScheduledParameter(480, SoundParameter.VOLUME, 40)));
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(40, controllerValueOn(track, 0, 7, 480));
        assertEquals(40, controllerValueOn(track, 1, 7, 480));
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

        assertEquals(List.of(6, 7), channelsSetUpIn(sequence.getTracks()[4]));
        assertEquals(List.of(8, 10), channelsSetUpIn(sequence.getTracks()[5]));
        assertEquals(List.of(11, 12), channelsSetUpIn(sequence.getTracks()[6]));
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

    @Test
    void everySoundParameterTravelsOnItsOwnController() {
        List<ScheduledParameter> parameters = List.of(
                new ScheduledParameter(480, SoundParameter.VOLUME, 40),
                new ScheduledParameter(480, SoundParameter.PAN, 20),
                new ScheduledParameter(480, SoundParameter.CHORUS, 30),
                new ScheduledParameter(480, SoundParameter.REVERB, 50),
                new ScheduledParameter(480, SoundParameter.PHASER, 60),
                new ScheduledParameter(480, SoundParameter.TREMOLO, 70));
        Timeline timeline = new Timeline(120, 960,
                List.of(new TrackTimeline(25, 100, 64, false, List.of(), List.of(), parameters)));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(40, controllerValueAt(track, 7, 480));
        assertEquals(20, controllerValueAt(track, 10, 480));
        assertEquals(30, controllerValueAt(track, 93, 480));
        assertEquals(50, controllerValueAt(track, 91, 480));
        assertEquals(60, controllerValueAt(track, 95, 480));
        assertEquals(70, controllerValueAt(track, 92, 480));
    }

    @Test
    void changingTheInstrumentMidWayIsAProgramChangeAtThatTick() {
        Timeline timeline = new Timeline(120, 960, List.of(new TrackTimeline(25, 100, 64, false,
                List.of(), List.of(), List.of(new ScheduledParameter(960, SoundParameter.PROGRAM, 30)))));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        MidiEvent programChange = lastEventOfCommand(track, ShortMessage.PROGRAM_CHANGE);
        assertEquals(960, programChange.getTick());
        assertEquals(30, ((ShortMessage) programChange.getMessage()).getData1());
    }

    @Test
    void everyTempoStretchIsItsOwnMetaEvent() {
        Timeline timeline = new Timeline(TempoMap.steady(120).changingTo(1920, 60), 960, List.of());

        Track conductor = MidiSequences.fromTimeline(timeline).getTracks()[0];

        List<MidiEvent> tempos = metaEventsOfType(conductor, 0x51);
        assertEquals(2, tempos.size());
        assertEquals(1920, tempos.get(1).getTick());
        assertEquals(60_000_000 / 60, microsecondsPerQuarterOf((MetaMessage) tempos.get(1).getMessage()));
    }

    @Test
    void aValueRecoveredAtTickZeroWinsOverTheOneTheTrackStartsWith() {
        Timeline timeline = new Timeline(120, 960, List.of(new TrackTimeline(25, 100, 64, false,
                List.of(), List.of(), List.of(new ScheduledParameter(0, SoundParameter.VOLUME, 40)))));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertEquals(40, lastControllerValue(track, 7),
                "arrancar en el medio recupera el volumen que dejo el cambio anterior");
    }

    private int lastControllerValue(Track track, int controller) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getMessage() instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.CONTROL_CHANGE
                        && sm.getData1() == controller)
                .map(event -> ((ShortMessage) event.getMessage()).getData2())
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("no se encontro el controlador " + controller));
    }

    private int controllerValueAt(Track track, int controller, long tick) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getTick() == tick)
                .filter(event -> event.getMessage() instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.CONTROL_CHANGE
                        && sm.getData1() == controller)
                .map(event -> ((ShortMessage) event.getMessage()).getData2())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no se encontro el controlador " + controller + " en el tick " + tick));
    }

    private MidiEvent lastEventOfCommand(Track track, int command) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getMessage() instanceof ShortMessage sm && sm.getCommand() == command)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("no se encontro un mensaje de comando " + command));
    }

    private List<MidiEvent> metaEventsOfType(Track track, int type) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getMessage() instanceof MetaMessage meta && meta.getType() == type)
                .toList();
    }

    private int microsecondsPerQuarterOf(MetaMessage message) {
        byte[] data = message.getData();
        return ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
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

    @Test
    void laVelocidadDeLaNotaSeUsaComoVelocityMidi() {
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64),
                new Velocity(45),
                PitchTrajectory.flat(), false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        ShortMessage noteOn = (ShortMessage) firstEventOfCommand(track, ShortMessage.NOTE_ON).getMessage();
        assertEquals(45, noteOn.getData2());
    }

    @Test
    void unaPistaDePercusionSiempreUsaElCanal10() {
        TrackTimeline first = new TrackTimeline(25, 100, 64, false, List.of(), List.of());
        TrackTimeline percussion = new TrackTimeline(0, 100, 64, true, List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(first, percussion));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals(9, channelOf(sequence.getTracks()[2])); // canal MIDI 10, indice 9
    }

    @Test
    void unaPistaDePercusionNoLeQuitaUnCanalALasDemas() {
        TrackTimeline percussion = new TrackTimeline(0, 100, 64, true, List.of(), List.of());
        TrackTimeline second = new TrackTimeline(25, 100, 64, false, List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(percussion, second));

        Sequence sequence = MidiSequences.fromTimeline(timeline);

        assertEquals(0, channelOf(sequence.getTracks()[2]));
    }

    @Test
    void unaNotaSinBendNoEmiteEventosDePitchBend() {
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64,
                List.of(new ScheduledNote(0, 960, new Pitch(64))), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertTrue(pitchBendEvents(track).isEmpty());
    }

    @Test
    void unaNotaConBendEmiteEventosDePitchBendQueSiguenLaCurva() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 2.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64),
                new Velocity(100), bend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        List<ShortMessage> bends = pitchBendEvents(track);
        assertFalse(bends.isEmpty());
        int centerValue = pitchBendValue(bends.get(0));
        boolean subioEnAlgunPunto = bends.stream().anyMatch(message -> pitchBendValue(message) > centerValue);
        assertTrue(subioEnAlgunPunto, "el pitch bend tiene que subir en algun punto de la curva");
        // vuelve al centro justo antes de soltar la nota, para no dejar el canal corrido
        assertEquals(centerValue, pitchBendValue(bends.get(bends.size() - 1)));
    }

    @Test
    void unaPistaConNotasConBendConfiguraElRangoDePitchBend() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 2.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64),
                new Velocity(100), bend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertTrue(hasRpnPitchBendRange(track));
    }

    @Test
    void unaNotaConFadeInEmiteUnaRampaDeExpresion() {
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64),
                new Velocity(100),
                PitchTrajectory.flat(), true);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        List<Integer> expressionValues = expressionValues(track);
        assertTrue(expressionValues.size() > 1, "el fade in tiene que mandar varios pasos");
        assertTrue(expressionValues.get(0) < expressionValues.get(expressionValues.size() - 1));
    }

    @Test
    void aBendBeyondOneToneIsSilencedWhenItsPortLimitsPitchVariation() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 3.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline, Set.of(1)).getTracks()[1];

        assertTrue(pitchBendEvents(track).isEmpty());
    }

    @Test
    void aBendWithinOneToneStillSoundsEvenWhenThePortLimitsPitchVariation() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 2.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline, Set.of(1)).getTracks()[1];

        assertFalse(pitchBendEvents(track).isEmpty());
    }

    @Test
    void limitingPitchVariationOnAPortDoesNotAffectAnotherPort() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 3.0);
        ScheduledNote limitedNote = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        ScheduledNote freeNote = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        TrackTimeline limitedTrack = new TrackTimeline(
                25, 100, 64, false, 1, List.of(limitedNote), List.of(), List.of());
        TrackTimeline freeTrack = new TrackTimeline(
                30, 100, 64, false, 2, List.of(freeNote), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(limitedTrack, freeTrack));

        Sequence sequence = MidiSequences.fromTimeline(timeline, Set.of(1));

        assertTrue(pitchBendEvents(sequence.getTracks()[1]).isEmpty());
        assertFalse(pitchBendEvents(sequence.getTracks()[2]).isEmpty());
    }

    @Test
    void withoutLimitedPortsFromTimelineNeverSilencesABend() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 5.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, List.of(note), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(trackTimeline));

        Track track = MidiSequences.fromTimeline(timeline).getTracks()[1];

        assertFalse(pitchBendEvents(track).isEmpty());
    }

    private List<ShortMessage> pitchBendEvents(Track track) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.PITCH_BEND)
                .map(message -> (ShortMessage) message)
                .toList();
    }

    private int pitchBendValue(ShortMessage message) {
        return (message.getData2() << 7) | message.getData1();
    }

    private boolean hasRpnPitchBendRange(Track track) {
        boolean sawRpnMsb = false;
        boolean sawRpnLsb = false;
        boolean sawDataEntry = false;
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof ShortMessage sm
                    && sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                if (sm.getData1() == 101 && sm.getData2() == 0) {
                    sawRpnMsb = true;
                }
                if (sm.getData1() == 100 && sm.getData2() == 0) {
                    sawRpnLsb = true;
                }
                if (sm.getData1() == 6) {
                    sawDataEntry = true;
                }
            }
        }
        return sawRpnMsb && sawRpnLsb && sawDataEntry;
    }

    @Test
    void agregaUnaPistaDeMetronomoConSusClicksEnElCanalDePercusion() {
        Timeline timeline = new Timeline(120, 960, List.of());
        Sequence sequence = MidiSequences.fromTimeline(timeline);
        List<MetronomeClick> clicks = List.of(
                new MetronomeClick(0, true),
                new MetronomeClick(960, false));

        MidiSequences.addMetronomeTrack(sequence, clicks);

        Track metronomeTrack = sequence.getTracks()[sequence.getTracks().length - 1];
        List<ShortMessage> notesOn = java.util.stream.IntStream.range(0, metronomeTrack.size())
                .mapToObj(metronomeTrack::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.NOTE_ON)
                .map(message -> (ShortMessage) message)
                .toList();
        assertEquals(2, notesOn.size());
        assertEquals(9, notesOn.get(0).getChannel());
        assertTrue(notesOn.get(0).getData1() != notesOn.get(1).getData1(), "el acento suena distinto del pulso comun");
    }

    @Test
    void elVolumenDelClickSeUsaComoVelocityMidi() {
        Timeline timeline = new Timeline(120, 960, List.of());
        Sequence sequence = MidiSequences.fromTimeline(timeline);
        List<MetronomeClick> clicks = List.of(new MetronomeClick(0, true, 42));

        MidiSequences.addMetronomeTrack(sequence, clicks);

        Track metronomeTrack = sequence.getTracks()[sequence.getTracks().length - 1];
        ShortMessage noteOn = java.util.stream.IntStream.range(0, metronomeTrack.size())
                .mapToObj(metronomeTrack::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.NOTE_ON)
                .map(message -> (ShortMessage) message)
                .findFirst()
                .orElseThrow();
        assertEquals(42, noteOn.getData2());
    }

    @Test
    void sinClicksNoAgregaNingunaPista() {
        Timeline timeline = new Timeline(120, 960, List.of());
        Sequence sequence = MidiSequences.fromTimeline(timeline);
        int tracksBefore = sequence.getTracks().length;

        MidiSequences.addMetronomeTrack(sequence, List.of());

        assertEquals(tracksBefore, sequence.getTracks().length);
    }

    private List<Integer> expressionValues(Track track) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData1() == 11)
                .map(message -> ((ShortMessage) message).getData2())
                .toList();
    }

    /** Los canales que la pista prepara antes de tocar, en el orden en que los prepara. */
    private List<Integer> channelsSetUpIn(Track track) {
        return messagesOfCommand(track, ShortMessage.PROGRAM_CHANGE).stream()
                .map(ShortMessage::getChannel)
                .distinct()
                .toList();
    }

    /** Los canales por los que entra cada nota, en el orden en que entran. */
    private List<Integer> notesOnByChannel(Track track) {
        return messagesOfCommand(track, ShortMessage.NOTE_ON).stream()
                .map(ShortMessage::getChannel)
                .toList();
    }

    private List<ShortMessage> messagesOfCommand(Track track, int command) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == command)
                .map(message -> (ShortMessage) message)
                .toList();
    }

    private int programChangeOn(Track track, int channel) {
        return messagesOfCommand(track, ShortMessage.PROGRAM_CHANGE).stream()
                .filter(message -> message.getChannel() == channel)
                .map(ShortMessage::getData1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no se encontro el instrumento del canal " + channel));
    }

    private int controllerValueOn(Track track, int channel, int controller) {
        return controllerOn(track, channel, controller).getData2();
    }

    private int controllerValueOn(Track track, int channel, int controller, long tick) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .filter(event -> event.getTick() == tick)
                .map(MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.CONTROL_CHANGE
                        && sm.getChannel() == channel
                        && sm.getData1() == controller)
                .map(message -> ((ShortMessage) message).getData2())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no se encontro el controlador " + controller + " del canal " + channel));
    }

    private ShortMessage controllerOn(Track track, int channel, int controller) {
        return messagesOfCommand(track, ShortMessage.CONTROL_CHANGE).stream()
                .filter(message -> message.getChannel() == channel && message.getData1() == controller)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no se encontro el controlador " + controller + " del canal " + channel));
    }
}
