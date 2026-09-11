package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public final class BarDurationCheckDialog {

    private BarDurationCheckDialog() {
    }

    public static void show(Component parent, Editor editor) {
        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(editor.score());
        DialogShell.show(parent, Texts.get("score_dialogs.BarDurationCheckDialog.title"), buildContent(findings));
    }

    static JPanel buildContent(List<BarDurationCheck.Finding> findings) {
        JPanel content = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(content);
        if (findings.isEmpty()) {
            content.add(new JLabel(Texts.get("score_dialogs.BarDurationCheckDialog.allBarsComplete")), BorderLayout.CENTER);
        } else {
            JList<String> list = new JList<>(findings.stream().map(BarDurationReport::describe).toArray(String[]::new));
            list.getAccessibleContext().setAccessibleName(Texts.get("score_dialogs.BarDurationCheckDialog.incompleteBars"));
            list.setToolTipText(Texts.get("score_dialogs.BarDurationCheckDialog.incompleteBars"));
            content.add(new JScrollPane(list), BorderLayout.CENTER);
        }
        return content;
    }
}
