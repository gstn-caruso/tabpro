package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Lee el bloque de direcciones (Coda, Segno, Da Capo, etc.) que GP5 agrega a
 * la cabecera, justo despues de los canales MIDI.
 */
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

        assertEquals(Map.of(3, DirectionSymbol.CODA), directions.symbols());
        assertTrue(directions.jumps().isEmpty());
    }

    @Test
    void unSaltoSeAtaAlCompasQueIndicaSuSlot() {
        // el septimo slot (indice 6) es "Da Capo al Coda": los cinco simbolos de destino
        // ocupan los primeros cinco, y el salto Da Capo al Coda es el segundo de los saltos.
        GuitarProByteReader byteReader = new GuitarProByteReader(directionsBlock(Map.of(6, 5)));

        GuitarProDirections directions = reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(Map.of(5, DirectionJump.DA_CAPO_AL_CODA), directions.jumps());
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
        writer.writeInt(0); // los cuatro bytes reservados que siguen a las 19 direcciones
        writer.writeInt(42); // lo que viene despues del bloque: la cantidad de compases
        GuitarProByteReader byteReader = new GuitarProByteReader(writer.bytes());

        reader.readDirections(byteReader, GuitarProVersion.GP5_00);

        assertEquals(42, byteReader.readInt());
    }

    /**
     * La armadura de la cabecera es un entero con signo y nada mas: el modo mayor o menor
     * solo existe en los cambios de armadura por compas. Un entero negativo no puede
     * leerse como si su segundo byte fuera el modo, porque ahi vive el signo.
     */
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

    /** La cabecera GP4 mas chica que se puede armar, con la armadura que se le pida. */
    private static byte[] gp4HeaderWithKey(int accidentals) {
        GuitarProFileWriter writer = new GuitarProFileWriter();
        for (int field = 0; field < 8; field++) {
            writer.writeLengthPrefixedString("");
        }
        writer.writeInt(0); // lineas del aviso
        writer.writeBoolean(false); // triplet feel global
        writer.writeInt(1); // pista de la letra
        for (int line = 0; line < LyricLine.MAX_LINES; line++) {
            writer.writeInt(1);
            writer.writeIntPrefixedString("");
        }
        writer.writeInt(120); // tempo
        writer.writeInt(accidentals);
        writer.writeUnsignedByte(0); // octava
        return writer.bytes();
    }

    /** Arma un bloque de 19 slots en -1, salvo los indicados en {@code destinations} (slot -> compas). */
    private static byte[] directionsBlock(Map<Integer, Integer> destinations) {
        GuitarProFileWriter writer = new GuitarProFileWriter();
        for (int slot = 0; slot < 19; slot++) {
            writer.writeShort(destinations.getOrDefault(slot, -1));
        }
        writer.writeInt(0);
        return writer.bytes();
    }
}
