package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.browser.ScoreBrowser;
import com.gstncaruso.tabpro.ui.dialogs.effects.NoteEffectsDialog;
import com.gstncaruso.tabpro.ui.dialogs.info.ScoreInfoDialog;
import com.gstncaruso.tabpro.ui.dialogs.instrument.InstrumentDialog;
import com.gstncaruso.tabpro.ui.dialogs.markers.MarkersDialog;
import com.gstncaruso.tabpro.ui.dialogs.measure.MeasurePropertiesDialog;
import com.gstncaruso.tabpro.ui.dialogs.metronome.MetronomeDialog;
import com.gstncaruso.tabpro.ui.dialogs.metronome.MetronomeSettings;
import com.gstncaruso.tabpro.ui.dialogs.note.DynamicsDialog;
import com.gstncaruso.tabpro.ui.dialogs.note.FingeringDialog;
import com.gstncaruso.tabpro.ui.dialogs.pagesetup.PageSetup;
import com.gstncaruso.tabpro.ui.dialogs.pagesetup.PageSetupDialog;
import com.gstncaruso.tabpro.ui.dialogs.paste.PasteDialog;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesDialog;
import com.gstncaruso.tabpro.ui.dialogs.track.AddTrackDialog;
import com.gstncaruso.tabpro.ui.dialogs.track.TrackPropertiesDialog;
import com.gstncaruso.tabpro.ui.dialogs.tuner.TunerDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.AutomaticFingeringDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.BarArrangerDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.BarDurationCheckDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.RestFillerDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.StringOptionsDialog;
import com.gstncaruso.tabpro.ui.dialogs.wizards.TranspositionDialog;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.sound.LoopDialog;
import com.gstncaruso.tabpro.ui.sound.MidiSetupDialog;
import com.gstncaruso.tabpro.ui.sound.StringAssignment;
import com.gstncaruso.tabpro.ui.sound.RelativeTempoDialog;
import com.gstncaruso.tabpro.ui.instruments.BeatViews;
import com.gstncaruso.tabpro.ui.menu.MenuBar;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.status.StatusBar;
import com.gstncaruso.tabpro.ui.theme.Palette;
import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;
import com.gstncaruso.tabpro.ui.toolbar.ToolBars;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/** La ventana principal: la partitura, las herramientas y la mesa de mezcla. */
public final class MainFrame extends JFrame {

    private final Editor editor;
    private final ScoreFiles files;
    private final ScoreDocument document;
    private final ScoreCanvas canvas;
    private final Transport transport;
    private final BeatViews beatViews;
    private final TrackPanel trackPanel;
    private final ToolBars toolBars;
    private final ThemeSwitch themes;
    private final Ports.Devices devices;
    private final Player player;
    private StringAssignment stringAssignment = StringAssignment.NO_CHANNEL_DETECTION;
    private PageSetup pageSetup = PageSetup.defaults();
    private com.gstncaruso.tabpro.ui.dialogs.preferences.Preferences editingPreferences =
            com.gstncaruso.tabpro.ui.dialogs.preferences.Preferences.defaults();
    private MetronomeSettings metronomeSettings = MetronomeSettings.off();
    private final JSplitPane split;

    public MainFrame(Editor editor, ScoreFiles files, Player player) {
        this(editor, files, player, ThemeSwitch.NONE, Ports.Devices.NONE);
    }

    public MainFrame(
            Editor editor, ScoreFiles files, Player player, ThemeSwitch themes, Ports.Devices devices) {
        super("tabpro");
        this.themes = themes;
        this.devices = devices;
        this.player = player;
        this.editor = editor;
        this.files = files;
        this.document = new ScoreDocument(editor, files);
        setSize(windowSize());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        canvas = new ScoreCanvas(editor);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ScoreColors.BACKGROUND);

        transport = new Transport(editor, player, SwingUtilities::invokeLater);
        trackPanel = new TrackPanel(editor);
        beatViews = new BeatViews(editor, player);

        JSpinner tempoSpinner = tempoSpinner();
        Commands commands = new Commands(
                editor, new Document(), new Windows(), new Playback(), new View(), themes.names());
        toolBars = new ToolBars(commands);
        toolBars.addToSoundRow(new JLabel("Tempo "));
        toolBars.addToSoundRow(tempoSpinner);
        setJMenuBar(new MenuBar(commands).build());

