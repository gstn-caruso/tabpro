package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class JavaSources {

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
}
