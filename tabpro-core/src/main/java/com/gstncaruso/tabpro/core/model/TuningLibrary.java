package com.gstncaruso.tabpro.core.model;

import java.util.List;

public final class TuningLibrary {

    private TuningLibrary() {
    }

    public static Tuning standardGuitar() {
        return Tuning.fromLibrary("guitar.standard", 64, 59, 55, 50, 45, 40);
    }

    public static Tuning standardBass() {
        return Tuning.fromLibrary("bass.standard", 43, 38, 33, 28);
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
                Tuning.fromLibrary("bass.dropD", 43, 38, 33, 26),
                Tuning.fromLibrary("bass.halfStepDown", 42, 37, 32, 27),
                Tuning.fromLibrary("bass.wholeStepDown", 41, 36, 31, 26),
                Tuning.fromLibrary("bass.fiveString", 43, 38, 33, 28, 23),
                Tuning.fromLibrary("bass.sixString", 48, 43, 38, 33, 28, 23));
    }

    public static List<Tuning> otherStringInstruments() {
        return List.of(
                Tuning.fromLibrary("banjo.openG", 62, 59, 55, 50, 67),
                Tuning.fromLibrary("banjo.openD", 62, 57, 54, 50, 66),
                Tuning.fromLibrary("banjo.dropC", 62, 59, 55, 48, 67),
                Tuning.fromLibrary("banjo.gMinor", 62, 58, 55, 50, 67),
                Tuning.fromLibrary("banjo.gModal", 62, 57, 55, 50, 67),
                Tuning.fromLibrary("mandolin.standard", 76, 69, 62, 55),
                Tuning.fromLibrary("ukulele.c", 69, 64, 60, 67),
                Tuning.fromLibrary("ukulele.g", 71, 66, 62, 69),
                Tuning.fromLibrary("violin.standard", 76, 69, 62, 55),
                Tuning.fromLibrary("viola.standard", 69, 62, 55, 48),
                Tuning.fromLibrary("cello.standard", 57, 50, 43, 36));
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
