package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class GlobalUiMutationScan {

    private static final Pattern INSTALLS_THE_THEME = Pattern.compile("Theme\\.install\\(");
    private static final Pattern ISOLATED = Pattern.compile("@Isolated\\b");

    private GlobalUiMutationScan() {
    }

    static List<Path> unisolatedMutators(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .filter(GlobalUiMutationScan::mutatesGlobalUiState)
                    .filter(path -> !isIsolated(path))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean mutatesGlobalUiState(Path file) {
        return INSTALLS_THE_THEME.matcher(read(file)).find();
    }

    private static boolean isIsolated(Path file) {
        return ISOLATED.matcher(read(file)).find();
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
