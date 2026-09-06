package com.gstncaruso.tabpro.ui.actions;

/**
 * Lo que las acciones necesitan de la ventana y que no sabe hacer el editor:
 * abrir un dialogo, tocar la partitura, cambiar la vista o manejar el archivo.
 */
public final class Ports {

    /** Cuantos puertos MIDI de salida se pueden usar a la vez, como permite el manual. */
    public static final int PORT_COUNT = 4;

    private Ports() {
    }

    /** Las ventanas que el manual abre para pedir datos. */
    public interface Dialogs {
        void scoreInformation();

        void pageSetup();

        void preferences();

        void midiSetup();

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

        void soundDuration();

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

        /** La ayuda del programa, que el manual pone en F1. */
        void help();
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

        /** Prende o apaga la captura de notas de un instrumento MIDI externo. */
        void toggleMidiInput();
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

        /** Si esta puesta la vista multipista, de la que depende el alcance del salto de linea. */
        boolean isMultitrack();

        /** Alterna el atenuado de la voz que no se esta editando, como el Ctrl+G del manual. */
        void toggleGrayInactiveVoice();

        /** El F11 del manual: pinta la cabeza de cada nota con un gradiente segun su dinamica. */
        void toggleShowsDynamicNotes();

        void toggleStandardNotation();

        void toggleTablature();

        void toggleFretboard();

        void toggleKeyboard();

        void togglePercussionAssistant();

        void toggleMixTable();

        /** Ver > Intercambiar vistas: la partitura y la mesa de mezcla cambian de lugar. */
        void toggleView();

        void toggleToolBars();

        /** Muestra u oculta la fila de barras de documento y edicion. */
        void toggleDocumentToolBar();

        /** Muestra u oculta la fila de barras de estructura y sonido. */
        void toggleStructureToolBar();

        /** Muestra u oculta la fila de barras de figuras y efectos. */
        void toggleNotationToolBar();

        /** Cambia el aspecto de la ventana, como el menu Skin del manual. */
        void useTheme(String name);
    }

    /**
     * Los dispositivos MIDI de la maquina: por donde sale el sonido -hasta
     * cuatro puertos a la vez, como permite el manual- y por donde entran las
     * notas de un instrumento externo.
     */
    public interface Devices {

        /** Cuando no hay MIDI, no hay nada que elegir ni que capturar. */
        Devices NONE = new Devices() {

            @Override
            public java.util.List<String> outputs() {
                return java.util.List.of();
            }

            @Override
            public String output(int port) {
                return "";
            }

            @Override
            public void useOutput(int port, String name) {
            }

            @Override
            public void playTestNote(String deviceName) {
            }

            @Override
            public java.util.List<String> inputs() {
                return java.util.List.of();
            }

            @Override
            public String input() {
                return "";
            }

            @Override
            public void useInput(String name) {
            }

            @Override
            public boolean isCapturing() {
                return false;
            }

            @Override
            public void startCapture(CapturedNote listener) {
            }

            @Override
            public void stopCapture() {
            }

            @Override
            public int sensitivityMillis() {
                return 0;
            }

            @Override
            public void useSensitivityMillis(int millis) {
            }

            @Override
            public boolean limitsPitchVariation(int port) {
                return false;
            }

            @Override
            public void useLimitPitchVariation(int port, boolean limit) {
            }
        };

        java.util.List<String> outputs();

        String output(int port);

        void useOutput(int port, String name);

        /** El boton de altavoz: toca una nota de prueba en el dispositivo elegido, sea cual sea su puerto. */
        void playTestNote(String deviceName);

        java.util.List<String> inputs();

        String input();

        void useInput(String name);

        boolean isCapturing();

        void startCapture(CapturedNote listener);

        void stopCapture();

        /** La sensibilidad de captura, en milisegundos: ver MidiCapture. */
        int sensitivityMillis();

        void useSensitivityMillis(int millis);

        /** Si ese puerto tilda Limit Pitch Variation: los bends de mas de un tono no suenan. */
        boolean limitsPitchVariation(int port);

        void useLimitPitchVariation(int port, boolean limit);
    }

    /** La entrada de audio que escucha el afinador digital. */
    public interface Microphone {

        /** Cuando la maquina no tiene entrada de audio, el afinador digital queda apagado. */
        Microphone NONE = new Microphone() {

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public void startListening(java.util.function.Consumer<HeardPitch> heard) {
            }

            @Override
            public void stopListening() {
            }
        };

        boolean isAvailable();

        void startListening(java.util.function.Consumer<HeardPitch> heard);

        void stopListening();
    }

    /** Lo que el afinador escucha: la nota mas cercana y cuanto se desvia de ella. */
    public record HeardPitch(boolean audible, int nearestMidiNumber, double frequencyHz) {

        public static HeardPitch nothing() {
            return new HeardPitch(false, 0, 0);
        }
    }

    /** Lo que llega de un instrumento MIDI mientras se escribe la partitura tocando. */
    public interface CapturedNote {

        void inTheSameChord(int midiNumber, int channel);

        void inANewBeat(int midiNumber, int channel);
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

        void importTabEdit();

        void importPowerTab();

        void exportMidi();

        void exportWave();

        void exportAscii();

        void exportMusicXml();

        void exportGuitarPro();

        void exportImage();

        void exportPdf();

        void print();

        void quit();
    }
}
