package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import org.junit.jupiter.api.Test;

class MixParameterTest {

    @Test
    void readsTheCurrentValueOfEveryKnob() {
        Editor editor = new Editor(Score.blank());

        assertEquals(100, MixParameter.VOLUME.valueOf(editor.currentTrack()));
        assertEquals(64, MixParameter.PAN.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.CHORUS.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.REVERB.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.PHASER.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.TREMOLO.valueOf(editor.currentTrack()));
    }

    @Test
    void movingAKnobChangesThatChannelFieldAndNoOther() {
        Editor editor = new Editor(Score.blank());

        MixParameter.CHORUS.applyTo(editor, 0, 90);

        assertEquals(90, editor.currentTrack().channel().chorus());
        assertEquals(100, editor.currentTrack().channel().volume());
    }

    @Test
    void everyParameterHasAReadableLabel() {
        for (MixParameter parameter : MixParameter.values()) {
            assertEquals(false, parameter.label().isBlank());
        }
    }
}
