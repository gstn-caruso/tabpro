package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

/**
 * Donde cae un componente del archivo: en que compas, en que lugar de la
 * grilla de dieciseisavos de ese compas, y en que cuerda (contando todas las
 * pistas apiladas, en el orden en que aparecen en el archivo).
 */
record TabEditPosition(int measureIndex, int positionInMeasure, int stringZeroBased, int trackIndex) {

    /** Cuantos lugares de grilla entran en un compas de 4/4: TablEdit usa una grilla de dieciseisavos. */
    private static final int GRID_POSITIONS_IN_FOUR_FOUR = 16;

    /** Cuanto "location" ocupa cada lugar de grilla, por cada cuerda de todas las pistas juntas. */
    private static final int VALUE_PER_POSITION_PER_STRING = 32;

    private static final int VALUE_PER_STRING = 8;

    /**
     * Decodifica el entero "location" que trae cada componente. No hay un compas
     * "actual" mientras se lee: cada componente es autosuficiente, y hay que
     * restarle el tamano de cada compas hasta encontrar en cual cae.
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
