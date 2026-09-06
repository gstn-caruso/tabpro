package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** El archivo abierto: donde vive, si tiene cambios sin guardar y como se recupera. */
public final class ScoreDocument {

    public static final String UNTITLED = "Sin título";
    public static final String EXTENSION = ".tabpro";

    private final Editor editor;
    private final ScoreFiles files;
    private final Preferences preferences;
    private Path path;
    private Score saved;
    private int changesSinceLastAutosave;

    public ScoreDocument(Editor editor, ScoreFiles files) {
        this(editor, files, new Preferences());
    }

    public ScoreDocument(Editor editor, ScoreFiles files, Preferences preferences) {
        this.editor = editor;
        this.files = files;
        this.preferences = preferences;
        this.saved = editor.score();
        editor.addListener(this::scoreChanged);
    }

    public Optional<Path> path() {
        return Optional.ofNullable(path);
    }

    /** Los archivos que se abrieron o guardaron hace poco, para ofrecerlos en el menu Archivo. */
    public java.util.List<Path> recentFiles() {
        return preferences.recentFiles();
    }

    public String displayName() {
        return path == null ? UNTITLED : path.getFileName().toString();
    }

    /** El titulo de la ventana: el archivo, un asterisco si hay cambios, y la partitura. */
    public String windowTitle() {
        String heading = editor.score().info().heading();
        return displayName() + (hasUnsavedChanges() ? " *" : "") + " — " + heading + " — tabpro";
    }

    public boolean hasUnsavedChanges() {
        return !editor.score().equals(saved);
    }

    public boolean save() {
        if (path == null) {
            return false;
        }
        files.save(editor.score(), path);
        markSaved();
        return true;
    }

    public void saveAs(Path path) {
        files.save(editor.score(), path);
        this.path = path;
        preferences.remember(path);
        markSaved();
    }

    public void open(Path path) {
        editor.replaceScore(files.load(path));
        this.path = path;
        preferences.remember(path);
        markSaved();
    }

    /** Al importar, la partitura entra sin archivo propio: hay que guardarla como .tabpro. */
    public void adopt(Score imported) {
        editor.replaceScore(imported);
        this.path = null;
        this.saved = null;
    }

    public void newScore() {
        newScore(Score.blank());
    }

    /** Archivo > Nuevo con una plantilla ya armada, como la que trae "Propiedades por defecto". */
    public void newScore(Score blank) {
        editor.replaceScore(blank);
        path = null;
        markSaved();
    }

    /** El archivo temporal con el que se recupera la partitura si el programa se corta. */
    public Path recoveryFile() {
        return Path.of(System.getProperty("java.io.tmpdir"), "tabpro-recuperacion" + EXTENSION);
    }

    public Optional<Path> pendingRecovery() {
        Path recovery = recoveryFile();
        return Files.exists(recovery) ? Optional.of(recovery) : Optional.empty();
    }

    public void discardRecovery() {
        try {
            Files.deleteIfExists(recoveryFile());
        } catch (IOException ignored) {
            // Si no se puede borrar, la proxima recuperacion simplemente lo pisa.
        }
    }

    private void scoreChanged() {
        int every = preferences.autosaveEvery();
        if (every <= 0 || !hasUnsavedChanges()) {
            return;
        }
        changesSinceLastAutosave++;
        if (changesSinceLastAutosave >= every) {
            changesSinceLastAutosave = 0;
            files.save(editor.score(), recoveryFile());
        }
    }

    private void markSaved() {
        saved = editor.score();
        changesSinceLastAutosave = 0;
        discardRecovery();
    }
}
