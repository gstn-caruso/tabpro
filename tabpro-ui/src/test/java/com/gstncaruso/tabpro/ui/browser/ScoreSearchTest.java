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
        assertTrue(ScoreSearch.isAScore(Path.of("song.tabpro")));
        assertTrue(ScoreSearch.isAScore(Path.of("song.gp5")));
        assertTrue(ScoreSearch.isAScore(Path.of("SONG.GP4")));
        assertFalse(ScoreSearch.isAScore(Path.of("song.mp3")));
    }

    @Test
    void findsTheScoresOfAFolder(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("one.tabpro"), "{}");
        Files.writeString(folder.resolve("another.gp5"), "");
        Files.writeString(folder.resolve("note.txt"), "");

        List<Path> found = ScoreSearch.inFolder(folder);

        assertEquals(List.of(folder.resolve("another.gp5"), folder.resolve("one.tabpro")), found);
    }

    @Test
    void aShallowSearchDoesNotEnterTheSubfolders(@TempDir Path folder) throws IOException {
        Files.createDirectory(folder.resolve("inside"));
        Files.writeString(folder.resolve("inside/hidden.tabpro"), "{}");

        assertEquals(List.of(), ScoreSearch.inFolder(folder));
    }

    @Test
    void aDeepSearchEntersTheSubfolders(@TempDir Path folder) throws IOException {
        Files.createDirectory(folder.resolve("inside"));
        Files.writeString(folder.resolve("inside/hidden.tabpro"), "{}");

        assertEquals(List.of(folder.resolve("inside/hidden.tabpro")), ScoreSearch.inFolderAndBelow(folder));
    }

    @Test
    void aMissingFolderFindsNothing(@TempDir Path folder) {
        assertEquals(List.of(), ScoreSearch.inFolderAndBelow(folder.resolve("does-not-exist")));
    }
}
