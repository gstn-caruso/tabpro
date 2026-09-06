package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.HashMap;
import java.util.Map;

/**
 * Decide que alteracion dibujar junto a cada nota de un compas, de acuerdo a la armadura y a lo
 * que ya se escribio antes en el mismo compas. Una alteracion vale hasta el final del compas: si
 * una nota vuelve al grado que pide la armadura hay que cancelarla con un becuadro. Se usa una
 * instancia nueva por compas, para que el compas siguiente vuelva a partir de la armadura sola.
 *
 * <p>Limitacion conocida: como {@link StaffPosition} solo deletrea con sostenidos (nunca con
 * bemoles), una nota que en una armadura de bemoles deberia escribirse bemol se dibuja como el
 * sostenido de la nota de abajo. La decision de SI hace falta alteracion es correcta; el signo
 * dibujado puede no ser el bemol "de libro" en esos casos.
 */
public final class KeySignatureAccidentals {

    private final Clef clef;
    private final KeySignature key;
    private final Map<Integer, Integer> printedAlterOf = new HashMap<>();

    public KeySignatureAccidentals(Clef clef, KeySignature key) {
        this.clef = clef;
        this.key = key;
    }

    /** Que hay que dibujar junto a esta nota; recuerda lo que ya paso en este mismo compas. */
    public AccidentalGlyph glyphFor(StaffPosition position) {
        int diatonicIndex = clef.bottomLineDiatonicIndex() + position.step();
        int letter = Math.floorMod(diatonicIndex, 7);
        int alter = position.sharp() ? 1 : 0;
        int keyAlteration = key.alterationOf(letter);
        int active = printedAlterOf.getOrDefault(diatonicIndex, keyAlteration);

        if (alter == active) {
            return AccidentalGlyph.NONE;
        }
        printedAlterOf.put(diatonicIndex, alter);
        return switch (alter) {
            case 1 -> AccidentalGlyph.SHARP;
            case -1 -> AccidentalGlyph.FLAT;
            default -> AccidentalGlyph.NATURAL;
        };
    }
}
