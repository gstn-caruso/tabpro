package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.List;

/**
 * El reloj de una pista: en que tick cae cada uno de sus beats al recorrer un
 * orden de compases, ya con el swing del triplet feel aplicado. Lo comparten
 * quien hace sonar las notas y quien aplica los cambios de parametro, para que
 * los dos hablen del mismo momento.
 */
final class TrackClock {

    private TrackClock() {
    }

    /** Los beats de las dos voces, compas a compas en el orden en que se tocan. */
    static List<TimedBeat> of(Track track, PlayOrder order) {
        List<TimedBeat> timed = new ArrayList<>();
        long tick = 0;
        for (int step = 0; step < order.size(); step++) {
            int measureIndex = order.measureAt(step);
            if (measureIndex >= track.measureCount()) {
                continue;
            }
            Measure measure = track.measure(measureIndex);
            TripletFeel feel = measure.attributes().tripletFeel();
            for (VoicePart part : VoicePart.values()) {
                addVoice(timed, measure.voice(part), part, tick, feel, measureIndex);
            }
            tick += measure.durationTicks();
        }
        return timed;
    }

    private static void addVoice(
            List<TimedBeat> timed, Voice voice, VoicePart part,
            long measureStartTick, TripletFeel feel, int measureIndex) {
        List<Beat> beats = voice.beats();
        long[] durations = SwingTiming.durationsFor(beats, feel);
        long tick = measureStartTick;
        for (int index = 0; index < beats.size(); index++) {
            timed.add(new TimedBeat(tick, durations[index], measureIndex, index, part, beats.get(index)));
            tick += durations[index];
        }
    }
}
