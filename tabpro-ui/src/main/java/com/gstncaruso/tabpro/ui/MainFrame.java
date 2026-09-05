package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.browser.ScoreBrowser;
import com.gstncaruso.tabpro.ui.instruments.BeatViews;
import com.gstncaruso.tabpro.ui.menu.MenuBar;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.Palette;
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
    private final JSplitPane split;

    public MainFrame(Editor editor, ScoreFiles files, Player player) {
        super("tabpro");
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
        Commands commands = new Commands(editor, new Document(), new NotYet(), new Playback(), new View());
        toolBars = new ToolBars(commands);
        toolBars.addToSoundRow(new JLabel("Tempo "));
        toolBars.addToSoundRow(tempoSpinner);
        setJMenuBar(new MenuBar(commands).build());

        JLabel status = statusBar();
        transport.addListener(() -> {
            canvas.showPlayhead(transport.playhead());
            trackPanel.showPlayingMeasure(transport.playhead().measure());
            beatViews.showPlayhead(transport.playhead());
        });
        editor.addListener(() -> {
            status.setText(StatusText.describe(editor.cursor(), editor.currentBeat()));
            updateTitle();
        });

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, trackPanel);
        split.setResizeWeight(1);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerLocation(getHeight() - trackPanel.preferredPanelHeight());

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
                canvas.requestFocusInWindow();
            }
        });
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

    private JLabel statusBar() {
        JLabel status = new JLabel(StatusText.describe(editor.cursor(), editor.currentBeat()));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Palette.separator()),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return status;
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
            editor.moveToFirstMeasure();
            transport.toggle();
            backToTheScore();
        }

        @Override
        public void loopAndSpeedTrainer() {
            PendingFeature.announce(MainFrame.this, "El loop y el entrenador de velocidad");
        }

        @Override
        public void toggleMetronome() {
            PendingFeature.announce(MainFrame.this, "El metrónomo");
        }

        @Override
        public void toggleCountDown() {
            PendingFeature.announce(MainFrame.this, "La cuenta regresiva");
        }

        @Override
        public void stepForward() {
            editor.moveRight();
        }

        @Override
        public void stepBack() {
            editor.moveLeft();
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
        public void relativeTempo() {
            PendingFeature.announce(MainFrame.this, "El tempo relativo");
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
            split.setDividerLocation(trackPanel.isVisible()
                    ? getHeight() - trackPanel.preferredPanelHeight()
                    : getHeight());
            backToTheScore();
        }

        @Override
        public void toggleToolBars() {
            toolBars.setVisible(!toolBars.isVisible());
            backToTheScore();
        }
    }

    /** Las ventanas que todavia no estan construidas. */
    private final class NotYet implements Ports.Dialogs {

        @Override
        public void scoreInformation() {
            announce("La información de la partitura");
        }

        @Override
        public void pageSetup() {
            announce("La configuración de página");
        }

        @Override
        public void preferences() {
            announce("Las preferencias");
        }

        @Override
        public void trackProperties() {
            announce("Las propiedades de la pista");
        }

        @Override
        public void instrument() {
            announce("La ventana de instrumento");
        }

        @Override
        public void addTrack() {
            announce("La ventana de agregar pista");
        }

        @Override
        public void timeSignature() {
            announce("La ventana de medida del compás");
        }

        @Override
        public void keySignature() {
            announce("La ventana de armadura");
        }

        @Override
        public void tripletFeel() {
            announce("La ventana de triplet feel");
        }

        @Override
        public void repeatClose() {
            announce("La ventana de repeticiones");
        }

        @Override
        public void alternateEndings() {
            announce("La ventana de finales alternativos");
        }

        @Override
        public void musicalDirections() {
            announce("La ventana de direcciones musicales");
        }

        @Override
        public void mixTableChange() {
            announce("El cambio de parámetros");
        }

        @Override
        public void bend() {
            announce("La ventana de bend");
        }

        @Override
        public void tremoloBar() {
            announce("La ventana de palanca");
        }

        @Override
        public void graceNote() {
            announce("La ventana de nota de adorno");
        }

        @Override
        public void stroke() {
            announce("La ventana de rasgueo");
        }

        @Override
        public void trill() {
            announce("La ventana de trino");
        }

        @Override
        public void tremoloPicking() {
            announce("La ventana de trémolo de púa");
        }

        @Override
        public void harmonics() {
            announce("La ventana de armónicos");
        }

        @Override
        public void text() {
            announce("La ventana de texto");
        }

        @Override
        public void dynamics() {
            announce("La ventana de dinámica");
        }

        @Override
        public void fingering() {
            announce("La ventana de digitación");
        }

        @Override
        public void chordDiagram() {
            announce("La ventana de acordes");
        }

        @Override
        public void scales() {
            announce("La ventana de escalas");
        }

        @Override
        public void tuner() {
            announce("El afinador");
        }

        @Override
        public void metronomeSettings() {
            announce("La configuración del metrónomo");
        }

        @Override
        public void insertMarker() {
            announce("La ventana de marcadores");
        }

        @Override
        public void markerList() {
            announce("La lista de marcadores");
        }

        @Override
        public void transpose() {
            announce("El asistente de transposición");
        }

        @Override
        public void checkBarDurations() {
            announce("La verificación de duración de compases");
        }

        @Override
        public void completeBarsWithRests() {
            announce("El asistente de silencios");
        }

        @Override
        public void arrangeBars() {
            announce("El organizador de compases");
        }

        @Override
        public void automaticFingering() {
            announce("La digitación automática");
        }

        @Override
        public void letRingOptions() {
            announce("Las opciones de let ring");
        }

        @Override
        public void palmMuteOptions() {
            announce("Las opciones de palm mute");
        }

        @Override
        public void dynamicOptions() {
            announce("Las opciones de dinámica");
        }

        @Override
        public void pasteOptions() {
            announce("Las opciones de pegado");
        }

        @Override
        public void about() {
            JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "tabpro — clon libre de Guitar Pro 5.",
                    "Acerca de tabpro",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        private void announce(String what) {
            PendingFeature.announce(MainFrame.this, what);
        }
    }
}
