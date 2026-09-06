package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporta una partitura al formato de Guitar Pro 4, tal como pide el manual en
 * "File &gt; Export &gt; Guitar Pro 4 Format". Es el espejo de {@link GuitarProFile}, pero
 * de una sola generacion: GP4 trae letra, un unico triplet feel global y octava en la
 * armadura, y no trae direcciones, page setup, autor de la musica ni segunda voz.
 *
 * <p>Lo que tabpro modela y GP4 no soporta se pierde al exportar; {@link #warningsFor}
 * dice exactamente que, para esta partitura en particular.
 */
public final class GuitarProExporter {

    private static final String VERSION_HEADER = "FICHIER GUITAR PRO v4.06";

    private final GuitarProHeaderWriter headerWriter = new GuitarProHeaderWriter();
    private final GuitarProChannelWriter channelWriter = new GuitarProChannelWriter();
    private final GuitarProTrackWriter trackWriter = new GuitarProTrackWriter();
    private final GuitarProBeatWriter beatWriter = new GuitarProBeatWriter();

    public void write(Score score, Path path) {
        try {
            Files.write(path, write(score));
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo escribir " + path, e);
        }
    }

    public byte[] write(Score score) {
        GuitarProByteWriter writer = new GuitarProByteWriter();
        writer.writeVersion(VERSION_HEADER);

        KeySignature keySignature = score.attributesOf(0).keySignature();
        TripletFeel globalTripletFeel = score.attributesOf(0).tripletFeel();
        headerWriter.write(writer, score.info(), globalTripletFeel, score.lyrics(), score.tempo(), keySignature);

        channelWriter.write(writer, GuitarProChannelWriter.tableFor(score));

        int measureCount = score.measureCount();
        writer.writeInt(measureCount);
        writer.writeInt(score.trackCount());

        writeMasterBars(writer, score, measureCount);
        for (Track track : score.tracks()) {
            trackWriter.write(writer, track);
        }
        writeMeasures(writer, score, measureCount);

        return writer.bytes();
    }

    private void writeMasterBars(GuitarProByteWriter writer, Score score, int measureCount) {
        GuitarProMeasureAttributesWriter barsWriter = new GuitarProMeasureAttributesWriter();
        for (int index = 0; index < measureCount; index++) {
            barsWriter.write(writer, score.timeSignatureOf(index), score.attributesOf(index));
        }
    }

    private void writeMeasures(GuitarProByteWriter writer, Score score, int measureCount) {
        for (int index = 0; index < measureCount; index++) {
            TimeSignature timeSignature = score.timeSignatureOf(index);
            for (Track track : score.tracks()) {
                writeVoice(writer, leadVoiceOf(track, index, timeSignature));
            }
        }
    }

    private static Voice leadVoiceOf(Track track, int measureIndex, TimeSignature fallbackTimeSignature) {
        if (measureIndex < track.measureCount()) {
            return track.measure(measureIndex).lead();
        }
        return Voice.restingFor(Duration.quarter());
    }

    private void writeVoice(GuitarProByteWriter writer, Voice voice) {
        writer.writeInt(voice.beatCount());
        for (Beat beat : voice.beats()) {
            beatWriter.write(writer, beat);
        }
    }

    /** Lo que esta partitura en particular pierde al exportarse a Guitar Pro 4. */
    public List<String> warningsFor(Score score) {
        List<String> warnings = new ArrayList<>();
        if (!score.info().musicAuthor().isBlank()) {
            warnings.add("El autor de la música ('" + score.info().musicAuthor()
                    + "') se pierde: Guitar Pro 4 no tiene un campo propio para él.");
        }
        if (anyMeasure(score, measure -> measure.usesTwoVoices())) {
            warnings.add("La segunda voz de los compases se pierde: Guitar Pro 4 admite una sola voz por compás.");
        }
        if (tripletFeelVaries(score)) {
            warnings.add("El 'triplet feel' cambia entre compases; Guitar Pro 4 admite uno solo para toda la "
                    + "partitura (se exporta el del primer compás).");
        }
        for (Track track : score.tracks()) {
            if (!track.settings().display().equals(TrackDisplay.standard())) {
                warnings.add("La pista '" + track.name()
                        + "' tiene una configuración de vista (pentagrama, tablatura o diagramas) que Guitar Pro 4 "
                        + "no guarda: vuelve a mostrarse con los valores por defecto.");
            }
            if (track.channel().port() != 1) {
                warnings.add("La pista '" + track.name() + "' usa el puerto MIDI " + track.channel().port()
                        + "; Guitar Pro 4 solo admite el puerto 1.");
            }
        }
        if (anyBeat(score, beat -> beat.effects().wideVibrato())) {
            warnings.add("El vibrato ancho de algún compás se pierde: solo existe en Guitar Pro 3.");
        }
        if (anyBeat(score, beat -> beat.effects().chord().isPresent() && !beat.effects().chord().get().shown())) {
            warnings.add("Algún acorde marcado para mostrar solo el nombre va a mostrarse con el diagrama completo.");
        }
        if (anyNote(score, note -> note.effects().grace().isPresent()
                && (note.effects().grace().get().onBeat() || note.effects().grace().get().dead()))) {
            warnings.add("Alguna nota de adorno usa 'en el tiempo' o 'nota muerta': esos datos no existen en "
                    + "Guitar Pro 4.");
        }
        return warnings;
    }

    private static boolean tripletFeelVaries(Score score) {
        TripletFeel first = score.attributesOf(0).tripletFeel();
        return anyMeasureAttributes(score, attributes -> attributes.tripletFeel() != first);
    }

    private static boolean anyMeasureAttributes(Score score, java.util.function.Predicate<MeasureAttributes> test) {
        for (Track track : score.tracks()) {
            for (int i = 0; i < track.measureCount(); i++) {
                if (test.test(track.measure(i).attributes())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyMeasure(Score score, java.util.function.Predicate<Measure> test) {
        for (Track track : score.tracks()) {
            for (int i = 0; i < track.measureCount(); i++) {
                if (test.test(track.measure(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyBeat(Score score, java.util.function.Predicate<Beat> test) {
        return anyMeasure(score, measure -> measure.beats().stream().anyMatch(test));
    }

    private static boolean anyNote(Score score, java.util.function.Predicate<com.gstncaruso.tabpro.core.model.Note> test) {
        return anyBeat(score, beat -> beat.notes().stream().anyMatch(test));
    }
}
