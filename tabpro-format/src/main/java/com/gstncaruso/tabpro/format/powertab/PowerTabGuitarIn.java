package com.gstncaruso.tabpro.format.powertab;

/**
 * A PowerTab "guitar in": says which guitars sound on which staff starting from a
 * given position of a given system, via a mask (bit i = guitar i).
 */
record PowerTabGuitarIn(int system, int staff, int position, int staffGuitarsMask) {
}
