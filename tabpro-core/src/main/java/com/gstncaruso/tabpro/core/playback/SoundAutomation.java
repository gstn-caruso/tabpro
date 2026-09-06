package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Los cambios de parametro que el manual deja insertar en medio de la
 * partitura, ya resueltos en el tiempo: que valor le toca a cada pista en cada
 * tick y como queda el mapa de tempo.
 *
 * <p>Se recorren en el orden en que suenan los compases, no en el que estan
 * escritos, asi que una repeticion los vuelve a disparar. Y arrancar en el
 * medio no pierde nada: lo que dejaron los cambios anteriores se aplica de
 * entrada, como pide el consejo del manual.
 */
record SoundAutomation(TempoMap tempo, List<List<ScheduledParameter>> byTrack) {

    /** Cada cuantos ticks se manda un valor nuevo mientras dura una transicion. */
    private static final long TRANSITION_STEP_TICKS = 60;

    SoundAutomation {
        byTrack = byTrack.stream().map(List::copyOf).toList();
    }

    static SoundAutomation of(Score score, PlayOrder order) {
        Mix mix = new Mix(score);
        mix.recover(askedIn(score, whatSoundedBefore(score, order)));
        mix.schedule(askedIn(score, order));
        return mix.automation();
    }

    List<ScheduledParameter> onTrack(int index) {
        return byTrack.get(index);
    }

    /**
     * Lo que ya sono antes de que empiece este orden: los compases que la
     * partitura entera toca hasta llegar al primero de los que se van a tocar.
     */
    private static PlayOrder whatSoundedBefore(Score score, PlayOrder order) {
        return order.isEmpty() ? PlayOrder.nothing() : PlayOrder.of(score).before(order.measureAt(0));
    }

    /** Los cambios que pide la partitura al recorrer ese orden, cada uno en el tick en que suena. */
    private static List<AskedChange> askedIn(Score score, PlayOrder order) {
        List<AskedChange> asked = new ArrayList<>();
        for (int index = 0; index < score.trackCount(); index++) {
            Track track = score.track(index);
            for (TimedBeat timed : TrackClock.of(track, order)) {
                ParameterChange change = timed.beat().effects().parameterChange();
                if (!change.isEmpty()) {
                    asked.add(new AskedChange(timed.tick(), index, change, pulseOf(track, timed.measureIndex())));
                }
            }
        }
        asked.sort(Comparator.comparingLong(AskedChange::tick).thenComparingInt(AskedChange::track));
        return asked;
    }

    /** Cuanto dura un beat de transicion: el pulso del compas que lleva el cambio. */
    private static long pulseOf(Track track, int measureIndex) {
        TimeSignature timeSignature = track.measure(measureIndex).timeSignature();
        return timeSignature.ticksPerMeasure() / timeSignature.beats();
    }

    private static Map<SoundParameter, Integer> valuesOf(Channel channel) {
        Map<SoundParameter, Integer> values = new EnumMap<>(SoundParameter.class);
        values.put(SoundParameter.PROGRAM, channel.program());
        values.put(SoundParameter.VOLUME, channel.volume());
        values.put(SoundParameter.PAN, channel.pan());
        values.put(SoundParameter.CHORUS, channel.chorus());
        values.put(SoundParameter.REVERB, channel.reverb());
        values.put(SoundParameter.PHASER, channel.phaser());
        values.put(SoundParameter.TREMOLO, channel.tremolo());
        return values;
    }

    /** Un cambio escrito en la partitura, ya ubicado en el tiempo. */
    private record AskedChange(long tick, int track, ParameterChange change, long pulseTicks) {

        long transitionTicks() {
            return change.transitionBeats() * pulseTicks;
        }

        boolean reaches(int otherTrack) {
            return change.everyTrack() || otherTrack == track;
        }
    }

    /** Un valor intermedio de una transicion. */
    private record Step(long tick, int value) {
    }

    /**
     * La mesa de mezcla mientras corre la partitura: se acuerda de en cuanto
     * quedo cada parametro, para que la transicion que venga arranque de ahi.
     */
    private static final class Mix {

