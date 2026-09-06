package com.gstncaruso.tabpro.ui.score;

import java.util.ArrayList;
import java.util.List;

/**
 * Quien guarda que pistas estan a la vista, una sola vez para toda la ventana: el menu Ver
 * prende y apaga la vista multipista, la mesa de mezcla apaga pistas sueltas y la partitura
 * dibuja lo que quede. Antes cada uno llevaba su propia copia y podian discrepar.
 */
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

    /** Si la pista esta prendida en la mesa de mezcla, sin importar cual se esta editando. */
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
