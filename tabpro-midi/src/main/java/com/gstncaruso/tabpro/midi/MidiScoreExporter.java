package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.playback.PlayOrder;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

/**
 * Guarda la partitura como archivo MIDI formato 1, sonando igual que adentro de tabpro: la
 * rinde a su linea de tiempo —el orden real de los compases, con repeticiones, finales
 * alternativos y saltos, y con todos los efectos: bends, palanca, slides, ligados, trinos,
 * tremolos, armonicos, rasgueos, notas de adorno, fade in y swing— y escribe esa misma
 * secuencia, la que se le manda al sintetizador cuando uno aprieta play.
 *
 * <p>Encima le agrega lo unico que un archivo necesita y la reproduccion no: el titulo, el
 * nombre de cada pista y los cambios de compas y de armadura, ubicados en el tick en el que
 * suenan. Las pistas que no suenan no se exportan, como avisa el manual.
 */
public final class MidiScoreExporter {

    private static final int SEQUENCE_FORMAT = 1;

    private static final int TRACK_NAME_META = 0x03;
    private static final int TIME_SIGNATURE_META = 0x58;
    private static final int KEY_SIGNATURE_META = 0x59;

    private static final int CLOCKS_PER_METRONOME_CLICK = 24;
    private static final int THIRTY_SECONDS_PER_QUARTER = 8;

    public Sequence toSequence(Score score) {
        PlayOrder order = PlayOrder.of(score);
        List<Integer> audible = audibleTracksOf(score);
        Sequence sequence = MidiSequences.fromTimeline(whatSounds(score, order, audible));
        try {
            nameThe(sequence, score, audible);
            writeBarChanges(sequence, score, order);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
        return sequence;
    }

    public void export(Score score, Path path) {
        try {
            MidiSystem.write(toSequence(score), SEQUENCE_FORMAT, path.toFile());
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo escribir " + path, e);
        }
    }

    /** Lo que se va a escuchar: la partitura rendida, sin las pistas que estan calladas. */
    private static Timeline whatSounds(Score score, PlayOrder order, List<Integer> audible) {
        Timeline everything = Timeline.of(score, order);
        return new Timeline(
                everything.tempo(),
                everything.ticksPerQuarter(),
                audible.stream().map(everything.tracks()::get).toList());
    }

    private static List<Integer> audibleTracksOf(Score score) {
        return IntStream.range(0, score.trackCount()).filter(score::isAudible).boxed().toList();
    }

    /** El titulo va en la pista de tempo, y el nombre de cada pista en la suya. */
    private static void nameThe(Sequence sequence, Score score, List<Integer> audible)
            throws InvalidMidiDataException {
        javax.sound.midi.Track[] tracks = sequence.getTracks();
        addText(tracks[0], score.title(), 0);
        for (int position = 0; position < audible.size(); position++) {
            addText(tracks[position + 1], score.track(audible.get(position)).name(), 0);
        }
    }

    /**
     * Los cambios de compas y de armadura, en el tick en el que suenan: si una repeticion vuelve
     * a pasar por un compas de 3/4, el archivo lo vuelve a anunciar.
     */
    private static void writeBarChanges(Sequence sequence, Score score, PlayOrder order)
            throws InvalidMidiDataException {
        javax.sound.midi.Track conductor = sequence.getTracks()[0];
        Track reference = score.track(0);
        TimeSignature previousSignature = null;
        KeySignature previousKey = null;
        long tick = 0;
        for (int step = 0; step < order.size(); step++) {
            int measureIndex = order.measureAt(step);
            if (measureIndex >= reference.measureCount()) {
                continue;
            }
            Measure measure = reference.measure(measureIndex);
            TimeSignature signature = measure.timeSignature();
            KeySignature key = measure.attributes().keySignature();
            if (!signature.equals(previousSignature)) {
                addTimeSignature(conductor, tick, signature);
            }
            if (!key.equals(previousKey)) {
                addKeySignature(conductor, tick, key);
            }
            previousSignature = signature;
            previousKey = key;
            tick += measure.durationTicks();
        }
    }

    private static void addTimeSignature(javax.sound.midi.Track track, long tick, TimeSignature signature)
            throws InvalidMidiDataException {
        byte denominatorPower = (byte) Integer.numberOfTrailingZeros(signature.beatUnit());
        byte[] data = {
            (byte) signature.beats(), denominatorPower, CLOCKS_PER_METRONOME_CLICK, THIRTY_SECONDS_PER_QUARTER
        };
        addMeta(track, TIME_SIGNATURE_META, data, tick);
    }

    private static void addKeySignature(javax.sound.midi.Track track, long tick, KeySignature key)
            throws InvalidMidiDataException {
        byte[] data = {(byte) key.accidentals(), (byte) (key.mode() == Mode.MINOR ? 1 : 0)};
        addMeta(track, KEY_SIGNATURE_META, data, tick);
    }

    private static void addText(javax.sound.midi.Track track, String text, long tick)
            throws InvalidMidiDataException {
        addMeta(track, TRACK_NAME_META, text.getBytes(StandardCharsets.UTF_8), tick);
    }

    private static void addMeta(javax.sound.midi.Track track, int type, byte[] data, long tick)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(new MetaMessage(type, data, data.length), tick));
    }
}
