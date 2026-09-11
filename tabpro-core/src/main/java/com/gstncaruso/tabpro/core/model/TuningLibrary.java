package com.gstncaruso.tabpro.core.model;

import java.util.List;

public final class TuningLibrary {

    private TuningLibrary() {
    }

    public static Tuning standardGuitar() {
        return Tuning.fromLibrary("guitar.standard", 64, 59, 55, 50, 45, 40);
    }

    public static Tuning standardBass() {
        return Tuning.of("Bajo estándar", 43, 38, 33, 28);
    }

    public static List<Tuning> guitars() {
        return List.of(
                standardGuitar(),
                Tuning.fromLibrary("guitar.dropD", 64, 59, 55, 50, 45, 38),
                Tuning.fromLibrary("guitar.halfStepDown", 63, 58, 54, 49, 44, 39),
                Tuning.fromLibrary("guitar.wholeStepDown", 62, 57, 53, 48, 43, 38),
                Tuning.fromLibrary("guitar.dropC", 62, 57, 53, 48, 43, 36),
                Tuning.fromLibrary("guitar.openD", 62, 57, 54, 50, 45, 38),
                Tuning.fromLibrary("guitar.openG", 62, 59, 55, 50, 43, 38),
                Tuning.fromLibrary("guitar.openC", 64, 60, 55, 48, 43, 36),
                Tuning.fromLibrary("guitar.openE", 64, 59, 56, 52, 47, 40),
                Tuning.fromLibrary("guitar.openA", 64, 61, 57, 52, 45, 40),
                Tuning.fromLibrary("guitar.dadgad", 62, 57, 55, 50, 45, 38),
                Tuning.fromLibrary("guitar.newStandard", 71, 64, 57, 50, 43, 36),
                Tuning.fromLibrary("guitar.openCm", 63, 60, 55, 48, 43, 36),
                Tuning.fromLibrary("guitar.openC6", 64, 57, 55, 48, 43, 36),
                Tuning.fromLibrary("guitar.openDm", 62, 57, 53, 50, 45, 38),
                Tuning.fromLibrary("guitar.openD5", 62, 57, 54, 50, 45, 38),
                Tuning.fromLibrary("guitar.openDsus4", 62, 57, 55, 50, 45, 38),
                Tuning.fromLibrary("guitar.openEm", 64, 59, 55, 52, 47, 40),
                Tuning.fromLibrary("guitar.openGm", 62, 58, 55, 50, 43, 38),
                Tuning.fromLibrary("guitar.openG6", 62, 59, 55, 50, 45, 38),
                Tuning.fromLibrary("guitar.openGsus4", 62, 60, 55, 50, 43, 38),
                Tuning.fromLibrary("guitar.openAm", 64, 60, 57, 52, 45, 40),
                Tuning.fromLibrary("guitar.openF", 65, 60, 57, 53, 45, 41),
                Tuning.fromLibrary("guitar.nashville", 64, 59, 67, 62, 57, 52),
                Tuning.fromLibrary("guitar.sevenString", 64, 59, 55, 50, 45, 40, 35),
                Tuning.fromLibrary("guitar.sevenStringDropA", 64, 59, 55, 50, 45, 40, 33));
    }

    public static List<Tuning> basses() {
        return List.of(
                standardBass(),
                Tuning.of("Bajo Drop D", 43, 38, 33, 26),
                Tuning.of("Bajo medio tono abajo", 42, 37, 32, 27),
                Tuning.of("Bajo un tono abajo", 41, 36, 31, 26),
                Tuning.of("Bajo de 5 cuerdas", 43, 38, 33, 28, 23),
                Tuning.of("Bajo de 6 cuerdas", 48, 43, 38, 33, 28, 23));
    }

    public static List<Tuning> otherStringInstruments() {
        return List.of(
                Tuning.of("Banjo Open G", 62, 59, 55, 50, 67),
                Tuning.of("Banjo Open D", 62, 57, 54, 50, 66),
                Tuning.of("Banjo Drop C", 62, 59, 55, 48, 67),
                Tuning.of("Banjo Sol menor", 62, 58, 55, 50, 67),
                Tuning.of("Banjo Sol modal", 62, 57, 55, 50, 67),
                Tuning.of("Mandolina", 76, 69, 62, 55),
                Tuning.of("Ukelele en Do", 69, 64, 60, 67),
                Tuning.of("Ukelele en Sol", 71, 66, 62, 69),
                Tuning.of("Violín", 76, 69, 62, 55),
                Tuning.of("Viola", 69, 62, 55, 48),
                Tuning.of("Violoncello", 57, 50, 43, 36));
    }

    public static List<Tuning> all() {
        return java.util.stream.Stream.of(guitars(), basses(), otherStringInstruments())
                .flatMap(List::stream)
                .toList();
    }

    public static Tuning identify(List<Pitch> strings) {
        return all().stream()
                .filter(tuning -> tuning.strings().equals(strings))
                .findFirst()
                .orElseGet(() -> new Tuning(strings));
    }

    public static List<Tuning> withStringCount(int stringCount) {
        return all().stream().filter(tuning -> tuning.stringCount() == stringCount).toList();
    }
}
