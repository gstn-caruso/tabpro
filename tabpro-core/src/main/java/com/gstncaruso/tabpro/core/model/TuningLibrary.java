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
                Tuning.of("Open Cm", 63, 60, 55, 48, 43, 36),
                Tuning.of("Open C6", 64, 57, 55, 48, 43, 36),
                Tuning.of("Open Dm", 62, 57, 53, 50, 45, 38),
                Tuning.of("Open D5", 62, 57, 54, 50, 45, 38),
                Tuning.of("Open Dsus4", 62, 57, 55, 50, 45, 38),
                Tuning.of("Open Em", 64, 59, 55, 52, 47, 40),
                Tuning.of("Open Gm", 62, 58, 55, 50, 43, 38),
                Tuning.of("Open G6", 62, 59, 55, 50, 45, 38),
                Tuning.of("Open Gsus4", 62, 60, 55, 50, 43, 38),
                Tuning.of("Open Am", 64, 60, 57, 52, 45, 40),
                Tuning.of("Open F", 65, 60, 57, 53, 45, 41),
                Tuning.of("Nashville", 64, 59, 67, 62, 57, 52),
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
                Tuning.of("Banjo Open G", 62, 59, 55, 50, 67),
                Tuning.of("Banjo Open D", 62, 57, 54, 50, 66),
                Tuning.of("Banjo Drop C", 62, 59, 55, 48, 67),
                Tuning.of("Banjo Sol menor", 62, 58, 55, 50, 67),
                Tuning.of("Banjo Sol modal", 62, 57, 55, 50, 67),
                Tuning.of("Mandolina", 76, 69, 62, 55),
                Tuning.of("Ukelele en Do", 69, 64, 60, 67),
                Tuning.of("Ukelele en Sol", 71, 66, 62, 69),
                Tuning.of("Violin", 76, 69, 62, 55),
                Tuning.of("Viola", 69, 62, 55, 48),
                Tuning.of("Violoncello", 57, 50, 43, 36));
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
