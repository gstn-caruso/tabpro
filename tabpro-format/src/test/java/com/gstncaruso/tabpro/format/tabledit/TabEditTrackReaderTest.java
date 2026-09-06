package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El encabezado de cada pista: su afinacion (en el mismo orden que Guitar Pro,
 * la cuerda 1 es la mas aguda), su nombre, su instrumento y si es percusion.
 */
class TabEditTrackReaderTest {

    private static final int MAX_TRACK_SIZE = 64;

    private final TabEditTrackReader reader = new TabEditTrackReader();

    @Test
    void leeLaAfinacionEnMidiYElNombreDeLaPista() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(1);
        // Afinacion estandar de guitarra, de la cuerda mas aguda a la mas grave: E4 B3 G3 D3 A2 E2.
        writeTrack(writer, 6, 25, 0, new int[] {64, 59, 55, 50, 45, 40}, "Guitarra");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(1, tracks.size());
        TabEditTrackHeader track = tracks.get(0);
        assertEquals("Guitarra", track.name());
        assertEquals(6, track.stringCount());
        assertEquals(List.of(64, 59, 55, 50, 45, 40), track.tuningMidiNumbers());
        assertEquals(25, track.midiInstrument());
        assertFalse(track.percussion());
    }

    @Test
    void leeElCapoYReconocePercusionPorElInstrumento96() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(2);
        writeTrack(writer, 6, 25, 3, new int[] {32, 37, 41, 46, 51, 56}, "Con cejilla");
        writeTrack(writer, 4, 96, 0, new int[] {60, 55, 50, 45}, "Bateria");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(3, tracks.get(0).capo());
        assertFalse(tracks.get(0).percussion());
        assertTrue(tracks.get(1).percussion());
    }

    @Test
    void variasPistasQuedanAlineadasCadaUnaEnSuBloque() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(2);
        writeTrack(writer, 4, 33, 0, new int[] {45, 50, 55, 60}, "Bajo");
        writeTrack(writer, 6, 25, 0, new int[] {32, 37, 41, 46, 51, 56}, "Guitarra 2");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals("Bajo", tracks.get(0).name());
        assertEquals(4, tracks.get(0).stringCount());
        assertEquals("Guitarra 2", tracks.get(1).name());
        assertEquals(6, tracks.get(1).stringCount());
    }

    private static void writeTrack(
            TabEditFileWriter writer, int stringCount, int midiInstrument, int capo, int[] tuningMidi,
            String name) {
        int start = writer.size();
        writer.writeUnsignedByte(stringCount);
        writer.padTo(start + 8);
        writer.writeUnsignedByte(midiInstrument);
        writer.padTo(start + 11);
        writer.writeUnsignedByte(0); // transposicion: no soportada
        writer.writeUnsignedByte(capo);
        writer.padTo(start + 14);
        writer.writeUnsignedByte(12); // middleCoffset crudo: no soportado
        writer.writeUnsignedByte(0); // clef/grandStaff/squareBracket: no soportado
        writer.writeUnsignedByte(0x30);
        writer.writeUnsignedByte(8); // pan
        writer.writeUnsignedByte(12); // volumen
        writer.writeUnsignedByte(0); // doubleStrings/letRing/pedalSteel/etc: no soportado
        for (int semitonesBelow96 : tuningMidi) {
            writer.writeUnsignedByte(96 - semitonesBelow96);
        }
        writer.padTo(start + 20 + 12);
        writer.writeNullTerminatedString(name);
        writer.padTo(start + MAX_TRACK_SIZE);
    }
}
