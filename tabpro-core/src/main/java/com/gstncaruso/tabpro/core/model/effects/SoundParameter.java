package com.gstncaruso.tabpro.core.model.effects;

/** Lo que puede cambiar a mitad de la partitura la ventana de mesa de mezcla. */
public enum SoundParameter {
    PROGRAM("Instrumento", 0, 127),
    VOLUME("Volumen", 0, 127),
    PAN("Paneo", 0, 127),
    CHORUS("Chorus", 0, 127),
    REVERB("Reverb", 0, 127),
    PHASER("Phaser", 0, 127),
    TREMOLO("Trémolo", 0, 127),
    TEMPO("Tempo", 20, 400);

    private final String label;
    private final int minimum;
    private final int maximum;

    SoundParameter(String label, int minimum, int maximum) {
        this.label = label;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public String label() {
        return label;
    }

    public int minimum() {
        return minimum;
    }

    public int maximum() {
        return maximum;
    }

    /** El tempo vale para toda la partitura; los demas, para la pista que los lleva. */
    public boolean isGlobal() {
        return this == TEMPO;
    }
}
