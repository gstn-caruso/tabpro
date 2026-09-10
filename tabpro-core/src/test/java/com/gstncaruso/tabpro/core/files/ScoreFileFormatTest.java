package com.gstncaruso.tabpro.core.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ScoreFileFormatTest {

    @Test
    void aTabproFileIsItsOwnFormat() {
        assertEquals(ScoreFileFormat.TABPRO, ScoreFileFormat.of(Path.of("cancion.tabpro")));
    }

    @Test
    void anUnknownExtensionFallsBackToTabpro() {
        assertEquals(ScoreFileFormat.TABPRO, ScoreFileFormat.of(Path.of("cancion")));
        assertEquals(ScoreFileFormat.TABPRO, ScoreFileFormat.of(Path.of("cancion.xyz")));
    }

    @Test
    void recognizesEveryGuitarProExtension() {
        assertEquals(ScoreFileFormat.GUITAR_PRO, ScoreFileFormat.of(Path.of("cancion.gp3")));
        assertEquals(ScoreFileFormat.GUITAR_PRO, ScoreFileFormat.of(Path.of("cancion.gp4")));
        assertEquals(ScoreFileFormat.GUITAR_PRO, ScoreFileFormat.of(Path.of("cancion.gp5")));
        assertEquals(ScoreFileFormat.GUITAR_PRO, ScoreFileFormat.of(Path.of("cancion.gtp")));
    }

    @Test
    void recognizesGuitarProRegardlessOfCase() {
        assertEquals(ScoreFileFormat.GUITAR_PRO, ScoreFileFormat.of(Path.of("Cancion.GP5")));
    }

    @Test
    void recognizesTabEdit() {
        assertEquals(ScoreFileFormat.TAB_EDIT, ScoreFileFormat.of(Path.of("cancion.tef")));
    }

    @Test
    void aPowerTabFileIsRecognizedByItsExtension() {
        assertEquals(ScoreFileFormat.POWER_TAB, ScoreFileFormat.of(Path.of("cancion.ptb")));
    }
}
