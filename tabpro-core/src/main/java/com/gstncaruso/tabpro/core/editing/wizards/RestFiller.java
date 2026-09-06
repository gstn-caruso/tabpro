package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.ArrayList;
import java.util.List;

/**
 * Completa con silencios los compases que quedaron cortos y saca los silencios
 * de mas de los que quedaron largos.
 */
public final class RestFiller {

    private RestFiller() {
    }

    public static Score run(Score score, MeasureRange range) {
        Score filled = score;
        for (int index = 0; index < score.trackCount(); index++) {
            filled = filled.mappingTrack(index, track -> runOn(track, range));
        }
        return filled;
    }

    public static Score runOnTrack(Score score, int trackIndex, MeasureRange range) {
        return score.mappingTrack(trackIndex, track -> runOn(track, range));
    }

    private static Track runOn(Track track, MeasureRange range) {
        Track updated = track;
        for (int index = 0; index < track.measureCount(); index++) {
            if (range.covers(index)) {
                updated = updated.mappingMeasure(index, RestFiller::adjust);
            }
        }
        return updated;
    }

    private static Measure adjust(Measure measure) {
        Measure updated = measure;
        for (VoicePart part : VoicePart.values()) {
            updated = updated.mappingVoice(part, voice -> adjust(voice, measure.timeSignature().ticksPerMeasure()));
        }
        return updated;
    }

    private static Voice adjust(Voice voice, long expected) {
        if (voice.isUnused()) {
            return voice;
        }
        long actual = voice.durationTicks();
        if (actual < expected) {
            return withRestsFor(voice, expected - actual);
        }
        return withoutSpareRests(voice, actual - expected);
    }

    private static Voice withRestsFor(Voice voice, long missing) {
        Voice grown = voice;
        for (Duration rest : restsFor(missing)) {
            grown = grown.withBeatAppended(Beat.rest(rest));
        }
        return grown;
    }

    /** Los silencios mas largos que entren en el hueco, del mas largo al mas corto. */
    private static List<Duration> restsFor(long missing) {
        List<Duration> rests = new ArrayList<>();
        long left = missing;
        for (NoteValue value : NoteValue.values()) {
            Duration plain = Duration.of(value);
            while (left >= plain.ticks()) {
                rests.add(plain);
                left -= plain.ticks();
            }
        }
        return rests;
    }

    private static Voice withoutSpareRests(Voice voice, long excess) {
        Voice trimmed = voice;
        long left = excess;
        for (int index = trimmed.beatCount() - 1; index >= 0 && left > 0; index--) {
            Beat beat = trimmed.beat(index);
            if (!beat.isRest() || beat.duration().ticks() > left) {
                continue;
            }
            left -= beat.duration().ticks();
            trimmed = trimmed.withoutBeatAt(index);
        }
        return trimmed;
    }
}
