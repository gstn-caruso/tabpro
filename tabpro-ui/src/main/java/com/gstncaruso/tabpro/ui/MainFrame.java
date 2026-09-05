package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.ui.tab.TabCanvas;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainFrame extends JFrame {

    private final ScoreDocument document;
    private final Editor editor;

    public MainFrame(Editor editor, ScoreFiles files) {
        super("tabpro");
        this.editor = editor;
        this.document = new ScoreDocument(editor, files);
        setSize(1000, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        TabCanvas canvas = new TabCanvas(editor);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JLabel status = new JLabel(StatusText.describe(editor.cursor(), editor.currentBeat()));
        status.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        editor.addListener(() -> status.setText(StatusText.describe(editor.cursor(), editor.currentBeat())));

        setJMenuBar(menuBar());
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
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

    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu());
        menuBar.add(editMenu());
        return menuBar;
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
