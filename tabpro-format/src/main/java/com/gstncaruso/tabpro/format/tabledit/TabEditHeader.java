package com.gstncaruso.tabpro.format.tabledit;

/**
 * Lo que trae el encabezado de 256 bytes de un archivo TEF3: el tempo inicial
 * y que secciones opcionales trae el resto del archivo.
 */
record TabEditHeader(
        int initialBpm,
        boolean hasTextEvents,
        boolean hasChords,
        boolean hasReadingList,
        boolean hasUrl,
        boolean hasCopyright) {
}
