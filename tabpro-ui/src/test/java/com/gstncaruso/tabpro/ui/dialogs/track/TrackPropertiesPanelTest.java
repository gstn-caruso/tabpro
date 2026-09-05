package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import org.junit.jupiter.api.Test;

class TrackPropertiesPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void startsWithTheTracksName() {
        Track track = Track.standardGuitar("Guitarra 1");

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertEquals("Guitarra 1", panel.trackName());
    }

    @Test
    void startsWithTheTracksTuning() {
        Track track = Track.standardBass("Bajo");

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertEquals(track.tuning(), panel.toTuning());
    }

    @Test
    void startsWithTheTracksSettings() {
        Track track = Track.standardGuitar("Guitarra").mappingSettings(
                settings -> settings.withCapo(3).withFretCount(22).withTwelveString(true));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertEquals(track.settings(), panel.toTrackSettings());
    }

    @Test
    void keepsThePercussionFlagEvenIfTheDialogDoesNotShowIt() {
        Track track = Track.percussion("Bateria");

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertTrue(panel.toTrackSettings().percussion());
    }

    @Test
    void changesToDisplayComeThroughInTheSettings() {
        Track track = Track.standardGuitar("Guitarra");

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);
        panel.toTrackSettings();

        assertEquals(TrackDisplay.standard(), panel.toTrackSettings().display());
    }

    @Test
    void colorRoundTrips() {
        ScoreColor color = new ScoreColor(10, 20, 30);
        Track track = Track.standardGuitar("Guitarra").mappingSettings(settings -> settings.withColor(color));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertEquals(color, panel.toTrackSettings().color());
    }
}
