package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.function.ToIntFunction;

public enum MixParameter {
    VOLUME(Channel::volume, Editor::setVolume),
    PAN(Channel::pan, Editor::setPan),
    CHORUS(Channel::chorus, Editor::setChorus),
    REVERB(Channel::reverb, Editor::setReverb),
    PHASER(Channel::phaser, Editor::setPhaser),
    TREMOLO(Channel::tremolo, Editor::setTremolo);

    private final ToIntFunction<Channel> reader;
    private final Setter setter;

    MixParameter(ToIntFunction<Channel> reader, Setter setter) {
        this.reader = reader;
        this.setter = setter;
    }

    public String label() {
        return Texts.get("views.MixParameter." + name());
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
