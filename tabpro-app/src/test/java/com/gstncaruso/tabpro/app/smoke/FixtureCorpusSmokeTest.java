package com.gstncaruso.tabpro.app.smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.sound.midi.Synthesizer;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FixtureCorpusSmokeTest {

    private static final Duration MAXIMUM_TIME_PER_FILE = Duration.ofSeconds(30);

    private final ScoreExchange exchange = new CombinedExchange(
            new NotationExchange(),
            new SoundExchange(new WaveRenderer(FixtureCorpusSmokeTest::noRealSynthesizerNeeded)));
    private final JsonScoreFiles tabproFiles = new JsonScoreFiles();

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpusFixtures")
    void aCorpusFixtureCompletesTheWholePipelineOfThePermanentSafetyNet(Path path, @TempDir Path tempDir) {
        assertTimeoutPreemptively(MAXIMUM_TIME_PER_FILE, () -> runPipeline(path, tempDir),
                () -> path.getFileName() + ": did not complete the pipeline in "
                        + MAXIMUM_TIME_PER_FILE.toSeconds() + "s");
    }

    static Stream<Path> corpusFixtures() throws IOException {
        return Stream.of(guitarProFixtures(), powerTabFixtures(), musicXmlFixtures(), tabproFixtures())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }

    static Stream<Path> tabproFixtures() throws IOException {
        return fixturesWith(repoFile("tabpro-format/src/test/resources"), ".tabpro");
    }

    static Stream<Path> musicXmlFixtures() throws IOException {
        return fixturesWith(repoFile("tabpro-format/src/test/resources/musicxml"), ".musicxml");
    }

    static Stream<Path> guitarProFixtures() throws IOException {
        return fixturesWith(repoFile("tabpro-format/src/test/resources/guitarpro"), ".gp3", ".gp4", ".gp5");
    }

    private static final Map<String, String> POWERTAB_WITH_KNOWN_LIMITATION = Map.of(
            "guitar_ins.ptb",
            "reassigns staff 0 to another guitar halfway through the piece, ScoreFileException on purpose"
                    + " (PowerTabFileTest.aGuitarReassignmentIsReportedInsteadOfGuessed)",
            "merge_multibar_rests.ptb",
            "uses a compressed multi-bar rest, ScoreFileException on purpose"
                    + " (PowerTabFileTest.aMultibarRestIsReportedInsteadOfGuessed)",
            "positions.ptb",
            "uses a compressed multi-bar rest, ScoreFileException on purpose"
                    + " (PowerTabFileTest.aMultibarRestIsReportedInsteadOfGuessed)");

    static Stream<Path> powerTabFixtures() throws IOException {
        return fixturesWith(repoFile("tabpro-format/src/test/resources/powertab"), ".ptb")
                .filter(path -> !POWERTAB_WITH_KNOWN_LIMITATION.containsKey(path.getFileName().toString()));
    }

    private static Stream<Path> fixturesWith(Path directory, String... extensions) throws IOException {
        return Files.list(directory)
                .filter(path -> {
                    String name = path.getFileName().toString();
                    for (String extension : extensions) {
                        if (name.endsWith(extension)) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted()
                .toList()
                .stream();
    }

    private void runPipeline(Path path, Path tempDir) {
        Score score = open(path);
        assertNotNull(score, () -> path.getFileName() + ": opens through the real import path");
        assertFalse(score.tracks().isEmpty(), () -> path.getFileName() + ": has at least one track");

        List<BufferedImage> pages = renderPages(score);
        assertFalse(pages.isEmpty(), () -> path.getFileName() + ": renders at least one page in Pagina mode");

        BufferedImage parchment = renderParchment(score);
        assertTrue(parchment.getWidth() > 0 && parchment.getHeight() > 0,
                () -> path.getFileName() + ": renders in Pergamino mode");

        Score reopenedGp4 = exportAndReopenGp4(score, tempDir.resolve("reexported.gp4"));
        assertNotNull(reopenedGp4, () -> path.getFileName() + ": the .gp4 export reopens");

        Score reopenedMidi = exportAndReopenMidi(score, tempDir.resolve("reexported.mid"));
        assertNotNull(reopenedMidi, () -> path.getFileName() + ": the MIDI export reopens");

        Score reopenedMusicXml = exportAndReopenMusicXml(score, tempDir.resolve("reexported.musicxml"));
        assertNotNull(reopenedMusicXml, () -> path.getFileName() + ": the MusicXML export reopens");

        Score reopenedTabpro = saveAsTabproAndReopen(score, tempDir.resolve("reexported.tabpro"));
        assertEquals(score, reopenedTabpro, () -> path.getFileName() + ": saving as .tabpro reopens the same");
    }

    private List<BufferedImage> renderPages(Score score) {
        return ScoreSheets.renderPages(score, Zoom.whole(), PageSetup.defaults());
    }

    private BufferedImage renderParchment(Score score) {
        return ScoreSheets.render(score, ViewMode.PARCHMENT, Zoom.whole(), PageSetup.defaults());
    }

    private Score exportAndReopenGp4(Score score, Path gp4Path) {
        exchange.exportGuitarPro(score, gp4Path);
        return exchange.importGuitarPro(gp4Path);
    }

    private Score exportAndReopenMidi(Score score, Path midiPath) {
        exchange.exportMidi(score, midiPath);
        return exchange.importMidi(midiPath);
    }

    private Score exportAndReopenMusicXml(Score score, Path musicXmlPath) {
        exchange.exportMusicXml(score, musicXmlPath);
        return exchange.importMusicXml(musicXmlPath);
    }

    private Score saveAsTabproAndReopen(Score score, Path tabproPath) {
        tabproFiles.save(score, tabproPath);
        return tabproFiles.load(tabproPath);
    }

    private Score open(Path path) {
        String name = path.getFileName().toString();
        if (name.endsWith(".gp3") || name.endsWith(".gp4") || name.endsWith(".gp5")) {
            return exchange.importGuitarPro(path);
        }
        if (name.endsWith(".ptb")) {
            return exchange.importPowerTab(path);
        }
        if (name.endsWith(".musicxml")) {
            return exchange.importMusicXml(path);
        }
        if (name.endsWith(".tabpro")) {
            return tabproFiles.load(path);
        }
        throw new UnsupportedOperationException("does not resolve the extension of " + name + " yet");
    }

    private static Path repoFile(String relativeFromRepoRoot) {
        return Path.of(System.getProperty("user.dir"), "..", relativeFromRepoRoot).normalize();
    }

    private static Synthesizer noRealSynthesizerNeeded() {
        throw new UnsupportedOperationException("this test does not export WAVE, it should not ask for a synthesizer");
    }
}
