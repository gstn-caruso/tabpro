package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

class MixTableTest {

    @Test
    void theColumnTitlesAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Name", english.text("views.MixTable.nameColumn"));
        assertEquals("Port", english.text("views.MixTable.portColumn"));
        assertEquals("Instrument", english.text("views.MixTable.instrumentColumn"));
        assertEquals("Reduce All Parameters", english.text("views.MixTable.reduceAll"));
    }

    @Test
    void everyControlOfTheMixTableHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        AccessibilityAssertions.assertNoViolations(table);
    }

    @Test
    void listsOneRowPerTrack() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        assertEquals(1, table.rows().size());
    }

    @Test
    void followsTheEditorWhenATrackIsAdded() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        editor.addTrack(Track.standardBass("Bajo"));
        table.refresh();

        assertEquals(2, table.rows().size());
    }

    @Test
    void reduceAllParametersButtonHidesEveryRowsKnobs() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        table.model().reduceAllParameters();
        table.refresh();

        for (MixTableRow row : table.rows()) {
            assertTrue(row.parameterCells().stream().noneMatch(java.awt.Component::isVisible));
            assertFalse(row.volumeSlider().isVisible());
            assertFalse(row.panSlider().isVisible());
        }
    }

    @Test
    void restoreAllParametersButtonShowsThemAgain() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);
        table.model().reduceAllParameters();
        table.refresh();

        table.model().restoreAllParameters();
        table.refresh();

        for (MixTableRow row : table.rows()) {
            assertTrue(row.parameterCells().stream().allMatch(java.awt.Component::isVisible));
            assertTrue(row.volumeSlider().isVisible());
            assertTrue(row.panSlider().isVisible());
        }
    }

    @Test
    void theReduceButtonHidesEveryKnobAndTheRestoreButtonBringsThemBack() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        table.reduceButton().doClick();
        assertTrue(table.rows().get(0).parameterCells().stream().noneMatch(java.awt.Component::isVisible));
        assertFalse(table.rows().get(0).volumeSlider().isVisible());

        table.restoreButton().doClick();
        assertTrue(table.rows().get(0).parameterCells().stream().allMatch(java.awt.Component::isVisible));
        assertTrue(table.rows().get(0).volumeSlider().isVisible());
    }

    @Test
    void noColumnHeaderEncroachesOnTheNext() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        for (JLabel title : table.columnTitleLabels()) {
            if (title.getText().isEmpty()) {
                continue;
            }
            int textWidth = title.getFontMetrics(title.getFont()).stringWidth(title.getText());
            int columnWidth = title.getPreferredSize().width;
            assertTrue(textWidth < columnWidth,
                    "header \"" + title.getText() + "\" (" + textWidth + "px) does not fit comfortably in its column of "
                            + columnWidth + "px");
        }
    }
}
