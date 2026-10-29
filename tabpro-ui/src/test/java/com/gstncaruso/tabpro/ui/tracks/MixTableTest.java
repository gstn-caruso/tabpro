package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.accessibility.AccessibleRole;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MixTableTest {

    @ParameterizedTest(name = "{0}: MIDI {1} renders GP5 {2}")
    @CsvSource({
            "VOLUME, 0, 0",
            "VOLUME, 64, 8",
            "VOLUME, 127, 16",
            "PAN, 0, -8",
            "PAN, 64, 0",
            "PAN, 127, 8"
    })
    void givenMidiValuesInMixTable_whenRendered_thenGp5NumbersAreVisibleAndMidiIsUnchanged(
            MixParameter parameter, int midiValue, int gp5Number) {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        parameter.applyTo(editor, 0, midiValue);
        MixTable table = new MixTable(editor);
        MixTableRow row = table.rows().get(0);
        boolean volume = parameter == MixParameter.VOLUME;
        LevelSlider actual = volume ? row.volumeSlider() : row.panSlider();
        // The reference already holds GP5 units; only the real table control holds MIDI.
        LevelSlider reference = new LevelSlider(volume ? 0 : -8, volume ? 16 : 8, gp5Number,
                volume ? ScoreColors.VOLUME_LEVEL : ScoreColors.PAN_LEVEL,
                volume ? ScoreColors.MUTED_INK : ScoreColors.PAN_LEVEL);
        reference.setFont(actual.getFont());
        // At 96px, MIDI 64 and the GP5 midpoint round to the same track/badge pixel.
        actual.setSize(96, 20);
        reference.setSize(actual.getSize());

        boolean[] rendered = renderedInkMask(actual);
        boolean[] expected = renderedInkMask(reference);
        var accessible = actual.getAccessibleContext().getAccessibleValue();

        assertAll(
                () -> assertArrayEquals(expected, rendered,
                        parameter + " MIDI " + midiValue + " must visibly render GP5 " + gp5Number),
                () -> assertEquals(midiValue, actual.getValue()),
                () -> assertEquals(midiValue, parameter.valueOf(editor.score().track(0))),
                () -> assertEquals(midiValue, accessible.getCurrentAccessibleValue().intValue()),
                () -> assertEquals(0, accessible.getMinimumAccessibleValue().intValue()),
                () -> assertEquals(127, accessible.getMaximumAccessibleValue().intValue()),
                () -> assertEquals(AccessibleRole.SLIDER, actual.getAccessibleContext().getAccessibleRole()),
                () -> assertFalse(actual.getAccessibleContext().getAccessibleName().isBlank()));
    }

    private static boolean[] renderedInkMask(LevelSlider slider) {
        BufferedImage image = new BufferedImage(slider.getWidth(), slider.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        slider.paint(graphics);
        graphics.dispose();
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        int inkRgb = ScoreColors.PAGE_INK.getRGB() & 0xFFFFFF;
        boolean[] mask = new boolean[pixels.length];
        // Compare every ink position, ignoring fill and groove colors rather than the number's shape.
        for (int i = 0; i < pixels.length; i++) {
            mask[i] = (pixels[i] & 0xFFFFFF) == inkRgb;
        }
        return mask;
    }

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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        AccessibilityAssertions.assertNoViolations(table);
    }

    @Test
    void listsOneRowPerTrack() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        MixTable table = new MixTable(editor);

        assertEquals(1, table.rows().size());
    }

    @Test
    void followsTheEditorWhenATrackIsAdded() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        MixTable table = new MixTable(editor);

        editor.addTrack(Track.standardBass("Bajo"));
        table.refresh();

        assertEquals(2, table.rows().size());
    }

    @Test
    void reduceAllParametersButtonHidesEveryRowsKnobs() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
