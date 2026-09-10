package com.gstncaruso.tabpro.ui.a11y;

import java.awt.event.KeyEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JLabel;

public final class MnemonicAssigner {

    private final Set<Character> taken = new LinkedHashSet<>();

    public void reserve(char letter) {
        taken.add(normalize(letter));
    }

    public int chooseIndex(String text) {
        for (int index : candidateIndexes(text)) {
            char normalized = normalize(text.charAt(index));
            if (taken.add(normalized)) {
                return index;
            }
        }
        return -1;
    }

    public void applyTo(AbstractButton button) {
        int index = chooseIndex(button.getText());
        if (index < 0) {
            return;
        }
        button.setMnemonic(keyCodeAt(button.getText(), index));
        button.setDisplayedMnemonicIndex(index);
    }

    public void applyTo(JLabel label) {
        int index = chooseIndex(label.getText());
        if (index < 0) {
            return;
        }
        label.setDisplayedMnemonic(keyCodeAt(label.getText(), index));
        label.setDisplayedMnemonicIndex(index);
    }

    private int keyCodeAt(String text, int index) {
        return KeyEvent.getExtendedKeyCodeForChar(normalize(text.charAt(index)));
    }

    private List<Integer> candidateIndexes(String text) {
        List<Integer> wordInitials = wordInitialIndexes(text);
        List<Integer> candidates = new ArrayList<>(wordInitials);
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i)) && !wordInitials.contains(i)) {
                candidates.add(i);
            }
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                candidates.add(i);
            }
        }
        return candidates;
    }

    private List<Integer> wordInitialIndexes(String text) {
        List<Integer> indexes = new ArrayList<>();
        boolean atWordStart = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                if (atWordStart) {
                    indexes.add(i);
                }
                atWordStart = false;
            } else {
                atWordStart = true;
            }
        }
        return indexes;
    }

    private char normalize(char c) {
        String stripped = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        char base = stripped.isEmpty() ? c : stripped.charAt(0);
        return Character.toUpperCase(base);
    }
}
