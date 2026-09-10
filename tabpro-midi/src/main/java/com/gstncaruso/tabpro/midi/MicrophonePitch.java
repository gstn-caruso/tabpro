package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.tuning.DetectedPitch;
import com.gstncaruso.tabpro.core.tuning.PitchDetector;
import java.util.function.Consumer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public final class MicrophonePitch implements AutoCloseable {

    private static final float SAMPLE_RATE = 44100;
    private static final int BITS = 16;
    private static final int WINDOW = 8192;

    private final PitchDetector detector = new PitchDetector((int) SAMPLE_RATE);
    private TargetDataLine line;
    private Thread listening;

    public static boolean isAvailable() {
        return AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, format()));
    }

    public void start(Consumer<DetectedPitch> heard) {
        close();
        try {
            line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format()));
            line.open(format(), WINDOW * 2);
            line.start();
        } catch (LineUnavailableException e) {
            line = null;
            throw new IllegalStateException("could not open the audio input: " + e.getMessage(), e);
        }
        listening = new Thread(() -> listen(heard), "tuner-input");
        listening.setDaemon(true);
        listening.start();
    }

    @Override
    public void close() {
        if (listening != null) {
            listening.interrupt();
            listening = null;
        }
        if (line != null) {
            line.stop();
            line.close();
            line = null;
        }
    }

    private void listen(Consumer<DetectedPitch> heard) {
        byte[] buffer = new byte[WINDOW * 2];
        while (!Thread.currentThread().isInterrupted() && line != null) {
            int read = line.read(buffer, 0, buffer.length);
            if (read <= 0) {
                return;
            }
            heard.accept(detector.detect(samplesOf(buffer, read)));
        }
    }

    /** AudioFormat here is signed, little-endian: 16-bit samples arrive as two bytes, low byte first. */
    private static double[] samplesOf(byte[] buffer, int read) {
        double[] samples = new double[read / 2];
        for (int index = 0; index < samples.length; index++) {
            int low = buffer[index * 2] & 0xFF;
            int high = buffer[index * 2 + 1];
            samples[index] = (short) ((high << 8) | low) / (double) Short.MAX_VALUE;
        }
        return samples;
    }

    private static AudioFormat format() {
        return new AudioFormat(SAMPLE_RATE, BITS, 1, true, false);
    }
}
