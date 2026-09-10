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

/**
 * Ninguna de estas formas sin tilde puede aparecer, como palabra completa, en un literal de texto
 * de la interfaz: son justamente las que la auditoria visual encontro escritas sin acento
 * (Tamano, Titulo, Guitarra estandar, Jonico, Dorico, Eolico...). Un identificador de comando, una
 * clave de switch, un placeholder de plantilla o un protocolo externo (MIDI, PDF, nombres de
 * fuente) no son texto de interfaz: quedan afuera de este chequeo.
 */
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
            "ningun", "alteracion", "pua", "recuperacion");

    /** Archivos enteros que son un protocolo externo en ingles, no texto de interfaz. */
    private static final Set<String> EXCLUDED_FILES = Set.of(
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Instruments.java");

    /** Un literal puntual que es una clave tecnica, no texto de interfaz. */
    private static final Set<String> TECHNICAL_KEYS = Set.of(
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/page/PageFields.java::album",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Channel.java::tremolo");

    /**
     * Archivos con deuda ya conocida que todavia no se corrigio: mientras un archivo este aca,
     * sus literales no se chequean. Se van sacando uno a uno a medida que se corrigen, hasta que
     * quede vacio.
     */
    private static final Set<String> PENDING_FILES = Set.of(
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/editing/wizards/MeasureRange.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/harmony/PitchClass.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/harmony/ScaleLibrary.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/bars/LineBreak.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/DiagramPlacement.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/LyricLine.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Measure.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/PercussionKit.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/ScoreInfo.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Track.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Tuning.java",
            "tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/TuningLibrary.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/effects/GraceNotePanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/effects/NoteEffectsDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/effects/StrokePanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/info/LyricsPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/info/ScoreInfoDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/info/ScoreInfoPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/markers/MarkerList.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/markers/MarkersDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/measure/DirectionsPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/measure/MeasurePropertiesDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/measure/RepeatPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/measure/TimeSignaturePanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/metronome/MetronomeDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/metronome/MetronomePanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/pagesetup/PageSetupDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/pagesetup/PageSetupPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/preferences/PreferencesPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/print/PrintPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/track/TrackPropertiesPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/AutomaticFingeringDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/BarArrangerDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/BarDurationCheckDialog.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/BarDurationReport.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/MeasureRangePanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/dialogs/wizards/StringOptionsPanel.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/score/ViewMode.java",
            "tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/ScoreDocument.java");

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
                    .filter(AccentedLiteralsTest::isNotPending)
                    .flatMap(AccentedLiteralsTest::literalsIn)
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isNotExcluded(Path file) {
        String path = file.toString().replace('\\', '/');
        return EXCLUDED_FILES.stream().noneMatch(path::endsWith);
    }

    private static boolean isNotPending(Path file) {
        String path = file.toString().replace('\\', '/');
        return PENDING_FILES.stream().noneMatch(path::endsWith);
    }

    private static Stream<Literal> literalsIn(Path file) {
        try {
            String content = Files.readString(file);
            return stringLiteralsIn(content).stream()
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
        return TECHNICAL_KEYS.stream().anyMatch(key -> (path + "::" + literal).endsWith(key));
    }

    /** Extrae el contenido de los literales de texto, sin comillas, salteando comentarios. */
    private static List<String> stringLiteralsIn(String source) {
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
