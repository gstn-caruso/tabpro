package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import javax.swing.JTabbedPane;

/** La ventana de propiedades del compas: medida, armadura, triplet feel, repeticion y direcciones. */
public final class MeasurePropertiesDialog {

    private MeasurePropertiesDialog() {
    }

    /** Los nombres de las solapas, para que cada comando del menu abra la suya. */
    public static final String TIME_SIGNATURE = "Medida";
    public static final String KEY_SIGNATURE = "Armadura";
    public static final String TRIPLET_FEEL = "Triplet feel";
    public static final String REPEAT = "Repeticion";
    public static final String ALTERNATE_ENDINGS = "Finales alternativos";
    public static final String DIRECTIONS = "Direcciones";

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
        tabs.addTab("Medida", timeSignaturePanel);
        tabs.addTab("Armadura", keySignaturePanel);
        tabs.addTab("Triplet feel", tripletFeelPanel);
        tabs.addTab("Repeticion", repeatPanel);
        tabs.addTab("Finales alternativos", alternateEndingsPanel);
        tabs.addTab("Direcciones", directionsPanel);

        selectTab(tabs, openOn);
        boolean accepted = DialogShell.ask(parent, "Propiedades del compas", tabs);
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

    /** Abre la ventana ya parada en la solapa que pidio el menu. */
    private static void selectTab(JTabbedPane tabs, String title) {
        int index = tabs.indexOfTab(title);
        if (index >= 0) {
            tabs.setSelectedIndex(index);
        }
    }
}
