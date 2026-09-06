package com.gstncaruso.tabpro.core.model;

/** Que partes de la pista se dibujan en la partitura. */
public record TrackDisplay(
        boolean standardNotation,
        boolean tablature,
        boolean tuningLegend,
        boolean rhythmOnTablature,
        DiagramPlacement diagrams) {

    private static final TrackDisplay DEFAULT =
            new TrackDisplay(true, true, true, false, DiagramPlacement.ABOVE_THE_STAFF);

    public static TrackDisplay standard() {
        return DEFAULT;
    }

    public TrackDisplay {
        if (!standardNotation && !tablature) {
            throw new IllegalArgumentException("una pista tiene que mostrar pentagrama o tablatura");
        }
    }

    public TrackDisplay withStandardNotation(boolean standardNotation) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams);
    }

    public TrackDisplay withTablature(boolean tablature) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams);
    }

    public TrackDisplay withTuningLegend(boolean tuningLegend) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams);
    }

    public TrackDisplay withRhythmOnTablature(boolean rhythmOnTablature) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams);
    }

    public TrackDisplay withDiagrams(DiagramPlacement diagrams) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams);
    }
}
