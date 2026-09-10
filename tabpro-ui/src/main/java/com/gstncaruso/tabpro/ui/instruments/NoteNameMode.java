package com.gstncaruso.tabpro.ui.instruments;

public enum NoteNameMode {
    NONE {
        @Override
        public boolean shows(MarkKind kind) {
            return false;
        }
    },
    BEAT_ONLY {
        @Override
        public boolean shows(MarkKind kind) {
            return kind == MarkKind.PRIMARY;
        }
    },
    ALL {
        @Override
        public boolean shows(MarkKind kind) {
            return true;
        }
    };

    public abstract boolean shows(MarkKind kind);
}
