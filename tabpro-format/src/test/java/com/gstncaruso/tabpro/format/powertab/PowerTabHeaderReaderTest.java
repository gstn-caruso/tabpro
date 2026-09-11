package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PowerTabHeaderReaderTest {

    private final PowerTabHeaderReader reader = new PowerTabHeaderReader();

    @Test
    void readsTheSongInformation() {
        PowerTabHeader header = read("song_header");

        assertEquals("Some Title", header.title());
        assertEquals("Some Artist", header.artist());
        assertEquals("Some Author", header.composer());
        assertEquals("Some Lyricist", header.lyricist());
        assertEquals("Some Arranger", header.arranger());
        assertEquals("Some Transcriber", header.transcriber());
        assertEquals("2001", header.copyright());
        assertEquals("Some lyrics", header.lyrics());
        assertEquals("Some notes.", header.notes());
    }

    @Test
    void aFileThatIsNotPowerTabIsReported() {
        PowerTabByteReader bytes = new PowerTabByteReader("this is not a PowerTab file".getBytes());

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> reader.read(bytes));

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("PowerTab"), failure.arguments());
    }

    @Test
    void aVersionOtherThanOnePointSevenIsReported() {
        PowerTabByteReader bytes = new PowerTabByteReader(new byte[] {'p', 't', 'a', 'b', 3, 0});

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> reader.read(bytes));

        assertEquals(ScoreFileProblem.UNSUPPORTED_VERSION, failure.problem());
        assertEquals(List.of("PowerTab", "3"), failure.arguments());
    }

    @Test
    void aLessonIsReportedAsContentThatIsNotSupported() {
        PowerTabByteReader bytes = new PowerTabByteReader(new byte[] {'p', 't', 'a', 'b', 4, 0, 1});

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> reader.read(bytes));

        assertEquals(ScoreFileProblem.UNSUPPORTED_CONTENT, failure.problem());
        assertEquals(List.of(ScoreFeature.POWER_TAB_LESSONS), failure.arguments());
    }

    @Test
    void anUnknownFileTypeIsNotRecognized() {
        PowerTabByteReader bytes = new PowerTabByteReader(new byte[] {'p', 't', 'a', 'b', 4, 0, 7});

        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> reader.read(bytes));

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("PowerTab"), failure.arguments());
    }

    private PowerTabHeader read(String name) {
        return reader.read(new PowerTabByteReader(bytesOf(name)));
    }

    static byte[] bytesOf(String name) {
        try {
            Path path = Path.of(PowerTabHeaderReaderTest.class.getResource("/powertab/" + name + ".ptb").toURI());
            return Files.readAllBytes(path);
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
