package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class RepeatPanel extends FormPanel {

    private final boolean initialRepeatOpen;
    private final JCheckBox repeatOpen = new JCheckBox(Texts.get("edit_dialogs.RepeatPanel.repeatOpen"));
    private final JSpinner repeatCount = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));

    public RepeatPanel(boolean repeatOpen, int repeatCount) {
        this.initialRepeatOpen = repeatOpen;
        this.repeatOpen.setSelected(repeatOpen);
        this.repeatCount.setValue(repeatCount);
        addFullWidthRow(this.repeatOpen);
        addRow(Texts.get("edit_dialogs.RepeatPanel.repeatCount"), this.repeatCount);
    }

    public boolean repeatOpenSelected() {
        return repeatOpen.isSelected();
    }

    public void setRepeatOpenSelected(boolean selected) {
        repeatOpen.setSelected(selected);
    }

    public boolean repeatOpenChanged() {
        return repeatOpenSelected() != initialRepeatOpen;
    }

    public int toRepeatCount() {
        return (Integer) repeatCount.getValue();
    }
}
