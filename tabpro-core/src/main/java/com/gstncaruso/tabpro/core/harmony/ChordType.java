package com.gstncaruso.tabpro.core.harmony;

import java.util.List;

/**
 * Los tipos de acorde que ofrece Guitar Pro, con su sufijo de nombre y su formula: que
 * intervalos lo forman, y cuales de ellos son imprescindibles cuando no alcanzan las
 * cuerdas para tocarlos todos (ahi es la quinta justa la que primero se sacrifica).
 */
public enum ChordType {
    MAJOR("", tone(Interval.ROOT), tone(Interval.MAJOR_THIRD), tone(Interval.PERFECT_FIFTH)),
    MINOR("m", tone(Interval.ROOT), tone(Interval.MINOR_THIRD), tone(Interval.PERFECT_FIFTH)),
    SEVENTH(
            "7",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    MINOR_SEVENTH(
            "m7",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    MAJOR_SEVENTH(
            "maj7",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_SEVENTH)),
    SIXTH(
            "6",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_SIXTH)),
    MINOR_SIXTH(
            "m6",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_SIXTH)),
    SUS2("sus2", tone(Interval.ROOT), tone(Interval.MAJOR_SECOND), tone(Interval.PERFECT_FIFTH)),
    SUS4("sus4", tone(Interval.ROOT), tone(Interval.PERFECT_FOURTH), tone(Interval.PERFECT_FIFTH)),
    NINTH(
            "9",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            tone(Interval.MAJOR_NINTH)),
    MINOR_NINTH(
            "m9",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            tone(Interval.MAJOR_NINTH)),
    MAJOR_NINTH(
            "maj9",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_SEVENTH),
            tone(Interval.MAJOR_NINTH)),
    ELEVENTH(
            "11",
            tone(Interval.ROOT),
            optional(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            optional(Interval.MAJOR_NINTH),
            tone(Interval.PERFECT_ELEVENTH)),
    THIRTEENTH(
            "13",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            optional(Interval.MAJOR_NINTH),
            optional(Interval.PERFECT_ELEVENTH),
            tone(Interval.MAJOR_THIRTEENTH)),
    DIMINISHED("dim", tone(Interval.ROOT), tone(Interval.MINOR_THIRD), tone(Interval.DIMINISHED_FIFTH)),
    DIMINISHED_SEVENTH(
            "dim7",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            tone(Interval.DIMINISHED_FIFTH),
            tone(Interval.DIMINISHED_SEVENTH)),
    AUGMENTED("aug", tone(Interval.ROOT), tone(Interval.MAJOR_THIRD), tone(Interval.AUGMENTED_FIFTH)),
    FIVE("5", tone(Interval.ROOT), tone(Interval.PERFECT_FIFTH)),
    ADD9(
            "add9",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_NINTH)),
    SEVEN_SUS4(
            "7sus4",
            tone(Interval.ROOT),
            tone(Interval.PERFECT_FOURTH),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    SEVEN_FLAT_FIVE(
            "7b5",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            tone(Interval.DIMINISHED_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    SEVEN_SHARP_FIVE(
            "7#5",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            tone(Interval.AUGMENTED_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    SEVEN_FLAT_NINE(
            "7b9",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            tone(Interval.MINOR_NINTH)),
    SEVEN_SHARP_NINE(
            "7#9",
            tone(Interval.ROOT),
            tone(Interval.MAJOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MINOR_SEVENTH),
            tone(Interval.AUGMENTED_NINTH)),
    MINOR_SEVENTH_FLAT_FIVE(
            "m7b5",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            tone(Interval.DIMINISHED_FIFTH),
            tone(Interval.MINOR_SEVENTH)),
    MINOR_MAJOR_SEVENTH(
            "mMaj7",
            tone(Interval.ROOT),
            tone(Interval.MINOR_THIRD),
            optional(Interval.PERFECT_FIFTH),
            tone(Interval.MAJOR_SEVENTH));

    private final String suffix;
    private final List<ChordTone> tones;

    ChordType(String suffix, ChordTone... tones) {
        this.suffix = suffix;
        this.tones = List.of(tones);
    }

    private static ChordTone tone(Interval interval) {
        return new ChordTone(interval, true);
    }

    private static ChordTone optional(Interval interval) {
        return new ChordTone(interval, false);
    }

    /** El sufijo con que Guitar Pro nombra este tipo de acorde: "m7", "sus4", "dim7"... */
    public String suffix() {
        return suffix;
    }

    /** La formula del acorde: que intervalos lo forman y cuales son imprescindibles. */
    public List<ChordTone> tones() {
        return tones;
    }
}
