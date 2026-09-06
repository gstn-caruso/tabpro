package com.gstncaruso.tabpro.ui.page;

/**
 * Cada uno de los casilleros del encabezado y del pie que ofrece Configurar pagina. El texto por
 * defecto de cada uno es el que trae el manual: el campo entre corchetes, que se reemplaza por su
 * valor de Informacion de la partitura.
 */
public enum PageElement {
    TITLE("Titulo", "[%title]"),
    SUBTITLE("Subtitulo", "[%subtitle]"),
    ARTIST("Artista", "[%artist]"),
    ALBUM("Album", "[%album]"),
    WORDS("Letra de", "Letra: [%words]"),
    MUSIC("Musica de", "Musica: [%music]"),
    COPYRIGHT("Copyright", "[%copyright]"),
    PAGE_NUMBER("Numero de pagina", "Pagina [%page] de [%pages]");

    private final String label;
    private final String defaultText;

    PageElement(String label, String defaultText) {
        this.label = label;
        this.defaultText = defaultText;
    }

    public String label() {
        return label;
    }

    public String defaultText() {
        return defaultText;
    }
}
