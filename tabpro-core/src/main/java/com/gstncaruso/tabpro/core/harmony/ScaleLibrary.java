package com.gstncaruso.tabpro.core.harmony;

import java.util.List;

public final class ScaleLibrary {

    private ScaleLibrary() {
    }

    private static Scale sevenNotes(String id, List<Integer> semitones) {
        return new Scale(id, semitones, List.of(0, 1, 2, 3, 4, 5, 6));
    }

    public static Scale major() {
        return sevenNotes("major", List.of(0, 2, 4, 5, 7, 9, 11));
    }

    public static Scale dorian() {
        return sevenNotes("dorian", List.of(0, 2, 3, 5, 7, 9, 10));
    }

    public static Scale phrygian() {
        return sevenNotes("phrygian", List.of(0, 1, 3, 5, 7, 8, 10));
    }

    public static Scale lydian() {
        return sevenNotes("lydian", List.of(0, 2, 4, 6, 7, 9, 11));
    }

    public static Scale mixolydian() {
        return sevenNotes("mixolydian", List.of(0, 2, 4, 5, 7, 9, 10));
    }

    public static Scale naturalMinor() {
        return sevenNotes("naturalMinor", List.of(0, 2, 3, 5, 7, 8, 10));
    }

    public static Scale locrian() {
        return sevenNotes("locrian", List.of(0, 1, 3, 5, 6, 8, 10));
    }

    public static Scale harmonicMinor() {
        return sevenNotes("harmonicMinor", List.of(0, 2, 3, 5, 7, 8, 11));
    }

    public static Scale melodicMinor() {
        return sevenNotes("melodicMinor", List.of(0, 2, 3, 5, 7, 9, 11));
    }

    public static Scale majorPentatonic() {
        return new Scale("majorPentatonic", List.of(0, 2, 4, 7, 9), List.of(0, 1, 2, 4, 5));
    }

    public static Scale minorPentatonic() {
        return new Scale("minorPentatonic", List.of(0, 3, 5, 7, 10), List.of(0, 2, 3, 4, 6));
    }

    public static Scale blues() {
        return new Scale("blues", List.of(0, 3, 5, 6, 7, 10), List.of(0, 2, 3, 4, 4, 6));
    }

    public static Scale wholeTone() {
        return new Scale("wholeTone", List.of(0, 2, 4, 6, 8, 10), List.of(0, 1, 2, 3, 4, 5));
    }

    public static Scale chromatic() {
        return new Scale(
                "chromatic",
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                List.of(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6));
    }

    public static Scale diminishedWholeHalf() {
        return new Scale(
                "diminishedWholeHalf",
                List.of(0, 2, 3, 5, 6, 8, 9, 11),
                List.of(0, 1, 2, 3, 4, 5, 5, 6));
    }

    public static Scale diminishedHalfWhole() {
        return new Scale(
                "diminishedHalfWhole",
                List.of(0, 1, 3, 4, 6, 7, 9, 10),
                List.of(0, 1, 2, 2, 3, 4, 5, 6));
    }

    public static Scale hungarianMinor() {
        return sevenNotes("hungarianMinor", List.of(0, 2, 3, 6, 7, 8, 11));
    }

    public static Scale phrygianDominant() {
        return sevenNotes("phrygianDominant", List.of(0, 1, 4, 5, 7, 8, 10));
    }

    public static Scale neapolitanMinor() {
        return sevenNotes("neapolitanMinor", List.of(0, 1, 3, 5, 7, 8, 11));
    }

    public static Scale neapolitanMajor() {
        return sevenNotes("neapolitanMajor", List.of(0, 1, 3, 5, 7, 9, 11));
    }

    public static Scale enigmatic() {
        return sevenNotes("enigmatic", List.of(0, 1, 4, 6, 8, 10, 11));
    }

    public static Scale hirajoshi() {
        return new Scale("hirajoshi", List.of(0, 2, 3, 7, 8), List.of(0, 1, 2, 4, 5));
    }

    public static Scale inSen() {
        return new Scale("inSen", List.of(0, 1, 5, 7, 10), List.of(0, 1, 3, 4, 6));
    }

    public static Scale iwato() {
        return new Scale("iwato", List.of(0, 1, 5, 6, 10), List.of(0, 1, 3, 4, 6));
    }

    public static Scale chinese() {
        return new Scale("chinese", List.of(0, 4, 6, 7, 11), List.of(0, 2, 3, 4, 6));
    }

    public static Scale egyptian() {
        return new Scale("egyptian", List.of(0, 2, 5, 7, 10), List.of(0, 1, 3, 4, 6));
    }

