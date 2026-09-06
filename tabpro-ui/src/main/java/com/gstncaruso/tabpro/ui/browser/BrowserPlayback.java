package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;
import java.util.List;

/**
 * El salto automatico que describe el manual: "it is possible to set the number of bars to play
 * before jumping to the next file". Escucha un archivo con un limite de compases y, cuando
 * terminan solos -sin que nadie haya parado antes-, sigue con el proximo de la lista. Se corta
 * si el archivo es el ultimo, si no se pudo abrir, o si alguien para a mano; si la partitura
 * tiene menos compases que el limite no hay nada especial que hacer aca, porque quien de verdad
 * acota cuantos compases suenan es Transport.previewBars.
 */
public final class BrowserPlayback {

    /** Lo que este objeto necesita para escuchar, sin atarse a Transport entero. */
    public interface Sound {

        void play(Score score, int bars, Runnable onFinished);

        void stop();
    }

    /** A quien le importa que la escucha encadenada avance o se corte. */
    public interface Listener {

        void advancedTo(Path path);

        /** Un archivo de la cadena no se pudo abrir; la cadena se corta ahi. */
        void loadFailed(Path path);

        void chainEnded();
    }

    private final ScoreFiles files;
    private final Sound sound;
    private final Listener listener;
    private List<Path> queue = List.of();
    private int index = -1;
    private int bars = 1;
    private boolean chaining;

    public BrowserPlayback(ScoreFiles files, Sound sound, Listener listener) {
        this.files = files;
        this.sound = sound;
        this.listener = listener;
    }

    /** Arranca la escucha encadenada desde ese archivo de esa lista, con ese limite de compases. */
    public void play(List<Path> queue, Path from, int bars) {
        this.queue = queue;
        this.index = queue.indexOf(from);
        this.bars = Math.max(1, bars);
        chaining = true;
        playCurrent();
    }

    /** El usuario para a mano: no hay salto al siguiente archivo. */
    public void stop() {
        chaining = false;
        sound.stop();
    }

    private void playCurrent() {
        if (index < 0 || index >= queue.size()) {
            endChain();
            return;
        }
        Path path = queue.get(index);
        Score score;
        try {
            score = files.load(path);
        } catch (ScoreFileException e) {
            listener.loadFailed(path);
            endChain();
            return;
        }
        listener.advancedTo(path);
        sound.play(score, bars, this::onCurrentFileFinished);
    }

    private void onCurrentFileFinished() {
        if (!chaining) {
            return;
        }
        index++;
        if (index >= queue.size()) {
            endChain();
            return;
        }
        playCurrent();
    }

    private void endChain() {
        chaining = false;
        listener.chainEnded();
    }
}
