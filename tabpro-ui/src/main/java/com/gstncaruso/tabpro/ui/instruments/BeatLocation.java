package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.List;
import java.util.Optional;

/**
 * Donde esta el beat que el diapason o el teclado tienen que mostrar: su pista,
 * compas, voz y su indice ahi adentro. Sirve para juntar, ademas del beat en si,
 * los del resto del compas y el que sigue, que es lo que necesitan los modos de
 * vista.
 */
public record BeatLocation(Track track, int measureIndex, VoicePart voice, int beatIndex) {

    public Beat beat() {
        return track.measure(measureIndex).voice(voice).beat(beatIndex);
    }

    /** Todos los beats del compas donde esta parado, en el orden de la partitura. */
    public List<Beat> measureBeats() {
        return track.measure(measureIndex).voice(voice).beats();
    }

    /** El beat siguiente, cruzando al proximo compas si hace falta. */
    public Optional<Beat> nextBeat() {
        List<Beat> beats = measureBeats();
        if (beatIndex + 1 < beats.size()) {
            return Optional.of(beats.get(beatIndex + 1));
        }
        if (measureIndex + 1 >= track.measureCount()) {
            return Optional.empty();
        }
        Voice nextVoice = track.measure(measureIndex + 1).voice(voice);
        return nextVoice.isUnused() ? Optional.empty() : Optional.of(nextVoice.beat(0));
    }
}
