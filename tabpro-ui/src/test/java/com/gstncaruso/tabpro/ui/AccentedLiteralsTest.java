package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AccentedLiteralsTest {

    private static final Set<String> MISSING_ACCENT_FORMS = Set.of(
            "tamano", "titulo", "subtitulo", "pagina", "paginas", "numero", "numeros", "opcion",
            "duracion", "digitacion", "afinacion", "notacion", "posicion", "compas", "musica",
            "tremolo", "armonico", "armonicos", "armonica", "armonicas", "dinamica", "dinamicas",
            "informacion", "orientacion", "automatico", "automatica", "metronomo", "estandar",
            "jonico", "dorico", "eolico", "electrica", "acustica", "acustico", "clasica", "basica",
            "configuracion", "cromatica", "pentatonica", "percusion", "arabe", "hungara",
            "enigmatica", "diapason", "simbolo", "renglon", "repeticion", "reproduccion",
            "direccion", "transicion", "violin", "album", "asi", "aqui", "despues", "encontro",
            "ningun", "alteracion", "pua", "recuperacion", "espanola", "margenes");

    private static final Pattern PLACEHOLDER = Pattern.compile("\\[%[a-zA-Z]+]");
    private static final Pattern WORD = Pattern.compile("[a-zA-ZñÑ]+");

    static Stream<SpanishText> spanishTexts() {
        return spanishBundles().flatMap(AccentedLiteralsTest::textsIn);
    }

    private static Stream<Path> spanishBundles() {
        Path bundles = Path.of(System.getProperty("user.dir"), "..", "tabpro-ui", "src", "main", "resources",
                "com", "gstncaruso", "tabpro", "ui", "i18n").normalize();
        try (Stream<Path> files = Files.list(bundles)) {
            return files.filter(file -> file.getFileName().toString().endsWith("_es.properties"))
                    .sorted()
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<SpanishText> textsIn(Path bundle) {
        Properties texts = new Properties();
        try (Reader reader = Files.newBufferedReader(bundle, StandardCharsets.UTF_8)) {
            texts.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return texts.stringPropertyNames().stream()
                .sorted()
                .map(key -> new SpanishText(bundle.getFileName().toString(), key, texts.getProperty(key)));
    }

    @Test
    void thereAreSpanishBundlesToCheck() {
        assertFalse(spanishTexts().toList().isEmpty(), "no _es.properties bundle was found");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spanishTexts")
    void noSpanishTextIsMissingAnAccent(SpanishText text) {
        Matcher words = WORD.matcher(PLACEHOLDER.matcher(text.value()).replaceAll(""));
        List<String> missingAccents = new ArrayList<>();
        while (words.find()) {
            if (MISSING_ACCENT_FORMS.contains(words.group().toLowerCase(Locale.ROOT))) {
                missingAccents.add(words.group());
            }
        }
        assertTrue(missingAccents.isEmpty(), () -> text + " is missing the accent in: " + missingAccents);
    }

    record SpanishText(String bundle, String key, String value) {
        @Override
        public String toString() {
            return bundle + " -> " + key + "=" + value;
        }
    }
}
