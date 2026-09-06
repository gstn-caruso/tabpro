package com.gstncaruso.tabpro.ui.dialogs.ascii;

import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.awt.print.PrinterException;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * La ventana de "ASCII Export" del manual: vista previa de la pista activa, cuantas columnas
 * entran en cada renglon, y los botones Imprimir y Exportar.
 */
public final class AsciiExportDialog {

    private AsciiExportDialog() {
    }

    public static void show(Component parent, ScoreExchange exchange, Track activeTrack) {
        AsciiExportPanel panel = new AsciiExportPanel();
        Runnable refreshPreview = () -> panel.showPreview(exchange.previewAscii(activeTrack, panel.columnsPerLine()));
        panel.onColumnsChanged(refreshPreview);
        refreshPreview.run();

        panel.printButton().addActionListener(event -> print(parent, panel.previewText()));
        panel.exportButton().addActionListener(event -> export(parent, exchange, activeTrack, panel.columnsPerLine()));

        DialogShell.show(parent, "Exportar tablatura ASCII", panel);
    }

    private static void export(Component parent, ScoreExchange exchange, Track track, int columnsPerLine) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Tablatura ASCII (*.tab)", "tab"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            exchange.exportAscii(track, withTabExtension(chooser.getSelectedFile().toPath()), columnsPerLine);
        } catch (ScoreFileException e) {
            showError(parent, e.getMessage());
        }
    }

    private static Path withTabExtension(Path path) {
        String name = path.getFileName().toString();
        return name.toLowerCase(Locale.ROOT).endsWith(".tab") ? path : path.resolveSibling(name + ".tab");
    }

    private static void print(Component parent, String text) {
        try {
            AsciiPrinting.print(text, "tabpro");
        } catch (PrinterException e) {
            showError(parent, "No se pudo imprimir: " + e.getMessage());
        }
    }

    private static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "tabpro", JOptionPane.ERROR_MESSAGE);
    }
}
