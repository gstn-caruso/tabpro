package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.DefaultNames;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a "master bar": the attributes of a measure that Guitar Pro stores once per
 * measure (not per track). The time signature and key signature only appear in the
 * file when they change; the rest of the time the previous one carries over.
 */
final class GuitarProMeasureAttributesReader {

    private static final int FLAG_NUMERATOR = 0x01;
    private static final int FLAG_DENOMINATOR = 0x02;
    private static final int FLAG_REPEAT_OPEN = 0x04;
    private static final int FLAG_REPEAT_COUNT = 0x08;
    private static final int FLAG_ALTERNATE_ENDINGS_PRE_GP5 = 0x10;
    private static final int FLAG_MARKER = 0x20;
    private static final int FLAG_KEY_SIGNATURE = 0x40;
    private static final int FLAG_DOUBLE_BAR = 0x80;

    private TimeSignature timeSignature;
    private KeySignature keySignature;
    private int roundsAlreadyTaken;
    private final TripletFeel defaultTripletFeel;
    private final DefaultNames names;

    GuitarProMeasureAttributesReader(
            TimeSignature initialTimeSignature, KeySignature initialKeySignature, TripletFeel defaultTripletFeel,
            DefaultNames names) {
        this.timeSignature = initialTimeSignature;
        this.keySignature = initialKeySignature;
        this.defaultTripletFeel = defaultTripletFeel;
        this.names = names;
    }

    GuitarProMasterBar read(GuitarProByteReader reader, GuitarProVersion version, boolean isFirstMeasure) {
        if (version.generation() >= 5 && !isFirstMeasure) {
            reader.skip(1);
        }
        int flags = reader.readUnsignedByte();
        timeSignature = readTimeSignature(reader, flags);
        boolean repeatOpen = (flags & FLAG_REPEAT_OPEN) != 0;
        int repeatCount = readRepeatCount(reader, version, flags);
        List<Integer> preGp5AlternateEndings = readPreGp5AlternateEndings(reader, version, flags);
        Marker marker = readMarker(reader, flags);
        keySignature = readKeySignature(reader, flags);
        boolean doubleBar = (flags & FLAG_DOUBLE_BAR) != 0;
        Gp5MasterBarTail tail = readGp5Tail(reader, version, flags, preGp5AlternateEndings);
        rememberRoundsOf(repeatOpen, tail.alternateEndings());

        MeasureAttributes attributes = new MeasureAttributes(
                keySignature, tail.tripletFeel(), doubleBar, repeatOpen, repeatCount, tail.alternateEndings(),
                java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.ofNullable(marker),
                // Guitar Pro's master bar carries no line break: that is a tabpro view
                // concern, not part of the score this format stores.
                LineBreak.AUTOMATIC,
                // Nor does it carry an octave mark in the master bar -- in real Guitar Pro it
                // lives on the beat/note (the effect's "ottava" property), and this reader does
                // not read that part of the format yet.
                OctaveMark.NONE);
        return new GuitarProMasterBar(timeSignature, attributes);
    }

    private TimeSignature readTimeSignature(GuitarProByteReader reader, int flags) {
        int beats = timeSignature.beats();
        int beatUnit = timeSignature.beatUnit();
        if ((flags & FLAG_NUMERATOR) != 0) {
            beats = reader.readUnsignedByte();
        }
        if ((flags & FLAG_DENOMINATOR) != 0) {
            beatUnit = reader.readUnsignedByte();
        }
        return new TimeSignature(Math.max(1, beats), nearestPowerOfTwo(beatUnit));
    }

    private static int nearestPowerOfTwo(int value) {
        if (value < 1) {
            return 4;
        }
        int power = Integer.highestOneBit(value);
        return Math.min(64, power);
    }

    private int readRepeatCount(GuitarProByteReader reader, GuitarProVersion version, int flags) {
        if ((flags & FLAG_REPEAT_COUNT) == 0) {
            return 0;
        }
        int raw = reader.readUnsignedByte();
        return raw + version.repeatCountOffset();
    }

    /**
     * Up to GP4 the byte does not say which rounds this measure plays but up to which one
     * it reaches: a 2 means "rounds 1 and 2". From that the ones already taken by earlier
     * measures of the same repeat must be subtracted, which is what {@link #roundsAlreadyTaken} does.
     */
    private List<Integer> readPreGp5AlternateEndings(GuitarProByteReader reader, GuitarProVersion version, int flags) {
        if (version.generation() >= 5 || (flags & FLAG_ALTERNATE_ENDINGS_PRE_GP5) == 0) {
            return List.of();
        }
        int lastRound = Math.clamp(reader.readUnsignedByte(), 0, MeasureAttributes.MAX_ALTERNATE_ENDINGS);
        return endingsFromMask(((1 << lastRound) - 1) & ~roundsAlreadyTaken);
    }

    /** The rounds already taken by earlier measures; a new repeat forgets them. */
    private void rememberRoundsOf(boolean repeatOpen, List<Integer> endings) {
        if (repeatOpen) {
            roundsAlreadyTaken = 0;
            return;
        }
        for (int round : endings) {
            roundsAlreadyTaken |= 1 << (round - 1);
        }
    }

    private Marker readMarker(GuitarProByteReader reader, int flags) {
        if ((flags & FLAG_MARKER) == 0) {
            return null;
        }
        String name = reader.readLengthPrefixedString();
        ScoreColor color = reader.readColor();
        return new Marker(name.isBlank() ? names.marker() : name, color);
    }

    private KeySignature readKeySignature(GuitarProByteReader reader, int flags) {
        if ((flags & FLAG_KEY_SIGNATURE) == 0) {
            return keySignature;
        }
        return reader.readKeySignature();
    }

    private Gp5MasterBarTail readGp5Tail(
            GuitarProByteReader reader, GuitarProVersion version, int flags, List<Integer> preGp5AlternateEndings) {
        if (version.generation() < 5) {
            return new Gp5MasterBarTail(preGp5AlternateEndings, defaultTripletFeel);
        }
        // The alternate ending comes before the beam grouping, not after.
        List<Integer> alternateEndings = List.of();
        if ((flags & FLAG_ALTERNATE_ENDINGS_PRE_GP5) != 0) {
            alternateEndings = endingsFromMask(reader.readUnsignedByte());
        }
        // When the time signature changes, the four beam-grouping figures follow.
        if ((flags & (FLAG_NUMERATOR | FLAG_DENOMINATOR)) != 0) {
            reader.skip(4);
        }
        // And where there is no alternate ending, a padding byte takes its place.
        if ((flags & FLAG_ALTERNATE_ENDINGS_PRE_GP5) == 0) {
            reader.skip(1);
        }
        TripletFeel tripletFeel = tripletFeelOf(reader.readUnsignedByte());
        return new Gp5MasterBarTail(alternateEndings, tripletFeel);
    }

    private static TripletFeel tripletFeelOf(int value) {
        TripletFeel[] values = TripletFeel.values();
        return value >= 0 && value < values.length ? values[value] : TripletFeel.NONE;
    }

    /** Alternate endings are stored as a bitmask: bit i enables round i+1. */
    private static List<Integer> endingsFromMask(int mask) {
        List<Integer> endings = new ArrayList<>();
        for (int pass = 1; pass <= MeasureAttributes.MAX_ALTERNATE_ENDINGS; pass++) {
            if ((mask & (1 << (pass - 1))) != 0) {
                endings.add(pass);
            }
        }
        return endings;
    }

    private record Gp5MasterBarTail(List<Integer> alternateEndings, TripletFeel tripletFeel) {
    }
}
