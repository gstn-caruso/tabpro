package com.gstncaruso.tabpro.ui.a11y;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MnemonicAssigner {

    private final Set<Character> taken = new LinkedHashSet<>();

    public int chooseIndex(String text) {
        for (int index : candidateIndexes(text)) {
            char normalized = normalize(text.charAt(index));
            if (taken.add(normalized)) {
                return index;
            }
        }
        return -1;
    }

    private List<Integer> candidateIndexes(String text) {
        List<Integer> wordInitials = wordInitialIndexes(text);
        List<Integer> candidates = new ArrayList<>(wordInitials);
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i)) && !wordInitials.contains(i)) {
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
