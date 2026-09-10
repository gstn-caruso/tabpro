package com.gstncaruso.tabpro.core.harmony;

public record PitchClass(int letter, int alteration) {

    private static final String[] LETTER_NAMES = {"C", "D", "E", "F", "G", "A", "B"};

    private static final String[] SOLFEGE_NAMES = {"Do", "Re", "Mi", "Fa", "Sol", "La", "Si"};

    private static final int[] LETTER_SEMITONES = {0, 2, 4, 5, 7, 9, 11};

    private static final int[] DEFAULT_LETTER_OF_SEMITONE = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6};
    private static final boolean[] DEFAULT_SHARP_OF_SEMITONE = {
        false, true, false, true, false, false, true, false, true, false, true, false
    };

    public PitchClass {
        if (letter < 0 || letter >= LETTER_NAMES.length) {
            throw new IllegalArgumentException("letter debe estar entre 0 y 6: " + letter);
        }
    }

    public static PitchClass of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("una nota necesita un nombre");
        }
        String trimmed = name.trim();
        return read(trimmed, SOLFEGE_NAMES)
                .or(() -> read(trimmed, LETTER_NAMES))
                .orElseThrow(() -> new IllegalArgumentException("nota desconocida: " + name));
    }

    private static java.util.Optional<PitchClass> read(String name, String[] names) {
        for (int letter = 0; letter < names.length; letter++) {
            String candidate = names[letter];
            if (name.regionMatches(true, 0, candidate, 0, candidate.length())) {
                return java.util.Optional.of(
                        new PitchClass(letter, parseAlteration(name.substring(candidate.length()))));
            }
        }
        return java.util.Optional.empty();
    }

    private static int parseAlteration(String suffix) {
        if (suffix.isEmpty()) {
            return 0;
        }
        char symbol = suffix.charAt(0);
        boolean sharp = symbol == '#';
        boolean flat = symbol == 'b';
        if ((!sharp && !flat) || suffix.chars().anyMatch(character -> character != symbol)) {
            throw new IllegalArgumentException("alteración desconocida: " + suffix);
        }
        return sharp ? suffix.length() : -suffix.length();
    }

    public static PitchClass fromSemitone(int semitone) {
        int pitchClass = Math.floorMod(semitone, 12);
        int letter = DEFAULT_LETTER_OF_SEMITONE[pitchClass];
        return new PitchClass(letter, DEFAULT_SHARP_OF_SEMITONE[pitchClass] ? 1 : 0);
    }

    public int semitone() {
        return Math.floorMod(LETTER_SEMITONES[letter] + alteration, 12);
    }

    public PitchClass steppedBy(int letterSteps, int semitones) {
        int newLetter = Math.floorMod(letter + letterSteps, LETTER_NAMES.length);
        int targetSemitone = Math.floorMod(semitone() + semitones, 12);
        int natural = LETTER_SEMITONES[newLetter];
        int raw = Math.floorMod(targetSemitone - natural, 12);
        int alteration = raw <= 6 ? raw : raw - 12;
        return new PitchClass(newLetter, alteration);
    }

    public String name() {
        return LETTER_NAMES[letter] + accidental();
    }

    public String solfegeName() {
        return SOLFEGE_NAMES[letter] + accidental();
    }

    private String accidental() {
        return alteration > 0 ? "#".repeat(alteration) : "b".repeat(-alteration);
    }

    @Override
    public String toString() {
        return name();
    }
}
