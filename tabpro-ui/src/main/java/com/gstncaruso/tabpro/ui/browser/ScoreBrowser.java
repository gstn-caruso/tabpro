package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * La ventana del explorador de partituras. El manual: "it is possible to set the number of bars
 * to play before jumping to the next file" -escuchar no suena la partitura entera, sino esa
 * cantidad de compases y sigue solo con la siguiente de la lista, hasta que alguien para o se
 * acaba la lista-. La ventana solo muestra el {@link ScoreBrowserPanel}; el contenido se puede
 * armar y probar sin ella.
 */
public final class ScoreBrowser extends JDialog {

    private final ScoreBrowserPanel panel;

    public ScoreBrowser(Component parent, ScoreFiles files, Consumer<Path> onOpen, BrowserPlayback.Sound sound) {
        super(SwingUtilities.getWindowAncestor(parent), "Explorar partituras", ModalityType.APPLICATION_MODAL);
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
