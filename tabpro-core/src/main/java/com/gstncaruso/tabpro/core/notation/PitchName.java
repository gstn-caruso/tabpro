package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;

/**
 * Como se llama y como se deletrea una altura: la letra, si lleva sostenido y en que octava
 * esta. Usamos siempre sostenidos y nunca bemoles, que es lo razonable mientras la partitura
 * no tenga tonalidad.
 */
public record PitchName(int letter, boolean sharp, int octave) {

    private static final String[] LETTERS = {"C", "D", "E", "F", "G", "A", "B"};
    private static final int[] LETTER_OF_PITCH_CLASS = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6};
    private static final boolean[] SHARP_OF_PITCH_CLASS = {
        false, true, false, true, false, false, true, false, true, false, true, false
    };
    private static final int LETTERS_PER_OCTAVE = 7;

    public static PitchName of(Pitch pitch) {
        int pitchClass = pitch.midiNumber() % 12;
        int octave = pitch.midiNumber() / 12 - 1;
        return new PitchName(LETTER_OF_PITCH_CLASS[pitchClass], SHARP_OF_PITCH_CLASS[pitchClass], octave);
    }

    /** Cuantos grados diatonicos hay desde do menos uno, contando solo las siete letras. */
    public int diatonicIndex() {
        return letter + LETTERS_PER_OCTAVE * octave;
    }

    public String text() {
        return LETTERS[letter] + (sharp ? "#" : "");
    }

    public String textWithOctave() {
        return text() + octave;
    }
}
