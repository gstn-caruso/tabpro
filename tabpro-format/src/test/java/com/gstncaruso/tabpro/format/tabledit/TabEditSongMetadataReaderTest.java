package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TabEditSongMetadataReaderTest {

    private final TabEditSongMetadataReader reader = new TabEditSongMetadataReader();

    @Test
    void readsTitleAuthorCommentsAndNotes() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("Mi cancion")
                .writeShortString("Un autor")
                .writeShortString("unos comentarios")
                .writeShortString("unas notas")
                .writeShortString("");
        TabEditHeader header = header(false, false, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("Mi cancion", metadata.title());
        assertEquals("Un autor", metadata.author());
        assertEquals("unos comentarios", metadata.comments());
        assertEquals("unas notas", metadata.notes());
        assertEquals("", metadata.copyright());
    }

    @Test
    void readsUrlAndCopyrightOnlyWhenTheHeaderAnnouncesThem() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("T")
                .writeShortString("A")
                .writeShortString("")
                .writeShortString("")
                .writeShortString("http://ejemplo.com")
                .writeShortString("(c) alguien")
                .writeShortString("");
        TabEditHeader header = header(true, true, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("(c) alguien", metadata.copyright());
    }

    @Test
    void consumesTheTextEventsWithoutLosingAlignment() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("T")
                .writeShortString("A")
                .writeShortString("")
                .writeShortString("")
                .writeShortString("")
                .writeShort(2)
                .writeShortString("primer evento")
                .writeShortString("segundo evento");
        writer.writeUnsignedByte(77);
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
