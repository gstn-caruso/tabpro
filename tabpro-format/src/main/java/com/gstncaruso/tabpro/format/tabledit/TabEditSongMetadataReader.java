package com.gstncaruso.tabpro.format.tabledit;

/**
 * Lee los metadatos de la cancion, justo despues del encabezado. La letra
 * completa y los eventos de texto tienen su propio formato (lineas por pista,
 * corchetes, saltos de linea) que tabpro todavia no traduce: se consumen igual
 * para no perder la alineacion del resto del archivo, y quedan sin usar.
 */
final class TabEditSongMetadataReader {

    TabEditSongMetadata read(TabEditByteReader input, TabEditHeader header) {
        String title = input.readShortString();
        String author = input.readShortString();
        String comments = input.readShortString();
        String notes = input.readShortString();

        if (header.hasUrl()) {
            input.readShortString(); // url: sin lugar en ScoreInfo, se descarta.
        }

        String copyright = header.hasCopyright() ? input.readShortString() : "";

        input.readShortString(); // letra completa (formato propio de TablEdit): no soportada.

        if (header.hasTextEvents()) {
            int totalTextEvents = input.readUnsignedShort();
            for (int i = 0; i < totalTextEvents; i++) {
                input.readShortString(); // eventos de texto: no soportados, solo se consumen.
            }
        }

        return new TabEditSongMetadata(title, author, comments, notes, copyright);
    }
}
