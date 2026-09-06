package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * El fixture es {@code song_header.ptb}, del propio repositorio de
 * powertabeditor (powertab/powertabeditor, GPLv3): un archivo real usado por
 * su propia suite de tests para probar la lectura de la cabecera.
 */
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
        PowerTabByteReader bytes = new PowerTabByteReader("esto no es un archivo de PowerTab".getBytes());

        assertThrows(ScoreFileException.class, () -> reader.read(bytes));
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
