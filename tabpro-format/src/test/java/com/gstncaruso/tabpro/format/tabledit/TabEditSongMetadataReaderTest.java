package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TabEditSongMetadataReaderTest {

    private final TabEditSongMetadataReader reader = new TabEditSongMetadataReader();

    @Test
    void readsTitleAuthorCommentsAndNotes() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("My song")
                .writeShortString("Some author")
                .writeShortString("some comments")
                .writeShortString("some notes")
                .writeShortString("");
        TabEditHeader header = header(false, false, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("My song", metadata.title());
        assertEquals("Some author", metadata.author());
        assertEquals("some comments", metadata.comments());
        assertEquals("some notes", metadata.notes());
        assertEquals("", metadata.copyright());
    }

    @Test
    void readsUrlAndCopyrightOnlyWhenTheHeaderAnnouncesThem() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShortString("T")
                .writeShortString("A")
                .writeShortString("")
                .writeShortString("")
                .writeShortString("http://example.com")
                .writeShortString("(c) someone")
                .writeShortString("");
        TabEditHeader header = header(true, true, false);

        TabEditSongMetadata metadata = reader.read(new TabEditByteReader(writer.bytes()), header);

        assertEquals("(c) someone", metadata.copyright());
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
                .writeShortString("first event")
                .writeShortString("second event");
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
