package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.app.SpanishUiTextScan.SpanishLiteral;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpanishUiTextInJavaIsFlaggedTest {

    private static final Path REPO_ROOT = Path.of(System.getProperty("user.dir"), "..").normalize();

    private static final Map<String, String> ALLOWED_SPANISH_WITH_REASON = Map.of(
            "tabpro-format/src/main/java/com/gstncaruso/tabpro/format/StoredTuningName.java",
            "the Spanish tuning names that .tabpro files store; reading them keeps old files on their tuning",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/ScoreDocument.java::tabpro-recuperación",
            "the recovery file name of older versions, still looked up so their recovery is offered",
            "tabpro-format/src/main/java/com/gstncaruso/tabpro/format/exchange/midi/TrackTuningGuess.java::bajo",
            "a token that recognizes imported tracks named in Spanish as bass tracks, never shown");

    @Test
    void noMainSourceOfTheFiveModulesHoldsSpanishUiText() {
        List<String> unexpected = spanishLiteralsInTheFiveModules().stream()
                .filter(literal -> allowlistEntryFor(literal).isEmpty())
                .toList();

        assertEquals(List.of(), unexpected, "move this text to a bundle or allowlist it with a reason");
    }

    @Test
    void everyAllowlistedEntryStillMatchesSomeSpanishLiteral() {
        List<String> literals = spanishLiteralsInTheFiveModules();
        List<String> stale = ALLOWED_SPANISH_WITH_REASON.keySet().stream()
                .filter(entry -> literals.stream().noneMatch(literal -> matches(entry, literal)))
                .sorted()
                .toList();

        assertEquals(List.of(), stale, "these allowlist entries no longer match anything: remove them");
    }

    private static List<String> spanishLiteralsInTheFiveModules() {
        return Stream.of("tabpro-core", "tabpro-midi", "tabpro-format", "tabpro-ui", "tabpro-app")
                .map(module -> REPO_ROOT.resolve(Path.of(module, "src", "main", "java")))
                .flatMap(root -> SpanishUiTextScan.spanishLiteralsUnder(root).stream())
                .map(literal -> REPO_ROOT.relativize(literal.file()).toString().replace('\\', '/') + "::" + literal.text())
                .toList();
    }

    private static Optional<String> allowlistEntryFor(String literal) {
        return ALLOWED_SPANISH_WITH_REASON.keySet().stream().filter(entry -> matches(entry, literal)).findFirst();
    }

    private static boolean matches(String entry, String literal) {
        return entry.contains("::") ? literal.equals(entry) : literal.startsWith(entry + "::");
    }

    @Test
    void literalsWithSpanishLettersOrMarksAreFlaggedButEnglishOnesAreNot(@TempDir Path root) throws IOException {
        Path file = write(root, "Dialog.java", """
                class Dialog {
                    String title = "Configuración";
                    String question = "¿Guardar?";
                    String warning = "¡Cuidado!";
                    String year = "Año";
                    String english = "Page Setup";
                }
                """);

        assertEquals(
                List.of(new SpanishLiteral(file, "Configuración"), new SpanishLiteral(file, "¿Guardar?"),
                        new SpanishLiteral(file, "¡Cuidado!"), new SpanishLiteral(file, "Año")),
                SpanishUiTextScan.spanishLiteralsUnder(root));
    }

    @Test
    void literalsWithACommonSpanishUiWordAreFlaggedButEnglishWordsThatContainThemAreNot(@TempDir Path root)
            throws IOException {
        Path file = write(root, "Cell.java", """
                class Cell {
                    String track = "Guitarra";
                    String joined = " de ";
                    String open = "Abrir";
                    String english = "Delete the bar";
                    String key = "defaults.guitarTrack";
                }
                """);

        assertEquals(
                List.of(new SpanishLiteral(file, "Guitarra"), new SpanishLiteral(file, " de "),
                        new SpanishLiteral(file, "Abrir")),
                SpanishUiTextScan.spanishLiteralsUnder(root));
    }

    @Test
    void commentsAndCharactersAreSkippedWhileTextBlocksAndStringsWithSlashesAreRead(@TempDir Path root)
            throws IOException {
        Path file = write(root, "Mixed.java", """
                class Mixed {
                    // "Configuración" is only mentioned here
                    /* "Guitarra" too */
                    char quote = '"';
                    String url = "http://example.org/pista";
                    String block = \"""
                            Nueva partitura
                            \""";
                }
                """);

        assertEquals(
                List.of(new SpanishLiteral(file, "http://example.org/pista"),
                        new SpanishLiteral(file, "\n            Nueva partitura\n            ")),
                SpanishUiTextScan.spanishLiteralsUnder(root));
    }

    private static Path write(Path root, String fileName, String content) throws IOException {
        Path file = root.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }
}
