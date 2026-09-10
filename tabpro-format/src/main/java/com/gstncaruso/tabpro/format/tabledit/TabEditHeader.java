package com.gstncaruso.tabpro.format.tabledit;

/**
 * What the 256-byte header of a TEF3 file carries: the initial tempo and which
 * optional sections the rest of the file brings.
 */
record TabEditHeader(
        int initialBpm,
        boolean hasTextEvents,
        boolean hasChords,
        boolean hasReadingList,
        boolean hasUrl,
        boolean hasCopyright) {
}
