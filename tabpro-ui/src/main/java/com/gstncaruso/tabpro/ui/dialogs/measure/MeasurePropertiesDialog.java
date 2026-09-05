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

    public static void show(Component parent, Editor editor) {
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
}
