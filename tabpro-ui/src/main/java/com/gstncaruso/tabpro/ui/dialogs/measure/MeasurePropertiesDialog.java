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

    public static String timeSignatureTabTitle() {
        return Texts.get("edit_dialogs.MeasurePropertiesDialog.timeSignature");
    }

    public static String keySignatureTabTitle() {
        return Texts.get("edit_dialogs.MeasurePropertiesDialog.keySignature");
    }

    public static String tripletFeelTabTitle() {
        return Texts.get("edit_dialogs.MeasurePropertiesDialog.tripletFeel");
    }

    public static String repeatTabTitle() {
        return Texts.get("edit_dialogs.MeasurePropertiesDialog.repeat");
    }

    public static String alternateEndingsTabTitle() {
        return Texts.get("edit_dialogs.AlternateEndingsPanel.title");
    }

    public static String directionsTabTitle() {
        return Texts.get("edit_dialogs.MeasurePropertiesDialog.directions");
    }

    public static void show(Component parent, Editor editor) {
        show(parent, editor, timeSignatureTabTitle());
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
        tabs.addTab(timeSignatureTabTitle(), timeSignaturePanel);
        tabs.addTab(keySignatureTabTitle(), keySignaturePanel);
        tabs.addTab(tripletFeelTabTitle(), tripletFeelPanel);
        tabs.addTab(repeatTabTitle(), repeatPanel);
        tabs.addTab(alternateEndingsTabTitle(), alternateEndingsPanel);
        tabs.addTab(directionsTabTitle(), directionsPanel);

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
