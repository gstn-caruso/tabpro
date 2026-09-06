package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.Velocity;
import com.gstncaruso.tabpro.core.playback.PitchTrajectory;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro permite usar 4 puertos MIDI a la vez, cada uno con su propio
 * dispositivo. Cada puerto necesita su propia secuencia -son 16 canales
 * MIDI por puerto, no 16 en total- para poder mandarla a un dispositivo
 * distinto durante la reproduccion.
 */
class MidiSequencesByPortTest {

    private static final int TEMPO_META_TYPE = 0x51;

    @Test
    void aTimelineWithoutTracksHasNoSequenceForAnyPort() {
        Timeline timeline = new Timeline(120, 960, List.of());

        Map<Integer, Sequence> byPort = MidiSequences.sequencesByPort(timeline, Set.of());

        assertTrue(byPort.isEmpty());
    }

    @Test
    void tracksGoIntoTheSequenceOfTheirOwnPort() {
        TrackTimeline enElPuertoUno = new TrackTimeline(25, 100, 64, false, 1, List.of(), List.of(), List.of());
        TrackTimeline enElPuertoTres = new TrackTimeline(30, 100, 64, false, 3, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(enElPuertoUno, enElPuertoTres));

        Map<Integer, Sequence> byPort = MidiSequences.sequencesByPort(timeline, Set.of());

        assertEquals(Set.of(1, 3), byPort.keySet());
        assertEquals(25, programOf(byPort.get(1).getTracks()[1]));
        assertEquals(30, programOf(byPort.get(3).getTracks()[1]));
    }

    @Test
    void eachPortNumbersItsChannelsFromZeroRegardlessOfTheOtherPorts() {
        TrackTimeline primeraDelPuerto1 = new TrackTimeline(25, 100, 64, false, 1, List.of(), List.of(), List.of());
        TrackTimeline segundaDelPuerto1 = new TrackTimeline(26, 100, 64, false, 1, List.of(), List.of(), List.of());
        TrackTimeline unicaDelPuerto2 = new TrackTimeline(30, 100, 64, false, 2, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(
                120, 960, List.of(primeraDelPuerto1, segundaDelPuerto1, unicaDelPuerto2));

        Map<Integer, Sequence> byPort = MidiSequences.sequencesByPort(timeline, Set.of());

        Track[] puerto1 = byPort.get(1).getTracks();
        assertEquals(0, channelOf(puerto1[1]));
        assertEquals(1, channelOf(puerto1[2]));
        Track[] puerto2 = byPort.get(2).getTracks();
        assertEquals(0, channelOf(puerto2[1]));
    }

    @Test
    void eachPortSequenceCarriesItsOwnTempoSoAnIndependentSequencerKeepsThePace() {
        TrackTimeline track = new TrackTimeline(25, 100, 64, false, 2, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(150, 960, List.of(track));

        Sequence portTwo = MidiSequences.sequencesByPort(timeline, Set.of()).get(2);

        MetaMessage tempoEvent = (MetaMessage) firstEventOfType(portTwo.getTracks()[0], TEMPO_META_TYPE).getMessage();
        assertEquals(60_000_000 / 150, microsecondsPerQuarterOf(tempoEvent));
    }

    @Test
    void limitingPitchVariationInsideSequencesByPortStillOnlyAffectsItsOwnPort() {
        PitchTrajectory bend = PitchTrajectory.ramp(0, 0.0, 960, 3.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bend, false);
        TrackTimeline limitado = new TrackTimeline(25, 100, 64, false, 1, List.of(note), List.of(), List.of());
        TrackTimeline libre = new TrackTimeline(30, 100, 64, false, 2, List.of(note), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(limitado, libre));

        Map<Integer, Sequence> byPort = MidiSequences.sequencesByPort(timeline, Set.of(1));

        assertTrue(pitchBendEvents(byPort.get(1).getTracks()[1]).isEmpty());
        assertFalse(pitchBendEvents(byPort.get(2).getTracks()[1]).isEmpty());
    }

    private int programOf(Track track) {
        return ((ShortMessage) track.get(0).getMessage()).getData1();
    }

    private int channelOf(Track track) {
        return ((ShortMessage) track.get(0).getMessage()).getChannel();
    }

    private List<ShortMessage> pitchBendEvents(Track track) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(javax.sound.midi.MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.PITCH_BEND)
                .map(message -> (ShortMessage) message)
                .toList();
    }

    private javax.sound.midi.MidiEvent firstEventOfType(Track track, int metaType) {
        for (int i = 0; i < track.size(); i++) {
            javax.sound.midi.MidiEvent event = track.get(i);
            if (event.getMessage() instanceof MetaMessage meta && meta.getType() == metaType) {
                return event;
            }
        }
        throw new AssertionError("no se encontro un evento meta de tipo " + metaType);
    }

    private int microsecondsPerQuarterOf(MetaMessage tempoEvent) {
        byte[] data = tempoEvent.getData();
        return ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
    }
}
