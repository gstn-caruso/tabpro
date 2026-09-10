package com.gstncaruso.tabpro.ui.actions;

import java.util.Objects;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/**
 * JScrollPane y JSplitPane traen atajos propios de fabrica (Ctrl+Home/Fin para el scroll, F6/F8
 * para navegar y redimensionar el split, Ctrl+Tab para cambiarle el foco): Swing los revisa
 * apenas encuentra, subiendo desde el componente enfocado, un antepasado que los tenga
 * registrados (WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) -- antes incluso de llegar al atajo de un
 * menu (WHEN_IN_FOCUSED_WINDOW). La partitura vive adentro de ambos, asi que si un atajo del
 * catalogo usa esa misma tecla, mientras la partitura tiene el foco -que es la situacion normal
 * al editar- ese atajo queda muerto sin que nada lo avise.
 *
 * <p>Este barrido, en cada antepasado dado, deja sin resolver cualquier tecla que el catalogo ya
 * use ({@code inputMap.put(tecla, "none")}, la convencion de Swing para "no hay accion registrada
 * con este nombre"): {@code processKeyBinding} encuentra un binding pero ninguna Action para el,
 * asi que devuelve false y la tecla sigue subiendo hasta el atajo del menu. No toca nada
 * compartido entre instancias: cada componente Swing tiene su propio InputMap y ActionMap, asi
 * que un JScrollPane que no se le pasa a {@link #letCommandsWin} sigue con el comportamiento de
 * fabrica.
 */
public final class AcceleratorGuard {

    private static final String SIN_ACCION_REGISTRADA = "none";

    private AcceleratorGuard() {
    }

    /** Le saca a cada antepasado dado cualquier tecla que ya use un comando del catalogo. */
    public static void letCommandsWin(Commands commands, JComponent... ancestorsOfTheFocusedComponent) {
        blockEachAccelerator(commands, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, ancestorsOfTheFocusedComponent);
    }

    /**
     * F10 sin modificador activa de fabrica la barra de menus (BasicMenuBarUI la instala en el
     * WHEN_IN_FOCUSED_WINDOW propio del JMenuBar, para cualquier L&amp;F derivado de
     * BasicLookAndFeel): le saca esa tecla a la barra para que el atajo real de Cambio de
     * parametros pueda seguir subiendo.
     */
    public static void letCommandsWinOverTheMenuBar(Commands commands, JMenuBar menuBar) {
        blockEachAccelerator(commands, JComponent.WHEN_IN_FOCUSED_WINDOW, menuBar);
    }

    private static void blockEachAccelerator(Commands commands, int condition, JComponent... components) {
        commands.all().values().stream()
                .map(Command::accelerator)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(accelerator -> block(accelerator, condition, components));
    }

    private static void block(KeyStroke accelerator, int condition, JComponent[] components) {
        for (JComponent component : components) {
            InputMap inputMap = component.getInputMap(condition);
            if (inputMap.get(accelerator) == null) {
                continue;
            }
            inputMap.put(accelerator, SIN_ACCION_REGISTRADA);
        }
    }
}
