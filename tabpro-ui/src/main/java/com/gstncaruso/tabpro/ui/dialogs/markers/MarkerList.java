package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.util.ArrayList;
import java.util.List;

public final class MarkerList {

    private MarkerList() {
    }

    public static List<Positioned> collect(Score score) {
        List<Positioned> found = new ArrayList<>();
        Track reference = score.track(0);
        for (int measure = 0; measure < reference.measureCount(); measure++) {
            int measureIndex = measure;
            score.attributesOf(measure).marker().ifPresent(marker -> found.add(new Positioned(measureIndex, marker)));
        }
        return List.copyOf(found);
    }

    public record Positioned(int measureIndex, Marker marker) {

        public String label() {
            return "Compás " + (measureIndex + 1) + ": " + marker.name();
        }
    }
}
