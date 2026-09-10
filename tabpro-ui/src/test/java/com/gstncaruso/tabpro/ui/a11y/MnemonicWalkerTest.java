package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JMenuBar;
import org.junit.jupiter.api.Test;

class MnemonicWalkerTest {

    private final MnemonicWalker walker = new MnemonicWalker();

    @Test
    void unaBarraSinMenusNoTieneHallazgos() {
        assertTrue(walker.walkMenuBar(new JMenuBar()).isEmpty());
    }
}
