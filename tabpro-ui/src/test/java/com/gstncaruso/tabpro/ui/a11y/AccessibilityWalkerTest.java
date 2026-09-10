package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
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
    void unaEtiquetaVinculadaConSetLabelForCuentaComoTextoVisibleParaUnBotonSinTexto() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Color");
        JButton boton = new JButton();
        boton.setText(null);
        etiqueta.setLabelFor(boton);
        panel.add(etiqueta);
        panel.add(boton);

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
    void losBotonesDeFlechaDeUnScrollPaneNoSonControlesDeLaAplicacion() {
        JPanel panel = new JPanel();
        panel.add(new JScrollPane(new JTextArea()));

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unJTabbedPaneConTitulosDeSolapaNoNecesitaTooltip() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bend", new JPanel());
        tabs.addTab("Armonicos", new JPanel());

        assertTrue(walker.walk(tabs).isEmpty());
    }

    @Test
    void unJPanelPlanoNoEsUnControlInteractivo() {
        JPanel panel = new JPanel();
        panel.add(new JPanel());

        assertTrue(walker.walk(panel).isEmpty());
    }

    private enum Figura { NEGRA, CORCHEA }

    private record Escala(String nombre) {
    }

    @Test
    void unComboConRenderPorDefectoQueMuestraElNombreCrudoDeUnEnumEsUnaViolacion() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Figura");
        JComboBox<Figura> combo = new JComboBox<>(new Figura[] {Figura.NEGRA});
        etiqueta.setLabelFor(combo);
        panel.add(etiqueta);
        panel.add(combo);

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(1, violaciones.size());
        assertEquals("toString() crudo: NEGRA", violaciones.get(0).reason());
    }

    @Test
    void unComboConRenderPorDefectoQueMuestraElToStringCrudoDeUnRecordEsUnaViolacion() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Escala");
        JComboBox<Escala> combo = new JComboBox<>(new Escala[] {new Escala("Mayor")});
        etiqueta.setLabelFor(combo);
        panel.add(etiqueta);
        panel.add(combo);

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(1, violaciones.size());
        assertTrue(violaciones.get(0).reason().startsWith("toString() crudo: Escala["));
    }

    private record FormatoDePapel(String etiqueta, int ancho, int alto) {

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    @Test
    void unComboQueMuestraElToStringPersonalizadoDeUnRecordNoEsUnaViolacion() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Formato");
        JComboBox<FormatoDePapel> combo =
                new JComboBox<>(new FormatoDePapel[] {new FormatoDePapel("A4", 210, 297)});
        etiqueta.setLabelFor(combo);
        panel.add(etiqueta);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    private enum Dinamica {
        FORTE;

        @Override
        public String toString() {
            return "f";
        }
    }

    @Test
    void unComboQueMuestraElToStringPersonalizadoDeUnEnumNoEsUnaViolacion() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Dinamica");
        JComboBox<Dinamica> combo = new JComboBox<>(new Dinamica[] {Dinamica.FORTE});
        etiqueta.setLabelFor(combo);
        panel.add(etiqueta);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unComboConRenderPropioNoEsUnaViolacionAunqueElTextoCoincidaConElToString() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Figura");
        JComboBox<Figura> combo = new JComboBox<>(Figura.values());
        etiqueta.setLabelFor(combo);
        combo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                setText(String.valueOf(value));
                return this;
            }
        });
        panel.add(etiqueta);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void unaListaConRenderPorDefectoQueMuestraElNombreCrudoDeUnEnumEsUnaViolacion() {
        JPanel panel = new JPanel();
        JLabel etiqueta = new JLabel("Figura");
        JList<Figura> lista = new JList<>(new Figura[] {Figura.NEGRA});
        etiqueta.setLabelFor(lista);
        panel.add(etiqueta);
        panel.add(lista);

        List<Violation> violaciones = walker.walk(panel);

        assertEquals(1, violaciones.size());
        assertEquals("toString() crudo: NEGRA", violaciones.get(0).reason());
    }
}
