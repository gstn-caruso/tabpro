package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.Soundbank;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SoundFontsTest {

    @TempDir
    Path tempDir;

    @Test
    void thereIsNothingWhenTheDirectoryDoesNotExist() {
        List<Path> found = SoundFonts.installed(List.of(tempDir.resolve("no-existe")));

        assertEquals(List.of(), found);
    }

    @Test
    void findsSf2AndDlsFilesButIgnoresOthers() throws IOException {
        Files.createFile(tempDir.resolve("banco.sf2"));
        Files.createFile(tempDir.resolve("otro.dls"));
        Files.createFile(tempDir.resolve("leeme.txt"));

        List<Path> found = SoundFonts.installed(List.of(tempDir));

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(p -> p.getFileName().toString().equals("banco.sf2")));
        assertTrue(found.stream().anyMatch(p -> p.getFileName().toString().equals("otro.dls")));
    }

    @Test
    void looksInEveryDirectoryGiven() throws IOException {
        Path segundo = Files.createDirectory(tempDir.resolve("segundo"));
        Files.createFile(tempDir.resolve("uno.sf2"));
        Files.createFile(segundo.resolve("dos.sf2"));

        List<Path> found = SoundFonts.installed(List.of(tempDir, segundo));

        assertEquals(2, found.size());
    }

    @Test
    void readingAMissingFileIsEmpty() {
        Optional<Soundbank> banco = SoundFonts.read(tempDir.resolve("no-existe.sf2"));

        assertTrue(banco.isEmpty());
    }

    @Test
    void readingAnInvalidFileIsEmpty() throws IOException {
        Path bogus = tempDir.resolve("invalido.sf2");
        Files.writeString(bogus, "esto no es un banco SoundFont valido");

        Optional<Soundbank> banco = SoundFonts.read(bogus);

        assertTrue(banco.isEmpty());
    }

    /**
     * Sin un banco real instalado en la maquina este test no tiene nada que verificar; se
     * salta solo en vez de fallar, como pide correr headless en CI.
     */
    @Test
    void readingARealSoundFontInstalledInTheSystemWorks() {
        List<Path> real = SoundFonts.installed();
        Assumptions.assumeFalse(real.isEmpty(), "no hay ningun banco de sonido instalado en esta maquina");

        Optional<Soundbank> banco = SoundFonts.read(real.get(0));

        assertTrue(banco.isPresent());
        assertTrue(banco.get().getInstruments().length > 0);
    }
}
