package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.ArrayList;
import java.util.List;

/**
 * Reacomoda los beats para que cada compas sume lo que su medida pide: los que
 * sobran pasan al compas siguiente y los que faltan vienen del que sigue.
 */
public final class BarArranger {

    private BarArranger() {
    }

    public static Score run(Score score) {
        Score arranged = score;
        for (int index = 0; index < score.trackCount(); index++) {
            arranged = arranged.mappingTrack(index, BarArranger::arrange);
        }
        return arranged;
    }

    public static Score runOnTrack(Score score, int trackIndex) {
        return score.mappingTrack(trackIndex, BarArranger::arrange);
    }

    private static Track arrange(Track track) {
        List<Measure> arranged = new ArrayList<>();
        List<Beat> pending = new ArrayList<>();
        for (Measure measure : track.measures()) {
            pending.addAll(measure.lead().beats());
            arranged.add(measure.withVoice(VoicePart.LEAD, take(pending, measure.timeSignature().ticksPerMeasure())));
        }
        while (!pending.isEmpty()) {
            Measure last = arranged.getLast();
            arranged.add(new Measure(last.timeSignature(), last.attributes(),
                    List.of(take(pending, last.timeSignature().ticksPerMeasure()), Voice.unused())));
        }
        return track.withMeasures(arranged);
    }

    /** Saca de la cola los beats que entran en un compas de esa medida. */
    private static Voice take(List<Beat> pending, long capacity) {
        List<Beat> taken = new ArrayList<>();
        long room = capacity;
        while (!pending.isEmpty() && pending.getFirst().duration().ticks() <= room) {
            Beat beat = pending.removeFirst();
            room -= beat.duration().ticks();
            taken.add(beat);
        }
        if (taken.isEmpty()) {
            taken.add(pending.isEmpty() ? Beat.rest(Duration.quarter()) : pending.removeFirst());
        }
        return new Voice(taken);
    }
}
