package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.DefaultNames;

public final class TestDefaultNames implements DefaultNames {

    @Override
    public String track(int number) {
        return "Test Track " + number;
    }

    @Override
    public String unnamedTrack() {
        return "Test Unnamed Track";
    }

    @Override
    public String guitarTrack() {
        return "Test Guitar";
    }

    @Override
    public String percussionTrack() {
        return "Test Percussion";
    }

    @Override
    public String chord() {
        return "Test Chord";
    }

    @Override
    public String marker() {
        return "Test Marker";
    }
}
