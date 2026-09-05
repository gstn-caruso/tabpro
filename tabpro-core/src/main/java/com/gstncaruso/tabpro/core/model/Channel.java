package com.gstncaruso.tabpro.core.model;

public record Channel(int program, int volume, int pan, boolean muted, boolean solo) {

    public static final int DEFAULT_VOLUME = 100;
    public static final int CENTER_PAN = 64;
    private static final int MAX = 127;

    public Channel {
        requireInRange(program, "program");
        requireInRange(volume, "volume");
        requireInRange(pan, "pan");
    }

    public static Channel playing(int program) {
        return new Channel(program, DEFAULT_VOLUME, CENTER_PAN, false, false);
    }

    public boolean isSilent() {
        return muted || volume == 0;
    }

    public Channel withProgram(int program) {
        return new Channel(program, volume, pan, muted, solo);
    }

    public Channel withVolume(int volume) {
        return new Channel(program, volume, pan, muted, solo);
    }

    public Channel withPan(int pan) {
        return new Channel(program, volume, pan, muted, solo);
    }

    public Channel toggledMute() {
        return new Channel(program, volume, pan, !muted, solo);
    }

    public Channel toggledSolo() {
        return new Channel(program, volume, pan, muted, !solo);
    }

    private static void requireInRange(int value, String name) {
        if (value < 0 || value > MAX) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y " + MAX + ": " + value);
        }
    }
}
