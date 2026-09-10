package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

    @Test
    void unaEtiquetaDeFormularioSinMnemonicoEsUnHallazgo() {
        JPanel form = new JPanel();
        JTextField field = new JTextField();
        JLabel label = new JLabel("Título");
        label.setLabelFor(field);
        form.add(label);
        form.add(field);

        assertTrue(walker.walkForm(form).stream().anyMatch(v -> v.reason().equals("sin mnemónico")));
    }

    @Test
    void unaEtiquetaSinSetLabelForNoSeReporta() {
        JPanel form = new JPanel();
        form.add(new JLabel("Sólo un título de sección"));

        assertTrue(walker.walkForm(form).isEmpty());
    }

    @Test
    void unaEtiquetaConMnemonicoEnUnFormularioAnidadoNoTieneHallazgos() {
        JPanel outer = new JPanel();
        JPanel inner = new JPanel();
        JTextField field = new JTextField();
        JLabel label = new JLabel("Nombre");
        label.setLabelFor(field);
        label.setDisplayedMnemonic('N');
        inner.add(label);
        inner.add(field);
        outer.add(inner);

        assertTrue(walker.walkForm(outer).isEmpty());
    }

    private static final class Formulario extends JPanel implements MnemonicScope {
    }

    @Test
    void dosFormulariosAnidadosPuedenCompartirLetraSinQueSeaUnChoque() {
        Formulario exterior = new Formulario();
        JTextField artista = new JTextField();
        JLabel artistaLabel = new JLabel("Artista");
        artistaLabel.setLabelFor(artista);
        artistaLabel.setDisplayedMnemonic('A');
        exterior.add(artistaLabel);
        exterior.add(artista);

        Formulario interior = new Formulario();
        JTextField armadura = new JTextField();
        JLabel armaduraLabel = new JLabel("Armadura");
        armaduraLabel.setLabelFor(armadura);
        armaduraLabel.setDisplayedMnemonic('A');
        interior.add(armaduraLabel);
        interior.add(armadura);
        exterior.add(interior);

        assertTrue(walker.walkForm(exterior).isEmpty());
    }
}
