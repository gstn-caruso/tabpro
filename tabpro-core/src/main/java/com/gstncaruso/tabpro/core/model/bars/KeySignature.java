package com.gstncaruso.tabpro.core.model.bars;

import java.util.List;

/** La armadura: cuantos sostenidos o bemoles lleva la clave, y si es mayor o menor. */
public record KeySignature(int accidentals, Mode mode) {

    /** El orden en que entran los sostenidos: fa do sol re la mi si. */
    private static final List<Integer> SHARP_ORDER = List.of(3, 0, 4, 1, 5, 2, 6);

    /** El orden en que entran los bemoles: si mi la re sol do fa. */
    private static final List<Integer> FLAT_ORDER = List.of(6, 2, 5, 1, 4, 0, 3);

    private static final List<String> MAJOR_NAMES =
            List.of("Do b", "Sol b", "Re b", "La b", "Mi b", "Si b", "Fa", "Do", "Sol", "Re", "La", "Mi", "Si", "Fa #", "Do #");

    private static final List<String> MINOR_NAMES =
            List.of("La b", "Mi b", "Si b", "Fa", "Do", "Sol", "Re", "La", "Mi", "Si", "Fa #", "Do #", "Sol #", "Re #", "La #");

    public KeySignature {
        if (accidentals < -7 || accidentals > 7) {
            throw new IllegalArgumentException("la armadura va de -7 a 7 alteraciones: " + accidentals);
        }
    }

    public static KeySignature cMajor() {
        return new KeySignature(0, Mode.MAJOR);
    }

    public boolean hasSharps() {
        return accidentals > 0;
    }

    public boolean hasFlats() {
        return accidentals < 0;
    }

    public int alteredCount() {
        return Math.abs(accidentals);
    }

    /** Los grados de la escala que quedan alterados, en el orden en que se escriben. */
    public List<Integer> alteredSteps() {
        return (hasFlats() ? FLAT_ORDER : SHARP_ORDER).subList(0, alteredCount());
    }

    /** Cuanto altera la armadura a ese grado: +1 sostenido, -1 bemol, 0 natural. */
    public int alterationOf(int step) {
        if (!alteredSteps().contains(step)) {
            return 0;
        }
        return hasFlats() ? -1 : 1;
    }

    public String name() {
        return (mode == Mode.MAJOR ? MAJOR_NAMES : MINOR_NAMES).get(accidentals + 7);
    }
}
