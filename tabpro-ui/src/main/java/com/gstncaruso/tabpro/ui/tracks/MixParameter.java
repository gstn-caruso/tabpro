package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.function.ToIntFunction;

/**
 * Un parametro de sonido de la mesa de mezcla que se ve como potenciometro o como numero:
 * volumen, paneo, chorus, reverb, phaser o tremolo. Sabe leer su valor de una pista y como
 * aplicarlo de vuelta al editor, para que la fila de la mesa no tenga que distinguir cual es cual.
 */
public enum MixParameter {
    VOLUME("Volumen", Channel::volume, Editor::setVolume),
    PAN("Paneo", Channel::pan, Editor::setPan),
    CHORUS("Chorus", Channel::chorus, Editor::setChorus),
    REVERB("Reverb", Channel::reverb, Editor::setReverb),
    PHASER("Phaser", Channel::phaser, Editor::setPhaser),
    TREMOLO("Trémolo", Channel::tremolo, Editor::setTremolo);

    private final String label;
    private final ToIntFunction<Channel> reader;
    private final Setter setter;

    MixParameter(String label, ToIntFunction<Channel> reader, Setter setter) {
        this.label = label;
        this.reader = reader;
        this.setter = setter;
    }

    public String label() {
        return label;
    }

    public int valueOf(Track track) {
        return reader.applyAsInt(track.channel());
    }

    public void applyTo(Editor editor, int trackIndex, int value) {
        setter.set(editor, trackIndex, value);
    }

    @FunctionalInterface
    private interface Setter {
        void set(Editor editor, int trackIndex, int value);
    }
}