    public static Scale locrianNatural6() {
        return sevenNotes("locrianNatural6", List.of(0, 1, 3, 5, 6, 9, 10));
    }

    public static Scale majorSharpFive() {
        return sevenNotes("majorSharpFive", List.of(0, 2, 4, 5, 8, 9, 11));
    }

    public static Scale dorianSharpFour() {
        return sevenNotes("dorianSharpFour", List.of(0, 2, 3, 6, 7, 9, 10));
    }

    public static Scale lydianSharpTwo() {
        return sevenNotes("lydianSharpTwo", List.of(0, 3, 4, 6, 7, 9, 11));
    }

    public static Scale dorianFlatTwo() {
        return sevenNotes("dorianFlatTwo", List.of(0, 1, 3, 5, 7, 9, 10));
    }

    public static Scale lydianAugmented() {
        return sevenNotes("lydianAugmented", List.of(0, 2, 4, 6, 8, 9, 11));
    }

    public static Scale lydianDominant() {
        return sevenNotes("lydianDominant", List.of(0, 2, 4, 6, 7, 9, 10));
    }

    public static Scale mixolydianFlatSix() {
        return sevenNotes("mixolydianFlatSix", List.of(0, 2, 4, 5, 7, 8, 10));
    }

    public static Scale locrianSharpTwo() {
        return sevenNotes("locrianSharpTwo", List.of(0, 2, 3, 5, 6, 8, 10));
    }

    public static Scale superLocrian() {
        return sevenNotes("superLocrian", List.of(0, 1, 3, 4, 6, 8, 10));
    }

    public static Scale bebopDominant() {
        return new Scale("bebopDominant", List.of(0, 2, 4, 5, 7, 9, 10, 11), List.of(0, 1, 2, 3, 4, 5, 6, 6));
    }

    public static Scale augmented() {
        return new Scale("augmented", List.of(0, 3, 4, 7, 8, 11), List.of(0, 1, 2, 4, 5, 6));
    }

    public static Scale bluesMajor() {
        return new Scale("bluesMajor", List.of(0, 2, 3, 4, 7, 9), List.of(0, 1, 2, 2, 4, 5));
    }

    public static Scale arabian() {
        return sevenNotes("arabian", List.of(0, 2, 4, 5, 6, 8, 10));
    }

    public static Scale balinese() {
        return new Scale("balinese", List.of(0, 1, 3, 7, 8), List.of(0, 1, 2, 4, 5));
    }

    public static Scale byzantine() {
        return sevenNotes("byzantine", List.of(0, 1, 4, 5, 7, 8, 11));
    }

    public static Scale hungarianMajor() {
        return sevenNotes("hungarianMajor", List.of(0, 3, 4, 6, 7, 9, 10));
    }

    public static Scale javanese() {
        return sevenNotes("javanese", List.of(0, 1, 3, 5, 7, 9, 10));
    }

    public static Scale kumoi() {
        return new Scale("kumoi", List.of(0, 2, 3, 7, 9), List.of(0, 1, 2, 4, 5));
    }

    public static Scale oriental() {
        return sevenNotes("oriental", List.of(0, 1, 4, 5, 6, 9, 10));
    }

    public static Scale persian() {
        return sevenNotes("persian", List.of(0, 1, 4, 5, 6, 8, 11));
    }

    public static Scale pelog() {
        return new Scale("pelog", List.of(0, 1, 3, 7, 8), List.of(0, 1, 2, 4, 5));
    }

    public static Scale overtone() {
        return sevenNotes("overtone", List.of(0, 2, 4, 6, 7, 9, 10));
    }

    public static List<Scale> all() {
        return List.of(
                major(), dorian(), phrygian(), lydian(), mixolydian(), naturalMinor(), locrian(),
                harmonicMinor(), melodicMinor(),
                majorPentatonic(), minorPentatonic(), blues(),
                wholeTone(), chromatic(),
                diminishedWholeHalf(), diminishedHalfWhole(),
                hungarianMinor(), phrygianDominant(), neapolitanMinor(), neapolitanMajor(), enigmatic(),
                hirajoshi(), inSen(), iwato(), chinese(), egyptian(),
                locrianNatural6(), majorSharpFive(), dorianSharpFour(), lydianSharpTwo(),
                dorianFlatTwo(), lydianAugmented(), lydianDominant(), mixolydianFlatSix(),
                locrianSharpTwo(), superLocrian(),
                bebopDominant(), augmented(), bluesMajor(), arabian(), balinese(), byzantine(),
                hungarianMajor(), javanese(), kumoi(), oriental(), persian(), pelog(), overtone());
    }
}
