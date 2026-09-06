package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import java.util.List;

/** Los datos de una pista, tal como los guarda el archivo. */
record GuitarProTrackHeader(
        String name,
        List<Integer> tuningMidiNumbers,
        int channelIndex1Based,
        int effectChannelIndex1Based,
        int fretCount,
        int capo,
        ScoreColor color,
        boolean percussion,
        boolean twelveString,
        boolean banjoFifthString,
        TrackDisplay display) {
}
