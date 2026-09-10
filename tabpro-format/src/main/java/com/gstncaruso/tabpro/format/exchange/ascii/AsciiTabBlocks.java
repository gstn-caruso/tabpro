package com.gstncaruso.tabpro.format.exchange.ascii;

import java.util.ArrayList;
import java.util.List;

final class AsciiTabBlocks {

    private AsciiTabBlocks() {
    }

    static List<List<String>> blocksIn(String text) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            if (isTabLine(line)) {
                current.add(line.strip());
                continue;
            }
            if (!current.isEmpty()) {
                blocks.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }
        return blocks;
    }

    private static boolean isTabLine(String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty() || trimmed.indexOf('-') < 0) {
            return false;
        }
        return trimmed.chars().allMatch(c -> c == '-' || c == '|' || Character.isDigit(c));
    }
}
