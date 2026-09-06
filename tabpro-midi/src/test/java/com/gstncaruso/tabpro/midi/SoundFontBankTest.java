package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.media.sound.AudioSynthesizer;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * SoundFontBank es el banco global: cada puerto de MidiPlayer que use el sintetizador interno le
 * pide su receiver aca. La mayoria de las maquinas -el CI incluido- NO tienen ningun SoundFont
 * instalado, asi que ese es el camino que mas importa probar de verdad: apuntando el banco
 * directamente a "ningun archivo" en vez de confiar en que la maquina de prueba no tenga nada
 * instalado. Estos tests corren siempre, nunca se saltean con Assumptions.
 */
class SoundFontBankTest {

    @TempDir
    Path tempDir;

    private SoundFontBank bank;

    @AfterEach
    void tearDown() {
        if (bank != null) {
            bank.close();
        }
    }

    // ---- sin ningun banco: el camino de la mayoria de los usuarios, siempre, sin Assumptions ----

    @Test
    void withoutAnyFileEveryPortStillSoundsWithTheInternalSynth() {
        bank = new SoundFontBank(Optional.empty());

        Receiver receiver = bank.receiverForPort(1);

        assertNotNull(receiver);
        assertDoesNotThrow(() -> receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1));
    }

    @Test
    void withoutAnyFileItIsNotActive() {
        bank = new SoundFontBank(Optional.empty());

        assertFalse(bank.active());
        assertTrue(bank.file().isEmpty());
    }

    @Test
    void withoutAnyFileTheStatusSaysSo() {
        bank = new SoundFontBank(Optional.empty());

        assertEquals("Sin ningún banco de sonido: suena el sintetizador interno del JDK", bank.status());
    }

    @Test
    void withoutAnyFileTogglingDoesNothing() {
        bank = new SoundFontBank(Optional.empty());

        bank.toggle();

        assertFalse(bank.active());
    }

    @Test
    void withoutAnyFileEveryPortWorksIndependently() {
        bank = new SoundFontBank(Optional.empty());

        Receiver port1 = bank.receiverForPort(1);
        Receiver port2 = bank.receiverForPort(2);
        Receiver port3 = bank.receiverForPort(3);
        Receiver port4 = bank.receiverForPort(4);

        assertDoesNotThrow(() -> {
            port1.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);
            port2.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 61, 100), -1);
            port3.send(new ShortMessage(ShortMessage.NOTE_ON, 2, 62, 100), -1);
            port4.send(new ShortMessage(ShortMessage.NOTE_ON, 3, 63, 100), -1);
        });
    }

    @Test
    void withoutAnyFileAFreshSynthesizerForWaveExportStillWorks() throws Exception {
        bank = new SoundFontBank(Optional.empty());

        Synthesizer synth = bank.freshSynthesizer();

        assertNotNull(synth);
        synth.close();
    }

    /** El caso de la consigna: un banco elegido a mano que no carga no puede tirar abajo nada. */
    @Test
    void anInvalidFileDegradesToTheInternalSynthOnEveryPortWithoutBreakingAnything() throws IOException {
        Path bogus = tempDir.resolve("invalido.sf2");
        Files.writeString(bogus, "esto no es un banco SoundFont valido");
        bank = new SoundFontBank(Optional.of(bogus));

        Receiver port1 = bank.receiverForPort(1);
        Receiver port2 = bank.receiverForPort(2);

        assertFalse(bank.active(), "un archivo que no carga no puede quedar activo");
        assertDoesNotThrow(() -> {
            port1.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);
            port2.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 61, 100), -1);
        });
        assertTrue(bank.status().contains("invalido.sf2"));
    }

    // ---- sin ningun sintetizador disponible: distinto de "sin banco". Aca ni el sintetizador
    // interno del JDK se puede abrir -una maquina sin placa de audio real ni virtual, como el CI.
    // Se inyecta de donde sale el sintetizador (la misma costura que MidiPlayer.PortOutput ya usa
    // con su Supplier<Sequencer>) para que estos tests corran siempre, en cualquier maquina.

    @Test
    void withoutAnySynthesizerAvailableEveryPortDegradesToSilenceWithoutBreakingAnything() {
        bank = new SoundFontBank(Optional.empty(), () -> null);

        Receiver port1 = bank.receiverForPort(1);
        Receiver port2 = bank.receiverForPort(2);

        assertFalse(bank.active());
        assertDoesNotThrow(() -> {
            port1.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);
            port2.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 61, 100), -1);
        });
    }

    @Test
    void withoutAnySynthesizerAvailableAChosenFileCannotBeActiveEither() {
        bank = new SoundFontBank(Optional.of(tempDir.resolve("cualquiera.sf2")), () -> null);

        bank.receiverForPort(1);

        assertFalse(bank.active(), "sin sintetizador no hay donde cargar nada, activo miente igual que con un archivo invalido");
        assertTrue(bank.status().contains("No se pudo cargar"));
    }

    @Test
    void withoutAnySynthesizerAvailableFreshSynthesizerFailsWithAClearException() {
        bank = new SoundFontBank(Optional.empty(), () -> null);

        MidiUnavailableException thrown = assertThrows(MidiUnavailableException.class, bank::freshSynthesizer);

        assertNotNull(thrown.getMessage());
    }

    /**
     * El render a WAVE es offline: nunca tiene que pedir una linea de audio real
     * (Synthesizer.open()), porque eso es exactamente lo que revienta en una maquina sin placa de
     * sonido aunque nadie vaya a escuchar nada en vivo. Se instrumenta el sintetizador para que
     * abrir la linea real explote, y se confirma que freshSynthesizer jamas la toca. Corre
     * siempre: no depende de si esta maquina tiene sonido de verdad.
     */
    @Test
    void freshSynthesizerNeverOpensARealTimeLineSinceWaveExportIsOffline() throws Exception {
        Synthesizer instrumented = synthesizerThatExplodesIfOpenedForRealTime();
        bank = new SoundFontBank(Optional.empty(), () -> instrumented);

        Synthesizer synth = assertDoesNotThrow(bank::freshSynthesizer);

        assertNotNull(synth);
    }

    /** Lo mismo, pero con un banco de verdad puesto: tampoco ahi puede abrir una linea real. */
    @Test
    void freshSynthesizerNeverOpensARealTimeLineWhenLoadingARealBank() throws Exception {
        Path real = firstInstalledOrSkip();
        Synthesizer instrumented = synthesizerThatExplodesIfOpenedForRealTime();
        bank = new SoundFontBank(Optional.of(real), () -> instrumented);

        Synthesizer synth = assertDoesNotThrow(bank::freshSynthesizer);
        AudioSynthesizer audioSynth = (AudioSynthesizer) synth;
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(44_100, 16, 2, true, false);
        javax.sound.sampled.AudioInputStream stream = audioSynth.openStream(format, java.util.Map.of());

        assertTrue(synth.getLoadedInstruments().length > 0, "el banco tendria que haber quedado cargado al abrir el stream offline");
        synth.close();
    }

    private static Synthesizer synthesizerThatExplodesIfOpenedForRealTime() throws MidiUnavailableException {
        Synthesizer real = MidiSystem.getSynthesizer();
        return (Synthesizer) Proxy.newProxyInstance(
                SoundFontBankTest.class.getClassLoader(),
                new Class<?>[] {AudioSynthesizer.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("open") && method.getParameterCount() == 0) {
                        throw new AssertionError(
                                "freshSynthesizer no puede abrir una linea de audio real: el render es offline");
                    }
                    return method.invoke(real, args);
                });
    }

    // ---- con un banco real instalado: se saltea sola si la maquina no tiene ninguno ----

    @Test
    void aRealFileLoadsIndependentlyOnEachPortItIsAskedFor() {
        Path real = firstInstalledOrSkip();
        bank = new SoundFontBank(Optional.of(real));

        bank.receiverForPort(1);
        bank.receiverForPort(2);

        assertTrue(bank.active());
        assertEquals("Sonando con " + real.getFileName(), bank.status());
    }

    @Test
    void toggleTurnsOffEveryOpenPortAtOnce() {
        Path real = firstInstalledOrSkip();
        bank = new SoundFontBank(Optional.of(real));
        bank.receiverForPort(1);
        bank.receiverForPort(2);

        bank.toggle();

        assertFalse(bank.active());
    }

    private Path firstInstalledOrSkip() {
        List<Path> real = SoundFonts.installed();
        Assumptions.assumeFalse(real.isEmpty(), "no hay ningun banco de sonido instalado en esta maquina");
        return real.get(0);
    }
}
