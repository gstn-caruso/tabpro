package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.model.Pitch;

/**
 * Como se escribe una altura MIDI en MusicXML (step/alter/octave) y como se lee de vuelta.
 * MusicXML no necesita la armadura para esto: cada nota lleva su propia alteracion explicita:
 * la armadura solo decide si, a igualdad de sonido, se prefiere escribir con sostenidos o con
 * bemoles.
 */
final class PitchSpelling {

    record Spelling(char step, int alter, int octave) {
    }

    private static final char[] SHARP_STEPS = {'C', 'C', 'D', 'D', 'E', 'F', 'F', 'G', 'G', 'A', 'A', 'B'};
    private static final int[] SHARP_ALTERS = {0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0};
    private static final char[] FLAT_STEPS = {'C', 'D', 'D', 'E', 'E', 'F', 'G', 'G', 'A', 'A', 'B', 'B'};
    private static final int[] FLAT_ALTERS = {0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0};

    private PitchSpelling() {
    }

    static Spelling spell(Pitch pitch, boolean preferFlats) {
        int pitchClass = Math.floorMod(pitch.midiNumber(), 12);
        int octave = pitch.midiNumber() / 12 - 1;
        char step = preferFlats ? FLAT_STEPS[pitchClass] : SHARP_STEPS[pitchClass];
        int alter = preferFlats ? FLAT_ALTERS[pitchClass] : SHARP_ALTERS[pitchClass];
        return new Spelling(step, alter, octave);
    }

    static Pitch pitchOf(char step, int alter, int octave) {
        int midiNumber = (octave + 1) * 12 + semitoneOf(step) + alter;
        return new Pitch(midiNumber);
    }

    private static int semitoneOf(char step) {
        return switch (Character.toUpperCase(step)) {
            case 'C' -> 0;
            case 'D' -> 2;
            case 'E' -> 4;
            case 'F' -> 5;
            case 'G' -> 7;
            case 'A' -> 9;
            case 'B' -> 11;
            default -> throw new IllegalArgumentException("step invalido: " + step);
        };
    }
}
