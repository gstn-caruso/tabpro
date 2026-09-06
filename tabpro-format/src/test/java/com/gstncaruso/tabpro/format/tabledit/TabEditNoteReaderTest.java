package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import org.junit.jupiter.api.Test;

/**
 * Los siete bytes de carga util de una nota, mas el traste y la marca de nota
 * de adorno que vienen en el byte de tipo del componente.
 */
class TabEditNoteReaderTest {

    private final TabEditNoteReader reader = new TabEditNoteReader();

    @Test
    void leeElTrasteLaDuracionLaDinamicaYLaVoz() {
        TabEditNoteFields fields = read(note(false, 3, 6, 0, 0, 0, 0, 0, 0, false)); // duracion negra, FFF, voz principal

        assertEquals(3, fields.fret());
        assertEquals(NoteValue.QUARTER, fields.duration().value());
        assertEquals(Dynamic.FORTE_FORTISSIMO, fields.dynamic());
        assertEquals(VoicePart.LEAD, fields.voice());
        assertFalse(fields.tied());
    }

    @Test
    void laVozSeSacaDelAtributoDeLaNota() {
        TabEditNoteFields fields = read(note(false, 0, 6, 3 /* LowerVoice */, 0, 0, 0, 0, 0, false));

        assertEquals(VoicePart.BASS, fields.voice());
    }

    @Test
    void unaNotaLigadaPorElBitDeTieQuedaMarcada() {
        TabEditNoteFields fields = read(note(false, 0, 6, 0, 0, 0, 0, 0, 0, true));

        assertTrue(fields.tied());
    }

    @Test
    void laDinamicaPppTambienMarcaLaLigaduraSegunLaConvencionDeTablEdit() {
        TabEditNoteFields fields = read(note(false, 0, 6, 0, 7 /* PPP */, 0, 0, 0, 0, false));

        assertEquals(Dynamic.PIANO_PIANISSIMO, fields.dynamic());
        assertTrue(fields.tied());
    }

    @Test
    void ligadoArmonicosYPalmMuteDesdeElPrimerEfecto() {
        assertTrue(read(note(false, 0, 6, 0, 0, 1 /* HammerOn */, 0, 0, 0, false))
                .effects().has(Ornament.HAMMER_ON_PULL_OFF));
        assertTrue(read(note(false, 0, 6, 0, 0, 2 /* PullOff */, 0, 0, 0, false))
                .effects().has(Ornament.HAMMER_ON_PULL_OFF));
        assertEquals(HarmonicType.NATURAL,
                read(note(false, 0, 6, 0, 0, 6, 0, 0, 0, false)).effects().harmonic().orElseThrow());
        assertEquals(HarmonicType.ARTIFICIAL,
                read(note(false, 0, 6, 0, 0, 7, 0, 0, 0, false)).effects().harmonic().orElseThrow());
        assertTrue(read(note(false, 0, 6, 0, 0, 8 /* PalmMute */, 0, 0, 0, false)).effects().has(Ornament.PALM_MUTE));
        assertTrue(read(note(false, 0, 6, 0, 0, 10 /* Vibrato */, 0, 0, 0, false)).effects().has(Ornament.VIBRATO));
        assertTrue(read(note(false, 0, 6, 0, 0, 15 /* DeadNote */, 0, 0, 0, false)).effects().has(Ornament.DEAD));
    }

    @Test
    void unSlideNoTraeDireccionYSeAproximaComoLegato() {
        TabEditNoteFields fields = read(note(false, 0, 6, 0, 0, 3 /* Slide */, 0, 0, 0, false));

        assertEquals(SlideType.LEGATO, fields.effects().slide().orElseThrow());
    }

    @Test
    void letRingFantasmaYStaccatoDesdeElSegundoEfecto() {
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 1 /* LetRing */, 0, 0, false)).effects().has(Ornament.LET_RING));
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 4 /* GhostNote */, 0, 0, false)).effects().has(Ornament.GHOST));
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 7 /* Staccato */, 0, 0, false)).effects().has(Ornament.STACCATO));
    }

    @Test
    void tapSlapYFadeInSonMarcasDeTodoElBeat() {
        assertTrue(read(note(false, 0, 6, 0, 0, 9 /* Tap */, 0, 0, 0, false)).tapping());
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 2 /* Slap */, 0, 0, false)).slapping());
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 8 /* FadeIn */, 0, 0, false)).fadeIn());
    }

    @Test
    void elTercerEfectoSumaLigadoYArmonicosSiElPrimeroNoLosTrajo() {
        assertTrue(read(note(false, 0, 6, 0, 0, 0, 0, 1 /* HammerOn */, 0, false))
                .effects().has(Ornament.HAMMER_ON_PULL_OFF));
        assertEquals(HarmonicType.NATURAL,
                read(note(false, 0, 6, 0, 0, 0, 0, 6, 0, false)).effects().harmonic().orElseThrow());
    }

    @Test
    void unaNotaDeAdornoTraeSuTrasteYSuTransicion() {
        TabEditNoteFields fields = read(note(true, 0, 6, 0, 0, 0, 0, 0, 5 /* graceNoteFret */, false));

        assertTrue(fields.isGraceNote());
        assertEquals(5, fields.graceNoteFret());
    }

    /**
     * Arma el byte de tipo y los siete de carga util con los campos que le
     * importan a cada test; el resto queda en cero.
     */
    private static byte[] note(
            boolean isGraceNote, int fret, int duration, int attributes, int dynamics, int effect1, int effect2,
            int effect3, int graceNoteFret, boolean tied) {
        int type = (fret + 1) | (isGraceNote ? 0x40 : 0);
        int byte1 = (dynamics << 5) | duration;
        int byte2 = (attributes << 4) | effect1;
        int byte3 = graceNoteFret & 0x1F;
        int byte4 = (effect3 << 4) | effect2;
        int byte7 = tied ? (1 << 5) : 0;
        return new byte[] {(byte) type, (byte) byte1, (byte) byte2, (byte) byte3, (byte) byte4, 0, 0, (byte) byte7};
    }

    private TabEditNoteFields read(byte[] bytes) {
        TabEditByteReader input = new TabEditByteReader(bytes);
        int type = input.readUnsignedByte();
        return reader.read(input, type);
    }
}
