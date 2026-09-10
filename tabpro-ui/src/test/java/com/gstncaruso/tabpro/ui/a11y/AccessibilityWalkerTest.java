package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/**
 * El recorredor de accesibilidad: encuentra controles interactivos sin nombre accesible o,
 * si no tienen texto visible, sin tooltip. Es la red que hace fallar el build cuando aparece
 * un control nuevo sin nombrar.
 */
class AccessibilityWalkerTest {

    private final AccessibilityWalker walker = new AccessibilityWalker();

    @Test
    void unPanelVacioNoTieneViolaciones() {
        assertTrue(walker.walk(new JPanel()).isEmpty());
    }

    @Test
    void unBotonSinNombreNiTextoEsUnaViolacionDeNombre() {
        JPanel panel = new JPanel();
        JButton boton = new JButton();
        panel.add(boton);

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(1, violaciones.stream().filter(v -> v.reason().equals("sin nombre accesible")).count());
    }

    @Test
    void unBotonConTextoVisibleTraeSuPropioNombreAccesible() {
        JPanel panel = new JPanel();
        JButton boton = new JButton("Guardar");
        panel.add(boton);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unBotonSoloIconoConNombreYTooltipNoTieneViolaciones() {
        JPanel panel = new JPanel();
        JButton boton = new JButton();
        boton.setText(null);
        boton.getAccessibleContext().setAccessibleName("Deshacer");
        boton.setToolTipText("Deshacer  [Ctrl+Z]");
        panel.add(boton);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unBotonSoloIconoConNombrePeroSinTooltipEsUnaViolacionDeTooltip() {
        JPanel panel = new JPanel();
        JButton boton = new JButton();
        boton.setText(null);
        boton.getAccessibleContext().setAccessibleName("Deshacer");
        panel.add(boton);

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(1, violaciones.size());
        assertEquals("sin tooltip y sin texto visible", violaciones.get(0).reason());
    }

    @Test
    void unNombreAccesibleEnBlancoCuentaComoSinNombre() {
        JPanel panel = new JPanel();
        JButton boton = new JButton();
        boton.getAccessibleContext().setAccessibleName("   ");
        panel.add(boton);

        List<Violation> violaciones = walker.walk(panel);

        assertTrue(violaciones.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
    }

    @Test
    void laRutaDeLaViolacionIncluyeCadaContenedorIntermedio() {
        JPanel raiz = new JPanel();
        JPanel fila = new JPanel();
        JButton boton = new JButton();
        fila.add(boton);
        raiz.add(fila);

        List<Violation> violaciones = walker.walk(raiz);

        assertEquals("JPanel > JPanel > JButton", violaciones.get(0).path());
    }

    @Test
    void variosControlesSinNombreProducenVariasViolaciones() {
        JPanel panel = new JPanel();
        panel.add(new JButton());
        panel.add(new JButton());
        panel.add(new JButton("Con nombre"));

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(2, violaciones.stream().filter(v -> v.reason().equals("sin nombre accesible")).count());
    }

    @Test
    void unaEtiquetaVinculadaConSetLabelForCuentaComoTextoVisibleParaElCombo() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Tono:");
        JComboBox<String> combo = new JComboBox<>(new String[] {"Do"});
        etiqueta.setLabelFor(combo);
        panel.add(etiqueta);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unComboSinEtiquetaVinculadaEsUnaViolacionDeNombreYDeTooltip() {
        JPanel panel = new JPanel();
        panel.add(new JComboBox<String>(new String[] {"Do"}));

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(2, violaciones.size());
    }

    @Test
    void unComponenteCustomMarcadoSinNombreProduceViolacionDeNombreYDeTooltip() {
        class Perilla extends JPanel implements AccessibleControl {}
        JPanel panel = new JPanel();
        panel.add(new Perilla());

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(2, violaciones.size());
        assertTrue(violaciones.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
        assertTrue(violaciones.stream().anyMatch(v -> v.reason().equals("sin tooltip y sin texto visible")));
    }

    @Test
    void unComponenteCustomMarcadoConNombreYTooltipNoTieneViolaciones() {
        class Perilla extends JPanel implements AccessibleControl {}
        Perilla perilla = new Perilla();
        perilla.getAccessibleContext().setAccessibleName("Volumen de Guitarra");
        perilla.setToolTipText("Volumen de Guitarra");
        JPanel panel = new JPanel();
        panel.add(perilla);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unItemDeMenuSinTextoDentroDeUnJMenuEsUnaViolacion() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem());
        bar.add(menu);

        List<Violation> violaciones = walker.walk(bar);

        assertTrue(violaciones.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
    }

    @Test
    void unItemDeMenuConTextoDentroDeUnJMenuNoTieneViolaciones() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem("Nuevo"));
        bar.add(menu);

        assertTrue(walker.walk(bar).isEmpty());
    }

    @Test
    void unJPanelPlanoNoEsUnControlInteractivo() {
        JPanel panel = new JPanel();
        panel.add(new JPanel());

        assertTrue(walker.walk(panel).isEmpty());
    }
}
