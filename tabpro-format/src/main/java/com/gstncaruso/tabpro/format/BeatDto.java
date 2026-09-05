package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record BeatDto(
        int value,
        boolean dotted,
        Integer tuplet,
        List<NoteDto> notes,
        StrokeDto stroke,
        String pickstroke,
        Boolean fadeIn,
        Boolean tapping,
        Boolean slapping,
        Boolean popping,
        Boolean wideVibrato,
        NoteDto.BendDto tremoloBar,
        String wah,
        String text,
        ChordDto chord) {

    public static BeatDto from(Beat beat) {
        BeatEffects effects = beat.effects();
        Duration duration = beat.duration();
        return new BeatDto(
                duration.value().denominator(),
                duration.dotted(),
                duration.tuplet().isPlain() ? null : duration.tuplet().enters(),
                beat.notes().stream().map(NoteDto::from).toList(),
                effects.stroke().map(StrokeDto::from).orElse(null),
                effects.pickstroke().map(Enum::name).orElse(null),
                flag(effects.fadeIn()),
                flag(effects.tapping()),
                flag(effects.slapping()),
                flag(effects.popping()),
                flag(effects.wideVibrato()),
                effects.tremoloBar().map(NoteDto.BendDto::from).orElse(null),
                effects.wah().map(Enum::name).orElse(null),
                effects.text().orElse(null),
                effects.chord().map(ChordDto::from).orElse(null));
    }

    public Beat toBeat() {
        if (notes == null) {
            throw new ScoreFileException("falta el campo notes");
        }
        List<Note> domainNotes = notes.stream().map(NoteDto::toNote).toList();
        return new Beat(toDuration(), domainNotes, toEffects());
    }

    private Duration toDuration() {
        Tuplet group = tuplet == null ? Tuplet.none() : Tuplet.of(tuplet);
        return new Duration(noteValueOf(value), dotted, group);
    }

    private BeatEffects toEffects() {
        return new BeatEffects(
                Optional.ofNullable(stroke).map(StrokeDto::toStroke),
                Optional.ofNullable(Enums.read(PickstrokeDirection.class, pickstroke, null)),
                isSet(fadeIn),
                isSet(tapping),
                isSet(slapping),
                isSet(popping),
                isSet(wideVibrato),
                Optional.ofNullable(tremoloBar).map(NoteDto.BendDto::toBend),
                Optional.ofNullable(Enums.read(Wah.class, wah, null)),
                Optional.ofNullable(text),
                Optional.ofNullable(chord).map(ChordDto::toChord));
    }

    private static Boolean flag(boolean value) {
        return value ? Boolean.TRUE : null;
    }

    private static boolean isSet(Boolean value) {
        return value != null && value;
    }

    private static NoteValue noteValueOf(int denominator) {
        return Arrays.stream(NoteValue.values())
                .filter(candidate -> candidate.denominator() == denominator)
                .findFirst()
                .orElseThrow(() -> new ScoreFileException("value no es un denominador de figura valido: " + denominator));
    }

    public record StrokeDto(String direction, String speed, Boolean rasgueado) {

        public static StrokeDto from(Stroke stroke) {
            return new StrokeDto(stroke.direction().name(), stroke.speed().name(),
                    stroke.rasgueado() ? Boolean.TRUE : null);
        }

        public Stroke toStroke() {
            return new Stroke(
                    Enums.required(StrokeDirection.class, direction),
                    Enums.required(NoteValue.class, speed),
                    rasgueado != null && rasgueado);
        }
    }

    public record ChordDto(String name, int baseFret, List<Integer> frets, List<String> fingering, Boolean shown) {

        public static ChordDto from(ChordDiagram chord) {
            return new ChordDto(
                    chord.name(), chord.baseFret(), chord.frets(),
                    chord.fingering().isEmpty() ? null : chord.fingering().stream().map(Enum::name).toList(),
                    chord.shown() ? null : Boolean.FALSE);
        }

        public ChordDiagram toChord() {
            List<Finger> fingers = fingering == null
                    ? List.of()
                    : fingering.stream().map(name -> Enums.read(Finger.class, name, null)).toList();
            return new ChordDiagram(name, baseFret, frets, fingers, shown == null || shown);
        }
    }
}
