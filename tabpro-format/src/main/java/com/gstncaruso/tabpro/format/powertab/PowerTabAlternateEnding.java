package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * A PowerTab alternate ending: which system and which position of that system it
 * starts at, and the round numbers it covers (1st, 2nd...). It lives in a score-level
 * array that mixes the endings of every system; the "system" field is what lets them be
 * sorted out. If it also marks D.C./D.S., that data has no place in the model yet and
 * is discarded.
 */
record PowerTabAlternateEnding(int system, int position, List<Integer> numbers) {
}
