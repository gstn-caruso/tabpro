package com.gstncaruso.tabpro.ui.instruments;

public enum ScaleLabelMode {
    NAME("Nombre") {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return name;
        }
    },
    INTERVAL("Intervalo") {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return scale.intervalLabelOf(midiNumber);
        }
    },
    DEGREE("Grado") {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return String.valueOf(scale.degreeOf(midiNumber));
        }
    };

    private final String label;

    ScaleLabelMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public abstract String textFor(String name, int midiNumber, Scale scale);
}
