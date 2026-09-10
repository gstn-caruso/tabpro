package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Ningun componente Swing puede suscribirse al Editor sin pasar por EdtEditorListener: el
 * hilo que dispare la notificacion puede ser cualquiera (el reproductor, el que mueve el
 * cursor), y solo el adaptador garantiza que la entrega llegue al EDT.
 *
 * <p>Mientras una suscripcion todavia no esta migrada figura en {@link #STILL_PENDING}. Cuando
 * se migra hay que sacarla de ahi en el mismo cambio: el test falla tanto si falta el adaptador
 * y no esta en la lista, como si ya tiene el adaptador y sigue en la lista.
 */
class EditorListenerSubscriptionTest {

    private static final Pattern SUBSCRIPTION = Pattern.compile("editor\\.addListener\\((.*)\\);");
    private static final String ADAPTER_CALL = "EdtEditorListener.onEdt(";

    private static final Set<String> STILL_PENDING = Set.of(
            "ScoreDocument.java#this::scoreChanged",
            "BeatViews.java#this::refresh",
            "MainFrame.java#this::updateTitle",
            "MainFrame.java#() -> spinner.setValue(editor.score().tempo())",
            "TrackSelector.java#this::refresh",
            "Commands.java#this::refreshEditMarkerCommand",
            "TrackPanel.java#this::editorChanged",
            "StatusBar.java#this::refresh");

    static Stream<Subscription> subscriptions() {
        return sourceRoots().filter(Files::isDirectory).flatMap(EditorListenerSubscriptionTest::subscriptionsUnder);
    }

    private static Stream<Path> sourceRoots() {
        String repoRoot = System.getProperty("user.dir") + "/..";
        return Stream.of(
                Path.of(repoRoot, "tabpro-ui", "src", "main", "java").normalize(),
                Path.of(repoRoot, "tabpro-app", "src", "main", "java").normalize());
    }

    private static Stream<Subscription> subscriptionsUnder(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .flatMap(EditorListenerSubscriptionTest::subscriptionsIn)
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<Subscription> subscriptionsIn(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            return IntStream.range(0, lines.size())
                    .mapToObj(number -> subscriptionOrNull(file, lines.get(number)))
                    .filter(Objects::nonNull)
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Subscription subscriptionOrNull(Path file, String line) {
        Matcher matcher = SUBSCRIPTION.matcher(line.strip());
        return matcher.find() ? new Subscription(file, matcher.group(1).strip()) : null;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("subscriptions")
    void seSuscribeATravesDelAdaptadorDeEdtOFiguraComoPendiente(Subscription subscription) {
        boolean throughAdapter = subscription.argument().startsWith(ADAPTER_CALL);
        boolean pending = STILL_PENDING.contains(subscription.key());
        if (throughAdapter) {
            assertFalse(pending, subscription + " ya usa el adaptador: sacala de STILL_PENDING");
        } else {
            assertTrue(pending, subscription + " tiene que suscribirse a traves de EdtEditorListener.onEdt(...)");
        }
    }

    record Subscription(Path file, String argument) {
        String key() {
            return file.getFileName() + "#" + argument;
        }

        @Override
        public String toString() {
            return key();
        }
    }
}
