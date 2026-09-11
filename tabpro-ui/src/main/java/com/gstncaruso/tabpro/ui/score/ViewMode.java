package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.i18n.Texts;

public enum ViewMode {
    PAGE(true, true, false),
    PARCHMENT(true, false, false),
    SCREEN_VERTICAL(false, false, false),
    SCREEN_HORIZONTAL(false, false, true);

    private final boolean showsPaper;
    private final boolean paginates;
    private final boolean scrollsHorizontally;

    ViewMode(boolean showsPaper, boolean paginates, boolean scrollsHorizontally) {
        this.showsPaper = showsPaper;
        this.paginates = paginates;
        this.scrollsHorizontally = scrollsHorizontally;
    }

    public String label() {
        return Texts.get("views.ViewMode." + name());
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
