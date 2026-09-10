package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GuitarProHeaderReaderTest {

    private final GuitarProHeaderReader reader = new GuitarProHeaderReader();

    @Test
    void unFormatoSinDireccionesNoLeeNadaYNoTraeNinguna() {
        GuitarProByteReader empty = new GuitarProByteReader(new byte[0]);

        GuitarProDirections directions = reader.readDirections(empty, GuitarProVersion.GP3);

        assertTrue(directions.symbols().isEmpty());
        assertTrue(directions.jumps().isEmpty());
    }

    @Test
    void unSimboloDeDestinoSeAtaAlCompasQueIndicaSuSlot() {
        GuitarProByteReader byteReader = new GuitarProByteReader(directionsBlock(Map.of(0, 3)));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(Map.of(2, DirectionSymbol.CODA), directions.symbols());
        assertTrue(directions.jumps().isEmpty());
    }

    @Test
    void elCompasUnoEsElPrimeroDeLaPartitura() {
        GuitarProByteReader byteReader = new GuitarProByteReader(directionsBlock(Map.of(0, 1)));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(Map.of(0, DirectionSymbol.CODA), directions.symbols());
    }

    @Test
    void unSaltoSeAtaAlCompasQueIndicaSuSlot() {
        int daCapoAlCodaSlot = 6;
        int fifthMeasure = 5;
        GuitarProByteReader byteReader =
                new GuitarProByteReader(directionsBlock(Map.of(daCapoAlCodaSlot, fifthMeasure)));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(Map.of(4, DirectionJump.DA_CAPO_AL_CODA), directions.jumps());
        assertTrue(directions.symbols().isEmpty());
    }

    @Test
    void unSlotEnCeroNoAtaNada() {
        GuitarProByteReader byteReader = new GuitarProByteReader(directionsBlock(Map.of(0, 0)));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertTrue(directions.symbols().isEmpty());
    }

    @Test
    void unSlotEnMenosUnoNoAtaNada() {
        GuitarProByteReader byteReader = new GuitarProByteReader(directionsBlock(Map.of()));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertTrue(directions.symbols().isEmpty());
        assertTrue(directions.jumps().isEmpty());
    }

    @Test
    void dejaElReaderListoParaLoQueSigueDespuesDelBloque() {
        GuitarProFileWriter writer = new GuitarProFileWriter();
        for (int slot = 0; slot < 19; slot++) {
            writer.writeShort(-1);
        }
        writer.writeInt(0);
        writer.writeInt(42);
        GuitarProByteReader byteReader = new GuitarProByteReader(writer.bytes());

        reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(42, byteReader.readInt());
    }

    @Test
    void unaArmaduraConBemolesEnLaCabeceraSigueSiendoMayor() {
        GuitarProByteReader byteReader = new GuitarProByteReader(gp4HeaderWithKey(-3));

        GuitarProHeader header = reader.read(byteReader, GuitarProVersion.GP4);

        assertEquals(-3, header.keySignature().accidentals());
        assertEquals(Mode.MAJOR, header.keySignature().mode());
    }

    @Test
    void unaArmaduraConSostenidosEnLaCabeceraTambienEsMayor() {
        GuitarProByteReader byteReader = new GuitarProByteReader(gp4HeaderWithKey(4));

        GuitarProHeader header = reader.read(byteReader, GuitarProVersion.GP4);

        assertEquals(4, header.keySignature().accidentals());
        assertEquals(Mode.MAJOR, header.keySignature().mode());
    }

    private static byte[] gp4HeaderWithKey(int accidentals) {
        GuitarProFileWriter writer = new GuitarProFileWriter();
        for (int field = 0; field < 8; field++) {
            writer.writeLengthPrefixedString("");
        }
        writer.writeInt(0);
        writer.writeBoolean(false);
        writer.writeInt(1);
        for (int line = 0; line < LyricLine.MAX_LINES; line++) {
            writer.writeInt(1);
            writer.writeIntPrefixedString("");
        }
        writer.writeInt(120);
        writer.writeInt(accidentals);
        writer.writeUnsignedByte(0);
        return writer.bytes();
    }

    private static byte[] directionsBlock(Map<Integer, Integer> destinations) {
        GuitarProFileWriter writer = new GuitarProFileWriter();
        for (int slot = 0; slot < 19; slot++) {
            writer.writeShort(destinations.getOrDefault(slot, -1));
        }
        writer.writeInt(0);
        return writer.bytes();
    }
}
