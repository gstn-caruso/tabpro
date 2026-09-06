package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.util.ArrayList;
import java.util.List;

/**
 * En que compases rige cada marcador de la partitura: desde el compas que lo lleva hasta el
 * proximo marcador (o hasta el final). Separado de como se dibuja la zona de marcadores.
 */
public final class MarkerSegments {

    private MarkerSegments() {
    }

    public static List<Segment> of(Score score) {
        List<Segment> segments = new ArrayList<>();
        int pendingFrom = -1;
        Marker pendingMarker = null;
        for (int measure = 0; measure < score.measureCount(); measure++) {
            var marker = score.attributesOf(measure).marker();
            if (marker.isPresent()) {
                if (pendingMarker != null) {
                    segments.add(new Segment(pendingFrom, measure, pendingMarker));
                }
                pendingFrom = measure;
                pendingMarker = marker.get();
            }
        }
        if (pendingMarker != null) {
            segments.add(new Segment(pendingFrom, score.measureCount(), pendingMarker));
        }
        return segments;
    }

    public record Segment(int fromMeasure, int toMeasureExclusive, Marker marker) {
    }
}
