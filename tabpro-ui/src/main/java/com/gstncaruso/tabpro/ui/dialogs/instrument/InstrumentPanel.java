package com.gstncaruso.tabpro.ui.dialogs.instrument;

import com.gstncaruso.tabpro.core.model.InstrumentPatch;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** El sonido de la pista, buscable por nombre -el de General MIDI, o el del patch elegido en MIDI Setup. */
public final class InstrumentPanel extends JPanel {

    private final InstrumentPatch patch;
    private final JTextField search = new JTextField();
    private final DefaultListModel<Integer> model = new DefaultListModel<>();
    private final JList<Integer> list = new JList<>(model);

    public InstrumentPanel(int initialProgram) {
        this(initialProgram, InstrumentPatch.generalMidi());
    }

    public InstrumentPanel(int initialProgram, InstrumentPatch patch) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        this.patch = patch;
        DialogStyle.padded(this);
        list.setCellRenderer((jlist, program, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(program + " - " + patch.nameOf(program)));

        add(search, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        search.getDocument().addDocumentListener((SimpleDocumentListener) event -> refresh(search.getText()));
        refresh("");
        selectProgram(initialProgram);
    }

    private void refresh(String query) {
        Integer previous = list.getSelectedValue();
        model.clear();
        for (int program : InstrumentSearch.matching(query, patch)) {
            model.addElement(program);
        }
        if (previous != null && model.contains(previous)) {
            list.setSelectedValue(previous, true);
        } else if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    public void selectProgram(int program) {
        search.setText("");
        list.setSelectedValue(program, true);
    }

    public int selectedProgram() {
        Integer selected = list.getSelectedValue();
        return selected == null ? 0 : selected;
    }

    public List<Integer> visiblePrograms() {
        return List.copyOf(java.util.Collections.list(model.elements()));
    }

    public void search(String query) {
        search.setText(query);
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends DocumentListener {
        void onChange(DocumentEvent event);

        @Override
        default void insertUpdate(DocumentEvent e) {
            onChange(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            onChange(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            onChange(e);
        }
    }
}
