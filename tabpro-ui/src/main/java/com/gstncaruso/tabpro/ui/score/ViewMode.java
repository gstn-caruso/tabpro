package com.gstncaruso.tabpro.ui.score;

/**
 * Como se organiza la partitura en la ventana, lo que el manual llama "Configure the Display".
 * Pagina y pergamino dibujan una hoja clara con margenes y encabezado; pantalla dibuja la
 * notacion sola, sin papel. Pagina corta en hojas del mismo alto; pergamino es una sola hoja sin
 * cortes; pantalla horizontal no envuelve en sistemas, sino que hace un unico renglon largo.
 */
public enum ViewMode {
    PAGE("Pagina", true, true, false),
    PARCHMENT("Pergamino", true, false, false),
    SCREEN_VERTICAL("Pantalla vertical", false, false, false),
    SCREEN_HORIZONTAL("Pantalla horizontal", false, false, true);

    private final String label;
    private final boolean showsPaper;
    private final boolean paginates;
    private final boolean scrollsHorizontally;

    ViewMode(String label, boolean showsPaper, boolean paginates, boolean scrollsHorizontally) {
        this.label = label;
        this.showsPaper = showsPaper;
        this.paginates = paginates;
        this.scrollsHorizontally = scrollsHorizontally;
    }

    public String label() {
        return label;
    }

    /** Si dibuja la hoja clara con margenes, sombra, encabezado y pie. */
    public boolean showsPaper() {
        return showsPaper;
    }

    /** Si corta el contenido en hojas del mismo alto, con saltos entre una y otra. */
    public boolean paginates() {
        return paginates;
    }

    /** Si en vez de envolver en sistemas hace un unico renglon largo que se recorre de lado. */
    public boolean scrollsHorizontally() {
        return scrollsHorizontally;
    }
}
