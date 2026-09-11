package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.DefaultNames;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads a chord diagram. GP3 stores the name fields as four-byte integers and six
 * frets; from GP4 on they narrow to one byte, store seven frets even when the track
 * has fewer strings, and add the fingering.
 */
final class GuitarProChordReader {

    private static final int NAME_FIELD_SIZE = 22;
    private static final int OMITTED_DEGREES = 7;

    private static final int GP3_STRING_SLOTS = 6;
    private static final int GP3_BARRE_SLOTS = 2;

    private static final int STRING_SLOTS = 7;
    private static final int BARRE_SLOTS = 5;

    private final DefaultNames names;

    GuitarProChordReader(DefaultNames names) {
        this.names = names;
    }

    ChordDiagram read(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        ChordDiagram diagram = reader.readBoolean() ? readNewFormat(reader, version) : readOldFormat(reader);
        return onlyTheStringsOfTheTrack(diagram, stringCount);
    }

    /**
     * The old format: name, base fret, and frets. A base fret of zero says the chord is
     * just a name with no diagram: no frets follow in that case.
     */
    private ChordDiagram readOldFormat(GuitarProByteReader reader) {
        String name = reader.readLengthPrefixedString();
        int baseFret = reader.readInt();
        List<Integer> frets = baseFret == 0
                ? Collections.nCopies(GP3_STRING_SLOTS, ChordDiagram.MUTED)
                : readFrets(reader, GP3_STRING_SLOTS);
        return chordOf(name, baseFret, frets, List.of());
    }

    private ChordDiagram readNewFormat(GuitarProByteReader reader, GuitarProVersion version) {
        boolean narrowFields = version.hasSecondFlagsByte();
        reader.readBoolean(); // sharp: notation preference, not fingering.
        reader.skip(3);
        readNumber(reader, narrowFields); // root note
        readNumber(reader, narrowFields); // chord type (major, m7, sus4...)
        readNumber(reader, narrowFields); // extension (ninth, eleventh, thirteenth)
        reader.readInt(); // bass note
        reader.readInt(); // key
        reader.readBoolean(); // add
        String name = reader.readFixedString(NAME_FIELD_SIZE);
        readNumber(reader, narrowFields); // fifth alteration
        readNumber(reader, narrowFields); // ninth alteration
        readNumber(reader, narrowFields); // eleventh alteration
        int baseFret = reader.readInt();
        List<Integer> frets = readFrets(reader, narrowFields ? STRING_SLOTS : GP3_STRING_SLOTS);
        readBarres(reader, narrowFields);
        reader.skip(OMITTED_DEGREES); // which degrees are omitted
        reader.skip(1);
        if (!narrowFields) {
            return chordOf(name, baseFret, frets, List.of());
        }
        List<Finger> fingering = readFingering(reader);
        reader.readBoolean(); // whether the fingering shows in the diagram
        return chordOf(name, baseFret, frets, fingering);
    }

    /** The name fields are integers in GP3 and bytes from GP4 on. */
    private static int readNumber(GuitarProByteReader reader, boolean narrow) {
        return narrow ? reader.readSignedByte() : reader.readInt();
    }

    private static void readBarres(GuitarProByteReader reader, boolean narrow) {
        int slots = narrow ? BARRE_SLOTS : GP3_BARRE_SLOTS;
        readNumber(reader, narrow); // how many barres
        for (int part = 0; part < 3; part++) { // fret, start string and end string of each one
            for (int slot = 0; slot < slots; slot++) {
                readNumber(reader, narrow);
            }
        }
    }

    private static List<Integer> readFrets(GuitarProByteReader reader, int slots) {
        List<Integer> frets = new ArrayList<>(slots);
        for (int slot = 0; slot < slots; slot++) {
            frets.add(reader.readInt());
        }
        return frets;
    }

    /**
     * Finger codes arrive in string order; as soon as an unfingered one appears the
     * list is cut short so it does not leave gaps, which ChordDiagram does not allow.
     */
    private static List<Finger> readFingering(GuitarProByteReader reader) {
        List<Finger> fingering = new ArrayList<>();
        boolean stillContiguous = true;
        for (int slot = 0; slot < STRING_SLOTS; slot++) {
            int code = reader.readSignedByte();
            if (stillContiguous && code >= 0 && code < Finger.values().length) {
                fingering.add(Finger.values()[code]);
            } else {
                stillContiguous = false;
            }
        }
        return fingering;
    }

    /**
     * The file stores more strings than the instrument has: the diagram keeps only its own.
     */
    private static ChordDiagram onlyTheStringsOfTheTrack(ChordDiagram diagram, int stringCount) {
        if (diagram.stringCount() <= stringCount) {
            return diagram;
        }
        return new ChordDiagram(
                diagram.name(),
                diagram.baseFret(),
                diagram.frets().subList(0, stringCount),
                diagram.fingering().size() > stringCount
                        ? diagram.fingering().subList(0, stringCount)
                        : diagram.fingering(),
                diagram.shown());
    }

    private ChordDiagram chordOf(String name, int baseFret, List<Integer> frets, List<Finger> fingering) {
        String shownName = name == null || name.isBlank() ? names.chord() : name.strip();
        return new ChordDiagram(shownName, Math.max(1, baseFret), frets, fingering, true);
    }
}
