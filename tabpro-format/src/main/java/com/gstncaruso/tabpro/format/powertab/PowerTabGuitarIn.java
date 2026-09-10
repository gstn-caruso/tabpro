package com.gstncaruso.tabpro.format.powertab;

/**
 * A PowerTab "guitar in": says which guitars sound on which staff starting from a
 * given position of a given system. Whoever assembles the score uses the mask (bit i
 * = guitar i) to resolve which guitar plays on each staff.
 */
record PowerTabGuitarIn(int system, int staff, int position, int staffGuitarsMask) {
}
