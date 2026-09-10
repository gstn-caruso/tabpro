package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Test;

class MnemonicWalkerTest {

    private final MnemonicWalker walker = new MnemonicWalker();

    @Test
    void unaBarraSinMenusNoTieneHallazgos() {
        assertTrue(walker.walkMenuBar(new JMenuBar()).isEmpty());
    }

    @Test
    void unMenuSinMnemonicoEsUnHallazgo() {
        JMenuBar bar = new JMenuBar();
        bar.add(new JMenu("Archivo"));

        assertTrue(walker.walkMenuBar(bar).stream().anyMatch(v -> v.reason().equals("sin mnemónico")));
    }

    @Test
    void dosMenusConMnemonicosDistintosNoTienenHallazgos() {
        JMenuBar bar = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        archivo.setMnemonic('A');
        JMenu editar = new JMenu("Editar");
        editar.setMnemonic('E');
        bar.add(archivo);
        bar.add(editar);

        assertTrue(walker.walkMenuBar(bar).isEmpty());
    }

    @Test
    void dosMenusConElMismoMnemonicoChocanEntreSi() {
        JMenuBar bar = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        archivo.setMnemonic('A');
        JMenu ayuda = new JMenu("Ayuda");
        ayuda.setMnemonic('A');
        bar.add(archivo);
        bar.add(ayuda);

        List<Violation> violaciones = walker.walkMenuBar(bar);

        assertEquals(2, violaciones.stream().filter(v -> v.reason().equals("mnemónico repetido")).count());
    }

    @Test
    void unItemDeMenuSinMnemonicoEsUnHallazgo() {
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem("Nuevo"));

        assertTrue(walker.walkMenu(menu).stream().anyMatch(v -> v.reason().equals("sin mnemónico")));
    }
}
