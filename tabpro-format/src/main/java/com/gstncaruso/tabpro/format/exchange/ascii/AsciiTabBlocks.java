package com.gstncaruso.tabpro.format.exchange.ascii;

import java.util.ArrayList;
import java.util.List;

/**
 * Reconoce los bloques de tablatura de un texto cualquiera: corridas de lineas seguidas hechas
 * solo de guiones, digitos y barras, sin lineas vacias en el medio. Todo lo demas (comentarios,
 * lineas en blanco) separa un bloque del siguiente y se ignora.
 */
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
