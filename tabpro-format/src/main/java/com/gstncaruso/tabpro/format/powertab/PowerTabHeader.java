package com.gstncaruso.tabpro.format.powertab;

/** Los datos de la cancion que trae la cabecera de un archivo PowerTab. */
record PowerTabHeader(
        String title,
        String artist,
        String composer,
        String lyricist,
        String arranger,
        String transcriber,
        String copyright,
        String lyrics,
        String notes) {
}
