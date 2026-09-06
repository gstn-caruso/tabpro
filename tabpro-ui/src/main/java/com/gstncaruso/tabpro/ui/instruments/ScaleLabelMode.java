package com.gstncaruso.tabpro.ui.instruments;

/**
 * Que texto lleva una nota de la escala sobre el diapason, tal como lo permite personalizar
 * el manual: su nombre, su intervalo respecto de la tonica, o el grado que ocupa en la escala.
 */
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

    @Override
    public String toString() {
        return label;
    }
}
