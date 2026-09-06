package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonScoreFilesTest {

    private final ScoreFiles scoreFiles = new JsonScoreFiles();

    @Test
    void savesAndLoadsTheSameScore(@TempDir Path tempDir) {
        Score score = Score.blank();
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(score, loaded);
    }

    @Test
    void writesTheFormatVersionFirst(@TempDir Path tempDir) throws IOException {
        Score score = Score.blank();
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);

        String content = Files.readString(path);
        assertTrue(content.startsWith("{\n  \"format\": " + ScoreDto.CURRENT_FORMAT + ","));
    }

    @Test
    void loadsTheVersionOneFixture() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        Beat firstBeat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2));
        Beat secondBeat = Beat.rest(new Duration(NoteValue.EIGHTH, true));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(firstBeat, secondBeat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score expected = new Score("Prueba", 120, List.of(track));

        assertEquals(expected, loaded);
    }

    @Test
    void aVersionOneFileGetsTheDefaultMixerSettings() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI());

        Channel channel = scoreFiles.load(path).track(0).channel();

        assertEquals(Channel.DEFAULT_VOLUME, channel.volume());
        assertEquals(Channel.CENTER_PAN, channel.pan());
        assertFalse(channel.muted());
        assertFalse(channel.solo());
    }

    @Test
    void savesAndLoadsTheMixerOfEveryTrack(@TempDir Path tempDir) {
        Track guitar = Track.standardGuitar("Guitarra")
                .withChannel(Channel.playing(30).withVolume(80).withPan(20).toggledSolo());
        Track bass = Track.standardBass("Bajo").withChannel(Channel.playing(33).toggledMute());
        Score score = new Score("Prueba", 120, List.of(guitar, bass));
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);

        assertEquals(score, scoreFiles.load(path));
    }

    @Test
    void rejectsAnUnsupportedFormatVersion(@TempDir Path tempDir) throws IOException, URISyntaxException {
        String validContent = Files.readString(Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI()));
        String unsupportedContent = validContent.replaceFirst("\"format\": 1", "\"format\": 99");
        Path path = tempDir.resolve("score.tabpro");
        Files.writeString(path, unsupportedContent);

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsMalformedJson(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("score.tabpro");
        Files.writeString(path, "{ esto no es json valido");

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsAMissingFile(@TempDir Path tempDir) {
        Path path = tempDir.resolve("no-existe.tabpro");

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsAMissingNestedField(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("score.tabpro");
        String jsonWithoutBeats = """
                {
                  "format": 1,
                  "title": "Prueba",
                  "tempo": 120,
                  "tracks": [
                    {
                      "name": "Guitarra",
                      "midiProgram": 25,
                      "tuning": [64, 59, 55, 50, 45, 40],
                      "measures": [
                        {
                          "timeSignature": { "beats": 4, "beatUnit": 4 }
                        }
                      ]
                    }
                  ]
                }
                """;
        Files.writeString(path, jsonWithoutBeats);

        ScoreFileException thrown = assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));

        assertTrue(thrown.getMessage().contains("beats"));
        assertFalse(thrown.getMessage().contains("vacio"));
    }

    @Test
    void aFileSavedBeforeTheSecondChannelPlaysItsEffectsNextToItsChannel() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v3-channel-without-its-effect-channel.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        Channel channel = loaded.track(0).channel();
        assertEquals(5, channel.number());
        assertEquals(6, channel.effectChannel());
    }

    /**
     * TrackDto siempre escribia el numero de canal, incluso el 1 por defecto, y hasta este
     * cambio MidiSequences lo ignoraba y repartia sus propios canales por orden de pista. Todo
     * archivo guardado hasta ahora con varias pistas las tiene, entonces, todas en el canal 1 --
     * la firma inconfundible de un valor que nunca sono. La firma es fuerte: nadie configura a
     * mano varias pistas en el mismo canal. Se reconstruye con el mismo calculo que hacia
     * MidiSequences (2n/2n+1, salteando la percusion), asi que suena identico a como sonaba.
     */
    @Test
    void aFileWhereEveryTrackSharesTheSameChannelGetsTheChannelsItUsedToSound() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v4-three-tracks-all-on-channel-one.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        assertEquals(1, loaded.track(0).channel().number());
        assertEquals(2, loaded.track(0).channel().effectChannel());
        assertEquals(3, loaded.track(1).channel().number());
        assertEquals(4, loaded.track(1).channel().effectChannel());
        assertEquals(5, loaded.track(2).channel().number());
        assertEquals(6, loaded.track(2).channel().effectChannel());
    }

    /**
     * Si los canales ya difieren entre si -por ejemplo porque el archivo viene de importar un
     * Guitar Pro real con canales propios- el usuario (o el importador) los eligio a proposito,
     * y no hay que reacomodarlos.
     */
    @Test
    void aFileWhereTracksAlreadyHaveDifferentChannelsIsLeftUntouched() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v4-two-tracks-with-different-channels.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        assertEquals(1, loaded.track(0).channel().number());
        assertEquals(2, loaded.track(0).channel().effectChannel());
        assertEquals(3, loaded.track(1).channel().number());
        assertEquals(4, loaded.track(1).channel().effectChannel());
    }

    @Test
    void keepsTheTwoChannelsOfATrackAcrossASave(@TempDir Path tempDir) {
        Score score = Score.blank().mappingTrack(0, track ->
                track.withChannel(track.channel().withNumber(3).withEffectChannel(11)));
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Channel loaded = scoreFiles.load(path).track(0).channel();

        assertEquals(3, loaded.number());
        assertEquals(11, loaded.effectChannel());
    }

    /**
     * La perdida que encontro la revision: un compas marcado 8va se guardaba y, al volver a
     * abrir el archivo, la marca habia desaparecido sin ningun aviso porque AttributesDto no
     * tenia campo para ella.
     */
    @Test
    void anOctaveMarkSurvivesSavingAndLoading(@TempDir Path tempDir) {
        Score score = scoreWithOctaveMark(OctaveMark.OTTAVA_ALTA);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(OctaveMark.OTTAVA_ALTA, loaded.track(0).measure(0).attributes().octaveMark());
    }

    /**
     * El default de {@link OctaveMark} tambien es {@code NONE}, asi que sin este otro compas el
     * test de arriba podria pasar por casualidad aunque el campo nunca se leyera: hace falta
     * probar tambien que un compas sin marca vuelve como NONE y no como cualquier otra cosa.
     */
    @Test
    void aMeasureWithoutAnOctaveMarkLoadsAsNone(@TempDir Path tempDir) {
        Score score = scoreWithOctaveMark(OctaveMark.NONE);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(OctaveMark.NONE, loaded.track(0).measure(0).attributes().octaveMark());
    }

    private static Score scoreWithOctaveMark(OctaveMark octaveMark) {
        Measure measure = new Measure(
                TimeSignature.fourFour(),
                MeasureAttributes.plain().withOctaveMark(octaveMark),
                List.of(new Voice(List.of(Beat.of(Duration.quarter(), new Note(6, 0)))), Voice.unused()));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }

    /**
     * El mismo tipo de perdida que la marca de octava (ver arriba), pero para el corte de barra
     * a mano que agrega BeamBreak: si BeatDto no lo escribe, forzar un corte se guarda y
     * desaparece sin aviso al volver a abrir el archivo.
     */
    @Test
    void aBeamBreakSurvivesSavingAndLoading(@TempDir Path tempDir) {
        Score score = scoreWithBeamBreak(BeamBreak.FORCED);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(BeamBreak.FORCED, loaded.track(0).measure(0).beat(0).effects().beamBreak());
    }

    /**
     * El default de {@link BeamBreak} tambien es AUTOMATIC, asi que sin este otro beat el test
     * de arriba podria pasar por casualidad aunque el campo nunca se leyera.
     */
    @Test
    void aBeatWithoutABeamBreakLoadsAsAutomatic(@TempDir Path tempDir) {
        Score score = scoreWithBeamBreak(BeamBreak.AUTOMATIC);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(BeamBreak.AUTOMATIC, loaded.track(0).measure(0).beat(0).effects().beamBreak());
    }

    @Test
    void aStemOverrideSurvivesSavingAndLoading(@TempDir Path tempDir) {
        Score score = scoreWithStemOverride(StemOverride.UP);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(StemOverride.UP, loaded.track(0).measure(0).beat(0).effects().stemOverride());
    }

    @Test
    void aBeatWithoutAStemOverrideLoadsAsAutomatic(@TempDir Path tempDir) {
        Score score = scoreWithStemOverride(StemOverride.AUTOMATIC);
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(StemOverride.AUTOMATIC, loaded.track(0).measure(0).beat(0).effects().stemOverride());
    }

    private static Score scoreWithBeamBreak(BeamBreak beamBreak) {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0))
                .withEffects(com.gstncaruso.tabpro.core.model.effects.BeatEffects.none().withBeamBreak(beamBreak));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }

    private static Score scoreWithStemOverride(StemOverride stemOverride) {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0))
                .withEffects(com.gstncaruso.tabpro.core.model.effects.BeatEffects.none().withStemOverride(stemOverride));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }
}
