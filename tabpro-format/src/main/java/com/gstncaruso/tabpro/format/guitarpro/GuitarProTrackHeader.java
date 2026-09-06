package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import java.util.List;

/** Los datos de una pista, tal como los guarda el archivo. */
record GuitarProTrackHeader(
        String name,
        List<Integer> tuningMidiNumbers,
        int channelIndex1Based,
        int effectChannelIndex1Based,
        int fretCount,
        int capo,
        ScoreColor color,
        boolean percussion,
        boolean twelveString,
        boolean banjoFifthString,
        TrackDisplay display) {

    /**
     * El canal del dominio que le toca a esta pista: el sonido que la tabla del
     * archivo guarda en su ranura, sonando en el canal que la pista eligio. Una
     * pista que apunta a una ranura que no existe arranca con el canal por
     * defecto de su instrumento.
     */
    Channel channelIn(List<GuitarProChannel> channels) {
        int slot = channelIndex1Based - 1;
        if (slot < 0 || slot >= channels.size()) {
            return percussion ? Channel.percussion() : Channel.playing(Track.GUITAR_PROGRAM);
        }
        GuitarProChannel sound = channels.get(slot);
        int number = channelOfThePort(channelIndex1Based);
        return new Channel(
                Math.clamp(sound.program(), 0, Channel.MAX),
                sound.volume(),
                sound.pan(),
                sound.chorus(),
                sound.reverb(),
                sound.phaser(),
                sound.tremolo(),
                1,
                number,
                Channel.effectChannelNextTo(number),
                false,
                false);
    }

    private static int channelOfThePort(int index1Based) {
        return Math.clamp(index1Based, 1, Channel.CHANNELS_PER_PORT);
    }
}
