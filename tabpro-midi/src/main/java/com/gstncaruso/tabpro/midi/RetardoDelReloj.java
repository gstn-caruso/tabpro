package com.gstncaruso.tabpro.midi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/** El Retardo de produccion: programa la accion en un hilo propio, contado por el reloj del sistema. */
final class RetardoDelReloj implements Retardo, AutoCloseable {

    private final ScheduledExecutorService reloj = Executors.newSingleThreadScheduledExecutor(daemonThreads());

    @Override
    public void luegoDe(long millis, Runnable accion) {
        reloj.schedule(accion, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        reloj.shutdownNow();
    }

    private static ThreadFactory daemonThreads() {
        return runnable -> {
            Thread thread = new Thread(runnable, "midi-note-release");
            thread.setDaemon(true);
            return thread;
        };
    }
}
