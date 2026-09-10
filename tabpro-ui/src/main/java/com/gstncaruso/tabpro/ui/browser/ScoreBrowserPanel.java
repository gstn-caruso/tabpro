package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

public final class ScoreBrowserPanel extends JPanel {

    private static final int DEFAULT_BARS_BEFORE_JUMPING = 8;

    private final Consumer<Path> onOpen;
    private final Runnable onClose;
    private final BrowserPlayback playback;
    private final DefaultListModel<Path> found = new DefaultListModel<>();
    private final JList<Path> results = new JList<>(found);
    private final JCheckBox includeSubfolders = new JCheckBox("Incluir subcarpetas", true);
    private final JSpinner barsBeforeJumping =
            new JSpinner(new SpinnerNumberModel(DEFAULT_BARS_BEFORE_JUMPING, 1, 999, 1));
    private final JButton listen = new JButton("Escuchar");
    private final JLabel summary = new JLabel(" ");
    private Path folder;
    private boolean listening;

    public ScoreBrowserPanel(ScoreFiles files, Consumer<Path> onOpen, BrowserPlayback.Sound sound, Runnable onClose) {
        super(new BorderLayout(8, 8));
        this.onOpen = onOpen;
        this.onClose = onClose;
        this.playback = new BrowserPlayback(files, sound, new ChainListener());

        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setCellRenderer(new PathRenderer());
        results.getAccessibleContext().setAccessibleName("Partituras encontradas");
        results.setToolTipText("Partituras encontradas");
        results.addListSelectionListener(event -> describeSelection());

        add(topBar(), BorderLayout.NORTH);
        add(new JScrollPane(results), BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void searchIn(Path folder) {
        this.folder = folder;
        refresh();
    }

    public void stopListening() {
        if (!listening) {
            return;
        }
        listening = false;
        listen.setText("Escuchar");
        playback.stop();
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton chooseFolder = new JButton("Elegir carpeta…");
        chooseFolder.addActionListener(event -> chooseFolder());
        bar.add(chooseFolder);
        bar.add(includeSubfolders);
        includeSubfolders.addActionListener(event -> refresh());
        return bar;
    }

    private JPanel bottomBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.add(summary, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JLabel barsLabel = new JLabel("Compases antes de saltar:");
        barsLabel.setLabelFor(barsBeforeJumping);
        buttons.add(barsLabel);
        buttons.add(barsBeforeJumping);
        listen.addActionListener(event -> toggleListening());
        JButton open = new JButton("Abrir");
        open.addActionListener(event -> selected().ifPresent(path -> {
            stopListening();
            onOpen.accept(path);
            onClose.run();
        }));
        JButton close = new JButton("Cerrar");
        close.addActionListener(event -> {
            stopListening();
            onClose.run();
        });
        buttons.add(listen);
        buttons.add(open);
        buttons.add(close);
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser(folder == null ? null : folder.toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            searchIn(chooser.getSelectedFile().toPath());
        }
    }

    private void refresh() {
        found.clear();
        if (folder == null) {
            return;
        }
        List<Path> paths = includeSubfolders.isSelected()
                ? ScoreSearch.inFolderAndBelow(folder)
                : ScoreSearch.inFolder(folder);
        paths.forEach(found::addElement);
        summary.setText(paths.size() + " partituras en " + folder);
    }

    private void describeSelection() {
        selected().ifPresent(path -> summary.setText(path.toString()));
    }

    private void toggleListening() {
        if (listening) {
            stopListening();
            return;
        }
        selected().ifPresent(this::startListening);
    }

    private void startListening(Path path) {
        listening = true;
        listen.setText("Parar");
        playback.play(allResults(), path, (Integer) barsBeforeJumping.getValue());
    }

    private List<Path> allResults() {
        return java.util.Collections.list(found.elements());
    }

    private Optional<Path> selected() {
        return Optional.ofNullable(results.getSelectedValue());
    }

    private final class ChainListener implements BrowserPlayback.Listener {

        @Override
        public void advancedTo(Path path) {
            results.setSelectedValue(path, true);
            summary.setText(path.toString());
        }

        @Override
        public void loadFailed(Path path) {
            summary.setText("No se pudo abrir: " + path);
        }

        @Override
        public void chainEnded() {
            listening = false;
            listen.setText("Escuchar");
        }
    }

    private static final class PathRenderer extends javax.swing.DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, value, index, selected, focused);
            if (value instanceof Path path) {
                setText(path.getFileName().toString());
            }
            return this;
        }
    }
}
