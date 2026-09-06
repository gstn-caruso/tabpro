package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedencia del formato: TablEdit tampoco publica una especificacion
 * aparte, asi que el layout binario de este lector sale de leer el codigo
 * fuente de TuxGuitar (github.com/helge17/tuxguitar, LGPL), puntualmente su
 * lector de TEF3 (desktop/TuxGuitar-tef/src/app/tuxguitar/io/tef3/:
 * TEInputStream, TESongParser, TESongReader). De ahi no se copio ni una
 * linea de codigo: se leyo el orden y el tamano de los campos (un hecho del
 * formato, no una expresion con derecho de autor) y se escribio esta
 * implementacion entera de cero, en Java, con el diseno y los nombres del
 * resto de tabpro. A diferencia de PowerTab, los fixtures de test de este
 * lector son sinteticos (armados a mano en {@code TabEditFileWriter}), no
 * archivos de terceros.
 *
 * <p>Abre una partitura de TablEdit (formato TEF3, la unica version que
 * soportamos). El archivo trae, en este orden: el encabezado, los metadatos
 * de la cancion, los acordes (si hay), los compases, las pistas, los datos de
 * impresion, la lista de lectura (si hay) y, al final, la lista de
 * componentes que ubica cada nota y cada silencio.
 */
public final class TabEditFile {

    private static final int PAGE_HEADER_SIZE = 128;
    private static final int MAX_INITIAL_TEMPO = 999;

    private final TabEditHeaderReader headerReader = new TabEditHeaderReader();
    private final TabEditSongMetadataReader metadataReader = new TabEditSongMetadataReader();
    private final TabEditMeasureReader measureReader = new TabEditMeasureReader();
    private final TabEditTrackReader trackReader = new TabEditTrackReader();
    private final TabEditComponentsReader componentsReader = new TabEditComponentsReader();
    private final TabEditBeatAssembler beatAssembler = new TabEditBeatAssembler();

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
        TabEditByteReader input = new TabEditByteReader(data);

        TabEditHeader header = headerReader.read(input);
        TabEditSongMetadata metadata = metadataReader.read(input, header);
        skipChordDefinitions(input, header);
        List<TabEditMeasure> measures = measureReader.read(input);
        List<TabEditTrackHeader> trackHeaders = trackReader.read(input);
        requireNoPercussionTracks(trackHeaders);
        skipPrintMetadata(input);
        skipReadingList(input, header);
        List<TabEditEvent> events = componentsReader.read(input, measures, trackStringCountsOf(trackHeaders));

        return assemble(metadata, header, measures, trackHeaders, events);
    }

    /** Los acordes tienen un tamano de registro propio, declarado justo antes de la lista. */
    private static void skipChordDefinitions(TabEditByteReader input, TabEditHeader header) {
        if (!header.hasChords()) {
            return;
        }
        int chordRecordSize = input.readUnsignedShort();
        int totalChords = input.readUnsignedShort();
        input.skip(chordRecordSize * totalChords);
        // Los diagramas de acorde no tienen donde vivir en esta primera version del
        // importador: se descartan, aunque el archivo los traiga.
    }

    /** Datos de paginacion e impresion: no afectan la musica, se descartan enteros. */
    private static void skipPrintMetadata(TabEditByteReader input) {
        int printDataLength = input.readUnsignedByte();
        input.skip(1);
        String discarded = input.readNullTerminatedString(printDataLength);
        input.skip(printDataLength - discarded.length() - 1);
        for (int i = 0; i < 2; i++) {
            String header = input.readNullTerminatedString(PAGE_HEADER_SIZE - 1);
            input.skip(PAGE_HEADER_SIZE - header.length() - 1);
        }
    }

    /** El orden de lectura de las secciones marcadas: no son repeticiones musicales. */
    private static void skipReadingList(TabEditByteReader input, TabEditHeader header) {
        if (!header.hasReadingList()) {
            return;
        }
        int entryRecordSize = input.readUnsignedShort();
        int totalEntries = input.readUnsignedShort();
        input.skip(entryRecordSize * totalEntries);
    }

    private static void requireNoPercussionTracks(List<TabEditTrackHeader> trackHeaders) {
        for (TabEditTrackHeader header : trackHeaders) {
            if (header.percussion()) {
                throw new ScoreFileException(
                        "esta partitura tiene una pista de percusion de TablEdit ('" + header.name()
                                + "'), y el mapeo de sonidos de bateria de TablEdit todavia no esta soportado.");
            }
        }
    }

    private static List<Integer> trackStringCountsOf(List<TabEditTrackHeader> trackHeaders) {
        List<Integer> counts = new ArrayList<>(trackHeaders.size());
        for (TabEditTrackHeader header : trackHeaders) {
            counts.add(header.stringCount());
        }
        return counts;
    }

    private Score assemble(
            TabEditSongMetadata metadata, TabEditHeader header, List<TabEditMeasure> measures,
            List<TabEditTrackHeader> trackHeaders, List<TabEditEvent> events) {
        List<Track> tracks = new ArrayList<>(trackHeaders.size());
        for (int index = 0; index < trackHeaders.size(); index++) {
            List<Measure> trackMeasures = beatAssembler.assembleTrack(index, measures, events);
            tracks.add(trackOf(trackHeaders.get(index), trackMeasures, index));
        }
        if (tracks.isEmpty()) {
            throw new ScoreFileException("el archivo no tiene ninguna pista");
        }

        ScoreInfo info = ScoreInfo.empty()
                .withTitle(metadata.title())
                .withMusicAuthor(metadata.author())
                .withInstructions(metadata.comments())
                .withNotice(metadata.notes())
                .withCopyright(metadata.copyright());
        int tempo = Math.clamp(header.initialBpm(), 1, MAX_INITIAL_TEMPO);
        return new Score(info, tempo, tracks, com.gstncaruso.tabpro.core.model.Lyrics.none());
    }

    private static Track trackOf(TabEditTrackHeader header, List<Measure> measures, int index) {
        Tuning tuning = tuningOf(header);
        TrackSettings settings = TrackSettings.standard(Track.colorFor(index)).withCapo(Math.max(0, header.capo()));
        return new Track(header.name(), tuning, channelOf(header), settings, measures);
    }

    private static Tuning tuningOf(TabEditTrackHeader header) {
        List<Pitch> strings = header.tuningMidiNumbers().stream()
                .map(midi -> new Pitch(Math.clamp(midi, 0, 127)))
                .toList();
        return strings.isEmpty() ? Tuning.standard() : TuningLibrary.identify(strings);
    }

    /** TablEdit guarda pan y volumen en una escala de 0 a 15; el volumen, ademas, al reves. */
    private static Channel channelOf(TabEditTrackHeader header) {
        int volume = Math.clamp((15 - header.volume()) * 127 / 15, 0, Channel.MAX);
        int pan = Math.clamp(header.pan() * 127 / 15, 0, Channel.MAX);
        return Channel.playing(Math.clamp(header.midiInstrument(), 0, Channel.MAX))
                .withVolume(volume)
                .withPan(pan);
    }
}
