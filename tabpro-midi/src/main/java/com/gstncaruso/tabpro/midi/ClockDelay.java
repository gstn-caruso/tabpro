package com.gstncaruso.tabpro.midi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class ClockDelay implements Delay, AutoCloseable {

    private final ScheduledExecutorService clock = Executors.newSingleThreadScheduledExecutor(daemonThreads());

    @Override
    public void after(long millis, Runnable action) {
        clock.schedule(action, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        clock.shutdownNow();
    }

    private static ThreadFactory daemonThreads() {
        return runnable -> {
            Thread thread = new Thread(runnable, "midi-note-release");
            thread.setDaemon(true);
            return thread;
        };
    }
}
