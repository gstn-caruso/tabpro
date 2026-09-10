package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.util.Map;

/** Which measure each target symbol and each jump of the GP5 directions block points to. */
record GuitarProDirections(Map<Integer, DirectionSymbol> symbols, Map<Integer, DirectionJump> jumps) {

    private static final GuitarProDirections NONE = new GuitarProDirections(Map.of(), Map.of());

    static GuitarProDirections none() {
        return NONE;
    }
}
