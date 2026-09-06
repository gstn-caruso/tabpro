package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuitarProTrackHeaderTest {

    private static final List<GuitarProChannel> TABLE = List.of(
            new GuitarProChannel(25, 100, 64, 0, 0, 0, 0),
            new GuitarProChannel(33, 90, 30, 0, 0, 0, 0),
            new GuitarProChannel(0, 110, 64, 0, 0, 0, 0));

    @Test
    void playsOnTheTwoChannelsTheFileGaveTheTrack() {
        GuitarProTrackHeader guitarra = trackOn(1, 6, false);

        Channel channel = guitarra.channelIn(TABLE);

        assertEquals(1, channel.number());
        assertEquals(6, channel.effectChannel());
    }

    @Test
    void takesItsSoundFromTheSlotItsChannelPointsAt() {
        GuitarProTrackHeader bajo = trackOn(2, 3, false);

        Channel channel = bajo.channelIn(TABLE);

        assertEquals(33, channel.program());
        assertEquals(90, channel.volume());
        assertEquals(30, channel.pan());
    }

    @Test
    void keepsBothChannelsInsideThePortWhenTheFileLies() {
        GuitarProTrackHeader rota = trackOn(1, 99, false);

        Channel channel = rota.channelIn(TABLE);

        assertEquals(Channel.CHANNELS_PER_PORT, channel.effectChannel());
    }

    @Test
    void aTrackPointingOutsideTheTableStartsOnItsDefaultChannel() {
        GuitarProTrackHeader perdida = trackOn(9, 10, false);
        GuitarProTrackHeader percusionPerdida = trackOn(9, 10, true);

        assertEquals(Channel.playing(Track.GUITAR_PROGRAM), perdida.channelIn(TABLE));
        assertEquals(Channel.percussion(), percusionPerdida.channelIn(TABLE));
    }

    private static GuitarProTrackHeader trackOn(int channelIndex, int effectChannelIndex, boolean percussion) {
        return new GuitarProTrackHeader(
                "Pista", List.of(64, 59, 55, 50, 45, 40), channelIndex, effectChannelIndex, 24, 0,
                ScoreColor.rgb(0xFF0000), percussion, false, false,
                new TrackDisplay(true, true, true, false, DiagramPlacement.ABOVE_THE_STAFF));
    }
}
