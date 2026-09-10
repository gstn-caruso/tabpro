package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.List;
import java.util.Optional;

public record BeatLocation(Track track, int measureIndex, VoicePart voice, int beatIndex) {

    public Beat beat() {
        return track.measure(measureIndex).voice(voice).beat(beatIndex);
    }

    public List<Beat> measureBeats() {
        return track.measure(measureIndex).voice(voice).beats();
    }

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
