package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Format provenance: PowerTab publishes no separate specification, so this reader's
 * binary layout comes from reading the powertabeditor source code
 * (github.com/powertab/powertabeditor, GPLv3), specifically the {@code Deserialize()}
 * methods of {@code source/formats/powertab_old/powertabdocument/} (powertabfileheader,
 * score, guitar, tuning, system, staff, barline, timesignature, keysignature, position,
 * note, alternateending, tempomarker, guitarin) and {@code powertaboldimporter.cpp},
 * which converts that old model into the modern one and clarified semantics the layout
 * alone does not explain (for example, that a barline opens the measure that starts at
 * its position and closes the previous one). Not a single line of code was copied from
 * there: the order and size of the fields were read (a fact of the format, not a
 * copyrightable expression) and this implementation was written entirely from scratch,
 * in Java, with the design and names of the rest of tabpro. The real test fixtures that
 * use these classes are a different matter: those are indeed third-party files, and
 * their provenance and license (GPLv3) are documented separately in the README.md of
 * {@code src/test/resources/powertab/}.
 *
 * <p>Opens a PowerTab score (.ptb). The file stores the header and then two complete
 * "scores", always in the same order: guitar and bass (even if the song only uses one).
 * Each system is a stretch of the score that can hold several measures inside,
 * delimited by their barlines; each staff stores all its positions in a single array,
 * indexed by the same position numbering the barlines use.
 *
 * <p>The number of staves in a score need not match the number of guitars defined:
 * PowerTab assigns guitars to staves with "guitar in" (a guitar may play on no staff,
 * or a staff may have no explicit assignment).
 */
public final class PowerTabFile {

    private static final int DEFAULT_TEMPO = 120;

    private final PowerTabHeaderReader headerReader = new PowerTabHeaderReader();
    private final PowerTabScoreReader scoreReader = new PowerTabScoreReader();

    public Score read(Path path) {
        try {
            return read(Files.readAllBytes(path));
        } catch (IOException e) {
            throw ScoreFileException.cannotRead(path, e);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw ScoreFileException.damaged("could not parse " + path + ": " + e.getMessage(), e);
        }
    }

    public Score read(byte[] data) {
        try {
            return assemble(data);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw ScoreFileException.damaged("could not parse the file: " + e.getMessage(), e);
        }
    }

    private Score assemble(byte[] data) {
        PowerTabByteReader reader = new PowerTabByteReader(data);
        PowerTabHeader header = headerReader.read(reader);
        PowerTabScore guitarScore = scoreReader.read(reader);
        PowerTabScore bassScore = scoreReader.read(reader);

        List<Track> tracks = new ArrayList<>();
        tracks.addAll(tracksOf(guitarScore));
        tracks.addAll(tracksOf(bassScore));
        if (tracks.isEmpty()) {
            throw ScoreFileException.nothingToImport("the file has no guitars");
        }

        int tempo = tempoOf(guitarScore).orElseGet(() -> tempoOf(bassScore).orElse(DEFAULT_TEMPO));
        return new Score(infoOf(header), tempo, tracks, Lyrics.none());
    }

