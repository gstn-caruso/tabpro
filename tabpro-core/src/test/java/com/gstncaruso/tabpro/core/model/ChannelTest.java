package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ChannelTest {

    @Test
    void aChannelStartsUnmutedAtFullBodiedVolumeAndCentered() {
        Channel channel = Channel.playing(25);

        assertEquals(25, channel.program());
        assertEquals(Channel.DEFAULT_VOLUME, channel.volume());
        assertEquals(Channel.CENTER_PAN, channel.pan());
        assertFalse(channel.muted());
        assertFalse(channel.solo());
    }

    @Test
    void rejectsAProgramOutsideTheGeneralMidiRange() {
        assertThrows(IllegalArgumentException.class, () -> Channel.playing(128));
        assertThrows(IllegalArgumentException.class, () -> Channel.playing(-1));
    }

    @Test
    void rejectsAVolumeOutsideTheMidiRange() {
        Channel channel = Channel.playing(25);

        assertThrows(IllegalArgumentException.class, () -> channel.withVolume(128));
        assertThrows(IllegalArgumentException.class, () -> channel.withVolume(-1));
    }

    @Test
    void rejectsAPanOutsideTheMidiRange() {
        Channel channel = Channel.playing(25);

        assertThrows(IllegalArgumentException.class, () -> channel.withPan(128));
        assertThrows(IllegalArgumentException.class, () -> channel.withPan(-1));
    }

    @Test
    void changesOneSettingAtATime() {
        Channel channel = Channel.playing(25);

        assertEquals(30, channel.withProgram(30).program());
        assertEquals(80, channel.withVolume(80).volume());
        assertEquals(0, channel.withPan(0).pan());
        assertEquals(Channel.DEFAULT_VOLUME, channel.withProgram(30).volume());
    }

    @Test
    void togglesMuteAndSoloIndependently() {
        Channel channel = Channel.playing(25);

        assertTrue(channel.toggledMute().muted());
        assertFalse(channel.toggledMute().solo());
        assertFalse(channel.toggledMute().toggledMute().muted());
        assertTrue(channel.toggledSolo().solo());
        assertFalse(channel.toggledSolo().muted());
    }

    @Test
    void isSilentWhenMuted() {
        assertFalse(Channel.playing(25).isSilent());
        assertTrue(Channel.playing(25).toggledMute().isSilent());
    }

    @Test
    void isSilentWhenTurnedAllTheWayDown() {
        assertTrue(Channel.playing(25).withVolume(0).isSilent());
    }

    @Test
    void playsItsEffectsOnTheChannelNextToItsOwn() {
        Channel channel = Channel.playing(25);

        assertEquals(1, channel.number());
        assertEquals(2, channel.effectChannel());
    }

    @Test
    void percussionPlaysEverythingOnTheTenthChannel() {
        Channel percussion = Channel.percussion();

        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.number());
        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.effectChannel());
    }

    @Test
    void movesItsChannelAndItsEffectChannelIndependently() {
        Channel channel = Channel.playing(25);

        assertEquals(5, channel.withNumber(5).number());
        assertEquals(2, channel.withNumber(5).effectChannel());
        assertEquals(6, channel.withEffectChannel(6).effectChannel());
        assertEquals(1, channel.withEffectChannel(6).number());
    }

    @Test
    void rejectsAnEffectChannelOutsideThePort() {
        Channel channel = Channel.playing(25);

        assertThrows(IllegalArgumentException.class, () -> channel.withEffectChannel(0));
        assertThrows(IllegalArgumentException.class, () -> channel.withEffectChannel(17));
    }

    @Test
    void theChannelNextToTheLastOneIsTheLastOneItself() {
        assertEquals(Channel.CHANNELS_PER_PORT, Channel.effectChannelNextTo(Channel.CHANNELS_PER_PORT));
    }

    @Test
    void theChannelNextToPercussionIsPercussionItself() {
        assertEquals(Channel.PERCUSSION_CHANNEL, Channel.effectChannelNextTo(Channel.PERCUSSION_CHANNEL));
    }

    /**
     * El canal que sigue al 9 es el 10, pero ese es el de percusion: una pista melodica en el
     * canal 9 no puede terminar con sus efectos (un bend, por ejemplo) sonando como bateria.
     */
    @Test
    void theChannelNextToTheOneBeforePercussionSkipsPercussion() {
        assertEquals(Channel.PERCUSSION_CHANNEL - 1, Channel.effectChannelNextTo(Channel.PERCUSSION_CHANNEL - 2));
        assertEquals(Channel.PERCUSSION_CHANNEL + 1, Channel.effectChannelNextTo(Channel.PERCUSSION_CHANNEL - 1));
    }

    @Test
    void theFirstFreeChannelPairWhenNoneIsTakenIsOneAndTwo() {
        Channel channel = Channel.playing(25).withNextFreeChannelPairAfter(Set.of());

        assertEquals(1, channel.number());
        assertEquals(2, channel.effectChannel());
    }

    @Test
    void theFreeChannelPairSkipsTheOnesAlreadyTaken() {
        Channel channel = Channel.playing(25).withNextFreeChannelPairAfter(Set.of(1, 2));

        assertEquals(3, channel.number());
        assertEquals(4, channel.effectChannel());
    }

    @Test
    void theFreeChannelPairNeverLandsItsEffectsOnPercussion() {
        Channel channel = Channel.playing(25).withNextFreeChannelPairAfter(Set.of(1, 2, 3, 4, 5, 6, 7, 8));

        // el 9 esta libre, pero effectChannelNextTo(9) salta el 10 -reservado para la percusion- y da 11
        assertEquals(9, channel.number());
        assertEquals(11, channel.effectChannel());
    }

    @Test
    void aFreeChannelPairKeepsTheRestOfTheChannelUntouched() {
        Channel channel = Channel.playing(25).withVolume(80).withReverb(50)
                .withNextFreeChannelPairAfter(Set.of());

        assertEquals(25, channel.program());
        assertEquals(80, channel.volume());
        assertEquals(50, channel.reverb());
    }

    /** Cuando no queda ningun par libre, la pista se degrada a un solo canal en vez de fallar. */
    @Test
    void whenNoPairIsFreeTheChannelSharesItselfWithItsEffects() {
        Set<Integer> everyNonPercussionChannel = java.util.stream.IntStream.rangeClosed(1, Channel.CHANNELS_PER_PORT)
                .filter(number -> number != Channel.PERCUSSION_CHANNEL)
                .boxed()
                .collect(java.util.stream.Collectors.toSet());

        Channel channel = Channel.playing(25).withNextFreeChannelPairAfter(everyNonPercussionChannel);

        assertEquals(channel.number(), channel.effectChannel());
    }
}
