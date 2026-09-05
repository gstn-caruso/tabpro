package com.gstncaruso.tabpro.ui.tracks;

import java.awt.Color;
import java.util.List;

/**
 * El color con el que se reconoce cada pista en la grilla de compases. Sale del orden de la
 * pista y no se guarda en el archivo: alcanza para distinguirlas de un vistazo.
 *
 * <p>El rojo no esta en la lista a proposito, porque es el que marca el compas que suena.
 */
public final class TrackColors {

    private static final List<Color> PALETTE = List.of(
            new Color(0x3574F0),
            new Color(0xE5A44A),
            new Color(0x46A758),
            new Color(0xA45AE5),
            new Color(0x2FB8C6),
            new Color(0xE56AA8),
            new Color(0xC2B33F));

    public static final int COUNT = PALETTE.size();

    private TrackColors() {
    }

    public static Color of(int trackIndex) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex debe ser >= 0: " + trackIndex);
        }
        return PALETTE.get(trackIndex % COUNT);
    }
}
