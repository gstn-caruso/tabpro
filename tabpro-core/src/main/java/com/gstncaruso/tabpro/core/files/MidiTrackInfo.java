package com.gstncaruso.tabpro.core.files;

public record MidiTrackInfo(int index, String name, boolean percussion, int program, int channelNumber, int noteCount) {
}
