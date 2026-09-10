package com.gstncaruso.tabpro.format.tabledit;

/**
 * Reads the song metadata, right after the header. The full lyrics and the text events
 * have their own format (lines per track, brackets, line breaks) that tabpro does not
 * translate yet: they are consumed all the same to not lose the alignment of the rest
 * of the file, and are left unused.
 */
final class TabEditSongMetadataReader {

    TabEditSongMetadata read(TabEditByteReader input, TabEditHeader header) {
        String title = input.readShortString();
        String author = input.readShortString();
        String comments = input.readShortString();
        String notes = input.readShortString();

        if (header.hasUrl()) {
            input.readShortString(); // url: no place in ScoreInfo, discarded.
        }

        String copyright = header.hasCopyright() ? input.readShortString() : "";

        input.readShortString(); // full lyrics (TablEdit's own format): not supported.

        if (header.hasTextEvents()) {
            int totalTextEvents = input.readUnsignedShort();
            for (int i = 0; i < totalTextEvents; i++) {
                input.readShortString(); // text events: not supported, only consumed.
            }
        }

        return new TabEditSongMetadata(title, author, comments, notes, copyright);
    }
}
