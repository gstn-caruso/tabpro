package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;

/**
 * Escribe un diagrama de acorde en el formato "nuevo" que usa GP4 en adelante (campos de
 * un byte, siete cuerdas, digitacion). El espejo de {@link GuitarProChordReader}.
 *
 * <p>El formato no distingue "solo mostrar el nombre" de "mostrar el diagrama completo":
 * todo acorde exportado vuelve a leerse con su diagrama visible, aunque
 * {@link ChordDiagram#shown()} sea falso.
 */
final class GuitarProChordWriter {

    private static final int NAME_FIELD_SIZE = 22;
    private static final int STRING_SLOTS = 7;
    private static final int BARRE_SLOTS = 5;
    private static final int OMITTED_DEGREES = 7;

    void write(GuitarProByteWriter writer, ChordDiagram chord) {
        writer.writeBoolean(true); // formato nuevo.
        writer.writeBoolean(false); // preferencia de notacion (sostenidos): no se modela.
        for (int i = 0; i < 3; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeSignedByte(0); // nota fundamental: no se modela.
        writer.writeSignedByte(0); // tipo de acorde: no se modela.
        writer.writeSignedByte(0); // extension: no se modela.
        writer.writeInt(0); // bajo: no se modela.
        writer.writeInt(0); // tonalidad: no se modela.
        writer.writeBoolean(false); // add: no se modela.
        writer.writeFixedString(chord.name(), NAME_FIELD_SIZE);
        writer.writeSignedByte(0); // alteracion de la quinta: no se modela.
        writer.writeSignedByte(0); // alteracion de la novena: no se modela.
        writer.writeSignedByte(0); // alteracion de la oncena: no se modela.
        writer.writeInt(chord.baseFret());
        writeFrets(writer, chord);
        writeBarres(writer);
        for (int i = 0; i < OMITTED_DEGREES; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeUnsignedByte(0);
        writeFingering(writer, chord.fingering());
        writer.writeBoolean(true); // se muestra el digitado en el diagrama.
    }

    private void writeFrets(GuitarProByteWriter writer, ChordDiagram chord) {
        for (int slot = 0; slot < STRING_SLOTS; slot++) {
            int fret = slot < chord.stringCount() ? chord.fretOfString(slot + 1) : ChordDiagram.MUTED;
            writer.writeInt(fret);
        }
    }

    private void writeBarres(GuitarProByteWriter writer) {
        writer.writeSignedByte(0); // cuantas cejillas: no se modela.
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
