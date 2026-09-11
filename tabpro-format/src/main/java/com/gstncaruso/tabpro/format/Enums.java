package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;

final class Enums {

    private Enums() {
    }

    static <E extends Enum<E>> E read(Class<E> type, String name, E fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            throw ScoreFileException.damaged("unknown value for " + type.getSimpleName() + ": " + name, e);
        }
    }

    static <E extends Enum<E>> E required(Class<E> type, String name) {
        E value = read(type, name, null);
        if (value == null) {
            throw ScoreFileException.damaged("missing value of " + type.getSimpleName());
        }
        return value;
    }
}
