package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import java.awt.Component;
import java.awt.Container;
import java.util.Optional;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.Test;

class TrackPropertiesPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(
                new TrackPropertiesPanel(Track.standardGuitar("Guitarra 1"), player));
    }

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
    void forceChannels11to16RoundTrips() {
        Track track = Track.standardGuitar("Guitarra").mappingSettings(
                settings -> settings.withForceChannels11to16(true));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertTrue(panel.toTrackSettings().forceChannels11to16());
    }

    @Test
    void colorRoundTrips() {
        ScoreColor color = new ScoreColor(10, 20, 30);
        Track track = Track.standardGuitar("Guitarra").mappingSettings(settings -> settings.withColor(color));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertEquals(color, panel.toTrackSettings().color());
    }

    @Test
    void diagramsBelowStandardNotationRoundTrips() {
        Track track = Track.standardGuitar("Guitarra").mappingSettings(
                settings -> settings.withDisplay(settings.display().withDiagramsBelowStandardNotation(true)));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertTrue(panel.toTrackSettings().display().diagramsBelowStandardNotation());
    }

    @Test
    void forceHorizontalBeamsRoundTrips() {
        Track track = Track.standardGuitar("Guitarra").mappingSettings(
                settings -> settings.withDisplay(settings.display().withForceHorizontalBeams(true)));

        TrackPropertiesPanel panel = new TrackPropertiesPanel(track, player);

        assertTrue(panel.toTrackSettings().display().forceHorizontalBeams());
    }

    @Test
    void showsTheDiagramPlacementAsTwoCheckboxesInsteadOfACombo() {
        TrackPropertiesPanel panel = new TrackPropertiesPanel(Track.standardGuitar("Guitarra"), player);

        assertTrue(checkBoxNamed(panel, "Diagramas en la partitura").isPresent());
        assertTrue(checkBoxNamed(panel, "Lista de diagramas arriba de la partitura").isPresent());
    }

    private static Optional<JCheckBox> checkBoxNamed(Container root, String text) {
        for (Component child : root.getComponents()) {
            if (child instanceof JCheckBox box && text.equals(box.getText())) {
                return Optional.of(box);
            }
            if (child instanceof Container container) {
                Optional<JCheckBox> found = checkBoxNamed(container, text);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }
}
