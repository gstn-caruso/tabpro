package com.gstncaruso.tabpro.ui.dialogs.tuner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MidiTunerPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();
    private final MidiTunerPanel panel = new MidiTunerPanel(Tuning.standard(), 25, player);

    @Test
    void theListenOnLoopButtonIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Listen on Loop", english.text("score_dialogs.MidiTunerPanel.listenOnLoop"));
        assertEquals("Listen to string 3 on loop", english.text("score_dialogs.MidiTunerPanel.listenToStringOnLoop", 3));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(panel);
    }

    @AfterEach
    void stopEveryLoop() {
        panel.stopAllLoops();
    }

    @Test
    void startingALoopPlaysTheStringRightAway() {
        panel.startLoop(6);

        assertEquals(1, player.sounded().size());
        assertEquals(Tuning.standard().pitchOfString(6), player.sounded().getFirst().pitch());
        assertEquals(25, player.sounded().getFirst().program());
    }

    @Test
    void tracksWhichStringsAreLooping() {
        assertFalse(panel.isLooping(1));

        panel.startLoop(1);
        assertTrue(panel.isLooping(1));

        panel.stopLoop(1);
        assertFalse(panel.isLooping(1));
    }

    @Test
    void restartingALoopDoesNotDuplicateTimers() {
        panel.startLoop(2);
        panel.startLoop(2);

        assertTrue(panel.isLooping(2));
        assertEquals(2, player.sounded().size());
    }
}
