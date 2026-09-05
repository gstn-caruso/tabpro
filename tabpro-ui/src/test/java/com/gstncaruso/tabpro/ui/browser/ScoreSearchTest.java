package com.gstncaruso.tabpro.ui.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScoreSearchTest {

    @Test
    void recognisesTheFormatsTheProgramOpens() {
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.tabpro")));
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.gp5")));
        assertTrue(ScoreSearch.isAScore(Path.of("CANCION.GP4")));
        assertFalse(ScoreSearch.isAScore(Path.of("cancion.mp3")));
    }

    @Test
    void findsTheScoresOfAFolder(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("una.tabpro"), "{}");
        Files.writeString(folder.resolve("otra.gp5"), "");
        Files.writeString(folder.resolve("nota.txt"), "");

        List<Path> found = ScoreSearch.inFolder(folder);

        assertEquals(List.of(folder.resolve("otra.gp5"), folder.resolve("una.tabpro")), found);
    }

    @Test
    void aShallowSearchDoesNotEnterTheSubfolders(@TempDir Path folder) throws IOException {
        Files.createDirectory(folder.resolve("adentro"));
        Files.writeString(folder.resolve("adentro/escondida.tabpro"), "{}");

        assertEquals(List.of(), ScoreSearch.inFolder(folder));
    }

    @Test
    void aDeepSearchEntersTheSubfolders(@TempDir Path folder) throws IOException {
        Files.createDirectory(folder.resolve("adentro"));
        Files.writeString(folder.resolve("adentro/escondida.tabpro"), "{}");

        assertEquals(List.of(folder.resolve("adentro/escondida.tabpro")), ScoreSearch.inFolderAndBelow(folder));
    }

    @Test
    void aMissingFolderFindsNothing(@TempDir Path folder) {
        assertEquals(List.of(), ScoreSearch.inFolderAndBelow(folder.resolve("no-existe")));
    }
}
