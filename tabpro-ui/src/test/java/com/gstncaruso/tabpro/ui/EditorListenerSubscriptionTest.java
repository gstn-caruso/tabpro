package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * No Swing component may subscribe to the Editor without going through EdtEditorListener: the
 * thread that fires the notification can be anything (the player, whatever moves the cursor),
 * and only the adapter guarantees delivery reaches the EDT, as Swing requires.
 */
class EditorListenerSubscriptionTest {

    private static final Pattern SUBSCRIPTION = Pattern.compile("editor\\.addListener\\((.*)\\);");
    private static final String ADAPTER_CALL = "EdtEditorListener.onEdt(";

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
    void subscribesThroughTheEdtAdapter(Subscription subscription) {
        assertTrue(
                subscription.argument().startsWith(ADAPTER_CALL),
                () -> subscription + " tiene que suscribirse a traves de EdtEditorListener.onEdt(...)");
    }

    record Subscription(Path file, String argument) {
        @Override
        public String toString() {
            return file.getFileName() + "#" + argument;
        }
    }
}
