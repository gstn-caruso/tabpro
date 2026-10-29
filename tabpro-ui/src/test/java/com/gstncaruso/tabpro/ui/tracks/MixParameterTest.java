package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MixParameterTest {

    @ParameterizedTest
    @CsvSource({"VOLUME, 100, 12", "PAN, 60, -1", "PAN, 63, 0", "PAN, 68, 0"})
    void givenIntermediateMidiLevels_whenShownAsGp5Numbers_thenExistingGuitarProStepsAreUsed(
            MixParameter parameter, int midiValue, int expectedNumber) {
        assertEquals(expectedNumber, parameter.gp5Number(midiValue));
    }

    @Test
    void everyLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Volume", english.text("views.MixParameter.VOLUME"));
        assertEquals("Panoramic", english.text("views.MixParameter.PAN"));
        assertEquals("Tremolo", english.text("views.MixParameter.TREMOLO"));
    }

    @Test
    void readsTheCurrentValueOfEveryKnob() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

        assertEquals(100, MixParameter.VOLUME.valueOf(editor.currentTrack()));
        assertEquals(64, MixParameter.PAN.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.CHORUS.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.REVERB.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.PHASER.valueOf(editor.currentTrack()));
        assertEquals(0, MixParameter.TREMOLO.valueOf(editor.currentTrack()));
    }

    @Test
    void movingAKnobChangesThatChannelFieldAndNoOther() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

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
