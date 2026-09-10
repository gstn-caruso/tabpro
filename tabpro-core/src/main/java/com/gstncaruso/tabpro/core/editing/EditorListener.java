package com.gstncaruso.tabpro.core.editing;

@FunctionalInterface
public interface EditorListener {
    void editorChanged();

    default void editorChanged(EditorChange change) {
        editorChanged();
    }
}
