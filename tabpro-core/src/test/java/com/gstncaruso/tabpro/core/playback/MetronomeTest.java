package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TestDefaultNames;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetronomeTest {

    @Test
    void whenOffProducesNoClicks() {
        Score score = Score.blank(new TestDefaultNames());

        List<MetronomeClick> clicks = Metronome.off().clicksFor(score);

        assertTrue(clicks.isEmpty());
    }

    @Test
    void marksOneClickPerBeatOfTheBar() {
        Score score = Score.blank(new TestDefaultNames());

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        assertEquals(4, clicks.size());
    }

    @Test
    void theFirstBeatOfEachBarIsAccented() {
        Score score = Score.blank(new TestDefaultNames());

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        assertTrue(clicks.get(0).accented());
        assertTrue(clicks.subList(1, clicks.size()).stream().noneMatch(MetronomeClick::accented));
    }

    @Test
    void beatsAreSpacedByAQuarterNote() {
        Score score = Score.blank(new TestDefaultNames());

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        long quarter = Duration.quarter().ticks();
        assertEquals(List.of(0L, quarter, quarter * 2, quarter * 3),
                clicks.stream().map(MetronomeClick::tick).toList());
    }

    @Test
    void theAccentedSoundDiffersFromTheNormalSound() {
        MetronomeClick accented = new MetronomeClick(0, true);
        MetronomeClick plain = new MetronomeClick(0, false);

        assertTrue(accented.sound() != plain.sound());
    }

    @Test
    void aClickWithoutItsOwnVolumeSoundsAtTheDefaultVelocity() {
        MetronomeClick click = new MetronomeClick(0, true);

        assertEquals(MetronomeClick.DEFAULT_VELOCITY, click.velocity());
    }

    @Test
    void clicksSoundAtTheVolumeConfiguredOnTheMetronome() {
        Score score = Score.blank(new TestDefaultNames());

        List<MetronomeClick> clicks = new Metronome(true, 42).clicksFor(score);

        assertTrue(clicks.stream().allMatch(click -> click.velocity() == 42));
    }

    @Test
    void rejectsANegativeVolume() {
        assertThrows(IllegalArgumentException.class, () -> new Metronome(true, -1));
    }

    @Test
    void rejectsAVolumeAboveTheMidiRange() {
        assertThrows(IllegalArgumentException.class, () -> new Metronome(true, 128));
    }

    @Test
    void withVolumePreservesWhetherItIsEnabled() {
        Metronome metronome = Metronome.on().withVolume(30);

        assertTrue(metronome.enabled());
        assertEquals(30, metronome.volume());
    }

    @Test
    void withEnabledPreservesTheVolume() {
        Metronome metronome = Metronome.on().withVolume(30).withEnabled(false);

        assertFalse(metronome.enabled());
        assertEquals(30, metronome.volume());
    }

    @Test
    void followsThePlaybackOrderWithRepeats() {
        Score score = Score.blank(new TestDefaultNames()).withMeasureInsertedInEveryTrackAt(1);
        Score withRepeat = score.withAttributesInEveryTrackAt(1,
                MeasureAttributes.plain().withRepeatOpen(false).withRepeatCount(2));
        Score fullScore = withRepeat.withAttributesInEveryTrackAt(0,
                MeasureAttributes.plain().withRepeatOpen(true));

        List<MetronomeClick> clicks = Metronome.on().clicksFor(fullScore);

        assertEquals(16, clicks.size());
    }
}
