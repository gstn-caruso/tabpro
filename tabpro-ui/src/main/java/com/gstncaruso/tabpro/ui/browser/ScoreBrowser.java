package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/** El explorador de partituras: buscar en una carpeta, escuchar y abrir. */
public final class ScoreBrowser extends JDialog {

    private final ScoreFiles files;
    private final Consumer<Path> onOpen;
    private final Consumer<Score> onListen;
    private final DefaultListModel<Path> found = new DefaultListModel<>();
    private final JList<Path> results = new JList<>(found);
    private final JCheckBox includeSubfolders = new JCheckBox("Incluir subcarpetas", true);
    private final JLabel summary = new JLabel(" ");
    private Path folder;

    public ScoreBrowser(Component parent, ScoreFiles files, Consumer<Path> onOpen, Consumer<Score> onListen) {
        super(SwingUtilities.getWindowAncestor(parent), "Explorar partituras", ModalityType.APPLICATION_MODAL);
        this.files = files;
        this.onOpen = onOpen;
        this.onListen = onListen;

        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setCellRenderer(new PathRenderer());
        results.addListSelectionListener(event -> describeSelection());

        setLayout(new BorderLayout(8, 8));
        add(topBar(), BorderLayout.NORTH);
        add(new JScrollPane(results), BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setSize(new Dimension(620, 460));
        setLocationRelativeTo(parent);
    }

    public void searchIn(Path folder) {
        this.folder = folder;
        refresh();
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
        JButton listen = new JButton("Escuchar");
        listen.addActionListener(event -> selected().ifPresent(this::listenTo));
        JButton open = new JButton("Abrir");
        open.addActionListener(event -> selected().ifPresent(path -> {
            onOpen.accept(path);
            dispose();
        }));
        JButton close = new JButton("Cerrar");
        close.addActionListener(event -> dispose());
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

    private void listenTo(Path path) {
        try {
            onListen.accept(files.load(path));
        } catch (ScoreFileException e) {
            summary.setText("No se pudo abrir: " + e.getMessage());
        }
    }

    private Optional<Path> selected() {
        return Optional.ofNullable(results.getSelectedValue());
    }

    /** En la lista se lee el nombre del archivo; la ruta completa va abajo. */
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
