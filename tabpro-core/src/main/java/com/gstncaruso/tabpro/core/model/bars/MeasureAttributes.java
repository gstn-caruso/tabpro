package com.gstncaruso.tabpro.core.model.bars;

import java.util.List;
import java.util.Optional;

/** Todo lo que un compas dice ademas de sus notas: armadura, repeticiones, direcciones. */
public record MeasureAttributes(
        KeySignature keySignature,
        TripletFeel tripletFeel,
        boolean doubleBar,
        boolean repeatOpen,
        int repeatCount,
        List<Integer> alternateEndings,
        Optional<DirectionSymbol> symbol,
        Optional<DirectionJump> jump,
        Optional<Marker> marker,
        LineBreak lineBreak,
        OctaveMark octaveMark) {

    /** Cuantas veces distintas puede pasar la repeticion por un final alternativo. */
    public static final int MAX_ALTERNATE_ENDINGS = 8;

    private static final MeasureAttributes PLAIN = new MeasureAttributes(
            KeySignature.cMajor(), TripletFeel.NONE, false, false, 0, List.of(),
            Optional.empty(), Optional.empty(), Optional.empty(), LineBreak.AUTOMATIC, OctaveMark.NONE);

    public MeasureAttributes {
        if (repeatCount < 0) {
            throw new IllegalArgumentException("repeatCount debe ser >= 0: " + repeatCount);
        }
        alternateEndings = alternateEndings.stream().distinct().sorted().toList();
    }

    public static MeasureAttributes plain() {
        return PLAIN;
    }

    public boolean isPlain() {
        return equals(PLAIN);
    }

    public boolean repeatCloses() {
        return repeatCount > 0;
    }

    public boolean hasAlternateEndings() {
        return !alternateEndings.isEmpty();
    }

    /** Si la vuelta numero tal de la repeticion tiene que tocar este compas. */
    public boolean playedOnPass(int pass) {
        return !hasAlternateEndings() || alternateEndings.contains(pass);
    }

    public MeasureAttributes withKeySignature(KeySignature keySignature) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withTripletFeel(TripletFeel tripletFeel) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withDoubleBar(boolean doubleBar) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withRepeatOpen(boolean repeatOpen) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withRepeatCount(int repeatCount) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withAlternateEndings(List<Integer> alternateEndings) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withSymbol(DirectionSymbol symbol) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, Optional.ofNullable(symbol), jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withJump(DirectionJump jump) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, Optional.ofNullable(jump), marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withMarker(Marker marker) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, Optional.ofNullable(marker), lineBreak, octaveMark);
    }

    public MeasureAttributes withLineBreak(LineBreak lineBreak) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }

    public MeasureAttributes withOctaveMark(OctaveMark octaveMark) {
        return new MeasureAttributes(keySignature, tripletFeel, doubleBar, repeatOpen, repeatCount, alternateEndings, symbol, jump, marker, lineBreak, octaveMark);
    }
}
