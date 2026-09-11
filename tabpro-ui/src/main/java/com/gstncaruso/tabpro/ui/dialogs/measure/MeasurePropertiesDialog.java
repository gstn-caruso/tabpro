package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import javax.swing.JTabbedPane;

public final class MeasurePropertiesDialog {

    private MeasurePropertiesDialog() {
    }

    public static final String TIME_SIGNATURE = Texts.get("edit_dialogs.MeasurePropertiesDialog.timeSignature");
    public static final String KEY_SIGNATURE = Texts.get("edit_dialogs.MeasurePropertiesDialog.keySignature");
    public static final String TRIPLET_FEEL = Texts.get("edit_dialogs.MeasurePropertiesDialog.tripletFeel");
    public static final String REPEAT = Texts.get("edit_dialogs.MeasurePropertiesDialog.repeat");
    public static final String ALTERNATE_ENDINGS = Texts.get("edit_dialogs.AlternateEndingsPanel.title");
    public static final String DIRECTIONS = Texts.get("edit_dialogs.MeasurePropertiesDialog.directions");

    public static void show(Component parent, Editor editor) {
        show(parent, editor, TIME_SIGNATURE);
    }

    public static void show(Component parent, Editor editor, String openOn) {
        Score score = editor.score();
        int measureIndex = editor.cursor().measure();
        MeasureAttributes attributes = score.attributesOf(measureIndex);

        TimeSignaturePanel timeSignaturePanel = new TimeSignaturePanel(score.timeSignatureOf(measureIndex));
        KeySignaturePanel keySignaturePanel = new KeySignaturePanel(attributes.keySignature());
        TripletFeelPanel tripletFeelPanel = new TripletFeelPanel(attributes.tripletFeel());
        RepeatPanel repeatPanel = new RepeatPanel(attributes.repeatOpen(), attributes.repeatCount());
        AlternateEndingsPanel alternateEndingsPanel = new AlternateEndingsPanel(attributes.alternateEndings());
        DirectionsPanel directionsPanel = new DirectionsPanel(attributes.symbol(), attributes.jump());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(TIME_SIGNATURE, timeSignaturePanel);
        tabs.addTab(KEY_SIGNATURE, keySignaturePanel);
        tabs.addTab(TRIPLET_FEEL, tripletFeelPanel);
        tabs.addTab(REPEAT, repeatPanel);
        tabs.addTab(ALTERNATE_ENDINGS, alternateEndingsPanel);
        tabs.addTab(DIRECTIONS, directionsPanel);

        selectTab(tabs, openOn);
        boolean accepted = DialogShell.ask(parent, Texts.get("edit_dialogs.MeasurePropertiesDialog.title"), tabs);
        if (!accepted) {
            return;
        }
        editor.setTimeSignature(timeSignaturePanel.toTimeSignature());
        editor.setKeySignature(keySignaturePanel.toKeySignature());
        editor.setTripletFeel(tripletFeelPanel.toTripletFeel());
        if (repeatPanel.repeatOpenChanged()) {
            editor.toggleRepeatOpen();
        }
        editor.setRepeatCount(repeatPanel.toRepeatCount());
        editor.setAlternateEndings(alternateEndingsPanel.toAlternateEndings());
        editor.setDirectionSymbol(directionsPanel.toSymbol());
        editor.setDirectionJump(directionsPanel.toJump());
    }

    private static void selectTab(JTabbedPane tabs, String title) {
        int index = tabs.indexOfTab(title);
        if (index >= 0) {
            tabs.setSelectedIndex(index);
        }
    }
}
