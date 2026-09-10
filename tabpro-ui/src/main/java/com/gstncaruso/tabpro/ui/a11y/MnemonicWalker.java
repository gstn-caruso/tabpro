package com.gstncaruso.tabpro.ui.a11y;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public final class MnemonicWalker {

    public List<Violation> walkMenuBar(JMenuBar bar) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            entries.add(new Entry(bar.getMenu(i).getText(), bar.getMenu(i).getMnemonic()));
        }
        return violationsOf(entries);
    }

    public List<Violation> walkMenu(JMenu menu) {
        List<Entry> entries = new ArrayList<>();
        for (Component component : menu.getMenuComponents()) {
            if (component instanceof JMenuItem item) {
                entries.add(new Entry(item.getText(), item.getMnemonic()));
            }
        }
        return violationsOf(entries);
    }

    /**
     * Un formulario anidado ({@link MnemonicScope}) es su propio ambito: dos formularios
     * distintos pueden compartir letra sin que sea un choque, porque cada uno la asigno sin
     * saber del otro.
     */
    public List<Violation> walkForm(Container form) {
        List<Entry> ownEntries = new ArrayList<>();
        List<Violation> violations = new ArrayList<>();
        collectLabeledFields(form, ownEntries, violations);
        violations.addAll(violationsOf(ownEntries));
        return violations;
    }

    private void collectLabeledFields(Component component, List<Entry> scope, List<Violation> violations) {
        if (component instanceof JLabel label && label.getLabelFor() != null) {
            scope.add(new Entry(label.getText(), label.getDisplayedMnemonic()));
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                if (child instanceof MnemonicScope) {
                    List<Entry> childScope = new ArrayList<>();
                    collectLabeledFields(child, childScope, violations);
                    violations.addAll(violationsOf(childScope));
                } else {
                    collectLabeledFields(child, scope, violations);
                }
            }
        }
    }

    private List<Violation> violationsOf(List<Entry> entries) {
        List<Violation> violations = new ArrayList<>();
        Map<Integer, List<String>> byMnemonic = new LinkedHashMap<>();
        for (Entry entry : entries) {
            if (entry.mnemonic() == 0) {
                violations.add(new Violation(entry.text(), "sin mnemónico"));
                continue;
            }
            byMnemonic.computeIfAbsent(entry.mnemonic(), key -> new ArrayList<>()).add(entry.text());
        }
        for (List<String> texts : byMnemonic.values()) {
            if (texts.size() > 1) {
                for (String text : texts) {
                    violations.add(new Violation(text, "mnemónico repetido"));
                }
            }
        }
        return violations;
    }

    private record Entry(String text, int mnemonic) {
    }
}
