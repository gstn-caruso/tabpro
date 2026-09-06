package com.gstncaruso.tabpro.ui.score;

/**
 * El acople del manual: "You can force the multitrack view when using the Horizontal Screen
 * Mode in the Options > Preferences". Fuera de pantalla horizontal, o con la preferencia
 * apagada, la vista multipista sigue su camino de siempre; entrar en pantalla horizontal con la
 * preferencia prendida la prende sola. Nunca la apaga: salir de pantalla horizontal no deshace
 * nada, porque apagarla es una decision del usuario, no de este acople.
 */
public final class HorizontalMultitrack {

    private HorizontalMultitrack() {
    }

    public static void applyTo(TrackVisibility visibleTracks, ViewMode mode, boolean forced) {
        if (forced && mode.scrollsHorizontally()) {
            visibleTracks.setMultitrack(true);
        }
    }
}
