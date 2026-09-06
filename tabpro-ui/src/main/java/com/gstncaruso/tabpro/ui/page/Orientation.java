package com.gstncaruso.tabpro.ui.page;

/** De que lado se apoya la hoja: vertical como viene medida, u horizontal, con los lados dados vuelta. */
public enum Orientation {
    PORTRAIT("Vertical") {
        @Override
        public double widthOf(PaperFormat format) {
            return format.widthMillimetres();
        }

        @Override
        public double heightOf(PaperFormat format) {
            return format.heightMillimetres();
        }
    },
    LANDSCAPE("Horizontal") {
        @Override
        public double widthOf(PaperFormat format) {
            return format.heightMillimetres();
        }

        @Override
        public double heightOf(PaperFormat format) {
            return format.widthMillimetres();
        }
    };

    private final String label;

    Orientation(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Cuanto mide de ancho, en milimetros, una hoja de ese formato puesta asi. */
    public abstract double widthOf(PaperFormat format);

    public abstract double heightOf(PaperFormat format);
}
