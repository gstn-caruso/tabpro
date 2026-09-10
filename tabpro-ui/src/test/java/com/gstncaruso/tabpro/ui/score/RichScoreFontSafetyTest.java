package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RichScoreFontSafetyTest {

    private static final int WIDTH = 900;

    @Test
    void everyTextInARichScoreIsShownByAFontThatCanDisplayIt() {
        LienzoDePrueba lienzo = paint(richScore());

        List<LienzoDePrueba.TextoDibujado> textos = lienzo.textosDibujados();
        assertFalse(textos.isEmpty(), "la partitura rica tiene que haber escrito algo");
        for (LienzoDePrueba.TextoDibujado escrito : textos) {
            assertEquals(-1, escrito.fuente().canDisplayUpTo(escrito.texto()),
                    "\"" + escrito.texto() + "\" se escribio con " + escrito.fuente().getFontName()
                            + ", que no sabe mostrar alguno de sus caracteres");
        }
    }

    private static LienzoDePrueba paint(Score score) {
        ScoreViewport viewport = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), WIDTH).withPageSetup(PageSetup.defaults());
        LienzoDePrueba lienzo = new LienzoDePrueba();
        PageScorePainter.paint(lienzo, score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(), viewport);
        return lienzo;
    }

    private static Score richScore() {
        Score score = new Score("Cancion de prueba", 96, List.of(guitarTrack(), bassTrack(), percussionTrack()));
        score = score.withInfo(score.info()
                .withSubtitle("Subtitulo de prueba")
                .withArtist("Alguien")
                .withAlbum("Un disco")
                .withLyricsAuthor("Fulano")
                .withMusicAuthor("Mengano")
                .withCopyright("(c) 2026"));
        score = score.withLyrics(Lyrics.none().onTrack(0)
                .withLine(0, LyricLine.empty().startingAt(1).saying("La vi- da si- gue i- gual")));
        score = score.withOctaveMarkInTrackAt(0, FLAG_MEASURE, OctaveMark.OTTAVA_ALTA);
        return score;
    }

    private static final int FLAG_MEASURE = 3;

    private static Track guitarTrack() {
        List<Measure> measures = new ArrayList<>();
        measures.add(measureWithKeyMarkerAndRepeatOpen());
        measures.add(measureWithAccidentalsAndArticulations());
        measures.add(measureWithDottedNoteAndRests());
        measures.add(measureWithAnIsolatedFlag());
        measures.add(measureWithATriplet());
        measures.add(measureWithTabEffects());
        measures.add(measureWithChordDeadAndGhost());
        measures.add(measureClosingTheRepeatWithAlternateEndings());
        measures.add(measureWithSegno());
        measures.add(measureWithCoda());
        measures.add(measureWithAJump());
        measures.add(measureWithATempoAndAPanChange());
        return Track.standardGuitar("Guitarra").withMeasures(measures);
    }

    private static Measure measureWithKeyMarkerAndRepeatOpen() {
        return plainMeasure().mappingAttributes(attrs -> attrs
                .withKeySignature(new KeySignature(-1, Mode.MAJOR))
                .withMarker(Marker.named("Intro"))
                .withRepeatOpen(true));
    }

    private static Measure measureWithAccidentalsAndArticulations() {
        Note sharped = new Note(3, 3);
        Note backToNatural = new Note(3, 2);
        Note accented = new Note(1, 0).toggling(Ornament.ACCENTED);
        Note tied = Note.tiedOn(1);
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), sharped), Beat.of(Duration.quarter(), backToNatural),
                Beat.of(Duration.quarter(), accented), Beat.of(Duration.quarter(), tied)));
    }

    private static Measure measureWithDottedNoteAndRests() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(new Duration(NoteValue.QUARTER, true), new Note(2, 5)),
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.HALF))));
    }

    private static Measure measureWithAnIsolatedFlag() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()),
                Beat.of(Duration.of(NoteValue.EIGHTH), new Note(2, 3)),
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.of(Duration.quarter(), new Note(2, 5)),
                Beat.of(Duration.quarter(), new Note(2, 7))));
    }

    private static Measure measureWithATriplet() {
        Duration eighthTriplet = new Duration(NoteValue.EIGHTH, false).in(Tuplet.of(3));
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(eighthTriplet, new Note(1, 0)),
                Beat.of(eighthTriplet, new Note(1, 2)),
                Beat.of(eighthTriplet, new Note(1, 4)),
                Beat.of(Duration.quarter(), new Note(1, 0))));
    }

    private static Measure measureWithTabEffects() {
        Note tremoloBarNote = new Note(2, 4);
        Beat rhythmicEffects = Beat.of(Duration.quarter(), tremoloBarNote).withEffects(BeatEffects.none()
                .withTapping(true).withSlapping(true).withPopping(true).withFadeIn(true)
                .withTremoloBar(Bend.of(BendType.BEND, -4)));

        Note muteRingHarmonic = new Note(3, 2).toggling(Ornament.PALM_MUTE).toggling(Ornament.LET_RING);
        muteRingHarmonic = muteRingHarmonic.withEffects(muteRingHarmonic.effects()
                .withHarmonic(HarmonicType.NATURAL)
                .withTrill(Trill.to(5))
                .withTremoloPicking(TremoloPicking.at(NoteValue.SIXTEENTH)));
        muteRingHarmonic = muteRingHarmonic.withBend(Bend.of(BendType.BEND, 4));
        Beat noteEffects = Beat.of(Duration.quarter(), muteRingHarmonic);

        Note wahNote = new Note(4, 0);
        Beat vibratoWahText = Beat.of(Duration.quarter(), wahNote).withEffects(BeatEffects.none()
                .withWideVibrato(true).withWah(Wah.OPEN).withText("let it ring"));

        Note fingeredNote = new Note(1, 3).toggling(Ornament.VIBRATO);
        fingeredNote = fingeredNote.withEffects(fingeredNote.effects()
                .withLeftHand(Finger.INDEX).withRightHand(Finger.THUMB).withGrace(GraceNote.before(1)));
        Beat pickAndStrum = Beat.of(Duration.quarter(), fingeredNote).withEffects(BeatEffects.none()
                .withPickstroke(PickstrokeDirection.DOWN)
                .withStroke(new Stroke(StrokeDirection.UP, NoteValue.THIRTY_SECOND, true)));

        return new Measure(TimeSignature.fourFour(), List.of(rhythmicEffects, noteEffects, vibratoWahText, pickAndStrum));
    }

    private static Measure measureWithChordDeadAndGhost() {
        ChordDiagram fChord = new ChordDiagram("F", 3, List.of(3, 4, 5, 5, 3, -1), List.of(), true);
        Beat chordBeat = Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(fChord));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                chordBeat,
                Beat.of(Duration.quarter(), new Note(2, 0).toggling(Ornament.DEAD)),
                Beat.of(Duration.quarter(), new Note(2, 5).toggling(Ornament.GHOST)),
                Beat.of(Duration.quarter(), new Note(1, 2).withSlide(SlideType.OUT_UPWARDS))));
        return measure.mappingAttributes(attrs -> attrs.withDoubleBar(true));
    }

    private static Measure measureClosingTheRepeatWithAlternateEndings() {
        return plainMeasure().mappingAttributes(attrs -> attrs.withRepeatCount(2).withAlternateEndings(List.of(1, 2)));
    }

    private static Measure measureWithSegno() {
        return plainMeasure().mappingAttributes(attrs -> attrs.withSymbol(DirectionSymbol.SEGNO));
    }

    private static Measure measureWithCoda() {
        return plainMeasure().mappingAttributes(attrs -> attrs.withSymbol(DirectionSymbol.CODA));
    }

    private static Measure measureWithAJump() {
        return plainMeasure().mappingAttributes(attrs -> attrs.withJump(DirectionJump.DA_SEGNO_AL_FINE));
    }

    private static Measure measureWithATempoAndAPanChange() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(
                        BeatEffects.none().withParameterChange(ParameterChange.nothing().changing(SoundParameter.TEMPO, 90))),
                Beat.of(Duration.quarter(), new Note(1, 2)).withEffects(
                        BeatEffects.none().withParameterChange(ParameterChange.nothing().changing(SoundParameter.PAN, 20))),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
    }

    private static Measure plainMeasure() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
    }

    private static Track bassTrack() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(4, 3)),
                Beat.of(Duration.quarter(), new Note(3, 2).withSlide(SlideType.SHIFT)),
                Beat.of(Duration.quarter(), new Note(3, 5)),
                Beat.of(Duration.quarter(), new Note(2, 0))));
        return Track.standardBass("Bajo").withMeasures(List.of(measure));
    }

    private static Track percussionTrack() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 42)),
                Beat.of(Duration.quarter(), new Note(2, 54)),
                Beat.of(Duration.quarter(), new Note(3, 38)),
                Beat.rest(Duration.quarter())));
        return Track.percussion("Bateria").withMeasures(List.of(measure));
    }
}
