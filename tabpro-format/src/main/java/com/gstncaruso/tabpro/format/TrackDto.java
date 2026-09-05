package com.gstncaruso.tabpro.format;

import java.util.List;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;

public record TrackDto(String name, int midiProgram, List<Integer> tuning, List<MeasureDto> measures) {

    public static TrackDto from(Track track) {
        List<Integer> tuning = track.tuning().strings().stream().map(Pitch::midiNumber).toList();
        List<MeasureDto> measures = track.measures().stream().map(MeasureDto::from).toList();
        return new TrackDto(track.name(), track.midiProgram(), tuning, measures);
    }

    public Track toTrack() {
        if (tuning == null) {
            throw new ScoreFileException("falta el campo tuning");
        }
        if (measures == null) {
            throw new ScoreFileException("falta el campo measures");
        }
        List<Pitch> pitches = tuning.stream().map(Pitch::new).toList();
        List<Measure> domainMeasures = measures.stream().map(MeasureDto::toMeasure).toList();
        requireNotesWithinTuning(domainMeasures, pitches.size());
        return new Track(name, new Tuning(pitches), midiProgram, domainMeasures);
    }

    private static void requireNotesWithinTuning(List<Measure> measures, int stringCount) {
        boolean beyondTuning = measures.stream()
                .flatMap(measure -> measure.beats().stream())
                .flatMap(beat -> beat.notes().stream())
                .anyMatch(note -> note.string() > stringCount);
        if (beyondTuning) {
            throw new ScoreFileException("una nota referencia una cuerda fuera de la afinacion de " + stringCount + " cuerdas");
        }
    }
}
