package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;

public record NewScoreDefaults(
        int tempo, TimeSignature timeSignature, KeySignature keySignature, String title, String artist) {

    public static NewScoreDefaults blank() {
        Score blank = Score.blank(new TextsDefaultNames());
        return new NewScoreDefaults(
                blank.tempo(), blank.timeSignatureOf(0), blank.attributesOf(0).keySignature(), "", "");
    }

    public Score newScore() {
        Score score = Score.blank(new TextsDefaultNames())
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
