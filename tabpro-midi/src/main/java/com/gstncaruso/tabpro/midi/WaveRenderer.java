package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.sun.media.sound.AudioSynthesizer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public final class WaveRenderer {

    private static final int TEMPO_META_TYPE = 0x51;
    private static final int DEFAULT_MICROSECONDS_PER_QUARTER = 500_000;

    private final Supplier<Synthesizer> synthesizers;

    public WaveRenderer(Supplier<Synthesizer> synthesizers) {
        this.synthesizers = synthesizers;
    }

    public void render(Sequence sequence, Path path, AudioQuality quality) {
        Synthesizer synth = synthesizers.get();
        if (!(synth instanceof AudioSynthesizer audioSynth)) {
            throw new ScoreFileException(
                    "el sintetizador " + synth.getClass().getName() + " no soporta el render fuera de tiempo real.");
        }
        AudioFormat format = new AudioFormat(
                quality.sampleRateHz(), quality.bitDepth(), quality.channels(), true, false);
        try {
            AudioInputStream stream = audioSynth.openStream(format, Map.of());
            try {
                sendEvents(sequence, audioSynth.getReceiver());
                writeWave(stream, format, sequence.getMicrosecondLength(), path);
            } finally {
                audioSynth.close();
            }
        } catch (MidiUnavailableException | IOException e) {
            throw new ScoreFileException("no se pudo exportar " + path, e);
        }
    }

    /**
     * Sends every event already timestamped in microseconds, all at once: Gervill's
     * AudioSynthesizer synchronizes them with the audio as it is read back, so nothing here has
     * to wait in real time.
     */
    private static void sendEvents(Sequence sequence, Receiver receiver) {
        double microsecondsPerTick = DEFAULT_MICROSECONDS_PER_QUARTER / (double) sequence.getResolution();
        long lastTick = 0;
        double atMicroseconds = 0;
        for (MidiEvent event : mergedEventsInTickOrder(sequence)) {
            atMicroseconds += (event.getTick() - lastTick) * microsecondsPerTick;
            lastTick = event.getTick();
            MidiMessage message = event.getMessage();
            if (message instanceof MetaMessage meta) {
                if (meta.getType() == TEMPO_META_TYPE) {
                    microsecondsPerTick = microsecondsPerQuarterOf(meta) / (double) sequence.getResolution();
                }
                continue;
            }
            receiver.send(message, Math.round(atMicroseconds));
        }
    }

    private static List<MidiEvent> mergedEventsInTickOrder(Sequence sequence) {
        List<MidiEvent> merged = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                merged.add(track.get(i));
            }
        }
        merged.sort(Comparator.comparingLong(MidiEvent::getTick));
        return merged;
    }

    private static int microsecondsPerQuarterOf(MetaMessage tempo) {
        byte[] data = tempo.getData();
        return ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
    }

    private static void writeWave(AudioInputStream stream, AudioFormat format, long microsecondLength, Path path)
            throws IOException {
        long frames = Math.round(microsecondLength / 1_000_000.0 * format.getSampleRate());
        try (AudioInputStream bounded = new AudioInputStream(stream, format, frames)) {
            AudioSystem.write(bounded, AudioFileFormat.Type.WAVE, path.toFile());
        }
    }
}
