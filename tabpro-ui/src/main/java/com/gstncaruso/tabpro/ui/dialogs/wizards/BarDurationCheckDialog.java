package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** El asistente Verificar duracion de compases [F4]: la lista de los que no cierran. */
public final class BarDurationCheckDialog {

    private BarDurationCheckDialog() {
    }

    public static void show(Component parent, Editor editor) {
        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(editor.score());

        JPanel content = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(content);
        if (findings.isEmpty()) {
            content.add(new JLabel("Todos los compases cierran su medida."), BorderLayout.CENTER);
        } else {
            JList<String> list = new JList<>(findings.stream().map(BarDurationReport::describe).toArray(String[]::new));
            content.add(new JScrollPane(list), BorderLayout.CENTER);
        }

        DialogShell.show(parent, "Verificar duracion de compases", content);
    }
}
