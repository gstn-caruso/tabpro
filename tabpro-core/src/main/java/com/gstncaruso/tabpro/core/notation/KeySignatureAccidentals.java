package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.HashMap;
import java.util.Map;

public final class KeySignatureAccidentals {

    private final Clef clef;
    private final KeySignature key;
    private final Map<Integer, Integer> printedAlterOf = new HashMap<>();

    public KeySignatureAccidentals(Clef clef, KeySignature key) {
        this.clef = clef;
        this.key = key;
    }

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
