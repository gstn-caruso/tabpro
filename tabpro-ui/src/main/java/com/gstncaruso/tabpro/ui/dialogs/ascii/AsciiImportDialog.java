package com.gstncaruso.tabpro.ui.dialogs.ascii;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * La ventana de "ASCII Import" del manual: pegar o escribir la tablatura, corregirla si hace
 * falta, y traerla sobre la pista activa. Tambien deja abrir un archivo de texto o imprimir lo
 * que hay en la zona de texto antes de importar.
 */
public final class AsciiImportDialog {

    private AsciiImportDialog() {
    }

    public static void show(Component parent, Editor editor, ScoreExchange exchange) {
        AsciiImportPanel panel = new AsciiImportPanel();
        panel.openButton().addActionListener(event -> openFile(parent, panel));
        panel.printButton().addActionListener(event -> print(parent, panel.text()));

        boolean accepted = DialogShell.ask(parent, "Importar tablatura ASCII", panel, "Importar");
        if (!accepted) {
            return;
        }
        try {
            int trackIndex = editor.cursor().track();
            String text = panel.text();
            Optional<NoteValue> fixedRhythm = panel.fixedRhythm();
            int intervalsPerQuarterNote = panel.intervalsPerQuarterNote();
            editor.apply(score -> score.mappingTrack(
                    trackIndex, track -> exchange.importAsciiInto(track, text, fixedRhythm, intervalsPerQuarterNote)));
        } catch (ScoreFileException e) {
            showError(parent, e.getMessage());
        }
    }

    private static void openFile(Component parent, AsciiImportPanel panel) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            panel.setText(java.nio.file.Files.readString(chooser.getSelectedFile().toPath()));
        } catch (IOException e) {
            showError(parent, "No se pudo leer el archivo: " + e.getMessage());
        }
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
