package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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

    private static final Set<String> FILES_WHOSE_CONTENT_IS_AN_ENGLISH_ONLY_EXTERNAL_PROTOCOL = Set.of(
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Instruments.java");

    private static final Set<String> TECHNICAL_LITERALS_THAT_ARE_NOT_INTERFACE_TEXT = Set.of(
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/page/PageFields.java::album",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Channel.java::tremolo");

    private static final Pattern PLACEHOLDER = Pattern.compile("\\[%[a-zA-Z]+]");
    private static final Pattern IDENTIFIER_LITERAL =
            Pattern.compile("^[A-Za-z][A-Za-z0-9]*(\\.[A-Za-z0-9]+)+$");
    private static final Pattern WORD = Pattern.compile("[a-zA-ZñÑ]+");

    static Stream<Literal> literals() {
        return sourceRoots().flatMap(AccentedLiteralsTest::literalsUnder);
    }

    private static Stream<Path> sourceRoots() {
        String repoRoot = System.getProperty("user.dir") + "/..";
        return Stream.of(
                        Path.of(repoRoot, "tabpro-ui", "src", "main", "java"),
                        Path.of(repoRoot, "tabpro-app", "src", "main", "java"),
                        Path.of(repoRoot, "tabpro-core", "src", "main", "java"))
                .map(Path::normalize);
    }

    private static Stream<Literal> literalsUnder(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .filter(AccentedLiteralsTest::isNotExcluded)
                    .flatMap(AccentedLiteralsTest::literalsIn)
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isNotExcluded(Path file) {
        String path = file.toString().replace('\\', '/');
        return FILES_WHOSE_CONTENT_IS_AN_ENGLISH_ONLY_EXTERNAL_PROTOCOL.stream().noneMatch(path::endsWith);
    }

    private static Stream<Literal> literalsIn(Path file) {
        try {
            String content = Files.readString(file);
            return stringLiteralsSkippingComments(content).stream()
                    .filter(literal -> !isTechnicalKey(file, literal))
                    .map(literal -> new Literal(file, literal))
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isTechnicalKey(Path file, String literal) {
        String path = file.toString().replace('\\', '/');
        return TECHNICAL_LITERALS_THAT_ARE_NOT_INTERFACE_TEXT.stream().anyMatch(key -> (path + "::" + literal).endsWith(key));
    }

    private static List<String> stringLiteralsSkippingComments(String source) {
        List<String> found = new java.util.ArrayList<>();
        int i = 0;
        int n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < n && source.charAt(i + 1) == '/') {
                int end = source.indexOf('\n', i);
                i = end == -1 ? n : end;
                continue;
            }
            if (c == '/' && i + 1 < n && source.charAt(i + 1) == '*') {
                int end = source.indexOf("*/", i + 2);
                i = end == -1 ? n : end + 2;
                continue;
            }
            if (c == '\'') {
                i++;
                if (i < n && source.charAt(i) == '\\') {
                    i += 2;
                } else {
                    i++;
                }
                if (i < n && source.charAt(i) == '\'') {
                    i++;
                }
                continue;
            }
            if (c == '"') {
                i++;
                StringBuilder literal = new StringBuilder();
                while (i < n && source.charAt(i) != '"') {
                    if (source.charAt(i) == '\\' && i + 1 < n) {
                        literal.append(source, i, i + 2);
                        i += 2;
                        continue;
                    }
                    literal.append(source.charAt(i));
                    i++;
                }
                i++;
                found.add(literal.toString());
                continue;
            }
            i++;
        }
        return found;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("literals")
    void noLeFaltanTildesAUnTextoDeLaInterfaz(Literal literal) {
        String withoutPlaceholders = PLACEHOLDER.matcher(literal.content()).replaceAll("");
        if (IDENTIFIER_LITERAL.matcher(literal.content()).matches()) {
            return;
        }
        Matcher words = WORD.matcher(withoutPlaceholders);
        List<String> missingAccents = new java.util.ArrayList<>();
        while (words.find()) {
            String word = words.group();
            if (MISSING_ACCENT_FORMS.contains(word.toLowerCase(java.util.Locale.ROOT))) {
                missingAccents.add(word);
            }
        }
        assertTrue(
                missingAccents.isEmpty(),
                () -> literal + " le falta la tilde a: " + missingAccents);
    }

    record Literal(Path file, String content) {
        @Override
        public String toString() {
            return file.getFileName() + " -> \"" + content + "\"";
        }
    }
}
