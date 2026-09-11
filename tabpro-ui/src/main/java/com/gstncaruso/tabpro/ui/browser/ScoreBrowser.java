package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public final class ScoreBrowser extends JDialog {

    private final ScoreBrowserPanel panel;

    public ScoreBrowser(Component parent, ScoreFiles files, Consumer<Path> onOpen, BrowserPlayback.Sound sound) {
        super(SwingUtilities.getWindowAncestor(parent), Texts.get("views.ScoreBrowser.title"), ModalityType.APPLICATION_MODAL);
        this.panel = new ScoreBrowserPanel(files, onOpen, sound, this::dispose);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        setSize(new Dimension(620, 460));
        setLocationRelativeTo(parent);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                panel.stopListening();
            }
        });
    }

    public void searchIn(Path folder) {
        panel.searchIn(folder);
    }
}
