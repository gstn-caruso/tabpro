package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import org.junit.jupiter.api.Test;

/**
 * El encabezado de 256 bytes de un archivo TEF3: si no trae las marcas que
 * TablEdit exige, no es un archivo de este formato y no hay que arriesgar una
 * lectura a ciegas.
 */
class TabEditHeaderReaderTest {

    private final TabEditHeaderReader reader = new TabEditHeaderReader();

    @Test
    void leeElTempoInicialYLasBanderasDeSeccionesOpcionales() {
        TabEditByteReader input = new TabEditByteReader(TabEditFixtures.minimalHeader(140).bytes());

        TabEditHeader header = reader.read(input);

        assertEquals(140, header.initialBpm());
        assertFalse(header.hasChords());
        assertFalse(header.hasTextEvents());
        assertFalse(header.hasReadingList());
        assertFalse(header.hasUrl());
        assertFalse(header.hasCopyright());
        assertEquals(256, input.position());
    }

    @Test
    void reconoceLasBanderasDeSeccionesOpcionalesPresentes() {
        TabEditFileWriter writer = TabEditFixtures.minimalHeader(120);
        byte[] bytes = writer.bytes();
        writeIntAt(bytes, 84, 1); // posOfTextEvents
        writeIntAt(bytes, 88, 1); // posOfChords
        writeIntAt(bytes, 128, 1); // posOfReadingList
        writeIntAt(bytes, 132, 1); // posOfUrl
        writeIntAt(bytes, 140, 1); // posOfCopyright

        TabEditHeader header = reader.read(new TabEditByteReader(bytes));

        assertTrue(header.hasChords());
        assertTrue(header.hasTextEvents());
        assertTrue(header.hasReadingList());
        assertTrue(header.hasUrl());
        assertTrue(header.hasCopyright());
    }

    @Test
    void unArchivoQueNoEsTablEditFallaConMensajeClaro() {
        TabEditByteReader input = new TabEditByteReader(new byte[256]);

        ScoreFileException exception = assertThrows(ScoreFileException.class, () -> reader.read(input));

        assertTrue(exception.getMessage().contains("TablEdit"));
    }

    @Test
    void unArchivoDemasiadoCortoFallaConMensajeClaro() {
        TabEditByteReader input = new TabEditByteReader(new byte[10]);

        assertThrows(ScoreFileException.class, () -> reader.read(input));
    }

    private static void writeIntAt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
        bytes[offset + 2] = (byte) (value >> 16);
        bytes[offset + 3] = (byte) (value >> 24);
    }
}
