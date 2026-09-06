package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import org.junit.jupiter.api.Test;

/**
 * Lo que se fija en la solapa "Propiedades por defecto" de Informacion de la partitura tiene
 * que llegar de verdad a la partitura que crea Archivo > Nuevo.
 */
class NewScoreDefaultsTest {

    @Test
    void laPartituraNuevaUsaElTempoYElCompasFijadosPorDefecto() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                90, new TimeSignature(3, 4), KeySignature.cMajor(), "", "");

        Score nueva = defaults.newScore();

        assertEquals(90, nueva.tempo());
        assertEquals(new TimeSignature(3, 4), nueva.timeSignatureOf(0));
    }

    @Test
    void laPartituraNuevaUsaLaArmaduraFijadaPorDefecto() {
        KeySignature reBemolMenor = new KeySignature(-5, Mode.MINOR);
        NewScoreDefaults defaults = new NewScoreDefaults(
                120, TimeSignature.fourFour(), reBemolMenor, "", "");

        Score nueva = defaults.newScore();

        assertEquals(reBemolMenor, nueva.attributesOf(0).keySignature());
    }

    @Test
    void sinTituloNiArtistaLaPartituraQuedaSinTitulo() {
        NewScoreDefaults defaults = NewScoreDefaults.blank();

        Score nueva = defaults.newScore();

        assertEquals("", nueva.info().title());
        assertEquals("", nueva.info().artist());
    }

    @Test
    void conTituloYArtistaPorDefectoLaPartituraNuevaLosTrae() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                120, TimeSignature.fourFour(), KeySignature.cMajor(), "Improvisando", "Yo");

        Score nueva = defaults.newScore();

        assertEquals("Improvisando", nueva.info().title());
        assertEquals("Yo", nueva.info().artist());
    }
}
