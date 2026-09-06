package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Lo que el manual llama insertar cambios de parametro: bajar el volumen al
 * final, cambiar el instrumento a mitad de partitura, acelerar de a poco.
 */
class ParameterChangePlaybackTest {

    private static final long PULSE = Duration.quarter().ticks();
    private static final long MEASURE = 4 * PULSE;

    @Test
    void aScoreWithoutParameterChangesLeavesTheMixAlone() {
        Score score = scoreOf(guitarWith(plainMeasure()));

        Timeline timeline = Timeline.of(score);

        assertTrue(timeline.tracks().get(0).parameters().isEmpty());
        assertTrue(timeline.tempo().isSteady());
    }

    @Test
    void aChangeSoundsAtTheBeatThatCarriesIt() {
        Score score = scoreOf(guitarWith(measureChangingAt(2, volume(40))));

        List<ScheduledParameter> scheduled = Timeline.of(score).tracks().get(0).parameters();

        assertEquals(List.of(new ScheduledParameter(2 * PULSE, SoundParameter.VOLUME, 40)), scheduled);
    }

    @Test
    void changingTheInstrumentIsJustAnotherParameter() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, change(SoundParameter.PROGRAM, 30))));

        List<ScheduledParameter> scheduled = Timeline.of(score).tracks().get(0).parameters();

        assertEquals(List.of(new ScheduledParameter(0, SoundParameter.PROGRAM, 30)), scheduled);
    }

    @Test
    void theTempoIsTheOneParameterThatGoesToTheTempoMap() {
        Score score = scoreOf(guitarWith(measureChangingAt(2, change(SoundParameter.TEMPO, 90))));

        Timeline timeline = Timeline.of(score);

        assertTrue(timeline.tracks().get(0).parameters().isEmpty());
        assertEquals(120, timeline.tempo().bpmAt(0));
        assertEquals(90, timeline.tempo().bpmAt(2 * PULSE));
    }

    @Test
    void aTransitionSpreadsTheChangeOverThoseBeats() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, volume(20).over(2))));

        List<ScheduledParameter> scheduled = Timeline.of(score).tracks().get(0).parameters();

        assertTrue(scheduled.size() > 1, "una transición no se resuelve de un salto");
        assertTrue(scheduled.getFirst().tick() > 0, "el valor viejo todavía manda cuando empieza la transición");
        assertEquals(new ScheduledParameter(2 * PULSE, SoundParameter.VOLUME, 20), scheduled.getLast());
    }

    @Test
    void halfWayThroughATransitionTheValueIsHalfWayToo() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, volume(20).over(2))));

        List<ScheduledParameter> scheduled = Timeline.of(score).tracks().get(0).parameters();

        assertTrue(scheduled.contains(new ScheduledParameter(PULSE, SoundParameter.VOLUME, 60)),
                "de 100 a 20 en dos pulsos, al primer pulso tiene que ir por 60");
    }

    @Test
    void theTempoAlsoTakesItsTimeWhenTheTransitionLasts() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, change(SoundParameter.TEMPO, 60).over(2))));

        TempoMap tempo = Timeline.of(score).tempo();

        assertEquals(90, tempo.bpmAt(PULSE), "de 120 a 60 en dos pulsos, al primer pulso va por 90");
        assertEquals(60, tempo.bpmAt(2 * PULSE));
    }

    @Test
    void theSecondChangeStartsFromWhereTheFirstOneLeftIt() {
        Measure measure = plainMeasure()
                .withBeat(0, beatChanging(volume(40)))
                .withBeat(2, beatChanging(volume(80).over(1)));

        List<ScheduledParameter> scheduled = Timeline.of(scoreOf(guitarWith(measure))).tracks().get(0).parameters();

        assertTrue(scheduled.contains(new ScheduledParameter(2 * PULSE + PULSE / 2, SoundParameter.VOLUME, 60)),
                "de 40 a 80 en un pulso, a mitad de camino tiene que ir por 60");
    }

    @Test
    void aChangeForEveryTrackReachesEveryTrack() {
        Score score = scoreOf(
                guitarWith(measureChangingAt(1, volume(40).onEveryTrack(true))),
                bassWith(plainMeasure()));

        Timeline timeline = Timeline.of(score);

        ScheduledParameter expected = new ScheduledParameter(PULSE, SoundParameter.VOLUME, 40);
        assertEquals(List.of(expected), timeline.tracks().get(0).parameters());
        assertEquals(List.of(expected), timeline.tracks().get(1).parameters());
    }

    @Test
    void aChangeForOneTrackLeavesTheOthersAlone() {
        Score score = scoreOf(guitarWith(measureChangingAt(1, volume(40))), bassWith(plainMeasure()));

        Timeline timeline = Timeline.of(score);

        assertFalse(timeline.tracks().get(0).parameters().isEmpty());
        assertTrue(timeline.tracks().get(1).parameters().isEmpty());
    }

    @Test
    void aTrackThatNobodyHearsGetsNoChanges() {
        Track muted = guitarWith(measureChangingAt(1, volume(40)))
                .withChannel(Channel.playing(Track.GUITAR_PROGRAM).toggledMute());

        Timeline timeline = Timeline.of(scoreOf(muted));

        assertTrue(timeline.tracks().get(0).parameters().isEmpty());
    }

    @Test
    void aChangeInsideARepeatSoundsOnceForEveryPass() {
        Measure repeated = measureChangingAt(0, volume(40))
                .mappingAttributes(attributes -> attributes.withRepeatOpen(true).withRepeatCount(2));

        List<ScheduledParameter> scheduled =
                Timeline.of(scoreOf(guitarWith(repeated))).tracks().get(0).parameters();

        assertEquals(List.of(
                new ScheduledParameter(0, SoundParameter.VOLUME, 40),
                new ScheduledParameter(MEASURE, SoundParameter.VOLUME, 40)), scheduled);
    }

    @Test
    void startingInTheMiddleAppliesTheEarlierChangesUpFront() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, volume(40)), plainMeasure()));

        List<ScheduledParameter> scheduled = fromMeasure(1, score).tracks().get(0).parameters();

        assertEquals(List.of(new ScheduledParameter(0, SoundParameter.VOLUME, 40)), scheduled);
    }

    @Test
    void startingInTheMiddleRecoversTheTempoThatWasSounding() {
        Score score = scoreOf(guitarWith(measureChangingAt(0, change(SoundParameter.TEMPO, 90)), plainMeasure()));

        TempoMap tempo = fromMeasure(1, score).tempo();

        assertEquals(90, tempo.bpmAt(0));
        assertTrue(tempo.isSteady());
    }

    @Test
    void startingInTheMiddleSaysNothingAboutWhatNobodyTouched() {
        Score score = scoreOf(guitarWith(plainMeasure(), plainMeasure()));

        assertTrue(fromMeasure(1, score).tracks().get(0).parameters().isEmpty());
    }

    @Test
    void whatComesAfterTheStartingPointIsNotAppliedUpFront() {
        Score score = scoreOf(guitarWith(plainMeasure(), plainMeasure(), measureChangingAt(0, volume(40))));

        List<ScheduledParameter> scheduled = fromMeasure(1, score).tracks().get(0).parameters();

        assertEquals(List.of(new ScheduledParameter(MEASURE, SoundParameter.VOLUME, 40)), scheduled);
    }

    @Test
    void delayingTheMusicForACountInDelaysTheChangesToo() {
        Score score = scoreOf(guitarWith(measureChangingAt(2, volume(40))));

        Timeline delayed = Timeline.of(score).shiftedBy(MEASURE);

        assertEquals(List.of(new ScheduledParameter(MEASURE + 2 * PULSE, SoundParameter.VOLUME, 40)),
                delayed.tracks().get(0).parameters());
    }

    // ---- armado de partituras de prueba -----------------------------------

    private static Timeline fromMeasure(int measure, Score score) {
        return Timeline.of(score, PlaybackRange.from(measure, score).asPlayOrder(score));
    }

    private static ParameterChange volume(int value) {
        return change(SoundParameter.VOLUME, value);
    }

    private static ParameterChange change(SoundParameter parameter, int value) {
        return ParameterChange.nothing().changing(parameter, value);
    }

    private static Beat beatChanging(ParameterChange change) {
        return Beat.of(Duration.quarter(), new Note(1, 0))
                .withEffects(BeatEffects.none().withParameterChange(change));
    }

    private static Measure plainMeasure() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
    }

    private static Measure measureChangingAt(int beat, ParameterChange change) {
        return plainMeasure().withBeat(beat, beatChanging(change));
    }

    private static Track guitarWith(Measure... measures) {
        return new Track("Guitarra", Tuning.standard(), Channel.playing(Track.GUITAR_PROGRAM), List.of(measures));
    }

    private static Track bassWith(Measure... measures) {
        return new Track("Bajo", Tuning.standard(), Channel.playing(Track.BASS_PROGRAM), List.of(measures));
    }

    private static Score scoreOf(Track... tracks) {
        return new Score("", 120, List.of(tracks));
    }
}
