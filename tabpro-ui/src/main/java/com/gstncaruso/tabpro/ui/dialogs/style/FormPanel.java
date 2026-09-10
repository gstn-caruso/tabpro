package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import com.gstncaruso.tabpro.ui.a11y.MnemonicScope;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class FormPanel extends JPanel implements MnemonicScope {

    private final MnemonicAssigner labelMnemonics = new MnemonicAssigner();
    private final FormRows sections;
    private FormRows currentRows;

    public FormPanel() {
        DialogStyle.padded(this);
        sections = new FormRows(this, labelMnemonics);
        currentRows = sections;
    }

    public FormPanel addRow(String label, JComponent field) {
        currentRows.addRow(label, field);
        return this;
    }

    public FormPanel addRow(String label, JComponent field, JComponent trailing) {
        currentRows.addRow(label, field, trailing);
        return this;
    }

    public FormPanel addFullWidthRow(JComponent component) {
        currentRows.addFullWidthRow(component);
        return this;
    }

    public FormPanel addSection(String title) {
        JPanel section = DialogStyle.section(title);
        sections.addFullWidthRow(section);
        currentRows = new FormRows(section, labelMnemonics);
        return this;
    }

    public Section newDetachedSection(String title) {
        return new Section(DialogStyle.section(title));
    }

    public FormPanel addSideBySide(Section left, Section right) {
        JPanel row = new JPanel(new GridLayout(1, 2, DialogStyle.GAP_M, 0));
        row.setOpaque(false);
        row.add(left.panel);
        row.add(right.panel);
        return addFullWidthRow(row);
    }

    public final class Section {

        private final JPanel panel;
        private final FormRows rows;

        private Section(JPanel panel) {
            this.panel = panel;
            this.rows = new FormRows(panel, labelMnemonics);
        }

        public Section addRow(String label, JComponent field) {
            rows.addRow(label, field);
            return this;
        }

        public Section addRow(String label, JComponent field, JComponent trailing) {
            rows.addRow(label, field, trailing);
            return this;
        }

        public JPanel asComponent() {
            return panel;
        }
    }
}
