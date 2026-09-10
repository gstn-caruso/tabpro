package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Descartable: mide el costo de exportar PDF/BMP antes y despues del fix de rango por hoja.
 * Se borra al cerrar la auditoria, no queda en el arbol.
 */
class PerfBenchScratchTest {

    @TempDir
    Path tmp;

    @Test
    void medirGrandeYEnorme() throws Exception {
        medir("grande", scoreWith(300, 6));
        medir("enorme", scoreWith(600, 8));
    }

    private void medir(String nombre, Score score) throws Exception {
        PageSetup setup = PageSetup.defaults();
        ScorePrinting.exportPdf(score, setup, tmp.resolve(nombre + "-warm.pdf"));

        List<Long> pdfNanos = new ArrayList<>();
        List<Long> bmpNanos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            long t0 = System.nanoTime();
            ScorePrinting.exportPdf(score, setup, tmp.resolve(nombre + "-" + i + ".pdf"));
            pdfNanos.add(System.nanoTime() - t0);

            long t1 = System.nanoTime();
            ScorePrinting.exportImage(
                    score, setup, tmp.resolve(nombre + "-" + i + ".bmp"), ViewMode.PAGE, Zoom.whole());
            bmpNanos.add(System.nanoTime() - t1);
        }
        System.out.println("BENCH " + nombre + " pdf median_ms=" + medianMs(pdfNanos) + " all_ms=" + allMs(pdfNanos));
        System.out.println("BENCH " + nombre + " bmp median_ms=" + medianMs(bmpNanos) + " all_ms=" + allMs(bmpNanos));
    }

    private static double medianMs(List<Long> nanos) {
        List<Long> sorted = new ArrayList<>(nanos);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2) / 1_000_000.0;
    }

    private static List<Double> allMs(List<Long> nanos) {
        List<Double> ms = new ArrayList<>();
        for (long n : nanos) {
            ms.add(n / 1_000_000.0);
        }
        return ms;
    }

    private static Score scoreWith(int measureCount, int trackCount) {
        List<Track> tracks = new ArrayList<>();
        for (int t = 0; t < trackCount; t++) {
            tracks.add(new Track("Pista " + t, Tuning.standard(), Channel.playing(Track.GUITAR_PROGRAM), measuresOf(measureCount)));
        }
        return new Score("Partitura de prueba", 120, tracks);
    }

    private static List<Measure> measuresOf(int count) {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<Beat> beats = new ArrayList<>();
            for (int b = 0; b < 4; b++) {
                beats.add(Beat.of(Duration.quarter(), new Note(1, (i + b) % 5)));
            }
            measures.add(new Measure(TimeSignature.fourFour(), beats));
        }
        return measures;
    }
}
