package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class GraceNotePanel extends FormPanel {

    private final JSpinner fret = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JCheckBox onBeat = new JCheckBox(Texts.get("edit_dialogs.GraceNotePanel.onBeat"));
    private final JComboBox<NoteValue> duration = new JComboBox<>(NoteValue.values());
    private final JComboBox<Dynamic> dynamic = new JComboBox<>(Dynamic.values());
    private final JComboBox<GraceTransition> transition = new JComboBox<>(GraceTransition.values());
    private final boolean initialDead;

    public GraceNotePanel(GraceNote initial) {
        this.initialDead = initial.dead();
        duration.setRenderer(new LabeledListCellRenderer());
        dynamic.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.symbol()));
        transition.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : Labels.of(value)));

        addRow(Texts.get("edit_dialogs.GraceNotePanel.fret"), fret);
        addFullWidthRow(onBeat);
        addRow(Texts.get("edit_dialogs.shared.duration"), duration);
        addRow(Texts.get("edit_dialogs.shared.dynamic"), dynamic);
        addRow(Texts.get("edit_dialogs.GraceNotePanel.transition"), transition);

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
