package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.util.Map;

/** A que compas apunta cada simbolo de destino y cada salto del bloque de direcciones de GP5. */
record GuitarProDirections(Map<Integer, DirectionSymbol> symbols, Map<Integer, DirectionJump> jumps) {

    private static final GuitarProDirections NONE = new GuitarProDirections(Map.of(), Map.of());

    static GuitarProDirections none() {
        return NONE;
    }
}
