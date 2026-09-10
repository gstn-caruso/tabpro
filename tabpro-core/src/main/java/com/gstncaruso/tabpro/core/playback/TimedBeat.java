package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.VoicePart;

record TimedBeat(long tick, long durationTicks, int measureIndex, int beatIndex, VoicePart voice, Beat beat) {
}
