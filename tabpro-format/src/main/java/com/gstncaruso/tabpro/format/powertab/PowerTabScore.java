package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Una de las dos "score" que trae un archivo de PowerTab (la de guitarra o la
 * de bajo, en ese orden): sus guitarras, sus asignaciones de guitarra a
 * pentagrama, sus finales alternativos y sus sistemas.
 */
record PowerTabScore(
        List<PowerTabGuitar> guitars,
        List<PowerTabGuitarIn> guitarIns,
        List<PowerTabAlternateEnding> alternateEndings,
        List<PowerTabSystem> systems) {

    boolean isEmpty() {
        return guitars.isEmpty() && systems.isEmpty();
    }
}
