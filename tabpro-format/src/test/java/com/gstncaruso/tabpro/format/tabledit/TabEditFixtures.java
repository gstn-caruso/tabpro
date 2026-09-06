package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

/**
 * Arma fixtures minimos de archivos TEF3 validos, para no repetir en cada test
 * los 256 bytes de encabezado que TablEdit exige. No son archivos de
 * TablEdit reales (no hay ninguno disponible para probar contra el):
 * describen a mano el mismo layout que entiende {@link TabEditByteReader}.
 */
final class TabEditFixtures {

    static final int HEADER_SIZE = 256;
    private static final int MAX_TRACK_SIZE = 64;

    private TabEditFixtures() {
    }

    /** Un encabezado TEF3 valido, con el tempo inicial pedido y ninguna seccion opcional. */
    static TabEditFileWriter minimalHeader(int initialBpm) {
        TabEditFileWriter writer = new TabEditFileWriter();
        writer.padTo(3);
        writer.writeUnsignedByte(3); // majorVersion
        writer.padTo(6);
        writer.writeShort(initialBpm);
        writer.padTo(56);
        writer.writeUnsignedByte('t').writeUnsignedByte('b').writeUnsignedByte('e').writeUnsignedByte('d');
        writer.padTo(202);
        writer.writeShort(4); // wOldNum
        writer.writeUnsignedByte(4); // wFormatLo
        writer.writeUnsignedByte(10); // wFormatHi
        writer.padTo(HEADER_SIZE);
        return writer;
    }

    /**
     * Una partitura completa y minima: una pista de guitarra en afinacion
     * estandar, un compas de 4/4 con las cuatro negras que le pidan.
     */
    static TabEditFileWriter oneTrackOneMeasureScore(String title, int bpm, String trackName, List<Integer> frets) {
        TabEditFileWriter writer = minimalHeader(bpm);
        writeSongMetadata(writer, title, "", "", "");
        writeOneMeasure(writer, 4, 4);
        writeOneTrack(writer, 6, 25, 0, 8, 12, new int[] {64, 59, 55, 50, 45, 40}, trackName);
        writePrintMetadata(writer);
        writeNotesEveryQuarter(writer, 6, frets);
        writer.writeInt(-1); // pie del archivo
        return writer;
    }

    /** Una partitura de una sola pista de percusion, para probar que se la rechaza. */
    static byte[] scoreWithPercussionTrack() {
        TabEditFileWriter writer = minimalHeader(120);
        writeSongMetadata(writer, "Con bateria", "", "", "");
        writeOneMeasure(writer, 4, 4);
        writeOneTrack(writer, 4, 96 /* instrumento MIDI de percusion en TablEdit */, 0, 8, 8,
                new int[] {49, 41, 32, 42}, "Bateria");
        writePrintMetadata(writer);
        writer.writeInt(-1);
        return writer.bytes();
    }

    private static void writeSongMetadata(TabEditFileWriter writer, String title, String author, String comments, String notes) {
        writer.writeShortString(title);
        writer.writeShortString(author);
        writer.writeShortString(comments);
        writer.writeShortString(notes);
        writer.writeShortString(""); // letra completa: sin pistas
    }

    private static void writeOneMeasure(TabEditFileWriter writer, int numerator, int denominator) {
        writer.writeShort(12); // sizeOfMeasure (8) + 4
        writer.writeShort(1); // measureCount
        writer.writeInt(0);
        writer.writeUnsignedByte(0); // banderas: nada especial, modo mayor
        writer.writeUnsignedByte(0);
        writer.writeSignedByte(0); // armadura: Do mayor
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(denominator);
        writer.writeUnsignedByte(numerator);
        writer.writeUnsignedByte(0); // ancho de relleno a la izquierda
        writer.writeUnsignedByte(0);
    }

    private static void writeOneTrack(
            TabEditFileWriter writer, int stringCount, int midiInstrument, int capo, int pan, int volume,
            int[] tuningMidi, String name) {
        writer.writeShort(MAX_TRACK_SIZE);
        writer.writeShort(1); // trackCount
        int start = writer.size();
        writer.writeUnsignedByte(stringCount);
        writer.padTo(start + 8);
        writer.writeUnsignedByte(midiInstrument);
        writer.padTo(start + 11);
        writer.writeUnsignedByte(0); // transposicion
        writer.writeUnsignedByte(capo);
        writer.padTo(start + 14);
        writer.writeUnsignedByte(12); // desplazamiento del Do central
        writer.writeUnsignedByte(0); // clave/gran pentagrama/corchete
        writer.writeUnsignedByte(0x30);
        writer.writeUnsignedByte(pan);
        writer.writeUnsignedByte(volume);
        writer.writeUnsignedByte(0); // doble cuerda/let ring/pedal steel/pista de ritmo
        for (int midi : tuningMidi) {
            writer.writeUnsignedByte(96 - midi);
        }
        writer.padTo(start + 20 + 12);
        writer.writeNullTerminatedString(name);
        writer.padTo(start + MAX_TRACK_SIZE);
    }

    private static void writePrintMetadata(TabEditFileWriter writer) {
        int printDataLength = 4;
        writer.writeUnsignedByte(printDataLength);
        writer.writeUnsignedByte(1);
        writer.padTo(writer.size() + printDataLength); // cadena vacia + relleno
        int maxHeaderSize = 128;
        writer.padTo(writer.size() + maxHeaderSize); // primer encabezado de pagina: vacio
        writer.padTo(writer.size() + maxHeaderSize); // segundo encabezado de pagina: vacio
    }

    /** Una nota por cada negra del compas de 4/4, en la sexta cuerda de la unica pista. */
    private static void writeNotesEveryQuarter(TabEditFileWriter writer, int durationCode, List<Integer> frets) {
        int valuePerPosition = 32 * 6; // una sola pista de 6 cuerdas
        int gridPosition = 0;
        for (int fret : frets) {
            writer.writeInt(gridPosition * valuePerPosition + 5 * 8); // cuerda 5 (0-based): la sexta cuerda
            writer.writeUnsignedByte(fret + 1); // tipo: traste+1, sin nota de adorno
            writer.writeUnsignedByte(durationCode); // duracion; dinamica FFF
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            gridPosition += 4; // una negra son 4 lugares de la grilla de dieciseisavos
        }
    }
}
