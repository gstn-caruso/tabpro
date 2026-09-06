package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PreferencesTest {

    private final java.util.prefs.Preferences node =
            java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro/test/" + UUID.randomUUID());
    private final Preferences preferences = new Preferences(node);

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void startsWithoutRecentFiles() {
        assertEquals(List.of(), preferences.recentFiles());
    }

    @Test
    void theLastFileOpenedComesFirst() {
        preferences.remember(Path.of("/tmp/una.tabpro"));
        preferences.remember(Path.of("/tmp/otra.tabpro"));

        assertEquals(List.of(Path.of("/tmp/otra.tabpro"), Path.of("/tmp/una.tabpro")), preferences.recentFiles());
    }

    @Test
    void openingTheSameFileTwiceDoesNotListItTwice() {
        preferences.remember(Path.of("/tmp/una.tabpro"));
        preferences.remember(Path.of("/tmp/otra.tabpro"));
        preferences.remember(Path.of("/tmp/una.tabpro"));

        assertEquals(List.of(Path.of("/tmp/una.tabpro"), Path.of("/tmp/otra.tabpro")), preferences.recentFiles());
    }

    @Test
    void theListDoesNotGrowPastItsLimit() {
        for (int index = 0; index < Preferences.MAX_RECENT_FILES + 5; index++) {
            preferences.remember(Path.of("/tmp/score" + index + ".tabpro"));
        }

        assertEquals(Preferences.MAX_RECENT_FILES, preferences.recentFiles().size());
    }

    @Test
    void remembersTheViewAndTheZoom() {
        preferences.setViewMode("HORIZONTAL_SCREEN");
        preferences.setZoomPercent(150);

        assertEquals("HORIZONTAL_SCREEN", preferences.viewMode());
        assertEquals(150, preferences.zoomPercent());
    }

    @Test
    void theZoomStaysWithinWhatTheManualOffers() {
        preferences.setZoomPercent(500);

        assertTrue(preferences.zoomPercent() <= 200);
    }
}
