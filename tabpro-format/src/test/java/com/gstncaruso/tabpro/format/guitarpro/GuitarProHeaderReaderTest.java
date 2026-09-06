package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
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
