package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.Optional;

/** Lo que trae la cabecera del archivo, antes de llegar a compases y pistas. */
record GuitarProHeader(
        ScoreInfo info,
        Lyrics lyrics,
        int tempo,
        KeySignature keySignature,
        Optional<TripletFeel> globalTripletFeel) {
}
