package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.Optional;

record GuitarProHeader(
        ScoreInfo info,
        Lyrics lyrics,
        int tempo,
        KeySignature keySignature,
        Optional<TripletFeel> globalTripletFeel) {
}
