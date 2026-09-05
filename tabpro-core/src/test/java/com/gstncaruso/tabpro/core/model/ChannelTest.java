package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
