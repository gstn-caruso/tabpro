package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;

/**
 * Los valores que usa la proxima partitura nueva, tal como los define la solapa "Propiedades
 * por defecto" de Informacion de la partitura: el tempo, el compas, la armadura y, si se
 * cargan, el titulo y el artista.
 */
public record NewScoreDefaults(
        int tempo, TimeSignature timeSignature, KeySignature keySignature, String title, String artist) {

    public static NewScoreDefaults blank() {
        Score blank = Score.blank();
        return new NewScoreDefaults(
                blank.tempo(), blank.timeSignatureOf(0), blank.attributesOf(0).keySignature(), "", "");
    }

    /** La partitura que crea Archivo > Nuevo cuando rigen estos valores por defecto. */
    public Score newScore() {
        Score score = Score.blank()
                .withTempo(tempo)
                .withTimeSignatureFrom(0, timeSignature)
                .withKeySignatureFrom(0, keySignature);
        if (title.isBlank() && artist.isBlank()) {
            return score;
        }
        var info = score.info();
        if (!title.isBlank()) {
            info = info.withTitle(title);
        }
        if (!artist.isBlank()) {
            info = info.withArtist(artist);
        }
        return score.withInfo(info);
    }
}
