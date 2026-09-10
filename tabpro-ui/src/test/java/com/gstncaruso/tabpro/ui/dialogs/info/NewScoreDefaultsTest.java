package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import org.junit.jupiter.api.Test;

class NewScoreDefaultsTest {

    @Test
    void theNewScoreUsesTheDefaultTempoAndTimeSignature() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                90, new TimeSignature(3, 4), KeySignature.cMajor(), "", "");

        Score newScore = defaults.newScore();

        assertEquals(90, newScore.tempo());
        assertEquals(new TimeSignature(3, 4), newScore.timeSignatureOf(0));
    }

    @Test
    void theNewScoreUsesTheDefaultKeySignature() {
        KeySignature dFlatMinor = new KeySignature(-5, Mode.MINOR);
        NewScoreDefaults defaults = new NewScoreDefaults(
                120, TimeSignature.fourFour(), dFlatMinor, "", "");

        Score newScore = defaults.newScore();

        assertEquals(dFlatMinor, newScore.attributesOf(0).keySignature());
    }

    @Test
    void withoutTitleOrArtistTheNewScoreHasNoTitle() {
        NewScoreDefaults defaults = NewScoreDefaults.blank();

        Score newScore = defaults.newScore();

        assertEquals("", newScore.info().title());
        assertEquals("", newScore.info().artist());
    }

    @Test
    void withDefaultTitleAndArtistTheNewScoreCarriesThem() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                120, TimeSignature.fourFour(), KeySignature.cMajor(), "Improvisando", "Yo");

        Score newScore = defaults.newScore();

        assertEquals("Improvisando", newScore.info().title());
        assertEquals("Yo", newScore.info().artist());
    }
}
