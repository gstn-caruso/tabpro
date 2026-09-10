package com.gstncaruso.tabpro.ui.score;

import java.util.ArrayList;
import java.util.List;

public final class TrackVisibility {

    private final List<Runnable> listeners = new ArrayList<>();
    private VisibleTracks tracks = VisibleTracks.all();

    public VisibleTracks tracks() {
        return tracks;
    }

    public boolean isMultitrack() {
        return tracks.multitrack();
    }

    public void setMultitrack(boolean multitrack) {
        change(tracks.withMultitrack(multitrack));
    }

    public boolean isTurnedOn(int track) {
        return tracks.isTurnedOn(track);
    }

    public void setTurnedOn(int track, boolean on) {
        change(tracks.withTrackShown(track, on));
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    private void change(VisibleTracks tracks) {
        this.tracks = tracks;
        listeners.forEach(Runnable::run);
    }
}
