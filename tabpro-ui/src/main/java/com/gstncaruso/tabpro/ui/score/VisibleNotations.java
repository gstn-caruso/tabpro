package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Track;

/**
 * Que notaciones se dibujan en toda la partitura. El menu Ver puede esconder el pentagrama o la
 * tablatura por encima de lo que pida cada pista; esconder una trae de vuelta la otra, porque
 * una pista sin ninguna de las dos no se veria.
 */
public record VisibleNotations(boolean standardNotation, boolean tablature) {

    public static VisibleNotations both() {
        return new VisibleNotations(true, true);
    }

    public boolean showsStandardNotationOf(Track track) {
        return standardNotation && track.settings().display().standardNotation();
    }

    public boolean showsTablatureOf(Track track) {
        return tablature && track.settings().display().tablature();
    }

    public VisibleNotations withStandardNotation(boolean standardNotation) {
        return new VisibleNotations(standardNotation, tablature || !standardNotation);
    }

    public VisibleNotations withTablature(boolean tablature) {
        return new VisibleNotations(standardNotation || !tablature, tablature);
    }
}
