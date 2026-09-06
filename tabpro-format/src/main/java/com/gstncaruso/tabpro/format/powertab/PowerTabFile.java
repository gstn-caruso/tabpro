package com.gstncaruso.tabpro.format.powertab;

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
 * Procedencia del formato: PowerTab no publica una especificacion aparte, asi
 * que el layout binario de este lector sale de leer el codigo fuente de
 * powertabeditor (github.com/powertab/powertabeditor, GPLv3), puntualmente
 * los {@code Deserialize()} de {@code source/formats/powertab_old/powertabdocument/}
 * (powertabfileheader, score, guitar, tuning, system, staff, barline,
 * timesignature, keysignature, position, note, alternateending,
 * tempomarker, guitarin) y {@code powertaboldimporter.cpp}, que convierte
 * ese modelo viejo al modelo moderno y aclaro semantica que el layout solo
 * no explica (por ejemplo, que una barra abre el compas que empieza en su
 * posicion y cierra el anterior). De ahi no se copio ni una linea de
 * codigo: se leyo el orden y el tamano de los campos (un hecho del formato,
 * no una expresion con derecho de autor) y se escribio esta implementacion
 * entera de cero, en Java, con el diseno y los nombres del resto de tabpro.
 * Los fixtures reales de test que usan estas clases son harina de otro
 * costal: esos si son archivos de terceros, y su procedencia y licencia
 * (GPLv3) estan aparte en el LEEME.md de {@code src/test/resources/powertab/}.
 *
 * <p>Abre una partitura de PowerTab (.ptb). El archivo guarda la cabecera y
 * despues dos "score" completas, siempre en el mismo orden: la de guitarra y
 * la de bajo (aunque la cancion solo use una). Cada sistema es un tramo de la
 * partitura que puede tener varios compases adentro, delimitados por sus
 * barras; cada pentagrama guarda todas sus posiciones en un solo arreglo,
 * indexado por la misma numeracion de posicion que usan las barras.
 *
 * <p>El archivo trae dos "score" independientes (guitarra y bajo); tabpro no
 * hace la fusion de compas a compas que hace PowerTab Editor 2.0 entre las
 * dos, asi que cada una entra con sus propios compases, como pistas
 * separadas. Si la cancion solo usa una, la otra llega vacia y no agrega
 * pistas.
 *
 * <p>La cantidad de pentagramas de una partitura no tiene por que coincidir
 * con la cantidad de guitarras definidas: PowerTab asigna guitarras a
 * pentagramas con "guitar in" (una guitarra puede no tocar en ningun
 * pentagrama, o un pentagrama puede no tener ninguna asignacion explicita).
 * Una pista sale de cada pentagrama; que guitarra le toca se resuelve por su
 * primer "guitar in" y, si no hay ninguno, por la regla simple de que el
 * pentagrama N usa la guitarra N.
 *
 * <p>Alcance de esta primera version: estructura (compases, pistas, afinacion),
 * notas y sus duraciones, y un grupo razonable de efectos por nota (ligado,
 * armonicos, slide, bend, trino). Lo que no se soporta se declara con una
 * excepcion clara en vez de adivinar: los silencios de varios compases
 * comprimidos (multibar rest), las barras de ritmo (rhythm slash), los
 * sistemas con distinta cantidad de pentagramas dentro de una misma partitura,
 * y la reasignacion de un pentagrama a otra guitarra a mitad de la pieza.
 * Una guitarra definida pero sin ningun pentagrama asignado en toda la
 * partitura no genera pista: no toca nada, asi que no se pierde musica.
 */
public final class PowerTabFile {

    private static final int DEFAULT_TEMPO = 120;

    private final PowerTabHeaderReader headerReader = new PowerTabHeaderReader();
    private final PowerTabScoreReader scoreReader = new PowerTabScoreReader();

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
        try {
            return assemble(data);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new ScoreFileException("el archivo no se pudo interpretar: " + e.getMessage(), e);
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
            throw new ScoreFileException("el archivo no tiene ninguna guitarra");
        }

        int tempo = tempoOf(guitarScore).orElseGet(() -> tempoOf(bassScore).orElse(DEFAULT_TEMPO));
        return new Score(infoOf(header), tempo, tracks, Lyrics.none());
    }

    // ---- pistas -------------------------------------------------------------

    private List<Track> tracksOf(PowerTabScore score) {
        if (score.systems().isEmpty()) {
            return List.of();
        }
        int staffCount = score.systems().get(0).staves().size();
        for (PowerTabSystem system : score.systems()) {
            if (system.staves().size() != staffCount) {
                throw new ScoreFileException(
                        "los sistemas de esta partitura no tienen todos la misma cantidad de pentagramas:"
                                + " no soportamos esa diferencia");
            }
            if (system.rhythmSlashCount() > 0) {
                throw new ScoreFileException(
                        "esta partitura usa barras de ritmo (rhythm slash), que todavia no soportamos");
            }
        }

        int[] guitarOfStaff = resolveGuitarPerStaff(score, staffCount);
        List<Track> tracks = new ArrayList<>();
        for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
            tracks.add(trackOf(score, guitarOfStaff[staffIndex], staffIndex));
        }
        return tracks;
    }

    /**
     * Que guitarra toca en cada pentagrama, segun el primer "guitar in" que lo
     * mencione (el bit mas bajo de su mascara). Si ninguno lo menciona, se usa
     * la regla simple de que el pentagrama N toca la guitarra N. Si esa
     * guitarra tampoco existe (metadatos incompletos: pasa en archivos de
     * prueba armados a mano), se usa la primera guitarra definida en vez de
     * fallar — eso solo cambia el nombre, la afinacion y el canal que se le
     * atribuyen al pentagrama, nunca las notas que trae.
     */
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
                throw new ScoreFileException(
                        "esta partitura reasigna el pentagrama " + staff + " a otra guitarra a mitad de la pieza,"
                                + " algo que todavia no soportamos");
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

    /**
     * La afinacion de la guitarra normalmente tiene tantas notas como cuerdas
     * declara el pentagrama; cuando no coincide (pasa en algunos archivos de
     * prueba armados a mano), se ajusta al pentagrama, que es quien de verdad
     * acota los numeros de cuerda que traen las notas.
     */
    private static Tuning tuningOf(PowerTabGuitar guitar, int stringCount) {
        List<Integer> notes = guitar.tuningMidiNotes();
        Tuning tuning = notes.isEmpty()
                ? Tuning.standard()
                : TuningLibrary.identify(notes.stream().map(midiNumber -> new Pitch(Math.clamp(midiNumber, 0, 127))).toList());
        return stringCount > 0 && tuning.stringCount() != stringCount ? tuning.withStringCount(stringCount) : tuning;
    }

    /**
     * PowerTab no distingue un canal de efectos aparte del canal principal (a
     * diferencia de Guitar Pro): se usa el mismo numero para los dos, que es
     * como tabpro modela "un solo canal por pista".
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
                false, false, false, TrackDisplay.standard());
    }

    // ---- compases -------------------------------------------------------------

    /** El tramo de posiciones que ocupa un compas dentro de un sistema, con sus atributos ya resueltos. */
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
                        throw new ScoreFileException(
                                "esta partitura usa un silencio de varios compases comprimido (multibar rest),"
                                        + " que todavia no soportamos");
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
                .orElseThrow(() -> new ScoreFileException("archivo PowerTab corrupto: no hay barra en la posicion " + position));
    }

    // ---- cabecera -------------------------------------------------------------

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
