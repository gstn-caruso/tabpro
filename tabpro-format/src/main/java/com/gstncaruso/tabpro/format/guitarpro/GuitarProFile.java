package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Abre una partitura de Guitar Pro. El archivo guarda primero la cabecera, los
 * canales MIDI, las propiedades de los compases y las pistas, y recien despues
 * la matriz de compas por pista con sus beats.
 */
public final class GuitarProFile {

    /** El largo fijo del bloque donde vive el nombre de la version. */
    private static final int VERSION_BLOCK = 30;

    private final GuitarProHeaderReader headerReader = new GuitarProHeaderReader();
    private final GuitarProChannelReader channelReader = new GuitarProChannelReader();
    private final GuitarProTrackReader trackReader = new GuitarProTrackReader();
    private final GuitarProBeatReader beatReader = new GuitarProBeatReader();

    public Score read(Path path) {
        try {
            return read(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo leer " + path, e);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new ScoreFileException("el archivo " + path + " no se pudo interpretar: " + e.getMessage(), e);
        }
    }

    public Score read(byte[] data) {
        GuitarProByteReader reader = new GuitarProByteReader(data);
        GuitarProVersion version = GuitarProVersion.parse(reader.readFixedString(VERSION_BLOCK));
        GuitarProHeader header = headerReader.read(reader, version);
        List<GuitarProChannel> channels = channelReader.read(reader);
        GuitarProDirections directions = headerReader.readDirections(reader, version);
        int measureCount = reader.readInt();
        int trackCount = reader.readInt();
        GuitarProMeasureAttributesReader measureReader = new GuitarProMeasureAttributesReader(
                com.gstncaruso.tabpro.core.model.TimeSignature.fourFour(),
                header.keySignature(),
                header.globalTripletFeel().orElse(com.gstncaruso.tabpro.core.model.bars.TripletFeel.NONE));
        List<GuitarProMasterBar> bars = withDirections(
                readMasterBars(measureReader, reader, version, measureCount), directions);
        List<GuitarProTrackHeader> trackHeaders = readTrackHeaders(reader, version, trackCount);
        skipGp5Padding(reader, version);
        List<List<Measure>> measuresByTrack = readMeasures(reader, version, bars, trackHeaders);
        return assemble(header, channels, trackHeaders, measuresByTrack);
    }

    private static List<GuitarProMasterBar> readMasterBars(
            GuitarProMeasureAttributesReader measureReader,
            GuitarProByteReader reader,
            GuitarProVersion version,
            int measureCount) {
        List<GuitarProMasterBar> bars = new ArrayList<>(measureCount);
        for (int index = 0; index < measureCount; index++) {
            bars.add(measureReader.read(reader, version, index == 0));
        }
        return bars;
    }

    /** Le pega a cada master bar el simbolo de destino o el salto que le apunta desde las direcciones. */
    private static List<GuitarProMasterBar> withDirections(
            List<GuitarProMasterBar> bars, GuitarProDirections directions) {
        List<GuitarProMasterBar> updated = new ArrayList<>(bars);
        directions.symbols().forEach((index, symbol) ->
                replaceAttributes(updated, index, attributes -> attributes.withSymbol(symbol)));
        directions.jumps().forEach((index, jump) ->
                replaceAttributes(updated, index, attributes -> attributes.withJump(jump)));
        return updated;
    }

    private static void replaceAttributes(
            List<GuitarProMasterBar> bars, int index, UnaryOperator<MeasureAttributes> update) {
        if (index < 0 || index >= bars.size()) {
            return;
        }
        GuitarProMasterBar bar = bars.get(index);
        bars.set(index, new GuitarProMasterBar(bar.timeSignature(), update.apply(bar.attributes())));
    }

    private List<GuitarProTrackHeader> readTrackHeaders(
            GuitarProByteReader reader, GuitarProVersion version, int trackCount) {
        List<GuitarProTrackHeader> headers = new ArrayList<>(trackCount);
        for (int index = 0; index < trackCount; index++) {
            headers.add(trackReader.read(reader, version, index + 1));
        }
        return headers;
    }

    private static void skipGp5Padding(GuitarProByteReader reader, GuitarProVersion version) {
        if (version.hasSecondVoice()) {
            reader.skip(version == GuitarProVersion.GP5_00 ? 2 : 1);
        }
    }

    private List<List<Measure>> readMeasures(
            GuitarProByteReader reader,
            GuitarProVersion version,
            List<GuitarProMasterBar> bars,
            List<GuitarProTrackHeader> trackHeaders) {
        List<List<Measure>> measuresByTrack = new ArrayList<>();
        for (int track = 0; track < trackHeaders.size(); track++) {
            measuresByTrack.add(new ArrayList<>());
        }
        for (GuitarProMasterBar bar : bars) {
            for (int track = 0; track < trackHeaders.size(); track++) {
                int stringCount = trackHeaders.get(track).tuningMidiNumbers().size();
                measuresByTrack.get(track).add(readMeasure(reader, version, bar, stringCount));
            }
        }
        return measuresByTrack;
    }

    private Measure readMeasure(
            GuitarProByteReader reader, GuitarProVersion version, GuitarProMasterBar bar, int stringCount) {
        Voice lead = readVoice(reader, version, stringCount);
        Voice bass = version.hasSecondVoice() ? readVoice(reader, version, stringCount) : Voice.unused();
        if (version.hasSecondVoice()) {
            reader.readUnsignedByte();
        }
        return new Measure(bar.timeSignature(), bar.attributes(), List.of(usable(lead), bass));
    }

    private Voice readVoice(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        int beatCount = reader.readInt();
        List<Beat> beats = new ArrayList<>(beatCount);
        for (int index = 0; index < beatCount; index++) {
            beats.add(beatReader.read(reader, version, stringCount));
        }
        return new Voice(beats);
    }

    /** La voz principal de un compas no puede quedar sin beats. */
    private static Voice usable(Voice voice) {
        return voice.isUnused() ? Voice.restingFor(Duration.quarter()) : voice;
    }

    private static Score assemble(
            GuitarProHeader header,
            List<GuitarProChannel> channels,
            List<GuitarProTrackHeader> trackHeaders,
            List<List<Measure>> measuresByTrack) {
        List<Track> tracks = new ArrayList<>();
        for (int index = 0; index < trackHeaders.size(); index++) {
            tracks.add(trackOf(trackHeaders.get(index), channels, measuresByTrack.get(index), index));
        }
        if (tracks.isEmpty()) {
            throw new ScoreFileException("el archivo no tiene ninguna pista");
        }
        ScoreInfo info = header.info();
        int tempo = Math.max(1, header.tempo());
        return new Score(info, tempo, tracks, header.lyrics());
    }

    private static Track trackOf(
            GuitarProTrackHeader header, List<GuitarProChannel> channels, List<Measure> measures, int index) {
        Tuning tuning = tuningOf(header);
        TrackSettings settings = new TrackSettings(
                header.color(),
                header.capo(),
                Math.clamp(header.fretCount(), 1, Tuning.MAX_FRET),
                header.percussion(),
                header.twelveString(),
                header.banjoFifthString(),
                header.display());
        return new Track(header.name(), tuning, channelOf(header, channels), settings, usable(measures));
    }

    private static Tuning tuningOf(GuitarProTrackHeader header) {
        // Una pista de percusion no tiene alturas: sus lineas son sonidos, no cuerdas.
        if (header.percussion()) {
            return com.gstncaruso.tabpro.core.model.PercussionKit.tuning();
        }
        List<Pitch> strings = header.tuningMidiNumbers().stream()
                .map(midiNumber -> new Pitch(Math.clamp(midiNumber, 0, 127)))
                .toList();
        return strings.isEmpty() ? Tuning.standard() : TuningLibrary.identify(strings);
    }

    private static Channel channelOf(GuitarProTrackHeader header, List<GuitarProChannel> channels) {
        int slot = header.channelIndex1Based() - 1;
        if (slot < 0 || slot >= channels.size()) {
            return header.percussion() ? Channel.percussion() : Channel.playing(Track.GUITAR_PROGRAM);
        }
        GuitarProChannel sound = channels.get(slot);
        return new Channel(
                Math.clamp(sound.program(), 0, Channel.MAX),
                sound.volume(),
                sound.pan(),
                sound.chorus(),
                sound.reverb(),
                sound.phaser(),
                sound.tremolo(),
                1,
                Math.clamp(header.channelIndex1Based(), 1, Channel.CHANNELS_PER_PORT),
                false,
                false);
    }

    /** Una pista necesita al menos un compas, aunque el archivo no traiga ninguno. */
    private static List<Measure> usable(List<Measure> measures) {
        if (!measures.isEmpty()) {
            return measures;
        }
        return List.of(Measure.empty(
                com.gstncaruso.tabpro.core.model.TimeSignature.fourFour(), Duration.quarter()));
    }
}
