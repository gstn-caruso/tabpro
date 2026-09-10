package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultScorePropertiesTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
    private final DefaultScoreProperties stored = new DefaultScoreProperties(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void withNothingSavedTheDefaultIsTheOneThatBringsABlankScore() {
        assertEquals(NewScoreDefaults.blank(), stored.get());
    }

    @Test
    void savingLeavesItAsTheDefault() {
        NewScoreDefaults custom = new NewScoreDefaults(
                90, new TimeSignature(3, 4), new KeySignature(-2, Mode.MINOR), "Improvising", "Me");

        stored.save(custom);

        assertEquals(custom, stored.get());
    }

    @Test
    void savingAgainReplacesWhatWasThere() {
        stored.save(new NewScoreDefaults(60, TimeSignature.fourFour(), KeySignature.cMajor(), "", ""));
        NewScoreDefaults latest = new NewScoreDefaults(
                180, new TimeSignature(6, 8), new KeySignature(4, Mode.MAJOR), "Fast", "Band");

        stored.save(latest);

        assertEquals(latest, stored.get());
    }

    @Test
    void whatIsSavedAsDefaultPropertiesEndsUpInTheScoreThatNewFileCreates() {
        stored.save(new NewScoreDefaults(90, new TimeSignature(3, 4), KeySignature.cMajor(), "", ""));

        Score newScore = stored.get().newScore();

        assertEquals(90, newScore.tempo());
        assertEquals(new TimeSignature(3, 4), newScore.timeSignatureOf(0));
    }
}
