package com.gstncaruso.tabpro.core.editing;

public interface ClipboardStorage {

    void hold(Clipboard.Clipping clipping);

    Clipboard.Clipping content();

    static ClipboardStorage inMemory() {
        return new ClipboardStorage() {
            private Clipboard.Clipping clipping = Clipboard.Clipping.EMPTY;

            @Override
            public void hold(Clipboard.Clipping clipping) {
                this.clipping = clipping;
            }

            @Override
            public Clipboard.Clipping content() {
                return clipping;
            }
        };
    }
}
