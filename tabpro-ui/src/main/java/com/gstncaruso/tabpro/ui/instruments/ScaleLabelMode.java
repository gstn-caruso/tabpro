package com.gstncaruso.tabpro.ui.instruments;

public enum ScaleLabelMode {
    NAME {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return name;
        }
    },
    INTERVAL {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return scale.intervalLabelOf(midiNumber);
        }
    },
    DEGREE {
        @Override
        public String textFor(String name, int midiNumber, Scale scale) {
            return String.valueOf(scale.degreeOf(midiNumber));
        }
    };

    public abstract String textFor(String name, int midiNumber, Scale scale);
}
