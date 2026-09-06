package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * El manual: "You can force the multitrack view when using the Horizontal Screen Mode in the
 * Options > Preferences". El efecto que importa no es que la preferencia se guarde -eso ya lo
 * prueba PreferencesTest- sino que entrar en pantalla horizontal con la preferencia prendida
 * deje la vista en multipista.
 */
class HorizontalMultitrackTest {

    private final TrackVisibility visibleTracks = new TrackVisibility();

    @Test
    void conLaPreferenciaPrendidaEntrarAPantallaHorizontalPrendeLaVistaMultipista() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, true);

        assertTrue(visibleTracks.isMultitrack());
    }

    @Test
    void conLaPreferenciaApagadaEntrarAPantallaHorizontalNoTocaLaVistaMultipista() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, false);

        assertFalse(visibleTracks.isMultitrack());
    }

    @Test
    void laPreferenciaNoHaceNadaFueraDePantallaHorizontal() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_VERTICAL, true);
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.PAGE, true);
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.PARCHMENT, true);

        assertFalse(visibleTracks.isMultitrack());
    }

    @Test
    void apagarLaVistaMultipistaAManoSigueFuncionandoDespuesDeForzarla() {
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, true);
        assertTrue(visibleTracks.isMultitrack());

        visibleTracks.setMultitrack(false);

        assertFalse(visibleTracks.isMultitrack());
    }
}
