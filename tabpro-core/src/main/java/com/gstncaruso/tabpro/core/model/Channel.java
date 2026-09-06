package com.gstncaruso.tabpro.core.model;

/**
 * Los parametros de sonido de una pista, los que muestra la mesa de mezcla.
 *
 * <p>Una pista ocupa dos canales del puerto, como hace Guitar Pro por defecto:
 * el suyo, donde suenan las notas limpias, y el de efectos, donde suenan las
 * que llevan bend o vibrato, para que corriendo la altura de una no se corra
 * la de las demas. Poner los dos en el mismo numero es volver a un solo canal
 * por pista, que viene bien cuando la partitura tiene muchas.
 */
public record Channel(
        int program, int volume, int pan, int chorus, int reverb, int phaser, int tremolo,
        int port, int number, int effectChannel, boolean muted, boolean solo) {

    public static final int DEFAULT_VOLUME = 100;
    public static final int CENTER_PAN = 64;
    public static final int MAX = 127;

    /** Por convencion de MIDI, la percusion suena siempre en el canal 10. */
    public static final int PERCUSSION_CHANNEL = 10;

    public static final int PORT_COUNT = 4;
    public static final int CHANNELS_PER_PORT = 16;

    public Channel {
        requireInRange(program, "program");
        requireInRange(volume, "volume");
        requireInRange(pan, "pan");
        requireInRange(chorus, "chorus");
        requireInRange(reverb, "reverb");
        requireInRange(phaser, "phaser");
        requireInRange(tremolo, "tremolo");
        if (port < 1 || port > PORT_COUNT) {
            throw new IllegalArgumentException("port debe estar entre 1 y " + PORT_COUNT + ": " + port);
        }
        requireAChannelOfThePort(number, "number");
        requireAChannelOfThePort(effectChannel, "effectChannel");
    }

    public static Channel playing(int program) {
        return new Channel(program, DEFAULT_VOLUME, CENTER_PAN, 0, 0, 0, 0, 1, 1, effectChannelNextTo(1), false, false);
    }

    public static Channel percussion() {
        return new Channel(0, DEFAULT_VOLUME, CENTER_PAN, 0, 0, 0, 0, 1, PERCUSSION_CHANNEL,
                effectChannelNextTo(PERCUSSION_CHANNEL), false, false);
    }

    /**
     * El canal de efectos que le toca por defecto al que use ese numero: el que
     * sigue, salteando el de percusion si es que cae justo ahi -una pista
     * melodica no puede terminar con sus efectos sonando como bateria-, salvo
     * en la propia percusion, que toca todo en el 10. Al que ya esta en el
     * ultimo canal del puerto no le queda a donde ir: se queda en el suyo,
     * degradando a un solo canal para la pista, algo que el propio modelo
     * permite a proposito.
     */
    public static int effectChannelNextTo(int number) {
        if (number == PERCUSSION_CHANNEL) {
            return PERCUSSION_CHANNEL;
        }
        int next = number + 1;
        if (next == PERCUSSION_CHANNEL) {
            next++;
        }
        return Math.min(next, CHANNELS_PER_PORT);
    }

    public boolean isPercussionChannel() {
        return number == PERCUSSION_CHANNEL;
    }

    public boolean isSilent() {
        return muted || volume == 0;
    }

    public Channel withProgram(int program) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withVolume(int volume) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withPan(int pan) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withChorus(int chorus) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withReverb(int reverb) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withPhaser(int phaser) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withTremolo(int tremolo) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withPort(int port) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withNumber(int number) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel withEffectChannel(int effectChannel) {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, solo);
    }

    public Channel toggledMute() {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, !muted, solo);
    }

    public Channel toggledSolo() {
        return new Channel(
                program, volume, pan, chorus, reverb, phaser, tremolo, port, number, effectChannel, muted, !solo);
    }

    private static void requireInRange(int value, String name) {
        if (value < 0 || value > MAX) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y " + MAX + ": " + value);
        }
    }

    private static void requireAChannelOfThePort(int value, String name) {
        if (value < 1 || value > CHANNELS_PER_PORT) {
            throw new IllegalArgumentException(
                    name + " debe estar entre 1 y " + CHANNELS_PER_PORT + ": " + value);
        }
    }
}
