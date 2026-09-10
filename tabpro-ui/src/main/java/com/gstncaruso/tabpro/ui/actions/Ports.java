package com.gstncaruso.tabpro.ui.actions;

public final class Ports {

    public static final int PORT_COUNT = 4;

    private Ports() {
    }

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

        void fingeringRightHand();

        void chordDiagram();

        void scales();

        void tuner();

        void metronomeSettings();

        void insertMarker();

        void editMarker();

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

        void help();
    }

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

        void toggleMidiInput();

        void toggleSoundFont();

        boolean soundFontActive();
    }

    public interface View {
        void pageMode();

        void parchmentMode();

        void verticalScreenMode();

        void horizontalScreenMode();

        void zoomIn();

        void zoomOut();

        void resetZoom();

        void toggleMultitrack();

        boolean isMultitrack();

        void toggleGrayInactiveVoice();

        void toggleShowsDynamicNotes();

        void toggleStandardNotation();

        void toggleTablature();

        void toggleFretboard();

        void toggleKeyboard();

        void togglePercussionAssistant();

        void toggleMixTable();

        void toggleView();

        void toggleToolBars();

        void toggleDocumentToolBar();

        void toggleStructureToolBar();

        void toggleNotationToolBar();

        void toggleEffectsToolBar();

        void useTheme(String name);
    }

    public interface Devices {

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

            @Override
            public java.util.Optional<String> soundFontFile() {
                return java.util.Optional.empty();
            }

            @Override
            public void chooseSoundFontFile(java.util.Optional<String> path) {
            }

            @Override
            public boolean soundFontActive() {
                return false;
            }

            @Override
            public void toggleSoundFont() {
            }

            @Override
            public String soundFontStatus() {
                return "MIDI no disponible";
            }
        };

        java.util.List<String> outputs();

        String output(int port);

        void useOutput(int port, String name);

        void playTestNote(String deviceName);

        java.util.List<String> inputs();

        String input();

        void useInput(String name);

        boolean isCapturing();

        void startCapture(CapturedNote listener);

        void stopCapture();

        int sensitivityMillis();

        void useSensitivityMillis(int millis);

        boolean limitsPitchVariation(int port);

        void useLimitPitchVariation(int port, boolean limit);

        java.util.Optional<String> soundFontFile();

        void chooseSoundFontFile(java.util.Optional<String> path);

        boolean soundFontActive();

        void toggleSoundFont();

        String soundFontStatus();
    }

    public interface Microphone {

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

    public record HeardPitch(boolean audible, int nearestMidiNumber, double frequencyHz) {

        public static HeardPitch nothing() {
            return new HeardPitch(false, 0, 0);
        }
    }

    public interface CapturedNote {

        void inTheSameChord(int midiNumber, int channel);

        void inANewBeat(int midiNumber, int channel);
    }

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
