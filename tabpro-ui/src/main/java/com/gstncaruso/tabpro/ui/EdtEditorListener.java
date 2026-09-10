package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.EditorChange;
import com.gstncaruso.tabpro.core.editing.EditorListener;
import javax.swing.SwingUtilities;

public final class EdtEditorListener implements EditorListener {

    private final EditorListener delegate;

    private EdtEditorListener(EditorListener delegate) {
        this.delegate = delegate;
    }

    public static EdtEditorListener onEdt(EditorListener delegate) {
        return new EdtEditorListener(delegate);
    }

    @Override
    public void editorChanged() {
        if (SwingUtilities.isEventDispatchThread()) {
            delegate.editorChanged();
        } else {
            SwingUtilities.invokeLater(delegate::editorChanged);
        }
    }

    @Override
    public void editorChanged(EditorChange change) {
        if (SwingUtilities.isEventDispatchThread()) {
            delegate.editorChanged(change);
        } else {
            SwingUtilities.invokeLater(() -> delegate.editorChanged(change));
        }
    }
}
