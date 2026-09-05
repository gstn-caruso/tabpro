package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainFrame extends JFrame {

    private final ScoreDocument document;
    private final Editor editor;

    public MainFrame(Editor editor, ScoreFiles files, Player player) {
        super("tabpro");
        this.editor = editor;
        this.document = new ScoreDocument(editor, files);
        setSize(1000, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ScoreCanvas canvas = new ScoreCanvas(editor);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ScoreColors.BACKGROUND);

        Transport transport = new Transport(editor, player, SwingUtilities::invokeLater);
        Runnable togglePlayback = () -> {
            transport.toggle();
            canvas.requestFocusInWindow();
        };

        JButton playButton = new JButton("Reproducir");
        playButton.addActionListener(e -> togglePlayback.run());

        JSpinner tempoSpinner = new JSpinner(new SpinnerNumberModel(editor.score().tempo(), 20, 400, 1));
        tempoSpinner.addChangeListener(e -> editor.setTempo((Integer) tempoSpinner.getValue()));
        editor.addListener(() -> tempoSpinner.setValue(editor.score().tempo()));

        TrackPanel trackPanel = new TrackPanel(editor);

        transport.addListener(() -> {
            canvas.showPlayhead(transport.playhead());
            trackPanel.showPlayingMeasure(transport.playhead().measure());
            playButton.setText(transport.isPlaying() ? "Detener" : "Reproducir");
        });

        JLabel status = new JLabel(StatusText.describe(editor.cursor(), editor.currentBeat()));
        status.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        editor.addListener(() -> status.setText(StatusText.describe(editor.cursor(), editor.currentBeat())));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, trackPanel);
        split.setResizeWeight(1);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(6);
        split.setDividerLocation(getHeight() - trackPanel.preferredPanelHeight());

        setJMenuBar(menuBar(togglePlayback, trackPanel, canvas));
        setLayout(new BorderLayout());
        add(toolBar(playButton, tempoSpinner), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        updateTitle();

        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                canvas.requestFocusInWindow();
            }
        });
    }

    private JToolBar toolBar(JButton playButton, JSpinner tempoSpinner) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(playButton);
        toolBar.addSeparator();
        toolBar.add(new JLabel("Tempo "));
        tempoSpinner.setMaximumSize(tempoSpinner.getPreferredSize());
        toolBar.add(tempoSpinner);
        return toolBar;
    }

    private JMenuBar menuBar(Runnable togglePlayback, TrackPanel trackPanel, ScoreCanvas canvas) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu());
        menuBar.add(editMenu());
        menuBar.add(trackMenu(trackPanel, canvas));
        menuBar.add(playMenu(togglePlayback));
        return menuBar;
    }

    private JMenu trackMenu(TrackPanel trackPanel, ScoreCanvas canvas) {
        JMenu menu = new JMenu("Pista");
        menu.add(menuItem("Agregar guitarra", "ctrl shift G", backToTheScore(trackPanel::addGuitar, canvas)));
        menu.add(menuItem("Agregar bajo", "ctrl shift B", backToTheScore(trackPanel::addBass, canvas)));
        menu.addSeparator();
        menu.add(menuItem("Renombrar…", null, backToTheScore(trackPanel::renameSelectedTrack, canvas)));
        menu.add(menuItem("Quitar pista", null, backToTheScore(trackPanel::removeSelectedTrack, canvas)));
        return menu;
    }

    private Runnable backToTheScore(Runnable action, ScoreCanvas canvas) {
        return () -> {
            action.run();
            canvas.requestFocusInWindow();
        };
    }

    private JMenu playMenu(Runnable togglePlayback) {
        JMenu menu = new JMenu("Reproducir");
        menu.add(menuItem("Reproducir / Detener", "SPACE", togglePlayback));
        return menu;
    }

    private JMenu fileMenu() {
        JMenu menu = new JMenu("Archivo");
        menu.add(menuItem("Nuevo", "ctrl N", this::newScore));
        menu.add(menuItem("Abrir…", "ctrl O", this::open));
        menu.add(menuItem("Guardar", "ctrl S", this::save));
        menu.add(menuItem("Guardar como…", null, this::saveAs));
        menu.addSeparator();
        menu.add(menuItem("Salir", null, this::dispose));
        return menu;
    }

    private JMenu editMenu() {
        JMenu menu = new JMenu("Editar");
        menu.add(menuItem("Deshacer", "ctrl Z", editor::undo));
        menu.add(menuItem("Rehacer", "ctrl Y", editor::redo));
        return menu;
    }

    private JMenuItem menuItem(String label, String accelerator, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        item.addActionListener(e -> action.run());
        return item;
    }

    private void newScore() {
        document.newScore();
        updateTitle();
    }

    private void open() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(tabproFilter());
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            document.open(chooser.getSelectedFile().toPath());
            updateTitle();
        } catch (ScoreFileException e) {
            showError(e);
        }
    }

    private void save() {
        try {
            if (!document.save()) {
                saveAs();
            }
        } catch (ScoreFileException e) {
            showError(e);
        }
    }

    private void saveAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(tabproFilter());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            document.saveAs(withTabproExtension(chooser.getSelectedFile()));
            updateTitle();
        } catch (ScoreFileException e) {
            showError(e);
        }
    }

    private Path withTabproExtension(File file) {
        String name = file.getName();
        if (name.endsWith(".tabpro")) {
            return file.toPath();
        }
        return file.toPath().resolveSibling(name + ".tabpro");
    }

    private FileNameExtensionFilter tabproFilter() {
        return new FileNameExtensionFilter("Partituras tabpro (*.tabpro)", "tabpro");
    }

    private void showError(ScoreFileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "tabpro", JOptionPane.ERROR_MESSAGE);
    }

    private void updateTitle() {
        setTitle("tabpro — " + document.displayName());
    }
}
