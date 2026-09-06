package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.VoicePart;

/** Un beat de una pista ya ubicado en el tiempo: en que tick arranca y cuanto ocupa. */
record TimedBeat(long tick, long durationTicks, int measureIndex, int beatIndex, VoicePart voice, Beat beat) {
}
