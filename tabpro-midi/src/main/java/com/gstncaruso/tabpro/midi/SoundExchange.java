package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;

/**
 * El lado sonoro del intercambio: los formatos que guardan como suena la partitura, no como
 * esta escrita. Hoy son el MIDI que exporta el manual en "File &gt; Export &gt; Midi" y el WAVE
 * de "File &gt; Export &gt; Wave".
 *
 * <p>Vive aca, con la reproduccion, y no con los formatos de notacion, porque exportar sonido
 * es rendir la partitura igual que cuando se la escucha. Leer un MIDI ajeno es otra cosa —
 * traducir una notacion que no es la nuestra— y de eso se ocupa tabpro-format.
 */
public final class SoundExchange implements ScoreExchange {

    private final MidiScoreExporter midiExporter = new MidiScoreExporter();
    private final WaveRenderer waveRenderer;

    /**
     * El renderer recibe el sintetizador de afuera (ver {@link WaveRenderer}): quien arma el
     * SoundExchange decide con que sintetizador y banco de sonidos suena el WAVE.
     */
    public SoundExchange(WaveRenderer waveRenderer) {
        this.waveRenderer = waveRenderer;
    }

    @Override
    public void exportMidi(Score score, Path path) {
        midiExporter.export(score, path);
    }

    @Override
    public void exportWave(Score score, Path path, AudioQuality quality) {
        waveRenderer.render(midiExporter.toSequence(score), path, quality);
    }
}
