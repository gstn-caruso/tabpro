package com.gstncaruso.tabpro.app;

import java.util.function.Consumer;

/** Supplies the initial appearance and subsequent {@link Theme#LIGHT}/{@link Theme#DARK} changes. */
public interface SystemAppearance {
    AutoCloseable observe(Consumer<String> listener);
}
