package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * One of the two "scores" a PowerTab file carries (guitar or bass, in that order): its
 * guitars, its guitar-to-staff assignments, its alternate endings, and its systems.
 */
record PowerTabScore(
        List<PowerTabGuitar> guitars,
        List<PowerTabGuitarIn> guitarIns,
        List<PowerTabTempoMarker> tempoMarkers,
        List<PowerTabAlternateEnding> alternateEndings,
        List<PowerTabSystem> systems) {

    boolean isEmpty() {
        return guitars.isEmpty() && systems.isEmpty();
    }
}
