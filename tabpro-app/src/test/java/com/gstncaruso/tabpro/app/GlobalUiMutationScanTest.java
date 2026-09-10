package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Swing tiene un solo UIManager por maquina virtual y la suite corre las clases de test en
 * paralelo dentro de una sola maquina virtual (ver tabpro-tests/pom.xml): un test que instala un
 * tema o muta el look-and-feel global (Theme.install/apply, useFontSize, useHighContrast,
 * FlatLaf.setup o updateUI, UIManager.put o setLookAndFeel) puede correr al mismo tiempo que otro
 * test que mide componentes reales contra ese mismo UIManager. Toda clase de test que toque ese
 * estado global tiene que anotarse {@code @Isolated}, o el resultado depende de con que otro test
 * le toco correr a la vez.
 */
class GlobalUiMutationScanTest {

    @Test
    void anEmptySourceTreeHasNoUnisolatedMutators(@TempDir Path root) {
        assertTrue(GlobalUiMutationScan.unisolatedMutators(root).isEmpty());
    }
}
