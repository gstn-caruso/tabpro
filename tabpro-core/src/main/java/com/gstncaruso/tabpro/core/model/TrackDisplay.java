package com.gstncaruso.tabpro.core.model;

public record TrackDisplay(
        boolean standardNotation,
        boolean tablature,
        boolean tuningLegend,
        boolean rhythmOnTablature,
        DiagramPlacement diagrams,
        boolean diagramsBelowStandardNotation,
        boolean forceHorizontalBeams) {

    private static final TrackDisplay DEFAULT =
            new TrackDisplay(true, true, false, false, DiagramPlacement.ABOVE_THE_STAFF, false, false);

    public static TrackDisplay standard() {
        return DEFAULT;
    }

    public TrackDisplay {
        if (!standardNotation && !tablature) {
            throw new IllegalArgumentException("a track has to show standard notation or tablature");
        }
    }

    public TrackDisplay withStandardNotation(boolean standardNotation) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withTablature(boolean tablature) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withTuningLegend(boolean tuningLegend) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withRhythmOnTablature(boolean rhythmOnTablature) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withDiagrams(DiagramPlacement diagrams) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withDiagramsBelowStandardNotation(boolean diagramsBelowStandardNotation) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }

    public TrackDisplay withForceHorizontalBeams(boolean forceHorizontalBeams) {
        return new TrackDisplay(standardNotation, tablature, tuningLegend, rhythmOnTablature, diagrams,
                diagramsBelowStandardNotation, forceHorizontalBeams);
    }
}
