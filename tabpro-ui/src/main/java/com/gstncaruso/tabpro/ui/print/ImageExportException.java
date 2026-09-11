package com.gstncaruso.tabpro.ui.print;

import java.util.List;
import java.util.Locale;

public class ImageExportException extends RuntimeException {

    private final ImageExportProblem problem;
    private final List<Object> arguments;

    private ImageExportException(ImageExportProblem problem, List<Object> arguments, String detail) {
        super(detail);
        this.problem = problem;
        this.arguments = arguments;
    }

    public static ImageExportException bmpOnlyInPageMode() {
        return new ImageExportException(
                ImageExportProblem.BMP_ONLY_IN_PAGE_MODE, List.of(), "BMP export is only available in Page mode");
    }

    public static ImageExportException noImageWriterFor(String format) {
        String upperCaseFormat = format.toUpperCase(Locale.ROOT);
        return new ImageExportException(
                ImageExportProblem.NO_IMAGE_WRITER, List.of(upperCaseFormat),
                "no installed image writer can encode " + upperCaseFormat);
    }

    public ImageExportProblem problem() {
        return problem;
    }

    public List<Object> arguments() {
        return arguments;
    }
}
