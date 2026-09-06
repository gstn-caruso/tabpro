package com.gstncaruso.tabpro.core.model;

import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/** Una pista de la partitura: su instrumento, su afinacion y sus compases. */
public record Track(
        String name, Tuning tuning, Channel channel, TrackSettings settings, List<Measure> measures) {

    public static final int GUITAR_PROGRAM = 25;
    public static final int BASS_PROGRAM = 33;
    public static final int PERCUSSION_PROGRAM = 0;

    /** Los colores con que Guitar Pro pinta las pistas nuevas, en orden. */
    private static final List<ScoreColor> PALETTE = List.of(
            ScoreColor.rgb(0xE05C5C), ScoreColor.rgb(0x5C9CE0), ScoreColor.rgb(0x63BD63),
            ScoreColor.rgb(0xE0B25C), ScoreColor.rgb(0xB07CD8), ScoreColor.rgb(0x5CC7C7),
            ScoreColor.rgb(0xD87CA8), ScoreColor.rgb(0x9BA85C));

    public Track {
        if (measures.isEmpty()) {
            throw new IllegalArgumentException("una pista necesita al menos un compas");
        }
        measures = List.copyOf(measures);
    }

    public Track(String name, Tuning tuning, Channel channel, List<Measure> measures) {
        this(name, tuning, channel, TrackSettings.standard(colorFor(0)), measures);
    }

    public static ScoreColor colorFor(int index) {
        return PALETTE.get(Math.floorMod(index, PALETTE.size()));
    }

    public static Track standardGuitar(String name) {
        return new Track(name, Tuning.standard(), Channel.playing(GUITAR_PROGRAM),
                TrackSettings.standard(colorFor(0)), List.of(emptyMeasure()));
    }

    public static Track standardBass(String name) {
        return new Track(name, Tuning.standardBass(), Channel.playing(BASS_PROGRAM),
                TrackSettings.standard(colorFor(1)), List.of(emptyMeasure()));
    }

    public static Track percussion(String name) {
        return new Track(name, PercussionKit.tuning(), Channel.percussion(),
                TrackSettings.percussion(colorFor(4)), List.of(emptyMeasure()));
    }

    private static Measure emptyMeasure() {
        return Measure.empty(TimeSignature.fourFour(), Duration.quarter());
    }

    public boolean isPercussion() {
        return settings.percussion();
    }

    /** Si al combinar dos digitos tipeados forman un traste o un sonido valido para esta pista. */
    public boolean acceptsTypedNumber(int number) {
        return isPercussion() ? PercussionKit.isPlayable(number) : number <= Tuning.MAX_FRET;
    }

    public Measure measure(int index) {
        return measures.get(index);
    }

    public int measureCount() {
        return measures.size();
    }

    public int stringCount() {
        return tuning.stringCount();
    }

    public ScoreColor color() {
        return settings.color();
    }

    public boolean hasNotesIn(int measureIndex) {
        if (measureIndex < 0 || measureIndex >= measureCount()) {
            return false;
        }
        return measure(measureIndex).hasNotes();
    }

    /** La quinta cuerda del banjo, si esta opcion esta activa. */
    private static final int BANJO_FIFTH_STRING = 5;

    /**
     * La quinta cuerda de un banjo no llega hasta la cejuela: arranca en el
     * traste 6, asi que el primer traste que se puede pisar no es el 1 sino
     * el 6, y de ahi para arriba.
     */
    private static final int BANJO_FIFTH_STRING_FRET_OFFSET = 5;

    /** La altura que suena esa nota, contando la cejilla y la quinta cuerda del banjo. */
    public Pitch pitchOf(Note note) {
        return tuning.pitchOf(effectiveNote(note)).transposed(settings.capo());
    }

    private Note effectiveNote(Note note) {
        if (settings.banjoFifthString() && note.string() == BANJO_FIFTH_STRING && note.fret() > 0) {
            return note.withFret(note.fret() + BANJO_FIFTH_STRING_FRET_OFFSET);
        }
        return note;
    }

    public Track withName(String name) {
        return new Track(name, tuning, channel, settings, measures);
    }

    public Track withChannel(Channel channel) {
        return new Track(name, tuning, channel, settings, measures);
    }

    public Track withTuning(Tuning tuning) {
        return new Track(name, tuning, channel, settings, measures);
    }

    public Track withSettings(TrackSettings settings) {
        return new Track(name, tuning, channel, settings, measures);
    }

    public Track mappingSettings(UnaryOperator<TrackSettings> change) {
        return withSettings(change.apply(settings));
    }

    public Track withMeasure(int index, Measure measure) {
        List<Measure> updated = new ArrayList<>(measures);
        updated.set(index, measure);
        return withMeasures(updated);
    }

    public Track mappingMeasure(int index, UnaryOperator<Measure> change) {
        return withMeasure(index, change.apply(measure(index)));
    }

    public Track mappingMeasures(UnaryOperator<Measure> change) {
        return withMeasures(measures.stream().map(change).toList());
    }

    public Track withMeasureInsertedAt(int index, Measure measure) {
        List<Measure> updated = new ArrayList<>(measures);
        updated.add(index, measure);
        return withMeasures(updated);
    }

    public Track withoutMeasureAt(int index) {
        if (measures.size() == 1) {
            return withMeasures(List.of(measure(index).emptied()));
        }
        List<Measure> updated = new ArrayList<>(measures);
        updated.remove(index);
        return withMeasures(updated);
    }

    public Track withMeasures(List<Measure> updated) {
        return new Track(name, tuning, channel, settings, updated);
    }

    /** Los atributos del compas, que en Guitar Pro son iguales en todas las pistas. */
    public MeasureAttributes attributesOf(int measureIndex) {
        return measure(measureIndex).attributes();
    }
}
