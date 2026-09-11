package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.app.SpanishUiTextScan.SpanishLiteral;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpanishUiTextInJavaIsFlaggedTest {

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

    private static Path write(Path root, String fileName, String content) throws IOException {
        Path file = root.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }
}
