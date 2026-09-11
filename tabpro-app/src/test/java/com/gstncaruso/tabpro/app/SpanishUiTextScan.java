package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class SpanishUiTextScan {

    private static final Pattern SPANISH_LETTER_OR_MARK = Pattern.compile("[áéíóúüÁÉÍÓÚÜñÑ¿¡]");
    private static final Pattern WORD = Pattern.compile("\\p{L}+");
    private static final Set<String> COMMON_SPANISH_UI_WORDS = Set.of(
            "abrir", "aceptar", "acorde", "archivo", "ayuda", "bajo", "borrar", "cancelar", "cerrar", "de", "del",
            "editar", "guardar", "guitarra", "insertar", "las", "los", "marcador", "nota", "nueva", "nuevo", "para",
            "partitura", "pista", "pistas", "por", "que", "sin", "una");

    private SpanishUiTextScan() {
    }

    static List<SpanishLiteral> spanishLiteralsUnder(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .flatMap(SpanishUiTextScan::spanishLiteralsIn)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<SpanishLiteral> spanishLiteralsIn(Path file) {
        return JavaSources.stringLiterals(JavaSources.read(file)).stream()
                .filter(SpanishUiTextScan::looksSpanish)
                .map(text -> new SpanishLiteral(file, text));
    }

    private static boolean looksSpanish(String text) {
        return SPANISH_LETTER_OR_MARK.matcher(text).find() || hasACommonSpanishUiWord(text);
    }

    private static boolean hasACommonSpanishUiWord(String text) {
        return WORD.matcher(text).results()
                .map(word -> word.group().toLowerCase(Locale.ROOT))
                .anyMatch(COMMON_SPANISH_UI_WORDS::contains);
    }

    record SpanishLiteral(Path file, String text) {
    }
}
