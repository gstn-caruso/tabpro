package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;

/**
 * Una barra de PowerTab: en que posicion del sistema esta anclada, su tipo
 * (compas comun, repeticion, doble barra...), y la armadura y la medida que
 * rigen desde ahi. Cada barra trae su propio valor completo: a diferencia de
 * Guitar Pro, PowerTab no marca "cambio" con una bandera.
 */
record PowerTabBarline(int position, int type, int repeatCount, TimeSignature timeSignature, KeySignature keySignature) {

    static final int BAR = 0;
    static final int DOUBLE_BAR = 1;
    static final int FREE_TIME_BAR = 2;
    static final int REPEAT_START = 3;
    static final int REPEAT_END = 4;
    static final int DOUBLE_BAR_FINE = 5;

    boolean isDoubleBar() {
        return type == DOUBLE_BAR || type == DOUBLE_BAR_FINE;
    }

    boolean isRepeatStart() {
        return type == REPEAT_START;
    }

    boolean isRepeatEnd() {
        return type == REPEAT_END;
    }
}
