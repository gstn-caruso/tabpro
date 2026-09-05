package com.gstncaruso.tabpro.core.harmony;

/**
 * Una nota de la formula de un acorde: el intervalo desde la fundamental, y si hace falta
 * que este siempre presente o si se la puede omitir cuando no alcanzan las cuerdas (la
 * quinta justa es lo primero que se sacrifica en un acorde de septima o mas).
 */
public record ChordTone(Interval interval, boolean essential) {
}
