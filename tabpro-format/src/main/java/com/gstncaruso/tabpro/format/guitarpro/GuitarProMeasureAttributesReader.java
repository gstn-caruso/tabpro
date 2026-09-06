package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un "master bar": los atributos de un compas que Guitar Pro guarda una
 * sola vez por compas (no por pista). La medida y la armadura solo aparecen
 * en el archivo cuando cambian; el resto del tiempo se arrastra la anterior.
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
    private final TripletFeel defaultTripletFeel;

    GuitarProMeasureAttributesReader(
            TimeSignature initialTimeSignature, KeySignature initialKeySignature, TripletFeel defaultTripletFeel) {
        this.timeSignature = initialTimeSignature;
        this.keySignature = initialKeySignature;
        this.defaultTripletFeel = defaultTripletFeel;
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

        MeasureAttributes attributes = new MeasureAttributes(
                keySignature, tail.tripletFeel(), doubleBar, repeatOpen, repeatCount, tail.alternateEndings(),
                java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.ofNullable(marker),
                LineBreak.AUTOMATIC);
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

    private List<Integer> readPreGp5AlternateEndings(GuitarProByteReader reader, GuitarProVersion version, int flags) {
        if (version.generation() >= 5 || (flags & FLAG_ALTERNATE_ENDINGS_PRE_GP5) == 0) {
            return List.of();
        }
        return endingsFromMask(reader.readUnsignedByte());
    }

    private Marker readMarker(GuitarProByteReader reader, int flags) {
        if ((flags & FLAG_MARKER) == 0) {
            return null;
        }
        String name = reader.readLengthPrefixedString();
        ScoreColor color = reader.readColor();
        return new Marker(name.isBlank() ? "Marcador" : name, color);
    }

    private KeySignature readKeySignature(GuitarProByteReader reader, int flags) {
        if ((flags & FLAG_KEY_SIGNATURE) == 0) {
            return keySignature;
        }
        return reader.readKeySignature();
    }

    /** Lo que Guitar Pro 5 agrega al final de cada master bar. */
    private Gp5MasterBarTail readGp5Tail(
            GuitarProByteReader reader, GuitarProVersion version, int flags, List<Integer> preGp5AlternateEndings) {
        if (version.generation() < 5) {
            return new Gp5MasterBarTail(preGp5AlternateEndings, defaultTripletFeel);
        }
        // Cuando cambia la medida vienen las cuatro cifras del agrupamiento de corcheas.
        if ((flags & (FLAG_NUMERATOR | FLAG_DENOMINATOR)) != 0) {
            reader.skip(4);
        }
        // Va un solo byte: la mascara de finales alternativos si los hay, o relleno si no.
        List<Integer> alternateEndings = (flags & FLAG_ALTERNATE_ENDINGS_PRE_GP5) != 0
                ? endingsFromMask(reader.readUnsignedByte())
                : skipped(reader);
        TripletFeel tripletFeel = tripletFeelOf(reader.readUnsignedByte());
        return new Gp5MasterBarTail(alternateEndings, tripletFeel);
    }

    private static List<Integer> skipped(GuitarProByteReader reader) {
        reader.skip(1);
        return List.of();
    }

    private static TripletFeel tripletFeelOf(int value) {
        TripletFeel[] values = TripletFeel.values();
        return value >= 0 && value < values.length ? values[value] : TripletFeel.NONE;
    }

    /** Los finales alternativos se guardan como mascara de bits: bit i habilita la vuelta i+1. */
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
