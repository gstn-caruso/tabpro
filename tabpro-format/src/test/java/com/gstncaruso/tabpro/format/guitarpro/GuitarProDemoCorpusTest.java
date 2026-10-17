package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.TestDefaultNames;
import java.nio.file.Path;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/** Uses a locally supplied GP5 installation; no vendor scores are redistributed. */
@EnabledIfSystemProperty(named = "tabpro.gp5.demos", matches = ".+")
class GuitarProDemoCorpusTest {

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/guitarpro/gp5-demos.csv", delimiter = '|', numLinesToSkip = 1)
    void importsTheStructureRecordedByPyGuitarPro(String relativePath, int tracks, int bars, int tempo, int notes) {
        Path path = Path.of(System.getProperty("tabpro.gp5.demos")).resolve(relativePath);
        Score score = new GuitarProFile(new TestDefaultNames()).read(path);

        assertEquals(tracks, score.trackCount());
        assertEquals(bars, score.measureCount());
        assertEquals(tempo, score.tempo());
        long noteCount = score.tracks().stream().flatMap(track -> track.measures().stream())
                .flatMap(measure -> measure.voices().stream()).flatMap(voice -> voice.beats().stream())
                .mapToLong(beat -> beat.notes().size()).sum();
        assertEquals(notes, noteCount);
    }
}