        private final Score score;
        private final List<Map<SoundParameter, Integer>> values = new ArrayList<>();
        private final List<List<ScheduledParameter>> scheduled = new ArrayList<>();
        private TempoMap tempo;

        Mix(Score score) {
            this.score = score;
            this.tempo = TempoMap.steady(score.tempo());
            for (int index = 0; index < score.trackCount(); index++) {
                values.add(valuesOf(score.track(index).channel()));
                scheduled.add(new ArrayList<>());
            }
        }

        /** Los cambios que ya pasaron: solo dejan su valor final, y se anuncia al arrancar. */
        void recover(List<AskedChange> asked) {
            for (AskedChange change : asked) {
                apply(change, false);
            }
            announceWhatChangedBeforeStarting();
        }

        void schedule(List<AskedChange> asked) {
            for (AskedChange change : asked) {
                apply(change, true);
            }
        }

        SoundAutomation automation() {
            for (List<ScheduledParameter> ofOneTrack : scheduled) {
                ofOneTrack.sort(Comparator.comparingLong(ScheduledParameter::tick));
            }
            return new SoundAutomation(tempo, scheduled);
        }

        private void apply(AskedChange asked, boolean scheduling) {
            for (SoundParameter parameter : SoundParameter.values()) {
                OptionalInt target = asked.change().valueOf(parameter);
                if (target.isEmpty()) {
                    continue;
                }
                if (parameter.isGlobal()) {
                    applyTempo(asked, target.getAsInt(), scheduling);
                } else {
                    applyToTracks(asked, parameter, target.getAsInt(), scheduling);
                }
            }
        }

        /** El tempo vale para toda la partitura, no importa a cuantas pistas apunte el cambio. */
        private void applyTempo(AskedChange asked, int target, boolean scheduling) {
            if (!scheduling) {
                tempo = TempoMap.steady(target);
                return;
            }
            for (Step step : stepsOf(asked, tempo.bpmAt(asked.tick()), target)) {
                tempo = tempo.changingTo(step.tick(), step.value());
            }
        }

        private void applyToTracks(AskedChange asked, SoundParameter parameter, int target, boolean scheduling) {
            for (int track = 0; track < score.trackCount(); track++) {
                if (asked.reaches(track)) {
                    applyToTrack(track, asked, parameter, target, scheduling);
                }
            }
        }

        private void applyToTrack(
                int track, AskedChange asked, SoundParameter parameter, int target, boolean scheduling) {
            int from = values.get(track).put(parameter, target);
            if (!scheduling || !score.isAudible(track)) {
                return;
            }
            for (Step step : stepsOf(asked, from, target)) {
                scheduled.get(track).add(new ScheduledParameter(step.tick(), parameter, step.value()));
            }
        }

        /** Lo que quedo distinto de como arranca la pista se manda apenas empieza a sonar. */
        private void announceWhatChangedBeforeStarting() {
            for (int track = 0; track < score.trackCount(); track++) {
                if (!score.isAudible(track)) {
                    continue;
                }
                Map<SoundParameter, Integer> written = valuesOf(score.track(track).channel());
                for (Map.Entry<SoundParameter, Integer> reached : values.get(track).entrySet()) {
                    if (!reached.getValue().equals(written.get(reached.getKey()))) {
                        scheduled.get(track).add(
                                new ScheduledParameter(0, reached.getKey(), reached.getValue()));
                    }
                }
            }
        }

        /**
         * Un cambio instantaneo es un solo valor; uno con transicion se reparte en
         * valores intermedios hasta llegar al ultimo, que cae justo al terminar.
         */
        private static List<Step> stepsOf(AskedChange asked, int from, int target) {
            long transition = asked.transitionTicks();
            if (transition <= 0 || from == target) {
                return List.of(new Step(asked.tick(), target));
            }
            int steps = (int) Math.max(1, transition / TRANSITION_STEP_TICKS);
            List<Step> ramp = new ArrayList<>(steps);
            for (int step = 1; step <= steps; step++) {
                long tick = asked.tick() + transition * step / steps;
                int value = from + (int) Math.round((double) (target - from) * step / steps);
                ramp.add(new Step(tick, value));
            }
            return ramp;
        }
    }
}
