package com.gstncaruso.tabpro.ui.browser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Busca partituras en el disco, como el explorador que describe el manual en
 * "Browse Guitar Pro Files on Your Computer".
 */
public final class ScoreSearch {

    /** Las extensiones que el explorador reconoce como partitura. */
    public static final List<String> EXTENSIONS = List.of(".tabpro", ".gp3", ".gp4", ".gp5", ".gtp");

    private static final int DEEP_SEARCH = Integer.MAX_VALUE;
    private static final int SHALLOW_SEARCH = 1;

    private ScoreSearch() {
    }

    public static List<Path> inFolder(Path folder) {
        return search(folder, SHALLOW_SEARCH);
    }

    public static List<Path> inFolderAndBelow(Path folder) {
        return search(folder, DEEP_SEARCH);
    }

    public static boolean isAScore(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private static List<Path> search(Path folder, int depth) {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> found = Files.walk(folder, depth)) {
            return found.filter(Files::isRegularFile)
                    .filter(ScoreSearch::isAScore)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo recorrer " + folder, e);
        }
    }
}