    private List<Track> tracksOf(PowerTabScore score) {
        if (score.systems().isEmpty()) {
            return List.of();
        }
        int staffCount = score.systems().get(0).staves().size();
        for (PowerTabSystem system : score.systems()) {
            if (system.staves().size() != staffCount) {
                throw ScoreFileException.unsupportedContent(
                        ScoreFeature.UNEVEN_STAFF_COUNTS, "the systems do not all have the same number of staves");
            }
            if (system.rhythmSlashCount() > 0) {
                throw ScoreFileException.unsupportedContent(ScoreFeature.RHYTHM_SLASHES, "rhythm slashes");
            }
        }

        int[] guitarOfStaff = resolveGuitarPerStaff(score, staffCount);
        List<Track> tracks = new ArrayList<>();
        for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
            tracks.add(trackOf(score, guitarOfStaff[staffIndex], staffIndex));
        }
        return tracks;
    }

    private static int[] resolveGuitarPerStaff(PowerTabScore score, int staffCount) {
        int[] guitarOfStaff = new int[staffCount];
        java.util.Arrays.fill(guitarOfStaff, -1);

        for (PowerTabGuitarIn guitarIn : score.guitarIns()) {
            int staff = guitarIn.staff();
            int guitar = lowestSetBit(guitarIn.staffGuitarsMask());
            if (staff < 0 || staff >= staffCount || guitar < 0) {
                continue;
            }
            if (guitarOfStaff[staff] != -1 && guitarOfStaff[staff] != guitar) {
                throw ScoreFileException.unsupportedContent(
                        ScoreFeature.STAFF_GUITAR_CHANGES, "staff " + staff + " changes guitar partway through");
            }
            guitarOfStaff[staff] = guitar;
        }

        for (int staff = 0; staff < staffCount; staff++) {
            if (guitarOfStaff[staff] == -1) {
                guitarOfStaff[staff] = staff;
            }
            if (guitarOfStaff[staff] >= score.guitars().size()) {
                guitarOfStaff[staff] = 0;
            }
        }
        return guitarOfStaff;
    }

    private static int lowestSetBit(int mask) {
        for (int bit = 0; bit < 8; bit++) {
            if ((mask & (1 << bit)) != 0) {
                return bit;
            }
        }
        return -1;
    }

    private Track trackOf(PowerTabScore score, int guitarIndex, int staffIndex) {
        PowerTabGuitar guitar = score.guitars().get(guitarIndex);
        List<Measure> measures = new ArrayList<>();
        int stringCount = 0;
        for (int systemIndex = 0; systemIndex < score.systems().size(); systemIndex++) {
            PowerTabSystem system = score.systems().get(systemIndex);
            List<PowerTabAlternateEnding> endingsHere = endingsIn(score, systemIndex);
            PowerTabStaff staff = system.staves().get(staffIndex);
            stringCount = staff.stringCount();
            for (MeasureSlice slice : slicesOf(system, endingsHere)) {
                measures.add(measureOf(staff, slice));
            }
        }
        if (measures.isEmpty()) {
            measures.add(Measure.empty(TimeSignature.fourFour(), Duration.quarter()));
        }
        String name = guitar.description().isBlank() ? "Pista" : guitar.description();
        return new Track(
                name, tuningOf(guitar, stringCount), channelOf(guitar, staffIndex), settingsOf(guitar, staffIndex),
                measures);
    }

    private static List<PowerTabAlternateEnding> endingsIn(PowerTabScore score, int systemIndex) {
        return score.alternateEndings().stream().filter(ending -> ending.system() == systemIndex).toList();
    }

    private Measure measureOf(PowerTabStaff staff, MeasureSlice slice) {
        Voice lead = usableLead(voiceOf(staff.voices().get(0), slice));
        Voice bass = voiceOf(staff.voices().get(1), slice);
        return new Measure(slice.timeSignature(), slice.attributes(), List.of(lead, bass));
    }

    private static Voice voiceOf(List<PowerTabPosition> positions, MeasureSlice slice) {
        List<Beat> beats = positions.stream()
                .filter(position -> position.index() >= slice.start() && position.index() < slice.end())
                .sorted(Comparator.comparingInt(PowerTabPosition::index))
                .map(PowerTabPosition::beat)
                .toList();
        return beats.isEmpty() ? Voice.unused() : new Voice(beats);
    }

    private static Voice usableLead(Voice voice) {
        return voice.isUnused() ? Voice.restingFor(Duration.quarter()) : voice;
    }

    private static Tuning tuningOf(PowerTabGuitar guitar, int stringCount) {
        List<Integer> notes = guitar.tuningMidiNotes();
        Tuning tuning = notes.isEmpty()
                ? Tuning.standard()
                : TuningLibrary.identify(notes.stream().map(midiNumber -> new Pitch(Math.clamp(midiNumber, 0, 127))).toList());
        return stringCount > 0 && tuning.stringCount() != stringCount ? tuning.withStringCount(stringCount) : tuning;
    }

    /**
     * PowerTab does not distinguish an effects channel separate from the main channel
     * (unlike Guitar Pro): the same number is used for both.
     */
    private static Channel channelOf(PowerTabGuitar guitar, int staffIndex) {
        int number = Math.clamp(staffIndex + 1, 1, Channel.CHANNELS_PER_PORT);
        return new Channel(
                Math.clamp(guitar.preset(), 0, Channel.MAX),
                Math.clamp(guitar.initialVolume(), 0, Channel.MAX),
                Math.clamp(guitar.pan(), 0, Channel.MAX),
                Math.clamp(guitar.chorus(), 0, Channel.MAX),
                Math.clamp(guitar.reverb(), 0, Channel.MAX),
                Math.clamp(guitar.phaser(), 0, Channel.MAX),
                Math.clamp(guitar.tremolo(), 0, Channel.MAX),
                1,
                number,
                number,
                false,
                false);
    }

    private static TrackSettings settingsOf(PowerTabGuitar guitar, int staffIndex) {
        return new TrackSettings(
                Track.colorFor(staffIndex), guitar.capo(), TrackSettings.DEFAULT_FRET_COUNT,
                false, false, false, TrackDisplay.standard(), false);
    }

    private record MeasureSlice(int start, int end, TimeSignature timeSignature, MeasureAttributes attributes) {
    }

    private List<MeasureSlice> slicesOf(PowerTabSystem system, List<PowerTabAlternateEnding> endingsHere) {
        List<PowerTabBarline> barlines = new ArrayList<>();
        barlines.add(system.startBar());
        barlines.addAll(system.internalBarlines());
        barlines.sort(Comparator.comparingInt(PowerTabBarline::position));

        int lastPosition = lastPositionUsedIn(system, barlines);
        int systemEnd = lastPosition + 1;
        List<Integer> starts = barlines.stream().map(PowerTabBarline::position).distinct().sorted().toList();

        List<MeasureSlice> slices = new ArrayList<>(starts.size());
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            boolean lastMeasure = i + 1 >= starts.size();
            int end = lastMeasure ? systemEnd : starts.get(i + 1);

            PowerTabBarline opener = barlineAt(barlines, start);
            int closingType = lastMeasure ? system.endBarType() : barlineAt(barlines, end).type();
            int closingRepeatCount = lastMeasure ? system.endBarRepeatCount() : barlineAt(barlines, end).repeatCount();

            List<Integer> numbers = new ArrayList<>();
            for (PowerTabAlternateEnding ending : endingsHere) {
                if (ending.position() >= start && ending.position() < end) {
                    numbers.addAll(ending.numbers());
                }
            }

            MeasureAttributes attributes = new MeasureAttributes(
                    opener.keySignature(),
                    TripletFeel.NONE,
                    isDoubleBar(closingType),
                    opener.isRepeatStart(),
                    closingType == PowerTabBarline.REPEAT_END ? closingRepeatCount : 0,
                    numbers,
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    LineBreak.AUTOMATIC, OctaveMark.NONE);

            slices.add(new MeasureSlice(start, end, opener.timeSignature(), attributes));
        }
        return slices;
    }

    private static boolean isDoubleBar(int type) {
        return type == PowerTabBarline.DOUBLE_BAR || type == PowerTabBarline.DOUBLE_BAR_FINE;
    }

    private static int lastPositionUsedIn(PowerTabSystem system, List<PowerTabBarline> barlines) {
        int lastPosition = 0;
        for (PowerTabBarline barline : barlines) {
            lastPosition = Math.max(lastPosition, barline.position());
        }
        for (PowerTabStaff staff : system.staves()) {
            for (List<PowerTabPosition> voice : staff.voices()) {
                for (PowerTabPosition position : voice) {
                    if (position.hasMultibarRest()) {
                        throw ScoreFileException.unsupportedContent(
                                ScoreFeature.MULTIBAR_RESTS, "compressed multibar rest");
                    }
                    lastPosition = Math.max(lastPosition, position.index());
                }
            }
        }
        return lastPosition;
    }

    private static PowerTabBarline barlineAt(List<PowerTabBarline> barlines, int position) {
        return barlines.stream()
                .filter(barline -> barline.position() == position)
                .findFirst()
                .orElseThrow(() -> ScoreFileException.damaged("corrupt PowerTab file: no bar at position " + position));
    }

    private static ScoreInfo infoOf(PowerTabHeader header) {
        return new ScoreInfo(
                header.title(), "", header.artist(), "", header.lyricist(), header.composer(),
                header.copyright(), header.transcriber(), header.notes(), "");
    }

    private static Optional<Integer> tempoOf(PowerTabScore score) {
        return score.tempoMarkers().stream()
                .filter(PowerTabTempoMarker::isStandardMarker)
                .map(PowerTabTempoMarker::beatsPerMinute)
                .filter(bpm -> bpm > 0)
                .findFirst();
    }
}
