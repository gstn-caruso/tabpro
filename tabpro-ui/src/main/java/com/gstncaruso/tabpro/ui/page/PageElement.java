package com.gstncaruso.tabpro.ui.page;

import com.gstncaruso.tabpro.ui.i18n.Texts;

public enum PageElement {
    TITLE,
    SUBTITLE,
    ARTIST,
    ALBUM,
    WORDS,
    MUSIC,
    COPYRIGHT,
    PAGE_NUMBER;

    public String label() {
        return Texts.get("views.PageElement." + name() + ".label");
    }

    public String defaultText() {
        return Texts.get("views.PageElement." + name() + ".defaultText");
    }
}
