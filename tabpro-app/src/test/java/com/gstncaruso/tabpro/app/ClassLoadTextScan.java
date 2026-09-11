package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class ClassLoadTextScan {

    private static final Pattern STATIC_FINAL_FIELD_READING_TEXTS =
            Pattern.compile("\\bstatic\\s+final\\b[^;{(]*=[^;]*\\bTexts\\.get\\(");

    private ClassLoadTextScan() {
    }

    static List<Path> filesReadingTextAtClassLoad(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .filter(ClassLoadTextScan::readsTextAtClassLoad)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean readsTextAtClassLoad(Path file) {
        return STATIC_FINAL_FIELD_READING_TEXTS.matcher(JavaSources.withoutComments(JavaSources.read(file))).find();
    }
}
