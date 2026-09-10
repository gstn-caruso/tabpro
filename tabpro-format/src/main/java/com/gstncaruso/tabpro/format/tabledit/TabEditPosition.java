package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

/**
 * Where a file component falls: in which measure, in which spot of that measure's
 * sixteenth-note grid, and on which string (counting every track stacked together, in
 * the order they appear in the file).
 */
record TabEditPosition(int measureIndex, int positionInMeasure, int stringZeroBased, int trackIndex) {

    /** How many grid spots fit in a 4/4 measure: TablEdit uses a sixteenth-note grid. */
    private static final int GRID_POSITIONS_IN_FOUR_FOUR = 16;

    /** How much "location" each grid spot occupies, per string across every track combined. */
    private static final int VALUE_PER_POSITION_PER_STRING = 32;

    private static final int VALUE_PER_STRING = 8;

    /**
     * Decodes the "location" integer each component carries. There is no "current"
     * measure while reading: each component is self-sufficient, and its value must be
     * reduced by the size of each measure until the one it falls in is found.
     */
    static TabEditPosition fromLocation(int location, List<TabEditMeasure> measures, List<Integer> trackStringCounts) {
        int totalStringCount = trackStringCounts.stream().mapToInt(Integer::intValue).sum();
        int valuePerPosition = VALUE_PER_POSITION_PER_STRING * totalStringCount;

        int measureIndex = 0;
        int positionInMeasure = 0;
        int stringInMeasure = 0;
        int remaining = location;

        for (TabEditMeasure measure : measures) {
            double timeSignatureRatio =
                    (double) measure.timeSignature().beats() / measure.timeSignature().beatUnit();
            int gridPositionsInMeasure = (int) (GRID_POSITIONS_IN_FOUR_FOUR * timeSignatureRatio);
            int valueForWholeMeasure = valuePerPosition * gridPositionsInMeasure;

            if (remaining - valueForWholeMeasure <= 0) {
                positionInMeasure = remaining / valuePerPosition;
                stringInMeasure = (remaining % valuePerPosition) / VALUE_PER_STRING;
                break;
            }
            remaining -= valueForWholeMeasure;
            measureIndex++;
        }

        int trackIndex = 0;
        int stringInTrack = stringInMeasure;
        for (int i = 0; i < trackStringCounts.size(); i++) {
            int stringCount = trackStringCounts.get(i);
            if (stringInTrack - stringCount < 0) {
                trackIndex = i;
                break;
            }
            stringInTrack -= stringCount;
        }

        return new TabEditPosition(measureIndex, positionInMeasure, stringInTrack, trackIndex);
    }
}
