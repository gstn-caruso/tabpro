package com.gstncaruso.tabpro.format.powertab;

/**
 * Un "guitar in" de PowerTab: dice que guitarras suenan en que pentagrama a
 * partir de cierta posicion de cierto sistema. Quien ensambla la partitura
 * usa la mascara (bit i = guitarra i) para resolver que guitarra le toca a
 * cada pentagrama.
 */
record PowerTabGuitarIn(int system, int staff, int position, int staffGuitarsMask) {
}
