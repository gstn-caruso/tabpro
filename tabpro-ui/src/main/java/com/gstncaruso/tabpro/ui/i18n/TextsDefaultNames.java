package com.gstncaruso.tabpro.ui.i18n;

import com.gstncaruso.tabpro.core.model.DefaultNames;

public final class TextsDefaultNames implements DefaultNames {

    private final TextSource texts;

    public TextsDefaultNames() {
        this(Texts::get);
    }

    TextsDefaultNames(TextSource texts) {
        this.texts = texts;
    }

    @Override
    public String track(int number) {
        return texts.text("defaults.track", number);
    }

    @Override
    public String unnamedTrack() {
        return texts.text("defaults.unnamedTrack");
    }

    @Override
    public String guitarTrack() {
        return texts.text("defaults.guitarTrack");
    }

    @Override
    public String percussionTrack() {
        return texts.text("defaults.percussionTrack");
    }

    @Override
    public String chord() {
        return texts.text("defaults.chord");
    }

    @Override
    public String marker() {
        return texts.text("defaults.marker");
    }

    interface TextSource {
        String text(String key, Object... arguments);
    }
}
