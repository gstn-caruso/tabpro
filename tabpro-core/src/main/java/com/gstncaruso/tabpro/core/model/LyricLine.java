package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Una linea de letra: el texto tal como lo escribio el usuario y el compas desde
 * el que se reparte. Las silabas se separan con un espacio o un guion; un mas
 * une dos palabras, lo que va entre corchetes no se dibuja, y varios saltos de
 * linea seguidos cuentan como un solo espacio.
 */
public record LyricLine(int startingMeasure, String text) {

    public static final int MAX_LINES = 5;

    public LyricLine {
        if (startingMeasure < 1) {
            throw new IllegalArgumentException("el compas inicial se cuenta desde 1: " + startingMeasure);
        }
        text = text == null ? "" : text;
    }

    public static LyricLine empty() {
        return new LyricLine(1, "");
    }

    public boolean isEmpty() {
        return syllables().isEmpty();
    }

    /**
     * Las silabas en el orden en que caen sobre los beats. Una silaba vacia deja
     * el beat sin texto.
     */
    public List<String> syllables() {
        List<String> syllables = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char character : readable().toCharArray()) {
            if (character == ' ' || character == '-') {
                syllables.add(current.toString());
                current.setLength(0);
            } else if (character == '+') {
                current.append(' ');
            } else {
                current.append(character);
            }
        }
        syllables.add(current.toString());
        return withoutTrailingBlanks(syllables);
    }

    /** El texto sin los comentarios entre corchetes y con los saltos hechos espacios. */
    private String readable() {
        return text.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\R+", " ");
    }

    private static List<String> withoutTrailingBlanks(List<String> syllables) {
        List<String> trimmed = new ArrayList<>(syllables);
        while (!trimmed.isEmpty() && trimmed.getLast().isEmpty()) {
            trimmed.removeLast();
        }
        return List.copyOf(trimmed);
    }

    public LyricLine startingAt(int measure) {
        return new LyricLine(measure, text);
    }

    public LyricLine saying(String text) {
        return new LyricLine(startingMeasure, text);
    }
}
