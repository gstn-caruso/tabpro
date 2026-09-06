package com.gstncaruso.tabpro.core.model;

import java.util.List;

/** Las afinaciones que ofrece el programa, agrupadas por instrumento. */
public final class TuningLibrary {

    private TuningLibrary() {
    }

    public static Tuning standardGuitar() {
        return Tuning.of("Guitarra estandar", 64, 59, 55, 50, 45, 40);
    }

    public static Tuning standardBass() {
        return Tuning.of("Bajo estandar", 43, 38, 33, 28);
    }

    public static List<Tuning> guitars() {
        return List.of(
                standardGuitar(),
                Tuning.of("Drop D", 64, 59, 55, 50, 45, 38),
                Tuning.of("Medio tono abajo", 63, 58, 54, 49, 44, 39),
                Tuning.of("Un tono abajo", 62, 57, 53, 48, 43, 38),
                Tuning.of("Drop C", 62, 57, 53, 48, 43, 36),
                Tuning.of("Open D", 62, 57, 54, 50, 45, 38),
                Tuning.of("Open G", 62, 59, 55, 50, 43, 38),
                Tuning.of("Open C", 64, 60, 55, 48, 43, 36),
                Tuning.of("Open E", 64, 59, 56, 52, 47, 40),
                Tuning.of("Open A", 64, 61, 57, 52, 45, 40),
                Tuning.of("DADGAD", 62, 57, 55, 50, 45, 38),
                Tuning.of("Nuevo estandar", 71, 64, 57, 50, 43, 36),
                Tuning.of("Guitarra de 7 cuerdas", 64, 59, 55, 50, 45, 40, 35),
                Tuning.of("Guitarra de 7 cuerdas Drop A", 64, 59, 55, 50, 45, 40, 33));
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
                Tuning.of("Banjo estandar", 62, 59, 55, 50, 67),
                Tuning.of("Mandolina", 76, 69, 62, 55),
                Tuning.of("Ukelele", 69, 64, 60, 67),
                Tuning.of("Violin", 76, 69, 62, 55),
                Tuning.of("Violoncello", 69, 62, 55, 48));
    }

    /** Todas las afinaciones, en el orden en que las ofrece la biblioteca. */
    public static List<Tuning> all() {
        return java.util.stream.Stream.of(guitars(), basses(), otherStringInstruments())
                .flatMap(List::stream)
                .toList();
    }

    /** El nombre con que la biblioteca conoce esas alturas, si es que la conoce. */
    public static Tuning identify(List<Pitch> strings) {
        return all().stream()
                .filter(tuning -> tuning.strings().equals(strings))
                .findFirst()
                .orElseGet(() -> new Tuning(strings));
    }

    /** Las afinaciones de la biblioteca que tienen esa cantidad de cuerdas. */
    public static List<Tuning> withStringCount(int stringCount) {
        return all().stream().filter(tuning -> tuning.stringCount() == stringCount).toList();
    }
}