        StatusBar status = new StatusBar(editor);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Palette.separator()),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        transport.addListener(() -> {
            canvas.showPlayhead(transport.playhead());
            trackPanel.showPlayingMeasure(transport.playhead().measure());
            beatViews.showPlayhead(transport.playhead());
        });
        editor.addListener(this::updateTitle);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, trackPanel);
        split.setResizeWeight(1);
        split.setBorder(BorderFactory.createEmptyBorder());

        JPanel top = new JPanel(new BorderLayout());
        top.add(toolBars.component(), BorderLayout.NORTH);
        top.add(beatViews, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        updateTitle();

        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                showMixTable();
                offerToRecover();
                canvas.requestFocusInWindow();
            }
        });
    }

    /**
     * Si quedo una copia de recuperacion de una sesion anterior, se ofrece
     * abrirla, que es lo que hace Guitar Pro despues de una terminacion anormal.
     */
    private void offerToRecover() {
        document.pendingRecovery().ifPresent(recovery -> {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Quedó una partitura sin guardar de la última sesión. ¿Recuperarla?",
                    "tabpro",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                openOnStartup(recovery);
            } else {
                document.discardRecovery();
            }
        });
    }

    /** La mesa de mezcla ocupa lo suyo abajo; el resto es partitura. */
    private void showMixTable() {
        split.setDividerLocation(Math.max(0, split.getHeight() - trackPanel.preferredPanelHeight()));
    }

    /** Abre la partitura que el escritorio paso por linea de comandos. */
    public void openOnStartup(Path path) {
        try {
            document.open(path);
            updateTitle();
        } catch (ScoreFileException e) {
            showError(e);
        }
    }

    private JSpinner tempoSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(editor.score().tempo(), 20, 400, 1));
        spinner.setMaximumSize(new Dimension(70, 24));
        spinner.setPreferredSize(new Dimension(70, 24));
        spinner.addChangeListener(event -> editor.setTempo((Integer) spinner.getValue()));
        editor.addListener(() -> spinner.setValue(editor.score().tempo()));
        return spinner;
    }

    /** Lo mas grande que entre comodo en la pantalla, sin pasarse. */
    private static Dimension windowSize() {
        Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
        return new Dimension(Math.min(1440, screen.width - 60), Math.min(950, screen.height - 60));
    }

    private void backToTheScore() {
        canvas.requestFocusInWindow();
    }

    private void updateTitle() {
        setTitle(document.windowTitle());
    }

    private void showError(ScoreFileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "tabpro", JOptionPane.ERROR_MESSAGE);
    }

    private Path withTabproExtension(File file) {
        String name = file.getName();
        return name.endsWith(ScoreDocument.EXTENSION)
                ? file.toPath()
                : file.toPath().resolveSibling(name + ScoreDocument.EXTENSION);
    }

    private FileNameExtensionFilter tabproFilter() {
        return new FileNameExtensionFilter("Partituras tabpro (*.tabpro)", "tabpro");
    }

    /** El archivo abierto, tal como lo pide el menu Archivo del manual. */
    private final class Document implements Ports.Document {

        @Override
        public void newScore() {
            if (askToDiscardChanges()) {
                document.newScore();
                updateTitle();
                backToTheScore();
            }
        }

        @Override
        public void open() {
            if (!askToDiscardChanges()) {
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(tabproFilter());
            if (chooser.showOpenDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                document.open(chooser.getSelectedFile().toPath());
                updateTitle();
                backToTheScore();
            } catch (ScoreFileException e) {
                showError(e);
            }
        }

        @Override
        public void browse() {
            ScoreBrowser browser = new ScoreBrowser(MainFrame.this, files, this::openQuietly, transport::preview);
            browser.searchIn(document.path().map(Path::getParent).orElse(Path.of(System.getProperty("user.home"))));
            browser.setVisible(true);
        }

        private void openQuietly(Path path) {
            try {
                document.open(path);
                updateTitle();
                backToTheScore();
            } catch (ScoreFileException e) {
                showError(e);
            }
        }

        @Override
        public void save() {
            try {
                if (!document.save()) {
                    saveAs();
                }
                updateTitle();
            } catch (ScoreFileException e) {
                showError(e);
            }
        }

        @Override
        public void saveAs() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(tabproFilter());
            if (chooser.showSaveDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                document.saveAs(withTabproExtension(chooser.getSelectedFile()));
                updateTitle();
            } catch (ScoreFileException e) {
                showError(e);
            }
        }

        @Override
        public void importMidi() {
            PendingFeature.announce(MainFrame.this, "La importación de MIDI");
        }

        @Override
        public void importAscii() {
            PendingFeature.announce(MainFrame.this, "La importación de tablatura ASCII");
        }

        @Override
        public void importMusicXml() {
            PendingFeature.announce(MainFrame.this, "La importación de MusicXML");
        }

        @Override
        public void importGuitarPro() {
            PendingFeature.announce(MainFrame.this, "La apertura de archivos de Guitar Pro");
        }

        @Override
        public void exportMidi() {
            PendingFeature.announce(MainFrame.this, "La exportación a MIDI");
        }

        @Override
        public void exportAscii() {
            PendingFeature.announce(MainFrame.this, "La exportación a tablatura ASCII");
        }

        @Override
        public void exportMusicXml() {
            PendingFeature.announce(MainFrame.this, "La exportación a MusicXML");
        }

        @Override
        public void exportImage() {
            PendingFeature.announce(MainFrame.this, "La exportación a imagen");
        }

        @Override
        public void exportPdf() {
            PendingFeature.announce(MainFrame.this, "La exportación a PDF");
        }

        @Override
        public void print() {
            PendingFeature.announce(MainFrame.this, "La impresión");
        }

        @Override
        public void quit() {
            if (askToDiscardChanges()) {
                dispose();
            }
        }

        private boolean askToDiscardChanges() {
            if (!document.hasUnsavedChanges()) {
                return true;
            }
            int answer = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "La partitura tiene cambios sin guardar. ¿Guardarlos?",
                    "tabpro",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (answer == JOptionPane.YES_OPTION) {
                save();
            }
            return true;
        }
    }

    /** El transporte del menu Sonido. */
    private final class Playback implements Ports.Playback {

        @Override
        public void togglePlay() {
            transport.toggle();
            backToTheScore();
        }

        @Override
        public void playFromTheBeginning() {
            transport.playFromTheBeginning();
            backToTheScore();
        }

        @Override
        public void loopAndSpeedTrainer() {
            if (transport.loop().isPresent()) {
                transport.stopLooping();
                return;
            }
            LoopDialog.ask(MainFrame.this, editor, transport.relativeTempo())
                    .ifPresent(loop -> transport.loopOver(loop.range(), loop.trainer().orElse(null)));
        }

        @Override
        public void toggleMetronome() {
            transport.toggleMetronome();
        }

        @Override
        public void toggleCountDown() {
            transport.toggleCountDown();
        }

        @Override
        public void stepForward() {
            transport.stepForward();
        }

        @Override
        public void stepBack() {
            transport.stepBack();
        }

        @Override
        public void tempo() {
            String answer = JOptionPane.showInputDialog(
                    MainFrame.this, "Tempo en negras por minuto", editor.score().tempo());
            if (answer == null) {
                return;
            }
            try {
                editor.setTempo(Integer.parseInt(answer.trim()));
            } catch (NumberFormatException e) {
                showTempoError();
            }
        }

        @Override
        public void toggleMidiInput() {
            if (devices.isCapturing()) {
                devices.stopCapture();
                return;
            }
            if (devices.inputs().isEmpty()) {
                JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "No hay ningún instrumento MIDI conectado.",
                        "tabpro",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            devices.startCapture(new CapturedNotes());
        }

        @Override
        public void relativeTempo() {
            RelativeTempoDialog.ask(MainFrame.this, transport.relativeTempo())
                    .ifPresent(transport::setRelativeTempo);
        }

        private void showTempoError() {
            JOptionPane.showMessageDialog(
                    MainFrame.this, "El tempo se escribe con un número.", "tabpro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Lo que el menu Ver decide sobre la pantalla. */
    private final class View implements Ports.View {

        @Override
        public void pageMode() {
            PendingFeature.announce(MainFrame.this, "El modo página");
        }

        @Override
        public void parchmentMode() {
            PendingFeature.announce(MainFrame.this, "El modo pergamino");
        }

        @Override
        public void verticalScreenMode() {
            PendingFeature.announce(MainFrame.this, "El modo de pantalla vertical");
        }

        @Override
        public void horizontalScreenMode() {
            PendingFeature.announce(MainFrame.this, "El modo de pantalla horizontal");
        }

        @Override
        public void zoomIn() {
            PendingFeature.announce(MainFrame.this, "El zoom");
        }

        @Override
        public void zoomOut() {
            zoomIn();
        }

        @Override
        public void resetZoom() {
            zoomIn();
        }

        @Override
        public void toggleMultitrack() {
            PendingFeature.announce(MainFrame.this, "La vista multipista");
        }

        @Override
        public void toggleStandardNotation() {
            PendingFeature.announce(MainFrame.this, "Ocultar el pentagrama");
        }

        @Override
        public void toggleTablature() {
            PendingFeature.announce(MainFrame.this, "Ocultar la tablatura");
        }

        @Override
        public void toggleFretboard() {
            beatViews.setFretboardVisible(!beatViews.isFretboardVisible());
            backToTheScore();
        }

        @Override
        public void toggleKeyboard() {
            beatViews.setKeyboardVisible(!beatViews.isKeyboardVisible());
            backToTheScore();
        }

        @Override
        public void togglePercussionAssistant() {
            PendingFeature.announce(MainFrame.this, "El asistente de percusión");
        }

        @Override
        public void toggleMixTable() {
            trackPanel.setVisible(!trackPanel.isVisible());
            if (trackPanel.isVisible()) {
                showMixTable();
            } else {
                split.setDividerLocation(split.getHeight());
            }
            backToTheScore();
        }

        @Override
        public void toggleToolBars() {
            toolBars.setVisible(!toolBars.isVisible());
            backToTheScore();
        }

        @Override
        public void useTheme(String name) {
            themes.apply(name);
            SwingUtilities.updateComponentTreeUI(MainFrame.this);
            backToTheScore();
        }
    }

    /**
     * Lo que llega del instrumento MIDI: la nota se escribe en la cuerda que
     * corresponda y el cursor avanza al beat siguiente, como dice el manual.
     */
    private final class CapturedNotes implements Ports.CapturedNote {

        @Override
        public void inTheSameChord(int midiNumber, int channel) {
            SwingUtilities.invokeLater(() -> write(midiNumber, channel));
        }

        @Override
        public void inANewBeat(int midiNumber, int channel) {
            SwingUtilities.invokeLater(() -> {
                editor.moveRight();
                write(midiNumber, channel);
            });
        }

        private void write(int midiNumber, int channel) {
            Track track = editor.currentTrack();
            Pitch pitch = new Pitch(Math.clamp(midiNumber, 0, 127));
            stringAssignment.stringFor(channel, track.stringCount()).stream()
                    .boxed()
                    .flatMap(string -> track.tuning().noteFor(pitch, string).stream())
                    .findFirst()
                    .or(() -> track.tuning().bestNoteFor(pitch, track.settings().fretCount()))
                    .ifPresent(note -> {
                        editor.moveTo(editor.cursor().measure(), editor.cursor().beat(), note.string());
                        editor.setFret(note.fret());
                    });
        }
    }

    /** Las ventanas del manual, cada una con lo que el editor necesita. */
    private final class Windows implements Ports.Dialogs {

        @Override
        public void scoreInformation() {
            ScoreInfoDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void pageSetup() {
            PageSetupDialog.ask(MainFrame.this, pageSetup).ifPresent(setup -> pageSetup = setup);
            backToTheScore();
        }

        @Override
        public void preferences() {
            PreferencesDialog.ask(MainFrame.this, editingPreferences)
                    .ifPresent(updated -> editingPreferences = updated);
            backToTheScore();
        }

        @Override
        public void midiSetup() {
            MidiSetupDialog.ask(MainFrame.this, devices, stringAssignment).ifPresent(setup -> {
                devices.useOutput(setup.output());
                devices.useInput(setup.input());
                stringAssignment = setup.strings();
            });
            backToTheScore();
        }

        @Override
        public void trackProperties() {
            TrackPropertiesDialog.show(MainFrame.this, editor, editor.cursor().track(), player);
            backToTheScore();
        }

        @Override
        public void instrument() {
            InstrumentDialog.show(MainFrame.this, editor, editor.cursor().track());
            backToTheScore();
        }

        @Override
        public void addTrack() {
            AddTrackDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void timeSignature() {
            measureProperties(MeasurePropertiesDialog.TIME_SIGNATURE);
        }

        @Override
        public void keySignature() {
            measureProperties(MeasurePropertiesDialog.KEY_SIGNATURE);
        }

        @Override
        public void tripletFeel() {
            measureProperties(MeasurePropertiesDialog.TRIPLET_FEEL);
        }

        @Override
        public void repeatClose() {
            measureProperties(MeasurePropertiesDialog.REPEAT);
        }

        @Override
        public void alternateEndings() {
            measureProperties(MeasurePropertiesDialog.ALTERNATE_ENDINGS);
        }

        @Override
        public void musicalDirections() {
            measureProperties(MeasurePropertiesDialog.DIRECTIONS);
        }

        @Override
        public void mixTableChange() {
            PendingFeature.announce(MainFrame.this, "El cambio de parámetros en medio de la partitura");
        }

        @Override
        public void bend() {
            noteEffects(NoteEffectsDialog.BEND);
        }

        @Override
        public void tremoloBar() {
            noteEffects(NoteEffectsDialog.TREMOLO_BAR);
        }

        @Override
        public void graceNote() {
            noteEffects(NoteEffectsDialog.GRACE_NOTE);
        }

        @Override
        public void stroke() {
            noteEffects(NoteEffectsDialog.STROKE);
        }

        @Override
        public void trill() {
            noteEffects(NoteEffectsDialog.TRILL);
        }

        @Override
        public void tremoloPicking() {
            noteEffects(NoteEffectsDialog.TREMOLO_PICKING);
        }

        @Override
        public void harmonics() {
            noteEffects(NoteEffectsDialog.HARMONICS);
        }

        @Override
        public void text() {
            String written = JOptionPane.showInputDialog(
                    MainFrame.this, "Texto sobre la tablatura",
                    editor.currentBeat().effects().text().orElse(""));
            if (written != null) {
                editor.setText(written);
            }
            backToTheScore();
        }

        @Override
        public void dynamics() {
            DynamicsDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void fingering() {
            FingeringDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void chordDiagram() {
            PendingFeature.announce(MainFrame.this, "La ventana de acordes");
        }

        @Override
        public void scales() {
            PendingFeature.announce(MainFrame.this, "La ventana de escalas");
        }

        @Override
        public void tuner() {
            TunerDialog.show(MainFrame.this, editor, player);
            backToTheScore();
        }

        @Override
        public void metronomeSettings() {
            MetronomeDialog.ask(MainFrame.this, editor, metronomeSettings)
                    .ifPresent(settings -> metronomeSettings = settings);
            backToTheScore();
        }

        @Override
        public void insertMarker() {
            MarkersDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void markerList() {
            insertMarker();
        }

        @Override
        public void transpose() {
            TranspositionDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void checkBarDurations() {
            BarDurationCheckDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void completeBarsWithRests() {
            RestFillerDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void arrangeBars() {
            BarArrangerDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void automaticFingering() {
            AutomaticFingeringDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void letRingOptions() {
            StringOptionsDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void palmMuteOptions() {
            letRingOptions();
        }

        @Override
        public void dynamicOptions() {
            letRingOptions();
        }

        @Override
        public void pasteOptions() {
            PasteDialog.show(MainFrame.this, editor);
            backToTheScore();
        }

        @Override
        public void about() {
            JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "tabpro — clon libre de Guitar Pro 5.",
                    "Acerca de tabpro",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        private void measureProperties(String tab) {
            MeasurePropertiesDialog.show(MainFrame.this, editor, tab);
            backToTheScore();
        }

        private void noteEffects(String tab) {
            NoteEffectsDialog.show(MainFrame.this, editor, tab);
            backToTheScore();
        }
    }
}
