package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.List;

/**
 * El swing del triplet feel: pares consecutivos de corcheas (o semicorcheas)
 * iguales se reparten dos tercios y un tercio del tiempo que ocupan, sin
 * tocar lo que esta escrito.
 */
final class SwingTiming {

    private SwingTiming() {
    }

    static long[] durationsFor(List<Beat> beats, TripletFeel feel) {
        long[] ticks = beats.stream().mapToLong(beat -> beat.duration().ticks()).toArray();
        if (!feel.swings()) {
            return ticks;
        }
        long unit = unitTicksFor(feel);
        long[] swung = ticks.clone();
        int i = 0;
        while (i + 1 < ticks.length) {
            if (ticks[i] == unit && ticks[i + 1] == unit) {
                long pair = unit * 2;
                swung[i] = pair * 2 / 3;
                swung[i + 1] = pair - swung[i];
                i += 2;
            } else {
                i++;
            }
        }
        return swung;
    }

    private static long unitTicksFor(TripletFeel feel) {
        NoteValue value = feel == TripletFeel.SIXTEENTH ? NoteValue.SIXTEENTH : NoteValue.EIGHTH;
        return new Duration(value, false).ticks();
    }
}
