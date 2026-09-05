package com.gstncaruso.tabpro.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        FlatDarculaLaf.setup();
        Editor editor = new Editor(Score.blank());
        SwingUtilities.invokeLater(() -> new MainFrame(editor).setVisible(true));
    }
}
