package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComboBox;

/** Los carteles y los saltos que puede llevar el final de un compas. */
public final class DirectionsPanel extends FormPanel {

    private final JComboBox<DirectionSymbol> symbol = new JComboBox<>(withNone(DirectionSymbol.values()));
    private final JComboBox<DirectionJump> jump = new JComboBox<>(withNone(DirectionJump.values()));

    public DirectionsPanel(Optional<DirectionSymbol> initialSymbol, Optional<DirectionJump> initialJump) {
        symbol.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "(Ninguno)" : value.label()));
        jump.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "(Ninguno)" : value.label()));

        addRow("Simbolo", symbol);
        addRow("Salto", jump);
        symbol.setSelectedItem(initialSymbol.orElse(null));
        jump.setSelectedItem(initialJump.orElse(null));
    }

    private static <T> T[] withNone(T[] values) {
        List<T> withNone = new ArrayList<>();
        withNone.add(null);
        withNone.addAll(List.of(values));
        return withNone.toArray(values.clone());
    }

    public DirectionSymbol toSymbol() {
        return (DirectionSymbol) symbol.getSelectedItem();
    }

    public DirectionJump toJump() {
        return (DirectionJump) jump.getSelectedItem();
    }
}
