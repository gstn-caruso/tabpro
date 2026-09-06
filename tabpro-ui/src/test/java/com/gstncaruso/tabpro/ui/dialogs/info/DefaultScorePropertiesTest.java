package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultScorePropertiesTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName());
    private final DefaultScoreProperties stored = new DefaultScoreProperties(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void sinNadaGuardadoElDefectoEsElQueTraePartituraEnBlanco() {
        assertEquals(NewScoreDefaults.blank(), stored.get());
    }

    @Test
    void guardarLoDejaComoElDefecto() {
        NewScoreDefaults mios = new NewScoreDefaults(
                90, new TimeSignature(3, 4), new KeySignature(-2, Mode.MINOR), "Improvisando", "Yo");

        stored.save(mios);

        assertEquals(mios, stored.get());
    }

    @Test
    void guardarDeNuevoReemplazaLoQueHabia() {
        stored.save(new NewScoreDefaults(60, TimeSignature.fourFour(), KeySignature.cMajor(), "", ""));
        NewScoreDefaults ultimo = new NewScoreDefaults(
                180, new TimeSignature(6, 8), new KeySignature(4, Mode.MAJOR), "Rapida", "Banda");

        stored.save(ultimo);

        assertEquals(ultimo, stored.get());
    }

    /**
     * El bug de siempre en este repo: un valor que se guarda pero nadie lee. Esta cadena
     * completa -guardar, leer y construir la partitura- es la que prueba que no pasa aca.
     */
    @Test
    void loQueSeGuardaComoPropiedadesPorDefectoTerminaEnLaPartituraQueArchivoNuevoCrea() {
        stored.save(new NewScoreDefaults(90, new TimeSignature(3, 4), KeySignature.cMajor(), "", ""));

        Score nueva = stored.get().newScore();

        assertEquals(90, nueva.tempo());
        assertEquals(new TimeSignature(3, 4), nueva.timeSignatureOf(0));
    }
}
