package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.actions.Ports.SoundBankStatus;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class SoundBankStatusTextTest {

    @Test
    void withoutAnyBankTheStatusSaysSoInSpanish() {
        assertEquals("Sin ningún banco de sonido: suena el sintetizador interno del JDK",
                SoundBankStatusText.of(SoundBankStatus.none()));
    }

    @Test
    void aPlayingBankNamesTheFileInSpanish() {
        assertEquals("Sonando con real.sf2", SoundBankStatusText.of(SoundBankStatus.playing("real.sf2")));
    }

    @Test
    void whenMidiIsUnavailableTheStatusSaysSoInSpanish() {
        assertEquals("MIDI no disponible", SoundBankStatusText.of(SoundBankStatus.unavailable()));
    }

    @Test
    void everyKindIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("No sound font: the JDK internal synthesizer plays", english.text("views.soundBank.none"));
        assertEquals("MIDI unavailable", english.text("views.soundBank.unavailable"));
        assertEquals("Chosen bank: real.sf2 (applies on playback)",
                english.text("views.soundBank.chosen", "real.sf2"));
        assertEquals("Sound font disabled (real.sf2): the JDK internal synthesizer plays",
                english.text("views.soundBank.disabled", "real.sf2"));
        assertEquals("Playing with real.sf2", english.text("views.soundBank.playing", "real.sf2"));
        assertEquals("Could not load real.sf2: the JDK internal synthesizer plays",
                english.text("views.soundBank.failed", "real.sf2"));
    }
}
