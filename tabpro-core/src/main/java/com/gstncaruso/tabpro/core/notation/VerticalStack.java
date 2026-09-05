package com.gstncaruso.tabpro.core.notation;

import java.util.ArrayList;
import java.util.List;

/**
 * Apila simbolos sin que se pisen ni dejen huecos: cada uno se lleva el carril mas cercano a la
 * pauta que este libre, y el que sigue arranca justo donde termino el anterior mas un respiro
 * fijo. Es el mecanismo que permite el "posicionamiento vertical automatico" que promete el
 * manual: quien pide un carril no necesita saber cuantos simbolos hay antes.
 */
public final class VerticalStack {

    private final int gap;
    private final List<Integer> claimedHeights = new ArrayList<>();

    public VerticalStack(int gap) {
        this.gap = gap;
    }

    /** Reserva el proximo carril y devuelve cuanto hay que alejarse del borde de la pauta. */
    public int claim(int symbolHeight) {
        int offset = claimedHeights.stream().mapToInt(height -> height + gap).sum();
        claimedHeights.add(symbolHeight);
        return offset;
    }

    /** Cuanto espacio se llevo apilado hasta ahora, contando los respiros entre carriles. */
    public int totalHeight() {
        if (claimedHeights.isEmpty()) {
            return 0;
        }
        return claimedHeights.stream().mapToInt(Integer::intValue).sum() + gap * (claimedHeights.size() - 1);
    }
}
