package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record NoteDto(
        int string,
        int fret,
        Boolean tied,
        List<String> ornaments,
        String dynamic,
        BendDto bend,
        String slide,
        String harmonic,
        TrillDto trill,
        String tremoloPicking,
        GraceNoteDto grace,
        String leftHand,
        String rightHand) {

    public static NoteDto from(Note note) {
        NoteEffects effects = note.effects();
        return new NoteDto(
                note.string(),
                note.fret(),
                note.tied() ? Boolean.TRUE : null,
                effects.ornaments().isEmpty() ? null : effects.ornaments().stream().map(Enum::name).sorted().toList(),
                effects.dynamic() == Dynamic.defaultDynamic() ? null : effects.dynamic().name(),
                effects.bend().map(BendDto::from).orElse(null),
                effects.slide().map(Enum::name).orElse(null),
                effects.harmonic().map(Enum::name).orElse(null),
                effects.trill().map(TrillDto::from).orElse(null),
                effects.tremoloPicking().map(picking -> picking.speed().name()).orElse(null),
                effects.grace().map(GraceNoteDto::from).orElse(null),
                effects.leftHand().map(Enum::name).orElse(null),
                effects.rightHand().map(Enum::name).orElse(null));
    }

    public Note toNote() {
        return new Note(string, fret, tied != null && tied, toEffects());
    }

    private NoteEffects toEffects() {
        return new NoteEffects(
                toOrnaments(),
                Enums.read(Dynamic.class, dynamic, Dynamic.defaultDynamic()),
                Optional.ofNullable(bend).map(BendDto::toBend),
                Optional.ofNullable(Enums.read(SlideType.class, slide, null)),
                Optional.ofNullable(Enums.read(HarmonicType.class, harmonic, null)),
                Optional.ofNullable(trill).map(TrillDto::toTrill),
                Optional.ofNullable(Enums.read(NoteValue.class, tremoloPicking, null)).map(TremoloPicking::at),
                Optional.ofNullable(grace).map(GraceNoteDto::toGraceNote),
                Optional.ofNullable(Enums.read(Finger.class, leftHand, null)),
                Optional.ofNullable(Enums.read(Finger.class, rightHand, null)));
    }

    private Set<Ornament> toOrnaments() {
        EnumSet<Ornament> read = EnumSet.noneOf(Ornament.class);
        if (ornaments != null) {
            ornaments.stream().map(name -> Enums.read(Ornament.class, name, null))
                    .filter(java.util.Objects::nonNull)
                    .forEach(read::add);
        }
        return read;
    }

    public record BendDto(String type, List<PointDto> points) {

        public static BendDto from(Bend bend) {
            return new BendDto(bend.type().name(), bend.points().stream().map(PointDto::from).toList());
        }

        public Bend toBend() {
            return new Bend(Enums.required(BendType.class, type), points.stream().map(PointDto::toPoint).toList());
        }
    }

    public record PointDto(int position, int quarterTones, Integer vibrato) {

        public static PointDto from(BendPoint point) {
            return new PointDto(point.position(), point.quarterTones(), point.vibrato() == 0 ? null : point.vibrato());
        }

        public BendPoint toPoint() {
            return new BendPoint(position, quarterTones, vibrato == null ? 0 : vibrato);
        }
    }

    public record TrillDto(int fret, String speed) {

        public static TrillDto from(Trill trill) {
            return new TrillDto(trill.fret(), trill.speed().name());
        }

        public Trill toTrill() {
            return new Trill(fret, Enums.required(NoteValue.class, speed));
        }
    }

    public record GraceNoteDto(
            int fret, String duration, String dynamic, String transition, Boolean onBeat, Boolean dead) {

        public static GraceNoteDto from(GraceNote grace) {
            return new GraceNoteDto(
                    grace.fret(), grace.duration().name(), grace.dynamic().name(), grace.transition().name(),
                    grace.onBeat() ? Boolean.TRUE : null, grace.dead() ? Boolean.TRUE : null);
        }

        public GraceNote toGraceNote() {
            return new GraceNote(
                    fret,
                    Enums.required(NoteValue.class, duration),
                    Enums.read(Dynamic.class, dynamic, Dynamic.defaultDynamic()),
                    Enums.read(GraceTransition.class, transition, GraceTransition.NONE),
                    onBeat != null && onBeat,
                    dead != null && dead);
        }
    }
}
