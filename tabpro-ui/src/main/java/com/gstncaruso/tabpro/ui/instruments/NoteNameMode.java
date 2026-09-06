package com.gstncaruso.tabpro.ui.instruments;

/** Cuanto nombre de nota se dibuja sobre las marcas del diapason. */
public enum NoteNameMode {
    NONE("Sin nombres") {
        @Override
        public boolean shows(MarkKind kind) {
            return false;
        }
    },
    BEAT_ONLY("Solo las del beat") {
        @Override
        public boolean shows(MarkKind kind) {
            return kind == MarkKind.PRIMARY;
        }
    },
    ALL("Todas") {
        @Override
        public boolean shows(MarkKind kind) {
            return true;
        }
    };

    private final String label;

    NoteNameMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public abstract boolean shows(MarkKind kind);

    @Override
    public String toString() {
        return label;
    }
}
