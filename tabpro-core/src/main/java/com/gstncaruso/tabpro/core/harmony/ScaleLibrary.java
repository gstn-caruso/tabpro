package com.gstncaruso.tabpro.core.harmony;

import java.util.List;

/**
 * Las escalas que ofrece Guitar Pro: el modo mayor y sus siete modos, las tres menores,
 * las pentatonicas, la blues, la cromatica, la de tonos enteros, las dos disminuidas y un
 * puñado de escalas "exoticas" bien documentadas.
 */
public final class ScaleLibrary {

    private ScaleLibrary() {
    }

    private static Scale sevenNotes(String name, List<Integer> semitones) {
        return new Scale(name, semitones, List.of(0, 1, 2, 3, 4, 5, 6));
    }

    public static Scale major() {
        return sevenNotes("Mayor (Jonico)", List.of(0, 2, 4, 5, 7, 9, 11));
    }

    public static Scale dorian() {
        return sevenNotes("Dorico", List.of(0, 2, 3, 5, 7, 9, 10));
    }

    public static Scale phrygian() {
        return sevenNotes("Frigio", List.of(0, 1, 3, 5, 7, 8, 10));
    }

    public static Scale lydian() {
        return sevenNotes("Lidio", List.of(0, 2, 4, 6, 7, 9, 11));
    }

    public static Scale mixolydian() {
        return sevenNotes("Mixolidio", List.of(0, 2, 4, 5, 7, 9, 10));
    }

    public static Scale naturalMinor() {
        return sevenNotes("Menor natural (Eolico)", List.of(0, 2, 3, 5, 7, 8, 10));
    }

    public static Scale locrian() {
        return sevenNotes("Locrio", List.of(0, 1, 3, 5, 6, 8, 10));
    }

    public static Scale harmonicMinor() {
        return sevenNotes("Menor armonica", List.of(0, 2, 3, 5, 7, 8, 11));
    }

    public static Scale melodicMinor() {
        return sevenNotes("Menor melodica", List.of(0, 2, 3, 5, 7, 9, 11));
    }

    public static Scale majorPentatonic() {
        return new Scale("Pentatonica mayor", List.of(0, 2, 4, 7, 9), List.of(0, 1, 2, 4, 5));
    }

    public static Scale minorPentatonic() {
        return new Scale("Pentatonica menor", List.of(0, 3, 5, 7, 10), List.of(0, 2, 3, 4, 6));
    }

    public static Scale blues() {
        return new Scale("Blues", List.of(0, 3, 5, 6, 7, 10), List.of(0, 2, 3, 4, 4, 6));
    }

    public static Scale wholeTone() {
        return new Scale("Tonos enteros", List.of(0, 2, 4, 6, 8, 10), List.of(0, 1, 2, 3, 4, 5));
    }

    public static Scale chromatic() {
        return new Scale(
                "Cromatica",
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                List.of(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6));
    }

    public static Scale diminishedWholeHalf() {
        return new Scale(
                "Disminuida (tono-semitono)",
                List.of(0, 2, 3, 5, 6, 8, 9, 11),
                List.of(0, 1, 2, 3, 4, 5, 5, 6));
    }

    public static Scale diminishedHalfWhole() {
        return new Scale(
                "Disminuida dominante (semitono-tono)",
                List.of(0, 1, 3, 4, 6, 7, 9, 10),
                List.of(0, 1, 2, 2, 3, 4, 5, 6));
    }

    public static Scale hungarianMinor() {
        return sevenNotes("Menor hungara", List.of(0, 2, 3, 6, 7, 8, 11));
    }

    public static Scale phrygianDominant() {
        return sevenNotes("Espanola (frigia dominante)", List.of(0, 1, 4, 5, 7, 8, 10));
    }

    public static Scale neapolitanMinor() {
        return sevenNotes("Napolitana menor", List.of(0, 1, 3, 5, 7, 8, 11));
    }

    public static Scale neapolitanMajor() {
        return sevenNotes("Napolitana mayor", List.of(0, 1, 3, 5, 7, 9, 11));
    }

    public static Scale enigmatic() {
        return sevenNotes("Enigmatica", List.of(0, 1, 4, 6, 8, 10, 11));
    }

    public static Scale hirajoshi() {
        return new Scale("Hirajoshi", List.of(0, 2, 3, 7, 8), List.of(0, 1, 2, 4, 5));
    }

    public static Scale inSen() {
        return new Scale("In Sen", List.of(0, 1, 5, 7, 10), List.of(0, 1, 3, 4, 6));
    }

    public static Scale iwato() {
        return new Scale("Iwato", List.of(0, 1, 5, 6, 10), List.of(0, 1, 3, 4, 6));
    }

    public static Scale chinese() {
        return new Scale("China", List.of(0, 4, 6, 7, 11), List.of(0, 2, 3, 4, 6));
    }

    public static Scale egyptian() {
        return new Scale("Egipcia", List.of(0, 2, 5, 7, 10), List.of(0, 1, 3, 4, 6));
    }

    /** Todas las escalas de la biblioteca, en el orden en que las ofrece Guitar Pro. */
    public static List<Scale> all() {
        return List.of(
                major(), dorian(), phrygian(), lydian(), mixolydian(), naturalMinor(), locrian(),
                harmonicMinor(), melodicMinor(),
                majorPentatonic(), minorPentatonic(), blues(),
                wholeTone(), chromatic(),
                diminishedWholeHalf(), diminishedHalfWhole(),
                hungarianMinor(), phrygianDominant(), neapolitanMinor(), neapolitanMajor(), enigmatic(),
                hirajoshi(), inSen(), iwato(), chinese(), egyptian());
    }
}
