package com.gstncaruso.tabpro.ui.actions;

/**
 * Lo que las acciones necesitan de la ventana y que no sabe hacer el editor:
 * abrir un dialogo, tocar la partitura, cambiar la vista o manejar el archivo.
 */
public final class Ports {

    private Ports() {
    }

    /** Las ventanas que el manual abre para pedir datos. */
    public interface Dialogs {
        void scoreInformation();

        void pageSetup();

        void preferences();

        void trackProperties();

        void instrument();

        void addTrack();

        void timeSignature();

        void keySignature();

        void tripletFeel();

        void repeatClose();

        void alternateEndings();

        void musicalDirections();

        void mixTableChange();

        void bend();

        void tremoloBar();

        void graceNote();

        void stroke();

        void trill();

        void tremoloPicking();

        void harmonics();

        void text();

        void dynamics();

        void fingering();

        void chordDiagram();

        void scales();

        void tuner();

        void metronomeSettings();

        void insertMarker();

        void markerList();

        void transpose();

        void checkBarDurations();

        void completeBarsWithRests();

        void arrangeBars();

        void automaticFingering();

        void letRingOptions();

        void palmMuteOptions();

        void dynamicOptions();

        void pasteOptions();

        void about();
    }

    /** El transporte, tal como lo describe "Play the Score". */
    public interface Playback {
        void togglePlay();

        void playFromTheBeginning();

        void loopAndSpeedTrainer();

        void toggleMetronome();

        void toggleCountDown();

        void stepForward();

        void stepBack();

        void tempo();

        void relativeTempo();
    }

    /** Lo que el menu Ver decide sobre la pantalla. */
    public interface View {
        void pageMode();

        void parchmentMode();

        void verticalScreenMode();

        void horizontalScreenMode();

        void zoomIn();

        void zoomOut();

        void resetZoom();

        void toggleMultitrack();

        void toggleStandardNotation();

        void toggleTablature();

        void toggleFretboard();

        void toggleKeyboard();

        void togglePercussionAssistant();

        void toggleMixTable();

        void toggleToolBars();
    }

    /** El archivo abierto: crear, abrir, guardar, importar, exportar e imprimir. */
    public interface Document {
        void newScore();

        void open();

        void browse();

        void save();

        void saveAs();

        void importMidi();

        void importAscii();

        void importMusicXml();

        void importGuitarPro();

        void exportMidi();

        void exportAscii();

        void exportMusicXml();

        void exportImage();

        void exportPdf();

        void print();

        void quit();
    }
}
