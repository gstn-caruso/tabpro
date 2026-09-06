package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Los metadatos de la cancion: titulo, autor, comentarios y notas siempre
 * estan; la url y el copyright son opcionales segun el encabezado. La letra
 * completa y los eventos de texto se consumen para no perder la alineacion,
 * pero tabpro todavia no los traduce a su propio modelo.
 */
class TabEditSongMetadataReaderTest {

    private final TabEditSongMetadataReader reader = new TabEditSongMetadataReader();

    @Test
    void leeTituloAutorComentariosYNotas() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("Mi cancion")
                .writeShortString("Un autor")
                .writeShortString("unos comentarios")
                .writeShortString("unas notas")
                .writeShortString(""); // lyrics: sin pistas, una sola entrada vacia
        TabEditHeader header = header(false, false, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("Mi cancion", metadata.title());
        assertEquals("Un autor", metadata.author());
        assertEquals("unos comentarios", metadata.comments());
        assertEquals("unas notas", metadata.notes());
        assertEquals("", metadata.copyright());
    }

    @Test
    void leeUrlYCopyrightSoloSiElEncabezadoLosAnuncia() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("T")
                .writeShortString("A")
                .writeShortString("")
                .writeShortString("")
                .writeShortString("http://ejemplo.com")
                .writeShortString("(c) alguien")
                .writeShortString(""); // lyrics
        TabEditHeader header = header(true, true, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("(c) alguien", metadata.copyright());
    }

    @Test
    void consumeLosEventosDeTextoSinPerderLaAlineacion() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("T")
                .writeShortString("A")
                .writeShortString("")
                .writeShortString("")
                .writeShortString("") // lyrics
                .writeShort(2)
                .writeShortString("primer evento")
                .writeShortString("segundo evento");
        writer.writeUnsignedByte(77); // marca para confirmar que la lectura sigue alineada
        TabEditHeader header = headerWithTextEvents();
        TabEditByteReader input = new TabEditByteReader(writer.bytes());

        reader.read(input, header);

        assertEquals(77, input.readUnsignedByte());
    }

    private static TabEditHeader header(boolean hasUrl, boolean hasCopyright, boolean hasTextEvents) {
        return new TabEditHeader(120, hasTextEvents, false, false, hasUrl, hasCopyright);
    }

    private static TabEditHeader headerWithTextEvents() {
        return new TabEditHeader(120, true, false, false, false, false);
    }
}
