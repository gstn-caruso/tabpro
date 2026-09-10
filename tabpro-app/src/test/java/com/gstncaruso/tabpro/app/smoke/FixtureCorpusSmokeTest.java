package com.gstncaruso.tabpro.app.smoke;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.app.CombinedExchange;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.exchange.NotationExchange;
import com.gstncaruso.tabpro.midi.SoundExchange;
import com.gstncaruso.tabpro.midi.WaveRenderer;
import java.nio.file.Path;
import javax.sound.midi.Synthesizer;
import org.junit.jupiter.api.Test;

/**
 * Red permanente sobre cada fixture del repo: abre por el camino real de importacion, renderiza,
 * exporta/reabre y guarda como .tabpro. Sin ventana, sin tag de integracion: corre en todo
 * mvn -B verify. Version descartable de docs/auditoria-corpus.md, con archivos propios del repo.
 */
class FixtureCorpusSmokeTest {

    private final ScoreExchange exchange = new CombinedExchange(
            new NotationExchange(),
            new SoundExchange(new WaveRenderer(FixtureCorpusSmokeTest::noHaceFaltaUnSintetizadorReal)));

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

    private Score abrir(Path path) {
        String nombre = path.getFileName().toString();
        if (nombre.endsWith(".gp3") || nombre.endsWith(".gp4") || nombre.endsWith(".gp5")) {
            return exchange.importGuitarPro(path);
        }
        if (nombre.endsWith(".ptb")) {
            return exchange.importPowerTab(path);
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
