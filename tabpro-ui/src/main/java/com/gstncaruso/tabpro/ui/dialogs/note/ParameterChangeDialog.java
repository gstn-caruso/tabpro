package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class ParameterChangeDialog {

    private ParameterChangeDialog() {
    }

    public static void show(Component parent, Editor editor) {
        ParameterChange current = editor.currentBeat().effects().parameterChange();
        Fields fields = buildFields(current, editor);

        if (!DialogShell.ask(parent, "Cambio de parámetros", fields.form())) {
            return;
        }
        ParameterChange change = ParameterChange.nothing()
                .over((Integer) fields.transition().getValue())
                .onEveryTrack(fields.everyTrack().isSelected());
        for (SoundParameter parameter : SoundParameter.values()) {
            if (fields.enabled().get(parameter).isSelected()) {
                change = change.changing(parameter, (Integer) fields.values().get(parameter).getValue());
            }
        }
        editor.setParameterChange(change.isEmpty() ? null : change);
    }

    static Fields buildFields(ParameterChange current, Editor editor) {
        Map<SoundParameter, JCheckBox> enabled = new EnumMap<>(SoundParameter.class);
        Map<SoundParameter, JSpinner> values = new EnumMap<>(SoundParameter.class);

        FormPanel form = new FormPanel();
        for (SoundParameter parameter : SoundParameter.values()) {
            JCheckBox box = new JCheckBox(parameter.label(), current.changes(parameter));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                    current.valueOf(parameter).orElse(defaultOf(parameter, editor)),
                    parameter.minimum(), parameter.maximum(), 1));
            spinner.setEnabled(box.isSelected());
            spinner.getAccessibleContext().setAccessibleName(parameter.label());
            spinner.setToolTipText(parameter.label());
            box.addActionListener(event -> spinner.setEnabled(box.isSelected()));
            enabled.put(parameter, box);
            values.put(parameter, spinner);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.add(box, BorderLayout.WEST);
            row.add(spinner, BorderLayout.CENTER);
            form.addFullWidthRow(row);
        }
        JSpinner transition = new JSpinner(new SpinnerNumberModel(current.transitionBeats(), 0, 64, 1));
        JCheckBox everyTrack = new JCheckBox("Aplicar a todas las pistas", current.everyTrack());
        form.addRow("Transición (beats)", transition);
        form.addFullWidthRow(everyTrack);

        return new Fields(form, enabled, values, transition, everyTrack);
    }

    record Fields(
            FormPanel form,
            Map<SoundParameter, JCheckBox> enabled,
            Map<SoundParameter, JSpinner> values,
            JSpinner transition,
            JCheckBox everyTrack) {
    }

    private static int defaultOf(SoundParameter parameter, Editor editor) {
        var channel = editor.currentTrack().channel();
        return switch (parameter) {
            case PROGRAM -> channel.program();
            case VOLUME -> channel.volume();
            case PAN -> channel.pan();
            case CHORUS -> channel.chorus();
            case REVERB -> channel.reverb();
            case PHASER -> channel.phaser();
            case TREMOLO -> channel.tremolo();
            case TEMPO -> editor.score().tempo();
        };
    }
}
