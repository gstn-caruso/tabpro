package com.gstncaruso.tabpro.core.model;

/** Los parametros de sonido de una pista, los que muestra la mesa de mezcla. */
public record Channel(
        int program, int volume, int pan, int chorus, int reverb, int phaser, int tremolo,
        int port, int number, boolean muted, boolean solo) {

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
        if (number < 1 || number > CHANNELS_PER_PORT) {
            throw new IllegalArgumentException("number debe estar entre 1 y " + CHANNELS_PER_PORT + ": " + number);
        }
    }

    public static Channel playing(int program) {
        return new Channel(program, DEFAULT_VOLUME, CENTER_PAN, 0, 0, 0, 0, 1, 1, false, false);
    }

    public static Channel percussion() {
        return new Channel(0, DEFAULT_VOLUME, CENTER_PAN, 0, 0, 0, 0, 1, PERCUSSION_CHANNEL, false, false);
    }

    public boolean isPercussionChannel() {
        return number == PERCUSSION_CHANNEL;
    }

    public boolean isSilent() {
        return muted || volume == 0;
    }

    public Channel withProgram(int program) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withVolume(int volume) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withPan(int pan) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withChorus(int chorus) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withReverb(int reverb) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withPhaser(int phaser) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withTremolo(int tremolo) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withPort(int port) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel withNumber(int number) {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, solo);
    }

    public Channel toggledMute() {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, !muted, solo);
    }

    public Channel toggledSolo() {
        return new Channel(program, volume, pan, chorus, reverb, phaser, tremolo, port, number, muted, !solo);
    }

    private static void requireInRange(int value, String name) {
        if (value < 0 || value > MAX) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y " + MAX + ": " + value);
        }
    }
}
