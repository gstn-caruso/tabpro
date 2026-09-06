package com.gstncaruso.tabpro.format.powertab;

/**
 * Un "guitar in" de PowerTab: dice que guitarras suenan en que pentagrama a
 * partir de cierta posicion de cierto sistema. tabpro solo soporta el caso
 * de siempre (pentagrama N = guitarra N, sin reasignar): {@link #matchesIdentity()}
 * dice si esta entrada respeta esa regla.
 */
record PowerTabGuitarIn(int system, int staff, int position, int staffGuitarsMask) {

    boolean matchesIdentity() {
        return staffGuitarsMask == (1 << staff);
    }
}
