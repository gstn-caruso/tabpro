package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa los beats de un compas en grupos unidos por barra (beam), como en una partitura.
 */
public final class Beaming {

    private Beaming() {
    }

    public static int beamCount(NoteValue value) {
        return switch (value) {
            case WHOLE, HALF, QUARTER -> 0;
            case EIGHTH -> 1;
            case SIXTEENTH -> 2;
            case THIRTY_SECOND -> 3;
            case SIXTY_FOURTH -> 4;
        };
    }

    public static List<BeamGroup> groupsOf(Measure measure) {
        long ticksPerBeat = Duration.TICKS_PER_QUARTER * 4L / measure.timeSignature().beatUnit();
        List<Beat> beats = measure.beats();

        List<BeamGroup> groups = new ArrayList<>();
        long tick = 0;
        int groupStart = -1;
        long groupBeatIndex = -1;
        int groupEnd = -1;

        for (int i = 0; i < beats.size(); i++) {
            Beat beat = beats.get(i);
            boolean beamable = isBeamable(beat);
            long beatIndex = tick / ticksPerBeat;

            if (beamable && groupStart >= 0 && beatIndex == groupBeatIndex) {
                groupEnd = i;
            } else {
                if (groupStart >= 0) {
                    groups.add(new BeamGroup(groupStart, groupEnd));
                    groupStart = -1;
                }
                if (beamable) {
                    groupStart = i;
                    groupBeatIndex = beatIndex;
                    groupEnd = i;
                }
            }

            tick += beat.duration().ticks();
        }
        if (groupStart >= 0) {
            groups.add(new BeamGroup(groupStart, groupEnd));
        }
        return groups;
    }

    private static boolean isBeamable(Beat beat) {
        return !beat.isRest() && beamCount(beat.duration().value()) > 0;
    }
}
