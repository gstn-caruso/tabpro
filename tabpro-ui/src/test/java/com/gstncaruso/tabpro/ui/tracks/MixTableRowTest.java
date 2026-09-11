package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.DrumKits;
import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class MixTableRowTest {

    @Test
    void theAccessibleNameTemplatesAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Show Track Bass", english.text("views.MixTableRow.showTrack", "Bass"));
        assertEquals("Mute Bass", english.text("views.MixTableRow.muteTrack", "Bass"));
        assertEquals("Solo Bass", english.text("views.MixTableRow.soloTrack", "Bass"));
        assertEquals("Volume for Bass", english.text("views.MixTableRow.parameterOfTrack", "Volume", "Bass"));
    }

    @Test
    void clickingTheNumberSelectsThatTrack() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 1);

        row.numberLabel().dispatchEvent(pressOn(row.numberLabel()));

        assertEquals(1, editor.cursor().track());
    }

    @Test
    void clickingTheNameSelectsThatTrack() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 1);

        row.nameLabel().dispatchEvent(pressOn(row.nameLabel()));

        assertEquals(1, editor.cursor().track());
    }

    @Test
    void everyTrackStartsVisibleInTheMultitrackView() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertTrue(row.visibleCheckbox().isSelected());
    }

    @Test
    void togglingTheCheckboxHidesTheTrackFromTheMultitrackView() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);

        row.visibleCheckbox().doClick();

        assertFalse(model.isVisibleInMultitrackView(0));
        assertFalse(row.visibleCheckbox().isSelected());
    }

    @Test
    void changingThePortPushesItToTheEditor() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.portField().setValue(3);

        assertEquals(3, editor.score().track(0).channel().port());
    }

    @Test
    void changingTheChannelPushesItToTheEditor() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.channelField().setValue(7);

        assertEquals(7, editor.score().track(0).channel().number());
    }

    @Test
    void changingTheSecondChannelPushesItToTheEditor() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.effectChannelField().setValue(9);

        assertEquals(9, editor.score().track(0).channel().effectChannel());
    }

    @Test
    void showsTheTwoChannelsOfTheTrack() {
        Editor editor = twoTrackEditor();
        editor.setChannelNumber(0, 5);
        editor.setEffectChannel(0, 6);
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertEquals(5, row.channelField().getValue());
        assertEquals(6, row.effectChannelField().getValue());
    }

    @Test
    void reducingAllParametersHidesTheKnobColumnsOnly() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);

        model.reduceAllParameters();
        row.refresh();

        assertTrue(row.parameterCells().stream().noneMatch(java.awt.Component::isVisible));
        assertTrue(row.nameLabel().isVisible(), "the name stays visible when reducing");
        assertTrue(row.portField().isVisible(), "the port is not a sound parameter");
    }

    @Test
    void restoringShowsTheParametersAgain() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);
        model.reduceAllParameters();
        row.refresh();

        model.restoreAllParameters();
        row.refresh();

        assertTrue(row.parameterCells().stream().allMatch(java.awt.Component::isVisible));
    }

    @Test
    void aGuitarTrackOffersTheGeneralMidiInstruments() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertEquals(Instruments.names(), comboContents(row.instrumentField()));
    }

    @Test
    void aPercussionTrackOffersDrumKitsInsteadOfInstruments() {
        Editor editor = new Editor(new Score("Song", 120, List.of(Track.percussion("Drums"))));
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertEquals(DrumKits.names(), comboContents(row.instrumentField()));
    }

    @Test
    void thePercussionComboPreselectsTheTracksCurrentKit() {
        Track drums = Track.percussion("Drums").withChannel(Channel.percussion().withProgram(25));
        Editor editor = new Editor(new Score("Song", 120, List.of(drums)));

        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertEquals(DrumKits.indexOf(25), row.instrumentField().getSelectedIndex());
    }

    @Test
    void selectingADrumKitPushesItsGeneralMidiProgramToTheEditor() {
        Editor editor = new Editor(new Score("Song", 120, List.of(Track.percussion("Drums"))));
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.instrumentField().setSelectedIndex(DrumKits.indexOf(25));

        assertEquals(25, editor.score().track(0).channel().program());
    }

    @Test
    void controlsWithoutAVisibleLabelNameTheTrack() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 1);

        assertEquals("Mostrar pista Bajo", row.visibleCheckbox().getAccessibleContext().getAccessibleName());
        assertEquals("Silenciar Bajo", row.muteToggle().getAccessibleContext().getAccessibleName());
        assertEquals("Solo Bajo", row.soloToggle().getAccessibleContext().getAccessibleName());
        assertEquals("Puerto de Bajo", row.portField().getAccessibleContext().getAccessibleName());
        assertEquals("Canal MIDI de Bajo", row.channelField().getAccessibleContext().getAccessibleName());
        assertEquals(
                "Canal de efectos de Bajo", row.effectChannelField().getAccessibleContext().getAccessibleName());
        assertEquals("Instrumento de Bajo", row.instrumentField().getAccessibleContext().getAccessibleName());
    }

    @Test
    void eachSpinnersFieldShowsTwoFullDigits() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);
        row.setSize(row.getPreferredSize());
        forceLayoutOfEntireTreeWithoutARealWindow(row);

        JSpinner probe = new JSpinner(
                new SpinnerNumberModel(Channel.CHANNELS_PER_PORT, 1, Channel.CHANNELS_PER_PORT, 1));
        int needed = probe.getPreferredSize().width;

        for (JSpinner spinner : List.of(row.portField(), row.channelField(), row.effectChannelField())) {
            assertTrue(spinner.getWidth() >= needed,
                    "the spinner measures " + spinner.getWidth() + "px, needs at least " + needed
                            + "px to show two full digits");
        }
    }

    @Test
    void theInstrumentComboShowsTheLongestGeneralMidiBankNameWithoutTruncatingIt() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);
        String longest =
                Instruments.names().stream().max(Comparator.comparingInt(String::length)).orElseThrow();
        row.instrumentField().setSelectedItem(longest);
        row.setSize(row.getPreferredSize());
        forceLayoutOfEntireTreeWithoutARealWindow(row);

        JComboBox<String> probe = new JComboBox<>(new String[] {longest});
        int needed = probe.getPreferredSize().width;

        assertTrue(row.instrumentField().getWidth() >= needed,
                "the combo measures " + row.instrumentField().getWidth() + "px, needs at least " + needed
                        + "px to show \"" + longest + "\" in full");
    }

    private static Editor twoTrackEditor() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        return editor;
    }

    private static void forceLayoutOfEntireTreeWithoutARealWindow(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                forceLayoutOfEntireTreeWithoutARealWindow(child);
            }
        }
    }

    private static MouseEvent pressOn(java.awt.Component target) {
        return new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 2, 2, 1, false);
    }

    private static List<String> comboContents(JComboBox<String> combo) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            items.add(combo.getItemAt(i));
        }
        return items;
    }
}
