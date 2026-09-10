package com.gstncaruso.tabpro.ui.score;

public enum ViewMode {
    PAGE("Página", true, true, false),
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

    public boolean showsPaper() {
        return showsPaper;
    }

    public boolean paginates() {
        return paginates;
    }

    public boolean scrollsHorizontally() {
        return scrollsHorizontally;
    }
}
