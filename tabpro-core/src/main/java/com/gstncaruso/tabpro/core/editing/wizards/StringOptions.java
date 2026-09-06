package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import java.util.Set;

/**
 * Los asistentes que aplican un efecto a ciertas cuerdas dentro de un rango de
 * compases: let ring, palm mute y dinamica, como los describe el manual.
 */
public final class StringOptions {

    private StringOptions() {
    }

    public static Score applyOrnament(
            Score score, int trackIndex, MeasureRange range, Set<Integer> strings, Ornament ornament, boolean on) {
        return score.mappingTrack(trackIndex, track ->
                Wizards.mappingBeatsInRange(track, range, beat -> beat.mappingEveryNote(note ->
                        strings.contains(note.string()) ? withOrnament(note, ornament, on) : note)));
    }

    public static Score applyDynamic(
            Score score, int trackIndex, MeasureRange range, Set<Integer> strings, Dynamic dynamic) {
        return score.mappingTrack(trackIndex, track ->
                Wizards.mappingBeatsInRange(track, range, beat -> beat.mappingEveryNote(note ->
                        strings.contains(note.string()) ? note.withDynamic(dynamic) : note)));
    }

    private static Note withOrnament(Note note, Ornament ornament, boolean on) {
        if (note.has(ornament) == on) {
            return note;
        }
        return note.toggling(ornament);
    }
}
