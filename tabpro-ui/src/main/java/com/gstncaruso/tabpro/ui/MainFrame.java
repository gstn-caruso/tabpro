package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.tab.TabCanvas;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public final class MainFrame extends JFrame {

    public MainFrame(Editor editor) {
        super("tabpro");
        setSize(1000, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        TabCanvas canvas = new TabCanvas(editor);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JLabel status = new JLabel(StatusText.describe(editor.cursor(), editor.currentBeat()));
        status.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        editor.addListener(() -> status.setText(StatusText.describe(editor.cursor(), editor.currentBeat())));

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                canvas.requestFocusInWindow();
            }
        });
    }
}
