package com.gstncaruso.tabpro.app.smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.app.CombinedExchange;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.format.exchange.NotationExchange;
import com.gstncaruso.tabpro.midi.SoundExchange;
import com.gstncaruso.tabpro.midi.WaveRenderer;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.print.ScoreSheets;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.sound.midi.Synthesizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Red permanente sobre cada fixture del repo: abre por el camino real de importacion, renderiza,
 * exporta/reabre y guarda como .tabpro. Sin ventana, sin tag de integracion: corre en todo
 * mvn -B verify. Version descartable de docs/auditoria-corpus.md, con archivos propios del repo.
 */
class FixtureCorpusSmokeTest {

    private final ScoreExchange exchange = new CombinedExchange(
            new NotationExchange(),
            new SoundExchange(new WaveRenderer(FixtureCorpusSmokeTest::noHaceFaltaUnSintetizadorReal)));
    private final JsonScoreFiles tabproFiles = new JsonScoreFiles();

    @Test
    void unGuitarProSimpleAbrePorElCaminoRealDeImportacion() {
        Path path = repoFile("tabpro-format/src/test/resources/guitarpro/tabpro-synthetic.gp5");

        Score score = abrir(path);

        assertNotNull(score, () -> path.getFileName() + ": abre por el camino real de importacion");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": tiene al menos una pista");
    }

    @Test
    void unPowerTabSimpleAbrePorElCaminoRealDeImportacion() {
        Path path = repoFile("tabpro-format/src/test/resources/powertab/guitars.ptb");

        Score score = abrir(path);

        assertNotNull(score, () -> path.getFileName() + ": abre por el camino real de importacion");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": tiene al menos una pista");
    }

    @Test
    void unTabproPropioAbrePorElCaminoRealDeImportacion() {
        Path path = repoFile("tabpro-format/src/test/resources/v1-one-measure.tabpro");

        Score score = abrir(path);

        assertNotNull(score, () -> path.getFileName() + ": abre por el camino real de importacion");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": tiene al menos una pista");
    }

    @Test
    void unMusicXmlSimpleAbrePorElCaminoRealDeImportacion() {
        Path path = repoFile("tabpro-format/src/test/resources/musicxml/armadura-en-fa.musicxml");

        Score score = abrir(path);

        assertNotNull(score, () -> path.getFileName() + ": abre por el camino real de importacion");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": tiene al menos una pista");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixturesDelCorpus")
    void unGuitarProCompletaTodoElPipelineDeLaRedPermanente(Path path, @TempDir Path tempDir) {
        ejecutarPipeline(path, tempDir);
    }

    static Stream<Path> fixturesDelCorpus() throws IOException {
        return Stream.of(fixturesDeGuitarPro(), fixturesDePowerTab(), fixturesDeMusicXml(), fixturesDeTabpro())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }

    static Stream<Path> fixturesDeTabpro() throws IOException {
        return fixturesCon(repoFile("tabpro-format/src/test/resources"), ".tabpro");
    }

    static Stream<Path> fixturesDeMusicXml() throws IOException {
        return fixturesCon(repoFile("tabpro-format/src/test/resources/musicxml"), ".musicxml");
    }

    static Stream<Path> fixturesDeGuitarPro() throws IOException {
        return fixturesCon(repoFile("tabpro-format/src/test/resources/guitarpro"), ".gp3", ".gp4", ".gp5");
    }

    private static final Map<String, String> POWERTAB_CON_LIMITACION_CONOCIDA = Map.of(
            "guitar_ins.ptb",
            "reasigna el pentagrama 0 a otra guitarra a mitad de la pieza, ScoreFileException a proposito"
                    + " (PowerTabFileTest.aGuitarReassignmentIsReportedInsteadOfGuessed)",
            "merge_multibar_rests.ptb",
            "usa un silencio de varios compases comprimido (multibar rest), ScoreFileException a proposito"
                    + " (PowerTabFileTest.aMultibarRestIsReportedInsteadOfGuessed)",
            "positions.ptb",
            "usa un silencio de varios compases comprimido (multibar rest), ScoreFileException a proposito"
                    + " (PowerTabFileTest.aMultibarRestIsReportedInsteadOfGuessed)");

    static Stream<Path> fixturesDePowerTab() throws IOException {
        return fixturesCon(repoFile("tabpro-format/src/test/resources/powertab"), ".ptb")
                .filter(path -> !POWERTAB_CON_LIMITACION_CONOCIDA.containsKey(path.getFileName().toString()));
    }

    private static Stream<Path> fixturesCon(Path directorio, String... extensiones) throws IOException {
        return Files.list(directorio)
                .filter(path -> {
                    String nombre = path.getFileName().toString();
                    for (String extension : extensiones) {
                        if (nombre.endsWith(extension)) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted()
                .toList()
                .stream();
    }

    private void ejecutarPipeline(Path path, Path tempDir) {
        Score score = abrir(path);
        assertNotNull(score, () -> path.getFileName() + ": abre por el camino real de importacion");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": tiene al menos una pista");

        List<BufferedImage> paginas = renderizarPaginas(score);
        assertFalse(paginas.isEmpty(), () -> path.getFileName() + ": renderiza al menos una pagina en modo Pagina");

        BufferedImage pergamino = renderizarPergamino(score);
        assertTrue(pergamino.getWidth() > 0 && pergamino.getHeight() > 0,
                () -> path.getFileName() + ": renderiza en modo Pergamino");

        Score reabiertoGp4 = exportarYReabrirGp4(score, tempDir.resolve("reexportado.gp4"));
        assertNotNull(reabiertoGp4, () -> path.getFileName() + ": el export a .gp4 se reabre");

        Score reabiertoMidi = exportarYReabrirMidi(score, tempDir.resolve("reexportado.mid"));
        assertNotNull(reabiertoMidi, () -> path.getFileName() + ": el export a MIDI se reabre");

        Score reabiertoMusicXml = exportarYReabrirMusicXml(score, tempDir.resolve("reexportado.musicxml"));
        assertNotNull(reabiertoMusicXml, () -> path.getFileName() + ": el export a MusicXML se reabre");

        Score reabiertoTabpro = guardarComoTabproYReabrir(score, tempDir.resolve("reexportado.tabpro"));
        assertEquals(score, reabiertoTabpro, () -> path.getFileName() + ": el guardado como .tabpro se reabre igual");
    }

    private List<BufferedImage> renderizarPaginas(Score score) {
        return ScoreSheets.renderPages(score, Zoom.whole(), PageSetup.defaults());
    }

    private BufferedImage renderizarPergamino(Score score) {
        return ScoreSheets.render(score, ViewMode.PARCHMENT, Zoom.whole(), PageSetup.defaults());
    }

    private Score exportarYReabrirGp4(Score score, Path gp4Path) {
        exchange.exportGuitarPro(score, gp4Path);
        return exchange.importGuitarPro(gp4Path);
    }

    private Score exportarYReabrirMidi(Score score, Path midiPath) {
        exchange.exportMidi(score, midiPath);
        return exchange.importMidi(midiPath);
    }

    private Score exportarYReabrirMusicXml(Score score, Path musicXmlPath) {
        exchange.exportMusicXml(score, musicXmlPath);
        return exchange.importMusicXml(musicXmlPath);
    }

    private Score guardarComoTabproYReabrir(Score score, Path tabproPath) {
        tabproFiles.save(score, tabproPath);
        return tabproFiles.load(tabproPath);
    }

    private Score abrir(Path path) {
        String nombre = path.getFileName().toString();
        if (nombre.endsWith(".gp3") || nombre.endsWith(".gp4") || nombre.endsWith(".gp5")) {
            return exchange.importGuitarPro(path);
        }
        if (nombre.endsWith(".ptb")) {
            return exchange.importPowerTab(path);
        }
        if (nombre.endsWith(".musicxml")) {
            return exchange.importMusicXml(path);
        }
        if (nombre.endsWith(".tabpro")) {
            return tabproFiles.load(path);
        }
        throw new UnsupportedOperationException("todavia no resuelve la extension de " + nombre);
    }

    private static Path repoFile(String relativeFromRepoRoot) {
        return Path.of(System.getProperty("user.dir"), "..", relativeFromRepoRoot).normalize();
    }

    private static Synthesizer noHaceFaltaUnSintetizadorReal() {
        throw new UnsupportedOperationException("este test no exporta WAVE, no deberia pedir sintetizador");
    }
}
