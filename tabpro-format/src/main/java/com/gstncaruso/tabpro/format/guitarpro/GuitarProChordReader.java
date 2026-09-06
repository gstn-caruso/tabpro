package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un diagrama de acorde. Guitar Pro 5 lo guarda con un layout fijo;
 * GP3/GP4 eligen entre un formato compacto (solo nombre y trastes) y uno
 * extendido segun un byte de bandera al principio.
 */
final class GuitarProChordReader {

    private static final int STRING_SLOTS = 7;
    private static final int BARRE_SLOTS = 5;

    ChordDiagram read(GuitarProByteReader reader, GuitarProVersion version) {
        return version.hasGp5ChordFormat() ? readGp5(reader) : readLegacy(reader);
    }

    private ChordDiagram readGp5(GuitarProByteReader reader) {
        reader.skip(17);
        String name = reader.readFixedString(21);
        reader.skip(4);
        int baseFret = reader.readInt();
        List<Integer> frets = readFrets(reader);
        reader.readUnsignedByte(); // cantidad de cejillas
        reader.skip(BARRE_SLOTS); // trastes de las cejillas: no tienen lugar en ChordDiagram.
        reader.skip(26);
        return chordOf(name, baseFret, frets, List.of());
    }

    private ChordDiagram readLegacy(GuitarProByteReader reader) {
        int formatFlag = reader.readUnsignedByte();
        return formatFlag == 0 ? readCompact(reader) : readExtended(reader);
    }

    /** El formato viejo de GP3: solo nombre, cejilla base y trastes. */
    private ChordDiagram readCompact(GuitarProByteReader reader) {
        String name = reader.readFixedString(20);
        int baseFret = reader.readInt();
        List<Integer> frets = new ArrayList<>(STRING_SLOTS);
        for (int i = 0; i < 6; i++) {
            frets.add(reader.readInt());
        }
        frets.add(ChordDiagram.MUTED);
        return chordOf(name, baseFret, frets, List.of());
    }

    private ChordDiagram readExtended(GuitarProByteReader reader) {
        reader.readBoolean(); // sharp: preferencia de notacion, no de digitado.
        reader.skip(3);
        reader.readSignedByte(); // nota fundamental
        reader.readSignedByte(); // tipo de acorde (mayor, m7, sus4...)
        reader.readSignedByte(); // novena/oncena/trecena
        reader.readInt(); // bajo
        reader.readInt(); // disminuido/aumentado
        reader.readBoolean(); // add
        String name = reader.readFixedString(20);
        reader.skip(2);
        reader.skip(3); // tonalidad de la quinta, novena y oncena
        int baseFret = reader.readInt();
        List<Integer> frets = readFrets(reader);
        reader.readUnsignedByte(); // cantidad de cejillas
        reader.skip(BARRE_SLOTS); // trastes de cejilla
        reader.skip(BARRE_SLOTS); // cuerda inicial de cada cejilla
        reader.skip(BARRE_SLOTS); // cuerda final de cada cejilla
        reader.skip(STRING_SLOTS); // que grados se omiten
        reader.skip(1);
        List<Finger> fingering = readFingering(reader);
        reader.readBoolean(); // si se muestra el digitado en el diagrama
        return chordOf(name, baseFret, frets, fingering);
    }

    private List<Integer> readFrets(GuitarProByteReader reader) {
        List<Integer> frets = new ArrayList<>(STRING_SLOTS);
        for (int i = 0; i < STRING_SLOTS; i++) {
            frets.add(reader.readInt());
        }
        return frets;
    }

    /**
     * Los codigos de dedo llegan en orden de cuerda; en cuanto aparece uno
     * sin digitar cortamos la lista para no dejar huecos, que ChordDiagram
     * no admite.
     */
    private List<Finger> readFingering(GuitarProByteReader reader) {
        List<Finger> fingering = new ArrayList<>();
        boolean stillContiguous = true;
        for (int i = 0; i < STRING_SLOTS; i++) {
            int code = reader.readSignedByte();
            if (stillContiguous && code >= 0 && code <= 4) {
                fingering.add(Finger.values()[code]);
            } else {
                stillContiguous = false;
            }
        }
        return fingering;
    }

    private ChordDiagram chordOf(String name, int baseFret, List<Integer> frets, List<Finger> fingering) {
        String safeName = name.isBlank() ? "Acorde" : name;
        int safeBaseFret = Math.max(1, baseFret);
        return new ChordDiagram(safeName, safeBaseFret, frets, fingering, true);
    }
}
