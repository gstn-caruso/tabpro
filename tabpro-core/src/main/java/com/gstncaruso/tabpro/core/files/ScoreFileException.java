package com.gstncaruso.tabpro.core.files;

import java.nio.file.Path;
import java.util.List;

public class ScoreFileException extends RuntimeException {

    private final ScoreFileProblem problem;
    private final List<Object> arguments;

    public ScoreFileException(String message) {
        super(message);
        this.problem = null;
        this.arguments = List.of();
    }

    public ScoreFileException(String message, Throwable cause) {
        super(message, cause);
        this.problem = null;
        this.arguments = List.of();
    }

    private ScoreFileException(ScoreFileProblem problem, List<Object> arguments, String detail) {
        super(detail);
        this.problem = problem;
        this.arguments = arguments;
    }

    private ScoreFileException(ScoreFileProblem problem, List<Object> arguments, String detail, Throwable cause) {
        super(detail, cause);
        this.problem = problem;
        this.arguments = arguments;
    }

    public static ScoreFileException cannotRead(Path path, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.CANNOT_READ, List.of(path), "could not read " + path, cause);
    }

    public static ScoreFileException cannotWrite(Path path, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.CANNOT_WRITE, List.of(path), "could not write " + path, cause);
    }

    public static ScoreFileException cannotExport(Path path, Throwable cause) {
        return new ScoreFileException(
                ScoreFileProblem.CANNOT_EXPORT, List.of(path), "could not export " + path, cause);
    }

    public static ScoreFileException cannotExport(Path path, String detail) {
        return new ScoreFileException(
                ScoreFileProblem.CANNOT_EXPORT, List.of(path), "could not export " + path + ": " + detail);
    }

    public static ScoreFileException notSupported(ScoreOperation operation) {
        return new ScoreFileException(
                ScoreFileProblem.NOT_SUPPORTED, List.of(operation), "not supported yet: " + operation);
    }

    public static ScoreFileException notRecognized(String format, String detail) {
        return new ScoreFileException(ScoreFileProblem.NOT_RECOGNIZED, List.of(format), detail);
    }

    public static ScoreFileException notRecognized(String format, String detail, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.NOT_RECOGNIZED, List.of(format), detail, cause);
    }

    public static ScoreFileException unsupportedVersion(String format, String version) {
        return new ScoreFileException(
                ScoreFileProblem.UNSUPPORTED_VERSION, List.of(format, version),
                "unsupported " + format + " version: " + version);
    }

    public static ScoreFileException unsupportedContent(ScoreFeature feature, String detail) {
        return new ScoreFileException(ScoreFileProblem.UNSUPPORTED_CONTENT, List.of(feature), detail);
    }

    public static ScoreFileException nothingToImport(String detail) {
        return new ScoreFileException(ScoreFileProblem.NOTHING_TO_IMPORT, List.of(), detail);
    }

    public static ScoreFileException damaged(String detail) {
        return new ScoreFileException(ScoreFileProblem.DAMAGED, List.of(), detail);
    }

    public static ScoreFileException damaged(String detail, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.DAMAGED, List.of(), detail, cause);
    }

    public ScoreFileProblem problem() {
        return problem;
    }

    public List<Object> arguments() {
        return arguments;
    }
}
