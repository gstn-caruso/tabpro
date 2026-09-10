package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UnisolatedGlobalUiStateMutationsAreFlaggedTest {

    @Test
    void anEmptySourceTreeHasNoUnisolatedMutators(@TempDir Path root) {
        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void aTestThatInstallsTheThemeWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "InstallsTheTheme.java", """
                class InstallsTheTheme {
                    @Test
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
                    @Test
                    void installs() {
                        Theme.install();
                    }
                }
                """);

        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void aTestThatChangesTheInterfaceFontSizeWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "ChangesTheFontSize.java", """
                class ChangesTheFontSize {
                    @Test
                    void changes() {
                        theme.useFontSize(16);
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatTogglesHighContrastWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "TogglesHighContrast.java", """
                class TogglesHighContrast {
                    @Test
                    void toggles() {
                        theme.useHighContrast(true);
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatSetsUpFlatLafWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "SetsUpFlatLaf.java", """
                class SetsUpFlatLaf {
                    @Test
                    void setsUp() {
                        FlatDarkLaf.setup();
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatUpdatesTheFlatLafUiWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "UpdatesTheFlatLafUi.java", """
                class UpdatesTheFlatLafUi {
                    @Test
                    void updates() {
                        FlatLaf.updateUI();
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatWritesToUiManagerWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "WritesToUiManager.java", """
                class WritesToUiManager {
                    @Test
                    void writes() {
                        UIManager.put("defaultFont", font);
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatSwitchesTheLookAndFeelWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "SwitchesTheLookAndFeel.java", """
                class SwitchesTheLookAndFeel {
                    @Test
                    void switches() throws Exception {
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aTestThatAppliesARealThemeWithoutIsolationIsFlagged(@TempDir Path root) throws IOException {
        Path culprit = write(root, "AppliesARealTheme.java", """
                class AppliesARealTheme {
                    private final Theme theme = new Theme();

                    @Test
                    void applies() {
                        theme.apply(Theme.LIGHT);
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aPanelThatAppliesAValueIsNotFlagged(@TempDir Path root) throws IOException {
        write(root, "AppliesAValueToAPanel.java", """
                class AppliesAValueToAPanel {
                    @Test
                    void applies() {
                        panel.apply(new Preferences());
                    }
                }
                """);

        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void aClassWithSeveralMutationsIsFlaggedOnlyOnce(@TempDir Path root) throws IOException {
        Path culprit = write(root, "MutatesSeveralThings.java", """
                class MutatesSeveralThings {
                    @Test
                    void mutates() {
                        Theme.install();
                        UIManager.put("defaultFont", font);
                        FlatLaf.updateUI();
                    }
                }
                """);

        assertEquals(List.of(culprit), GlobalUiMutationScan.unisolatedMutators(root));
    }

    @Test
    void aMutationMentionedOnlyInACommentIsNotFlagged(@TempDir Path root) throws IOException {
        write(root, "MentionsTheThemeInAComment.java", """
                class MentionsTheThemeInAComment {
                    // "No animations" turns off FlatLaf's: FlatLaf.updateUI() is called here.
                    /* Theme.install() is documented in the manual, it is not invoked here. */
                    @Test
                    void innocent() {
                    }
                }
                """);

        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void aHelperThatIsNotItselfATestClassIsNotFlagged(@TempDir Path root) throws IOException {
        write(root, "SwingHelper.java", """
                final class SwingHelper {
                    static void installTheme() {
                        Theme.install();
                    }
                }
                """);

        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }

    @Test
    void noTestClassInTheFiveModulesMutatesGlobalUiStateWithoutIsolation() {
        List<Path> culprits = moduleTestSourceRoots()
                .flatMap(root -> GlobalUiMutationScan.unisolatedMutators(root).stream())
                .toList();

        assertTrue(culprits.isEmpty(), () -> culprits + " must be annotated @Isolated");
    }

    private static Stream<Path> moduleTestSourceRoots() {
        Path repoRoot = Path.of(System.getProperty("user.dir"), "..").normalize();
        return Stream.of("tabpro-core", "tabpro-midi", "tabpro-format", "tabpro-ui", "tabpro-app")
                .map(module -> repoRoot.resolve(Path.of(module, "src", "test", "java")))
                .filter(Files::isDirectory);
    }

    private static Path write(Path root, String fileName, String content) throws IOException {
        Path file = root.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }
}
