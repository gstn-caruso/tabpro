package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** La notita de adorno: su traste, cuando cae, cuanto dura, que tan fuerte suena y como enlaza. */
public final class GraceNotePanel extends FormPanel {

    private final JSpinner fret = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JCheckBox onBeat = new JCheckBox("Sobre el beat (en vez de antes)");
    private final JComboBox<NoteValue> duration = new JComboBox<>(NoteValue.values());
    private final JComboBox<Dynamic> dynamic = new JComboBox<>(Dynamic.values());
    private final JComboBox<GraceTransition> transition = new JComboBox<>(GraceTransition.values());
    private final boolean initialDead;

    public GraceNotePanel(GraceNote initial) {
        this.initialDead = initial.dead();
        dynamic.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.symbol()));
        transition.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.label()));

        addRow("Traste", fret);
        addFullWidthRow(onBeat);
        addRow("Duracion", duration);
        addRow("Dinamica", dynamic);
        addRow("Transicion", transition);

        apply(initial);
    }

    public void apply(GraceNote grace) {
        fret.setValue(grace.fret());
        onBeat.setSelected(grace.onBeat());
        duration.setSelectedItem(grace.duration());
        dynamic.setSelectedItem(grace.dynamic());
        transition.setSelectedItem(grace.transition());
    }

    public GraceNote toGraceNote() {
        return new GraceNote(
                (Integer) fret.getValue(),
                (NoteValue) duration.getSelectedItem(),
                (Dynamic) dynamic.getSelectedItem(),
                (GraceTransition) transition.getSelectedItem(),
                onBeat.isSelected(),
                initialDead);
    }
}
