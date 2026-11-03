package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Linux desktop appearance via the GNOME/KDE XDG settings portal, without a D-Bus dependency. */
final class PortalAppearance implements SystemAppearance {
    private static final Pattern COLOR_SCHEME = Pattern.compile("\\(<<uint32 ([012])>>,\\)\\s*");
    private static final long POLL_MILLIS = 2_000;

    /** Open from the startup thread: the bounded initial read must not run on Swing's EDT. */
    @Override
    public AutoCloseable observe(Consumer<String> listener) {
        AtomicBoolean closed = new AtomicBoolean();
        AtomicReference<Process> reading = new AtomicReference<>();
        String initial;
        try {
            initial = read(closed, reading).orElse(Theme.DARK);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            listener.accept(Theme.DARK);
            return () -> {};
        }
        listener.accept(initial);
        Thread worker = new Thread(() -> {
            String current = initial;
            try {
                while (!closed.get()) {
                    Thread.sleep(POLL_MILLIS);
                    String next = read(closed, reading).orElse(current);
                    if (!closed.get() && !current.equals(next)) {
                        current = next;
                        listener.accept(next);
                    }
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }, "tabpro-system-appearance");
        worker.setDaemon(true);
        worker.start();
        return () -> {
            closed.set(true);
            worker.interrupt();
            Process process = reading.get();
            if (process != null) {
                process.destroyForcibly();
            }
        };
    }

    private static Optional<String> read(AtomicBoolean closed, AtomicReference<Process> reading)
            throws InterruptedException {
        if (closed.get()) {
            return Optional.empty();
        }
        Process process = null;
        try {
            process = new ProcessBuilder("gdbus", "call", "--session", "--timeout", "2",
                    "--dest", "org.freedesktop.portal.Desktop",
                    "--object-path", "/org/freedesktop/portal/desktop",
                    "--method", "org.freedesktop.portal.Settings.Read",
                    "org.freedesktop.appearance", "color-scheme")
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            reading.set(process);
            if (closed.get() || !process.waitFor(2, TimeUnit.SECONDS) || process.exitValue() != 0) {
                return Optional.empty();
            }
            String response = new String(process.getInputStream().readNBytes(256), StandardCharsets.UTF_8);
            Matcher scheme = COLOR_SCHEME.matcher(response);
            if (!scheme.matches()) {
                return Optional.empty();
            }
            // The portal defines 0 as no preference; use the standard light appearance.
            return Optional.of("1".equals(scheme.group(1)) ? Theme.DARK : Theme.LIGHT);
        } catch (IOException exception) {
            return Optional.empty();
        } finally {
            if (process != null) {
                reading.compareAndSet(process, null);
                process.destroyForcibly();
            }
        }
    }
}
