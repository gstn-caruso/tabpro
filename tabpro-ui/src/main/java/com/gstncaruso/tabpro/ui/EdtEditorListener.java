package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.EditorListener;

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
        delegate.editorChanged();
    }
}
