package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;

/**
 * Writes a chord diagram in the "new" format used from GP4 on (one-byte fields, seven
 * strings, fingering).
 *
 * <p>The format does not distinguish "show only the name" from "show the full diagram":
 * every exported chord reads back with its diagram visible, even when
 * {@link ChordDiagram#shown()} is false.
 */
final class GuitarProChordWriter {

    private static final int NAME_FIELD_SIZE = 22;
    private static final int STRING_SLOTS = 7;
    private static final int BARRE_SLOTS = 5;
    private static final int OMITTED_DEGREES = 7;

    void write(GuitarProByteWriter writer, ChordDiagram chord) {
        writer.writeBoolean(true); // new format.
        writer.writeBoolean(false); // sharp preference: not modeled.
        for (int i = 0; i < 3; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeSignedByte(0); // root note: not modeled.
        writer.writeSignedByte(0); // chord type: not modeled.
        writer.writeSignedByte(0); // extension: not modeled.
        writer.writeInt(0); // bass note: not modeled.
        writer.writeInt(0); // key: not modeled.
        writer.writeBoolean(false); // add: not modeled.
        writer.writeFixedString(chord.name(), NAME_FIELD_SIZE);
        writer.writeSignedByte(0); // fifth alteration: not modeled.
        writer.writeSignedByte(0); // ninth alteration: not modeled.
        writer.writeSignedByte(0); // eleventh alteration: not modeled.
        writer.writeInt(chord.baseFret());
        writeFrets(writer, chord);
        writeBarres(writer);
        for (int i = 0; i < OMITTED_DEGREES; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeUnsignedByte(0);
        writeFingering(writer, chord.fingering());
        writer.writeBoolean(true); // fingering shows in the diagram.
    }

    private void writeFrets(GuitarProByteWriter writer, ChordDiagram chord) {
        for (int slot = 0; slot < STRING_SLOTS; slot++) {
            int fret = slot < chord.stringCount() ? chord.fretOfString(slot + 1) : ChordDiagram.MUTED;
            writer.writeInt(fret);
        }
    }

    private void writeBarres(GuitarProByteWriter writer) {
        writer.writeSignedByte(0); // how many barres: not modeled.
        for (int part = 0; part < 3; part++) {
            for (int slot = 0; slot < BARRE_SLOTS; slot++) {
                writer.writeSignedByte(0);
            }
        }
    }

    private void writeFingering(GuitarProByteWriter writer, List<Finger> fingering) {
        for (int slot = 0; slot < STRING_SLOTS; slot++) {
            Finger finger = slot < fingering.size() ? fingering.get(slot) : null;
            writer.writeSignedByte(finger == null ? -1 : finger.ordinal());
        }
    }
}
