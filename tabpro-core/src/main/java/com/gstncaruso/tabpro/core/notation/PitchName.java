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
    /** La clase de altura (0=Do) de cada letra natural, en el orden de {@link #LETTERS}. */
    private static final int[] PITCH_CLASS_OF_LETTER = {0, 2, 4, 5, 7, 9, 11};
    private static final int LETTERS_PER_OCTAVE = 7;

    public static PitchName of(Pitch pitch) {
        int pitchClass = pitch.midiNumber() % 12;
        int octave = pitch.midiNumber() / 12 - 1;
        return new PitchName(LETTER_OF_PITCH_CLASS[pitchClass], SHARP_OF_PITCH_CLASS[pitchClass], octave);
    }

    /** La nota natural (nunca sostenido) que cae en ese grado diatonico absoluto -la inversa de
     * {@link #diatonicIndex()}, pero sin poder recuperar el sostenido: dos letras (la natural y
     * su sostenido) comparten el mismo grado, y solo la natural es unica en el. */
    public static PitchName natural(int diatonicIndex) {
        int letter = Math.floorMod(diatonicIndex, LETTERS_PER_OCTAVE);
        int octave = Math.floorDiv(diatonicIndex, LETTERS_PER_OCTAVE);
        return new PitchName(letter, false, octave);
    }

    /** Cuantos grados diatonicos hay desde do menos uno, contando solo las siete letras. */
    public int diatonicIndex() {
        return letter + LETTERS_PER_OCTAVE * octave;
    }

    /** El MIDI de esta nota, sostenido incluido -la inversa de {@link #of}. */
    public int midiNumber() {
        return (octave + 1) * 12 + PITCH_CLASS_OF_LETTER[letter] + (sharp ? 1 : 0);
    }

    public String text() {
        return LETTERS[letter] + (sharp ? "#" : "");
    }

    public String textWithOctave() {
        return text() + octave;
    }
}
