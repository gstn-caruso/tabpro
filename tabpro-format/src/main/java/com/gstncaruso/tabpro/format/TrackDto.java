package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import java.util.List;

public record TrackDto(
        String name,
        int midiProgram,
        Integer volume,
        Integer pan,
        Integer chorus,
        Integer reverb,
        Integer phaser,
        Integer tremolo,
        Integer port,
        Integer channel,
        Boolean muted,
        Boolean solo,
        String tuningName,
        List<Integer> tuning,
        Integer color,
        Integer capo,
        Integer fretCount,
        Boolean percussion,
        Boolean twelveString,
        Boolean banjoFifthString,
        Boolean showStandardNotation,
        Boolean showTablature,
        Boolean showTuning,
        Boolean showRhythm,
        String diagrams,
        List<MeasureDto> measures) {

    public static TrackDto from(Track track) {
        Channel sound = track.channel();
        TrackSettings settings = track.settings();
        TrackDisplay display = settings.display();
        return new TrackDto(
                track.name(),
                sound.program(),
                sound.volume(),
                sound.pan(),
                sound.chorus(),
                sound.reverb(),
                sound.phaser(),
                sound.tremolo(),
                sound.port(),
                sound.number(),
                sound.muted(),
                sound.solo(),
                track.tuning().name(),
                track.tuning().strings().stream().map(Pitch::midiNumber).toList(),
                settings.color().packed(),
                settings.capo(),
                settings.fretCount(),
                settings.percussion(),
                settings.twelveString(),
                settings.banjoFifthString(),
                display.standardNotation(),
                display.tablature(),
                display.tuningLegend(),
                display.rhythmOnTablature(),
                display.diagrams().name(),
                track.measures().stream().map(MeasureDto::from).toList());
    }

    public Track toTrack(int index) {
        if (tuning == null) {
            throw new ScoreFileException("falta el campo tuning");
        }
        if (measures == null) {
            throw new ScoreFileException("falta el campo measures");
        }
        List<Pitch> pitches = tuning.stream().map(Pitch::new).toList();
        List<Measure> domainMeasures = measures.stream().map(MeasureDto::toMeasure).toList();
        requireNotesWithinTuning(domainMeasures, pitches.size());
        Tuning readTuning = tuningName == null ? TuningLibrary.identify(pitches) : new Tuning(tuningName, pitches);
        return new Track(name, readTuning, toChannel(), toSettings(index), domainMeasures);
    }

    private Channel toChannel() {
        return new Channel(
                midiProgram,
                orElse(volume, Channel.DEFAULT_VOLUME),
                orElse(pan, Channel.CENTER_PAN),
                orElse(chorus, 0),
                orElse(reverb, 0),
                orElse(phaser, 0),
                orElse(tremolo, 0),
                orElse(port, 1),
                orElse(channel, 1),
                isSet(muted),
                isSet(solo));
    }

    private TrackSettings toSettings(int index) {
        TrackDisplay display = new TrackDisplay(
                orElse(showStandardNotation, true),
                orElse(showTablature, true),
                orElse(showTuning, true),
                orElse(showRhythm, false),
                Enums.read(DiagramPlacement.class, diagrams, DiagramPlacement.ABOVE_THE_STAFF));
        return new TrackSettings(
                color == null ? Track.colorFor(index) : ScoreColor.rgb(color),
                orElse(capo, 0),
                orElse(fretCount, TrackSettings.DEFAULT_FRET_COUNT),
                isSet(percussion),
                isSet(twelveString),
                isSet(banjoFifthString),
                display);
    }

    private static int orElse(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static boolean orElse(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    private static boolean isSet(Boolean value) {
        return value != null && value;
    }

    private static void requireNotesWithinTuning(List<Measure> measures, int stringCount) {
        boolean beyondTuning = measures.stream()
                .flatMap(measure -> measure.voices().stream())
                .flatMap(voice -> voice.beats().stream())
                .flatMap(beat -> beat.notes().stream())
                .anyMatch(note -> note.string() > stringCount);
        if (beyondTuning) {
            throw new ScoreFileException(
                    "una nota referencia una cuerda fuera de la afinacion de " + stringCount + " cuerdas");
        }
    }
}
