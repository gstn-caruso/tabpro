package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un diagrama de acorde. GP3 guarda los campos del nombre como enteros de
 * cuatro bytes y seis trastes; de GP4 en adelante los angosta a un byte, guarda
 * siete trastes aunque la pista tenga menos cuerdas, y agrega la digitacion.
 */
final class GuitarProChordReader {

    private static final int NAME_FIELD_SIZE = 22;
    private static final int OMITTED_DEGREES = 7;

    private static final int GP3_STRING_SLOTS = 6;
    private static final int GP3_BARRE_SLOTS = 2;

    private static final int STRING_SLOTS = 7;
    private static final int BARRE_SLOTS = 5;

    ChordDiagram read(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        ChordDiagram diagram = reader.readBoolean() ? readNewFormat(reader, version) : readOldFormat(reader);
        return onlyTheStringsOfTheTrack(diagram, stringCount);
    }

    /** El formato viejo: solo nombre, cejilla base y trastes. */
    private ChordDiagram readOldFormat(GuitarProByteReader reader) {
        String name = reader.readFixedString(20);
        int baseFret = reader.readInt();
        List<Integer> frets = new ArrayList<>(GP3_STRING_SLOTS);
        for (int slot = 0; slot < GP3_STRING_SLOTS; slot++) {
            frets.add(reader.readInt());
        }
        return chordOf(name, baseFret, frets, List.of());
    }

    private ChordDiagram readNewFormat(GuitarProByteReader reader, GuitarProVersion version) {
        boolean narrowFields = version.hasSecondFlagsByte();
        reader.readBoolean(); // sharp: preferencia de notacion, no de digitado.
        reader.skip(3);
        readNumber(reader, narrowFields); // nota fundamental
        readNumber(reader, narrowFields); // tipo de acorde (mayor, m7, sus4...)
        readNumber(reader, narrowFields); // extension (novena, oncena, trecena)
        reader.readInt(); // bajo
        reader.readInt(); // tonalidad
        reader.readBoolean(); // add
        String name = reader.readFixedString(NAME_FIELD_SIZE);
        readNumber(reader, narrowFields); // alteracion de la quinta
        readNumber(reader, narrowFields); // alteracion de la novena
        readNumber(reader, narrowFields); // alteracion de la oncena
        int baseFret = reader.readInt();
        List<Integer> frets = readFrets(reader, narrowFields ? STRING_SLOTS : GP3_STRING_SLOTS);
        readBarres(reader, narrowFields);
        reader.skip(OMITTED_DEGREES); // que grados se omiten
        reader.skip(1);
        if (!narrowFields) {
            return chordOf(name, baseFret, frets, List.of());
        }
        List<Finger> fingering = readFingering(reader);
        reader.readBoolean(); // si se muestra el digitado en el diagrama
        return chordOf(name, baseFret, frets, fingering);
    }

    /** Los campos del nombre son enteros en GP3 y bytes de GP4 en adelante. */
    private static int readNumber(GuitarProByteReader reader, boolean narrow) {
        return narrow ? reader.readSignedByte() : reader.readInt();
    }

    private static void readBarres(GuitarProByteReader reader, boolean narrow) {
        int slots = narrow ? BARRE_SLOTS : GP3_BARRE_SLOTS;
        readNumber(reader, narrow); // cuantas cejillas
        for (int part = 0; part < 3; part++) { // traste, cuerda inicial y cuerda final de cada una
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
     * Los codigos de dedo llegan en orden de cuerda; en cuanto aparece uno
     * sin digitar cortamos la lista para no dejar huecos, que ChordDiagram
     * no admite.
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
     * El archivo guarda mas cuerdas de las que el instrumento tiene: el diagrama
     * se queda con las suyas.
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

    private static ChordDiagram chordOf(String name, int baseFret, List<Integer> frets, List<Finger> fingering) {
        String shownName = name == null || name.isBlank() ? "Acorde" : name.strip();
        return new ChordDiagram(shownName, Math.max(1, baseFret), frets, fingering, true);
    }
}
