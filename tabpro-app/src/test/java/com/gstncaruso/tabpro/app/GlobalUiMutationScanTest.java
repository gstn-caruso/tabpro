package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Swing tiene un solo UIManager por maquina virtual y la suite corre las clases de test en
 * paralelo dentro de una sola maquina virtual (ver tabpro-tests/pom.xml): un test que instala un
 * tema o muta el look-and-feel global (Theme.install/apply, useFontSize, useHighContrast,
 * FlatLaf.setup o updateUI, UIManager.put o setLookAndFeel) puede correr al mismo tiempo que otro
 * test que mide componentes reales contra ese mismo UIManager. Toda clase de test que toque ese
 * estado global tiene que anotarse {@code @Isolated}, o el resultado depende de con que otro test
 * le toco correr a la vez.
 */
class GlobalUiMutationScanTest {

    @Test
    void anEmptySourceTreeHasNoUnisolatedMutators(@TempDir Path root) {
        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void aTestThatInstallsTheThemeWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "InstallsTheTheme.java", """
                class InstallsTheTheme {
                    void installs() {
                        Theme.install();
                    }
                }
                """);
        write(root, "DoesNothingWithTheTheme.java", """
                class DoesNothingWithTheTheme {
                    void innocent() {
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void anIsolatedTestThatInstallsTheThemeIsNotFlagged(@TempDir Path root) throws IOException {
        write(root, "InstallsTheThemeIsolated.java", """
                @Isolated
                class InstallsTheThemeIsolated {
                    void installs() {
                        Theme.install();
                    }
                }
                """);

        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    private static Path write(Path root, String fileName, String content) throws IOException {
        Path file = root.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }
}
