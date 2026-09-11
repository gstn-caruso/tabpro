package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class JavaSources {

    private static final String TEXT_BLOCK_QUOTES = "\"\"\"";

    private JavaSources() {
    }

    static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String withoutComments(String source) {
        StringBuilder withoutComments = new StringBuilder();
        int index = 0;
        int length = source.length();
        while (index < length) {
            char current = source.charAt(index);
            if (current == '/' && index + 1 < length && source.charAt(index + 1) == '/') {
                int lineEnd = source.indexOf('\n', index);
                index = lineEnd == -1 ? length : lineEnd;
                continue;
            }
            if (current == '/' && index + 1 < length && source.charAt(index + 1) == '*') {
                int blockEnd = source.indexOf("*/", index + 2);
                index = blockEnd == -1 ? length : blockEnd + 2;
                continue;
            }
            withoutComments.append(current);
            index++;
        }
        return withoutComments.toString();
    }

    static List<String> stringLiterals(String source) {
        List<String> literals = new ArrayList<>();
        int index = 0;
        int length = source.length();
        while (index < length) {
            if (source.startsWith("//", index)) {
                int lineEnd = source.indexOf('\n', index);
                index = lineEnd == -1 ? length : lineEnd;
            } else if (source.startsWith("/*", index)) {
                int blockEnd = source.indexOf("*/", index + 2);
                index = blockEnd == -1 ? length : blockEnd + 2;
            } else if (source.startsWith(TEXT_BLOCK_QUOTES, index)) {
                int blockEnd = source.indexOf(TEXT_BLOCK_QUOTES, index + 3);
                int end = blockEnd == -1 ? length : blockEnd;
                literals.add(source.substring(index + 3, end));
                index = Math.min(length, end + 3);
            } else if (source.charAt(index) == '"') {
                int end = closingQuote(source, index + 1, '"');
                literals.add(source.substring(index + 1, end));
                index = end + 1;
            } else if (source.charAt(index) == '\'') {
                index = closingQuote(source, index + 1, '\'') + 1;
            } else {
                index++;
            }
        }
        return literals;
    }

    private static int closingQuote(String source, int from, char quote) {
        int index = from;
        while (index < source.length() && source.charAt(index) != quote && source.charAt(index) != '\n') {
            index += source.charAt(index) == '\\' ? 2 : 1;
        }
        return Math.min(index, source.length());
    }
}
