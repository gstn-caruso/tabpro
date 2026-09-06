package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MidiUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * SoundFontSynthesizer es quien provee el sintetizador ya listo (abierto, con el banco puesto si
 * hay uno) que reusan tanto la reproduccion en vivo como cualquier renderizador a archivo.
 */
class SoundFontSynthesizerTest {

    @TempDir
    Path tempDir;

    private SoundFontSynthesizer bank;

    @AfterEach
    void tearDown() {
        if (bank != null) {
            bank.close();
        }
    }

    @Test
    void withoutAnyFileItStaysOnTheInternalSynthesizer() {
        bank = open(Optional.empty());

        assertFalse(bank.active());
        assertTrue(bank.file().isEmpty());
    }

    @Test
    void anInvalidFileDegradesToTheInternalSynthesizerWithoutThrowing() throws IOException {
        Path bogus = tempDir.resolve("invalido.sf2");
        Files.writeString(bogus, "esto no es un banco SoundFont valido");

        bank = open(Optional.of(bogus));

        assertFalse(bank.active());
        assertTrue(bank.file().isEmpty(), "un banco que no cargo no puede quedar activo");
        assertTrue(bank.status().contains("invalido.sf2"), "el estado tiene que nombrar el archivo que fallo");
    }

    @Test
    void aMissingFileDegradesToTheInternalSynthesizerWithoutThrowing() {
        bank = open(Optional.of(tempDir.resolve("no-existe.sf2")));

        assertFalse(bank.active());
    }

    @Test
    void togglingWithoutAnyBankLoadedDoesNothing() {
        bank = open(Optional.empty());

        bank.toggle();

        assertFalse(bank.active());
    }

    @Test
    void aRealSoundFontLoadsAndBecomesActive() {
        Path real = firstInstalledOrSkip();

        bank = open(Optional.of(real));

        assertTrue(bank.active());
        assertTrue(bank.file().isPresent());
    }

    @Test
    void togglingItOffAndOnAgainSwitchesTheLoadedInstruments() {
        Path real = firstInstalledOrSkip();
        bank = open(Optional.of(real));
        int loadedWithBank = bank.synthesizer().getLoadedInstruments().length;

        bank.toggle();
        assertFalse(bank.active());
        int loadedWithoutBank = bank.synthesizer().getLoadedInstruments().length;
        assertNotEquals(loadedWithBank, loadedWithoutBank, "apagar el banco tiene que cambiar los instrumentos cargados");

        bank.toggle();
        assertTrue(bank.active());
        assertEquals(loadedWithBank, bank.synthesizer().getLoadedInstruments().length);
    }

    private Path firstInstalledOrSkip() {
        List<Path> real = SoundFonts.installed();
        Assumptions.assumeFalse(real.isEmpty(), "no hay ningun banco de sonido instalado en esta maquina");
        return real.get(0);
    }

    private SoundFontSynthesizer open(Optional<Path> file) {
        try {
            return SoundFontSynthesizer.open(file);
        } catch (MidiUnavailableException e) {
            Assumptions.assumeTrue(false, "sin sintetizador MIDI disponible en esta maquina");
            throw new AssertionError(e);
        }
    }
}
