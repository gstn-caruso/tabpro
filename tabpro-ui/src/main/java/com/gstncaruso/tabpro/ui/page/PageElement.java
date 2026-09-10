package com.gstncaruso.tabpro.ui.page;

public enum PageElement {
    TITLE("Título", "[%title]"),
    SUBTITLE("Subtítulo", "[%subtitle]"),
    ARTIST("Artista", "[%artist]"),
    ALBUM("Álbum", "[%album]"),
    WORDS("Letra de", "Letra: [%words]"),
    MUSIC("Música de", "Música: [%music]"),
    COPYRIGHT("Copyright", "[%copyright]"),
    PAGE_NUMBER("Número de página", "Página [%page] de [%pages]");

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
