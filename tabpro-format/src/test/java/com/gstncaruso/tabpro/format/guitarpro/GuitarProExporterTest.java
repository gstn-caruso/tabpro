package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El escritor exporta a Guitar Pro 4 y el lector existente tiene que releer exactamente lo
 * mismo, salvo lo que GP4 no puede representar (documentado en cada test que lo toca).
 */
class GuitarProExporterTest {

    private final GuitarProExporter exporter = new GuitarProExporter();
    private final GuitarProFile files = new GuitarProFile();

    private Score exportAndReread(Score score) {
        return files.read(exporter.write(score));
    }

    @Test
    void unaSolaNota() {
        Score original = new Score("Una nota", 120,
                List.of(Track.standardGuitar("Guitarra")
                        .withMeasure(0, new Measure(TimeSignature.fourFour(),
                                List.of(Beat.of(Duration.quarter(), new Note(5, 3)))))));

        Score reread = exportAndReread(original);

        assertEquals("Una nota", reread.title());
        assertEquals(120, reread.tempo());
        assertEquals(1, reread.trackCount());
        assertEquals("Guitarra", reread.track(0).name());
        assertEquals(3, reread.track(0).measure(0).beat(0).noteOn(5).orElseThrow().fret());
    }

    @Test
    void variosCompasesConSilenciosYFigurasDistintas() {
        Measure primero = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.of(NoteValue.EIGHTH), new Note(6, 0)),
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.of(new Duration(NoteValue.QUARTER, true), new Note(6, 2)),
                Beat.of(Duration.of(NoteValue.EIGHTH), new Note(6, 3))));
        Measure segundo = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.of(NoteValue.WHOLE).in(Tuplet.of(3)), new Note(6, 5))));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, primero).withMeasureInsertedAt(1, segundo);
        Score original = new Score("Varios compases", 100, List.of(track));

        Score reread = exportAndReread(original);

        Measure primeroReleido = reread.track(0).measure(0);
        assertTrue(primeroReleido.beat(1).isRest());
        assertTrue(primeroReleido.beat(2).duration().dotted());
        assertEquals(NoteValue.EIGHTH, primeroReleido.beat(0).duration().value());
        assertEquals(Tuplet.of(3), reread.track(0).measure(1).beat(0).duration().tuplet());
    }

    @Test
    void variasPistasConAfinacionesEInstrumentosDistintos() {
        Track guitarra = Track.standardGuitar("Lead").withMeasure(0, unaNota(5, 3));
        Track bajo = Track.standardBass("Bajo")
                .withChannel(Channel.playing(Track.BASS_PROGRAM).withNumber(2))
                .withMeasure(0, unaNota(2, 1));
        Track percusion = Track.percussion("Batería").withMeasure(0, new Measure(TimeSignature.fourFour(),
                List.of(Beat.of(Duration.quarter(), new Note(1, 38)))));
        Score original = new Score("Banda", 90, List.of(guitarra, bajo, percusion));

        Score reread = exportAndReread(original);

        assertEquals(3, reread.trackCount());
        assertEquals("Lead", reread.track(0).name());
        assertEquals(Tuning.standard().strings(), reread.track(0).tuning().strings());
        assertEquals("Bajo", reread.track(1).name());
        assertEquals(Tuning.standardBass().strings(), reread.track(1).tuning().strings());
        assertTrue(reread.track(2).isPercussion());
        assertEquals(Channel.PERCUSSION_CHANNEL, reread.track(2).channel().number());
        assertTrue(PercussionKit.isPlayable(reread.track(2).measure(0).beat(0).notes().getFirst().fret()));
    }

    @Test
    void efectosDeNota() {
        Note bendNote = new Note(3, 7).withBend(Bend.of(BendType.BEND, 4));
        Note slideNote = new Note(3, 5).withSlide(SlideType.LEGATO);
        Note ligadoOrigen = new Note(2, 3).toggling(Ornament.HAMMER_ON_PULL_OFF);
        Note palmMuteNote = new Note(4, 2).toggling(Ornament.PALM_MUTE);
        Note harmonicNote = new Note(1, 12).withHarmonic(HarmonicType.NATURAL);
        Note graceNote = new Note(1, 5).withEffects(
                new Note(1, 5).effects().withGrace(GraceNote.before(3)));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), bendNote),
                Beat.of(Duration.quarter(), slideNote),
                Beat.of(Duration.quarter(), ligadoOrigen),
                Beat.of(Duration.quarter(), palmMuteNote)));
        Measure measure2 = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), harmonicNote),
                Beat.of(Duration.quarter(), graceNote),
                Beat.rest(Duration.of(NoteValue.HALF))));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, measure).withMeasureInsertedAt(1, measure2);
        Score original = new Score("Efectos", 120, List.of(track));

        Score reread = exportAndReread(original);

        Measure m0 = reread.track(0).measure(0);
        assertTrue(m0.beat(0).notes().getFirst().effects().bend().isPresent());
        assertEquals(BendType.BEND, m0.beat(0).notes().getFirst().effects().bend().get().type());
        assertEquals(4, m0.beat(0).notes().getFirst().effects().bend().get().peakQuarterTones());
        assertEquals(java.util.Optional.of(SlideType.LEGATO), m0.beat(1).notes().getFirst().effects().slide());
        assertTrue(m0.beat(2).notes().getFirst().has(Ornament.HAMMER_ON_PULL_OFF));
        assertTrue(m0.beat(3).notes().getFirst().has(Ornament.PALM_MUTE));

        Measure m1 = reread.track(0).measure(1);
        assertEquals(java.util.Optional.of(HarmonicType.NATURAL), m1.beat(0).notes().getFirst().effects().harmonic());
        assertTrue(m1.beat(1).notes().getFirst().effects().grace().isPresent());
        assertEquals(3, m1.beat(1).notes().getFirst().effects().grace().get().fret());
        assertTrue(m1.beat(2).isRest());
    }

    @Test
    void unAcordeConSuDiagrama() {
        ChordDiagram cadd9 = new ChordDiagram(
                "Cadd9", 1, List.of(-1, 3, 2, 0, 3, 0), List.of(), true);
        Beat beatConAcorde = Beat.of(Duration.quarter(), new Note(5, 3)).withEffects(
                com.gstncaruso.tabpro.core.model.effects.BeatEffects.none().withChord(cadd9));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beatConAcorde));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, measure);
        Score original = new Score("Acorde", 120, List.of(track));

        Score reread = exportAndReread(original);

        ChordDiagram releido = reread.track(0).measure(0).beat(0).effects().chord().orElseThrow();
        assertEquals("Cadd9", releido.name());
        assertEquals(List.of(-1, 3, 2, 0, 3, 0), releido.frets());
    }

    @Test
    void repeticionesYFinalesAlternativos() {
        Measure abre = new Measure(TimeSignature.fourFour(),
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain().withRepeatOpen(true),
                List.of(new com.gstncaruso.tabpro.core.model.Voice(List.of(Beat.of(Duration.quarter(), new Note(6, 0)))),
                        com.gstncaruso.tabpro.core.model.Voice.unused()));
        Measure cierraConFinales = new Measure(TimeSignature.fourFour(),
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain()
                        .withRepeatCount(2).withAlternateEndings(List.of(1)),
                List.of(new com.gstncaruso.tabpro.core.model.Voice(List.of(Beat.of(Duration.quarter(), new Note(6, 1)))),
                        com.gstncaruso.tabpro.core.model.Voice.unused()));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, abre).withMeasureInsertedAt(1, cierraConFinales);
        Score original = new Score("Repeticion", 120, List.of(track));

        Score reread = exportAndReread(original);

        assertTrue(reread.track(0).measure(0).attributes().repeatOpen());
        assertTrue(reread.track(0).measure(1).attributes().repeatCloses());
        assertEquals(2, reread.track(0).measure(1).attributes().repeatCount());
        assertEquals(List.of(1), reread.track(0).measure(1).attributes().alternateEndings());
    }

    @Test
    void cambiosDeCompasYDeArmadura() {
        Measure cuatroCuartos = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(6, 0))));
        Measure tresCuartosConArmadura = new Measure(new TimeSignature(3, 4),
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain()
                        .withKeySignature(new KeySignature(2, Mode.MAJOR))
                        .withMarker(Marker.named("Estribillo")),
                List.of(new com.gstncaruso.tabpro.core.model.Voice(List.of(Beat.of(Duration.of(NoteValue.HALF), new Note(6, 1)),
                        Beat.of(Duration.quarter(), new Note(6, 2)))),
                        com.gstncaruso.tabpro.core.model.Voice.unused()));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, cuatroCuartos).withMeasureInsertedAt(1, tresCuartosConArmadura);
        Score original = new Score("Cambios", 120, List.of(track));

        Score reread = exportAndReread(original);

        assertEquals(TimeSignature.fourFour(), reread.track(0).measure(0).timeSignature());
        assertEquals(new TimeSignature(3, 4), reread.track(0).measure(1).timeSignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), reread.track(0).measure(1).attributes().keySignature());
        assertEquals("Estribillo", reread.track(0).measure(1).attributes().marker().orElseThrow().name());
    }

    @Test
    void avisaQueLaSegundaVozSePierde() {
        Measure conSegundaVoz = new Measure(TimeSignature.fourFour(),
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain(),
                List.of(new com.gstncaruso.tabpro.core.model.Voice(List.of(Beat.of(Duration.quarter(), new Note(6, 0)))),
                        new com.gstncaruso.tabpro.core.model.Voice(List.of(Beat.of(Duration.quarter(), new Note(5, 2))))));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, conSegundaVoz);
        Score original = new Score("Con segunda voz", 120, List.of(track));

        List<String> warnings = exporter.warningsFor(original);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("segunda voz")));

        Score reread = exportAndReread(original);
        assertFalse(reread.track(0).measure(0).usesTwoVoices());
        assertEquals(0, reread.track(0).measure(0).beat(0).noteOn(6).orElseThrow().fret());
    }

    @Test
    void avisaQueElAutorDeLaMusicaSePierde() {
        Score original = new Score(ScoreInfo.titled("Titulo").withMusicAuthor("Compositor"), 120,
                List.of(Track.standardGuitar("Guitarra").withMeasure(0, unaNota(6, 0))), com.gstncaruso.tabpro.core.model.Lyrics.none());

        List<String> warnings = exporter.warningsFor(original);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("autor de la música")));

        Score reread = exportAndReread(original);
        assertEquals("", reread.info().musicAuthor());
    }

    @Test
    void sinPerdidasNoHayAvisos() {
        Score original = new Score("Simple", 120, List.of(Track.standardGuitar("Guitarra").withMeasure(0, unaNota(6, 0))));

        assertTrue(exporter.warningsFor(original).isEmpty());
    }

    private static Measure unaNota(int string, int fret) {
        return new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(string, fret))));
    }
}
