package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import org.junit.jupiter.api.Test;

/**
 * Los primitivos de bajo nivel del formato .tef de TablEdit: todo little
 * endian, con dos sabores de string y bloques de tamano fijo que se leen como
 * un sub-arreglo independiente.
 */
class TabEditByteReaderTest {

    @Test
    void leeBytesConYSinSigno() {
        TabEditByteReader reader =
                new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(200).writeSignedByte(-56).bytes());

        assertEquals(200, reader.readUnsignedByte());
        assertEquals(-56, reader.readSignedByte());
    }

    @Test
    void leeUnShortSinSignoLittleEndian() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeShort(0xFFEE).bytes());

        assertEquals(0xFFEE, reader.readUnsignedShort());
    }

    @Test
    void leeUnIntConSignoLittleEndian() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(-1).writeInt(305419896).bytes());

        assertEquals(-1, reader.readInt());
        assertEquals(305419896, reader.readInt());
    }

    @Test
    void saltaLaCantidadDeBytesPedida() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(1).writeInt(2).bytes());

        reader.skip(4);

        assertEquals(2, reader.readInt());
    }

    @Test
    void leeUnBloqueDeTamanoFijoComoSubArreglo() {
        TabEditByteReader reader =
                new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(1).writeUnsignedByte(2).writeUnsignedByte(3).bytes());

        byte[] block = reader.readBlock(2);

        assertArrayEquals(new byte[] {1, 2}, block);
        assertEquals(3, reader.readUnsignedByte());
    }

    @Test
    void leeUnStringConPrefijoDeLargoCorto() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeShortString("Cancion").bytes());

        assertEquals("Cancion", reader.readShortString());
    }

    @Test
    void unStringConPrefijoDeLargoCortoSeCortaEnElPrimerNulo() {
        // El largo declarado dice 10, pero adentro hay un byte nulo antes de terminar: TablEdit
        // corta ahi mismo y deja el cursor justo despues del nulo, sin consumir el resto del
        // campo declarado. Es el comportamiento real del lector de referencia, no uno ideal.
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(10);
        writer.writeUnsignedByte('h').writeUnsignedByte('o').writeUnsignedByte('l').writeUnsignedByte('a');
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(99);
        TabEditByteReader reader = new TabEditByteReader(writer.bytes());

        assertEquals("hola", reader.readShortString());
        assertEquals(99, reader.readUnsignedByte());
    }

    @Test
    void leeUnStringTerminadoEnNuloDentroDeUnMaximo() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writer.writeUnsignedByte('h').writeUnsignedByte('i').writeUnsignedByte(0).writeUnsignedByte(7);
        TabEditByteReader reader = new TabEditByteReader(writer.bytes());

        assertEquals("hi", reader.readNullTerminatedString(256));
        assertEquals(7, reader.readUnsignedByte());
    }

    @Test
    void informaSiQuedanBytesPorLeer() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(1).bytes());

        assertTrue(reader.hasMore());
        reader.readUnsignedByte();
        assertFalse(reader.hasMore());
    }

    @Test
    void informaCuantosBytesQuedanPorLeer() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(1).writeInt(2).bytes());

        assertEquals(8, reader.remaining());
        reader.readInt();
        assertEquals(4, reader.remaining());
    }

    @Test
    void unArchivoTruncadoFallaConMensajeClaro() {
        TabEditByteReader reader = new TabEditByteReader(new byte[] {1, 2});

        ScoreFileException exception = assertThrows(ScoreFileException.class, reader::readInt);

        assertTrue(exception.getMessage().contains("truncado"));
    }
}
