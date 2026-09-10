package com.gstncaruso.tabpro.ui.page;

public enum Orientation {
    PORTRAIT {
        @Override
        public double widthOf(PaperFormat format) {
            return format.widthMillimetres();
        }

        @Override
        public double heightOf(PaperFormat format) {
            return format.heightMillimetres();
        }
    },
    LANDSCAPE {
        @Override
        public double widthOf(PaperFormat format) {
            return format.heightMillimetres();
        }

        @Override
        public double heightOf(PaperFormat format) {
            return format.widthMillimetres();
        }
    };

    public abstract double widthOf(PaperFormat format);

    public abstract double heightOf(PaperFormat format);
}
