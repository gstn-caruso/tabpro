package com.gstncaruso.tabpro.ui.a11y;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuBar;

public final class MnemonicWalker {

    public List<Violation> walkMenuBar(JMenuBar bar) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            entries.add(new Entry(bar.getMenu(i).getText(), bar.getMenu(i).getMnemonic()));
        }
        return violationsOf(entries);
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
