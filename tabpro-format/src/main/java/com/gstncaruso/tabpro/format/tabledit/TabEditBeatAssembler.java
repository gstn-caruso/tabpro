package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Arma la secuencia de beats de cada compas a partir de los eventos posicionados
 * de TablEdit. TablEdit no lleva un "compas actual" mientras graba: cada nota
 * o silencio trae su propia posicion en una grilla de dieciseisavos. tabpro,
 * en cambio, pide una lista de beats consecutivos por voz. Cualquier lugar de
 * la grilla que el archivo no haya marcado con nada se rellena con silencio:
 * eso no es adivinar musica, es completar lo que ya era silencio.
 */
final class TabEditBeatAssembler {

    /** Un lugar de grilla es siempre un dieciseisavo, sin importar la medida del compas. */
    private static final long TICKS_PER_GRID_POSITION = Duration.TICKS_PER_QUARTER / 4;

    private static final long[] FILLER_TICKS = {3840, 1920, 960, 480, 240, 120, 60};
    private static final NoteValue[] FILLER_VALUES = {
            NoteValue.WHOLE, NoteValue.HALF, NoteValue.QUARTER, NoteValue.EIGHTH,
            NoteValue.SIXTEENTH, NoteValue.THIRTY_SECOND, NoteValue.SIXTY_FOURTH,
    };

    List<Measure> assembleTrack(int trackIndex, List<TabEditMeasure> measures, List<TabEditEvent> events) {
        List<Measure> result = new ArrayList<>(measures.size());
        for (int measureIndex = 0; measureIndex < measures.size(); measureIndex++) {
            result.add(assembleMeasure(trackIndex, measureIndex, measures.get(measureIndex), events));
        }
        return result;
    }

    private Measure assembleMeasure(
            int trackIndex, int measureIndex, TabEditMeasure measure, List<TabEditEvent> events) {
        Voice lead = assembleVoice(trackIndex, measureIndex, VoicePart.LEAD, measure, events);
        Voice bass = assembleVoice(trackIndex, measureIndex, VoicePart.BASS, measure, events);
        MeasureAttributes attributes = MeasureAttributes.plain().withKeySignature(measure.keySignature());
        return new Measure(measure.timeSignature(), attributes, List.of(usable(lead, measure), bass));
    }

    private Voice assembleVoice(
            int trackIndex, int measureIndex, VoicePart voicePart, TabEditMeasure measure,
            List<TabEditEvent> events) {
        Map<Integer, List<TabEditEvent>> byGridPosition = new TreeMap<>();
        for (TabEditEvent event : events) {
            TabEditPosition position = event.position();
            if (position.trackIndex() != trackIndex || position.measureIndex() != measureIndex
                    || event.voice() != voicePart) {
                continue;
            }
            byGridPosition.computeIfAbsent(position.positionInMeasure(), key -> new ArrayList<>()).add(event);
        }
        if (byGridPosition.isEmpty()) {
            return Voice.unused();
        }

        List<Beat> beats = new ArrayList<>();
        long cursor = 0;
        for (Map.Entry<Integer, List<TabEditEvent>> entry : byGridPosition.entrySet()) {
            long eventTick = entry.getKey() * TICKS_PER_GRID_POSITION;
            if (eventTick > cursor) {
                appendFillerRests(beats, eventTick - cursor);
                cursor = eventTick;
            }
            Beat beat = beatOf(entry.getValue());
            beats.add(beat);
            cursor += beat.duration().ticks();
        }
        long measureEndTicks = measure.timeSignature().ticksPerMeasure();
        if (cursor < measureEndTicks) {
            appendFillerRests(beats, measureEndTicks - cursor);
        }
        return new Voice(beats);
    }

    private static Beat beatOf(List<TabEditEvent> atThisPosition) {
        Duration duration = atThisPosition.get(0).duration();
        List<Note> notes = new ArrayList<>();
        BeatEffects effects = BeatEffects.none();
        for (TabEditEvent event : atThisPosition) {
            if (event instanceof TabEditNoteEvent noteEvent) {
                notes.add(noteEvent.note());
                if (noteEvent.tapping()) {
                    effects = effects.withTapping(true);
                }
                if (noteEvent.slapping()) {
                    effects = effects.withSlapping(true);
                }
                if (noteEvent.fadeIn()) {
                    effects = effects.withFadeIn(true);
                }
            }
        }
        return notes.isEmpty() ? Beat.rest(duration) : new Beat(duration, notes, effects);
    }

    /** La voz principal de un compas no puede quedar sin beats. */
    private static Voice usable(Voice voice, TabEditMeasure measure) {
        if (!voice.isUnused()) {
            return voice;
        }
        List<Beat> wholeMeasureOfRests = new ArrayList<>();
        appendFillerRests(wholeMeasureOfRests, measure.timeSignature().ticksPerMeasure());
        return new Voice(wholeMeasureOfRests);
    }

    private static void appendFillerRests(List<Beat> beats, long ticksToFill) {
        long remaining = ticksToFill;
        int i = 0;
        while (remaining > 0 && i < FILLER_TICKS.length) {
            if (FILLER_TICKS[i] <= remaining) {
                beats.add(Beat.rest(new Duration(FILLER_VALUES[i], false)));
                remaining -= FILLER_TICKS[i];
            } else {
                i++;
            }
        }
        // Lo que sobre por debajo de una fusa (60 ticks) no tiene figura estandar que lo
        // anote: es un resto de cuantizacion tan chico que no hay nada razonable que hacer.
    }
}
