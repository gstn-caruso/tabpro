package com.gstncaruso.tabpro.ui.page;

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

    public abstract double widthOf(PaperFormat format);

    public abstract double heightOf(PaperFormat format);
}
