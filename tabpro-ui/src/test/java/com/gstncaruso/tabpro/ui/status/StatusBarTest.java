package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Component;
import java.awt.Container;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class StatusBarTest {

    @Test
    void thePanelNamesAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Page", english.text("views.StatusBar.page"));
        assertEquals("Position", english.text("views.StatusBar.position"));
        assertEquals("Bar Status", english.text("views.StatusBar.measureStatus"));
        assertEquals("Track", english.text("views.StatusBar.track"));
        assertEquals("Bar Duration", english.text("views.StatusBar.measureDuration"));
        assertEquals("Title and Author", english.text("views.StatusBar.titleAndAuthor"));
        assertEquals("Page 1/1", english.text("views.StatusBar.pageText", "1", "1"));
    }

    @Test
    void everyStatusBarControlHasAnAccessibleNameAndTooltip() {
        StatusBar bar = new StatusBar(new Editor(Score.blank(new TextsDefaultNames())));

        AccessibilityAssertions.assertNoViolations(bar);
    }

    @Test
    void showsThePageThePositionAndTheActiveTrack() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        StatusBar bar = new StatusBar(editor);

        assertEquals("Pág. 1/1", bar.pageText());
        assertEquals("001 : 001", bar.positionText());
        assertEquals("Guitarra", bar.trackNameText());
    }

    @Test
    void warnsWhenTheMeasureIsNotComplete() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        StatusBar bar = new StatusBar(editor);

        assertEquals("Compás corto", bar.completenessText());
    }

    @Test
    void showsTheMeasureDurationInBeats() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        StatusBar bar = new StatusBar(editor);

        assertEquals("1.000 : 4.000", bar.durationText());
    }

    @Test
    void showsTheTitleAndTheAuthorOnTheRight() {
        ScoreInfo info = ScoreInfo.empty().withTitle("Sultans of Swing").withMusicAuthor("Mark Knopfler");
        Editor editor = new Editor(new Score(info, 120, Score.blank(new TextsDefaultNames()).tracks(), Score.blank(new TextsDefaultNames()).lyrics()));
        StatusBar bar = new StatusBar(editor);

        assertEquals("Sultans of Swing — Música: Mark Knopfler", bar.creditsText());
    }

    @Test
    void followsTheEditorWhenTheCursorMoves() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        StatusBar bar = new StatusBar(editor);

        editor.selectTrack(1);

        assertEquals("001 : 001", bar.positionText());
        assertEquals("Bajo", bar.trackNameText());
    }

    @Test
    void everyPanelHasAnAccessibleNameAndASunkenBorder() {
        StatusBar bar = new StatusBar(new Editor(Score.blank(new TextsDefaultNames())));

        assertHasASunkenPanel(bar, "Página");
        assertHasASunkenPanel(bar, "Posición");
        assertHasASunkenPanel(bar, "Estado del compás");
        assertHasASunkenPanel(bar, "Pista");
        assertHasASunkenPanel(bar, "Duración del compás");
        assertHasASunkenPanel(bar, "Título y autor");
    }

    private static void assertHasASunkenPanel(Container root, String accessibleName) {
        JLabel label = findLabelByAccessibleName(root, accessibleName);
        assertNotNull(label, "found no panel named " + accessibleName);
        assertNotNull(label.getParent(), accessibleName + " is not inside a panel");
        assertNotNull(((JPanel) label.getParent()).getBorder(), accessibleName + " has no sunken border");
    }

    private static JLabel findLabelByAccessibleName(Container root, String accessibleName) {
        for (Component child : root.getComponents()) {
            if (child instanceof JLabel label
                    && accessibleName.equals(label.getAccessibleContext().getAccessibleName())) {
                return label;
            }
            if (child instanceof Container container) {
                JLabel found = findLabelByAccessibleName(container, accessibleName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
