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
 * Format provenance: TablEdit also publishes no separate specification, so this
 * reader's binary layout comes from reading the TuxGuitar source code
 * (github.com/helge17/tuxguitar, LGPL), specifically its TEF3 reader
 * (desktop/TuxGuitar-tef/src/app/tuxguitar/io/tef3/: TEInputStream, TESongParser,
 * TESongReader). Not a single line of code was copied from there: the order and size of
 * the fields were read (a fact of the format, not a copyrightable expression) and this
 * implementation was written entirely from scratch, in Java, with the design and names
 * of the rest of tabpro. Unlike PowerTab, this reader's test fixtures are synthetic
 * (hand-built in {@code TabEditFileWriter}), not third-party files.
 *
 * <p>Opens a TablEdit score (TEF3 format, the only version supported). The file
 * carries, in this order: the header, the song metadata, the chords (if any), the
 * measures, the tracks, the print data, the reading list (if any) and, at the end, the
 * list of components that places each note and each rest.
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

    /** Chords have their own record size, declared right before the list. */
    private static void skipChordDefinitions(TabEditByteReader input, TabEditHeader header) {
        if (!header.hasChords()) {
            return;
        }
        int chordRecordSize = input.readUnsignedShort();
        int totalChords = input.readUnsignedShort();
        input.skip(chordRecordSize * totalChords);
        // Chord diagrams have nowhere to live in this first version of the importer:
        // they are discarded, even when the file carries them.
    }

    /** Pagination and print data: they do not affect the music, discarded whole. */
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

    /** The reading order of the marked sections: not musical repeats. */
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

    /** TablEdit stores pan and volume in a 0-to-15 scale; the volume, moreover, reversed. */
    private static Channel channelOf(TabEditTrackHeader header) {
        int volume = Math.clamp((15 - header.volume()) * 127 / 15, 0, Channel.MAX);
        int pan = Math.clamp(header.pan() * 127 / 15, 0, Channel.MAX);
        return Channel.playing(Math.clamp(header.midiInstrument(), 0, Channel.MAX))
                .withVolume(volume)
                .withPan(pan);
    }
}
