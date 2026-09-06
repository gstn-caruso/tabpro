package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreDtoTest {

    @Test
    void roundTripsABlankScore() {
        assertRoundTrips(Score.blank());
    }

    @Test
    void roundTripsNotesAndRests() {
        Beat beatWithNotes = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2));
        Beat rest = Beat.rest(Duration.quarter());
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(beatWithNotes, rest))));
    }

    @Test
    void roundTripsDottedDurations() {
        Beat beat = Beat.rest(new Duration(NoteValue.EIGHTH, true));
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(beat))));
    }

    @Test
    void roundTripsTuplets() {
        Beat beat = Beat.of(Duration.of(NoteValue.EIGHTH).in(Tuplet.of(3)), new Note(1, 5));
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(beat))));
    }

    @Test
    void roundTripsTiedNotes() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 5).tied(true));
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(beat))));
    }

    @Test
    void roundTripsTheOrnamentsOfANote() {
        Note note = new Note(1, 5).toggling(Ornament.PALM_MUTE).toggling(Ornament.GHOST).toggling(Ornament.VIBRATO);
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), note)))));
    }

    @Test
    void roundTripsTheParameterisedEffectsOfANote() {
        Note note = new Note(1, 5)
                .withDynamic(Dynamic.FORTISSIMO)
                .withBend(Bend.of(BendType.BEND_RELEASE, 4))
                .withSlide(SlideType.LEGATO)
                .withHarmonic(HarmonicType.PINCH);
        note = note.withEffects(note.effects()
                .withTrill(Trill.to(7))
                .withTremoloPicking(TremoloPicking.at(NoteValue.SIXTEENTH))
                .withGrace(GraceNote.before(3))
                .withLeftHand(Finger.MIDDLE)
                .withRightHand(Finger.INDEX));
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), note)))));
    }

    @Test
    void roundTripsTheEffectsOfABeat() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 5)).withEffects(
                com.gstncaruso.tabpro.core.model.effects.BeatEffects.none()
                        .withStroke(Stroke.of(StrokeDirection.UP))
                        .withFadeIn(true)
                        .withSlapping(true)
                        .withWideVibrato(true)
                        .withTremoloBar(Bend.of(BendType.PREBEND, 2))
                        .withWah(Wah.CLOSED)
                        .withText("Intro")
                        .withChord(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1))));
        assertRoundTrips(scoreWith(new Measure(TimeSignature.fourFour(), List.of(beat))));
    }

    @Test
    void roundTripsTheSecondVoice() {
        Voice lead = new Voice(List.of(Beat.of(Duration.quarter(), new Note(1, 5))));
        Voice bass = new Voice(List.of(Beat.of(Duration.quarter(), new Note(6, 3))));
        Measure measure = new Measure(TimeSignature.fourFour(), MeasureAttributes.plain(), List.of(lead, bass));
        Score score = scoreWith(measure);

        Score loaded = ScoreDto.from(score).toScore();

        assertEquals(score, loaded);
        assertEquals(bass, loaded.track(0).measure(0).voice(VoicePart.BASS));
    }

    @Test
    void roundTripsTheAttributesOfAMeasure() {
        MeasureAttributes attributes = MeasureAttributes.plain()
                .withKeySignature(new KeySignature(-3, Mode.MINOR))
                .withTripletFeel(TripletFeel.EIGHTH)
                .withDoubleBar(true)
                .withRepeatOpen(true)
                .withRepeatCount(3)
                .withAlternateEndings(List.of(1, 2))
                .withSymbol(DirectionSymbol.SEGNO)
                .withJump(DirectionJump.DA_SEGNO_AL_CODA)
                .withMarker(Marker.named("Estribillo"));
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter()).withAttributes(attributes);
        assertRoundTrips(scoreWith(measure));
    }

    @Test
    void roundTripsTheSettingsOfATrack() {
        Track track = Track.standardGuitar("Guitarra")
                .mappingSettings(settings -> settings.withCapo(3).withFretCount(22).withTwelveString(true))
                .withChannel(Channel.playing(30).withChorus(40).withReverb(50).withPort(2).withNumber(5));
        assertRoundTrips(new Score(ScoreInfo.titled("Prueba"), 120, List.of(track), Lyrics.none()));
    }

    @Test
    void roundTripsThePercussionTrack() {
        assertRoundTrips(new Score(
                ScoreInfo.titled("Prueba"), 120, List.of(Track.percussion("Bateria")), Lyrics.none()));
    }

    @Test
    void roundTripsTheScoreInformationAndTheLyrics() {
        ScoreInfo info = ScoreInfo.titled("Prueba")
                .withArtist("Alguien")
                .withAlbum("Un disco")
                .withMusicAuthor("Otro")
                .withCopyright("2026");
        Lyrics lyrics = Lyrics.none().onTrack(0).withLine(0, new LyricLine(2, "es-to es una le-tra"));
        assertRoundTrips(new Score(info, 120, List.of(Track.standardGuitar("Guitarra")), lyrics));
    }

    @Test
    void roundTripsSeveralTracks() {
        assertRoundTrips(new Score(
                "Prueba", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));
    }

    @Test
    void rejectsAMissingTracksField() {
        ScoreDto dto = new ScoreDto(
                ScoreDto.CURRENT_FORMAT, "Prueba", null, null, null, null, null, null, null, null, null, 120, null, null);

        assertThrows(ScoreFileException.class, dto::toScore);
    }

    @Test
    void wrapsDomainInvariantsInScoreFileException() {
        TrackDto track = TrackDto.from(Track.standardGuitar("Guitarra"));
        ScoreDto dto = new ScoreDto(
                ScoreDto.CURRENT_FORMAT, "Prueba", null, null, null, null, null, null, null, null, null, 0,
                List.of(track), null);

        ScoreFileException thrown = assertThrows(ScoreFileException.class, dto::toScore);

        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
    }

    private static Score scoreWith(Measure measure) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }

    private static void assertRoundTrips(Score score) {
        assertEquals(score, ScoreDto.from(score).toScore());
    }
}
