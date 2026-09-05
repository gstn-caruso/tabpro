package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.function.UnaryOperator;

/** Los recorridos que comparten los asistentes para llegar hasta cada nota. */
final class Wizards {

    private Wizards() {
    }

    static Track mappingNotes(Track track, UnaryOperator<Note> change) {
        return mappingBeats(track, beat -> beat.mappingEveryNote(change));
    }

    static Track mappingBeats(Track track, UnaryOperator<Beat> change) {
        return track.mappingMeasures(measure -> mappingBeats(measure, change));
    }

    static Measure mappingBeats(Measure measure, UnaryOperator<Beat> change) {
        Measure updated = measure;
        for (VoicePart part : VoicePart.values()) {
            updated = updated.mappingVoice(part, voice -> voice.isUnused() ? voice : voice.mappingBeats(change));
        }
        return updated;
    }

    static Track mappingBeatsInRange(Track track, MeasureRange range, UnaryOperator<Beat> change) {
        Track updated = track;
        for (int index = 0; index < track.measureCount(); index++) {
            if (range.covers(index)) {
                updated = updated.mappingMeasure(index, measure -> mappingBeats(measure, change));
            }
        }
        return updated;
    }
}
