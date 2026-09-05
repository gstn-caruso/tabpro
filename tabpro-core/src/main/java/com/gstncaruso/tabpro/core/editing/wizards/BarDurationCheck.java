package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.ArrayList;
import java.util.List;

/** Revisa la partitura buscando compases que no suman lo que su medida pide. */
public final class BarDurationCheck {

    private BarDurationCheck() {
    }

    public static List<Finding> run(Score score) {
        List<Finding> findings = new ArrayList<>();
        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            Track track = score.track(trackIndex);
            for (int measureIndex = 0; measureIndex < track.measureCount(); measureIndex++) {
                Measure measure = track.measure(measureIndex);
                if (!measure.isComplete()) {
                    findings.add(new Finding(trackIndex, measureIndex, measure.isTooShort()));
                }
            }
        }
        return List.copyOf(findings);
    }

    /** Un compas que no cierra, y si le falta o le sobra. */
    public record Finding(int trackIndex, int measureIndex, boolean tooShort) {

        public boolean tooLong() {
            return !tooShort;
        }
    }
}
