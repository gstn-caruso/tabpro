package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Un bend o una palanca: el tipo base, la altura de un cuarto a tres tonos, y la
 * grilla donde se afina la curva a mano.
 */
public final class BendPanel extends FormPanel {

    private final JComboBox<BendType> type = new JComboBox<>(BendType.values());
    private final JSpinner height = new JSpinner(new SpinnerNumberModel(4, 1, BendPoint.MAX_QUARTER_TONES, 1));
    private final BendCurveEditor curve;
    private final BendGridPanel grid;

    public BendPanel(Bend initial) {
        type.setSelectedItem(initial.type());
        height.setValue(initial.peakQuarterTones() == 0 ? 4 : initial.peakQuarterTones());
        curve = BendCurveEditor.of(initial);
        grid = new BendGridPanel(curve);

        type.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.label()));

        type.addActionListener(event -> regenerate());
        height.addChangeListener(event -> regenerate());

        addRow("Tipo", type);
        addRow("Altura (" + toneLabel() + ")", height);
        addFullWidthRow(grid);
    }

    private String toneLabel() {
        return "1 = 1/4 tono, 4 = 1 tono, 12 = 3 tonos";
    }

    private void regenerate() {
        curve.reset(Bend.of(selectedType(), selectedHeight()).points());
        grid.repaint();
    }

    private BendType selectedType() {
        return (BendType) type.getSelectedItem();
    }

    private int selectedHeight() {
        return (Integer) height.getValue();
    }

    /** Clic en la grilla, expuesto para probar la decision sin simular el mouse. */
    public void clickAt(int position, int quarterTones) {
        curve.clickAt(position, quarterTones);
        grid.repaint();
    }

    public void rightClickAt(int position) {
        curve.addVibratoAt(position);
        grid.repaint();
    }

    public Bend toBend() {
        return curve.toBend(selectedType());
    }
}
