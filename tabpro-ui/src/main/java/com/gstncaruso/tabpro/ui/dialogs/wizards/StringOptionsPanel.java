package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/** Let ring, palm mute y dinamica, aplicados a las cuerdas elegidas en un rango de compases. */
public final class StringOptionsPanel extends FormPanel {

    private final List<JCheckBox> strings = new ArrayList<>();
    private final MeasureRangePanel range;
    private final JComboBox<ToggleChoice> letRing = new JComboBox<>(ToggleChoice.values());
    private final JComboBox<ToggleChoice> palmMute = new JComboBox<>(ToggleChoice.values());
    private final JComboBox<Dynamic> dynamic = new JComboBox<>(withNoChange());

    public StringOptionsPanel(int stringCount, int measureCount) {
        range = new MeasureRangePanel(measureCount);

        javax.swing.JPanel stringRow = new javax.swing.JPanel(new java.awt.GridLayout(1, 0, DialogStyle.GAP_S, 0));
        for (int string = 1; string <= stringCount; string++) {
            JCheckBox box = new JCheckBox(String.valueOf(string), true);
            strings.add(box);
            stringRow.add(box);
        }

        letRing.setRenderer((list, value, index, isSelected, hasFocus) -> new javax.swing.JLabel(value == null ? "" : value.label()));
        palmMute.setRenderer((list, value, index, isSelected, hasFocus) -> new javax.swing.JLabel(value == null ? "" : value.label()));
        dynamic.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "Sin cambios" : value.symbol()));

        addRow("Cuerdas", stringRow);
        addFullWidthRow(range);
        addRow("Let ring", letRing);
        addRow("Palm mute", palmMute);
        addRow("Dinamica", dynamic);
    }

    private static Dynamic[] withNoChange() {
        List<Dynamic> values = new ArrayList<>();
        values.add(null);
        values.addAll(List.of(Dynamic.values()));
        return values.toArray(Dynamic.values().clone());
    }

    public Set<Integer> selectedStrings() {
        return strings.stream()
                .filter(JCheckBox::isSelected)
                .map(box -> Integer.parseInt(box.getText()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public MeasureRange toMeasureRange() {
        return range.toMeasureRange();
    }

    public Optional<Boolean> letRingChange() {
        return ((ToggleChoice) letRing.getSelectedItem()).asChange();
    }

    public Optional<Boolean> palmMuteChange() {
        return ((ToggleChoice) palmMute.getSelectedItem()).asChange();
    }

    public Optional<Dynamic> dynamicChange() {
        return Optional.ofNullable((Dynamic) dynamic.getSelectedItem());
    }

    public void setStringSelected(int string, boolean selected) {
        strings.get(string - 1).setSelected(selected);
    }
}
