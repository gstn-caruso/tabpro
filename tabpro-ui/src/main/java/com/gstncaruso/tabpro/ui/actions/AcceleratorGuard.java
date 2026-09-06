package com.gstncaruso.tabpro.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
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
 * <p>Este barrido tapa, en cada antepasado dado, cualquier tecla que el catalogo ya use, para
 * que gane siempre el atajo del manual. No toca nada compartido entre instancias: cada
 * componente Swing tiene su propio InputMap y ActionMap, asi que un JScrollPane que no se le
 * pasa a {@link #letCommandsWin} sigue con el comportamiento de fabrica.
 */
public final class AcceleratorGuard {

    private static final Action NO_HACE_NADA = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent event) {
        }
    };

    private AcceleratorGuard() {
    }

    /** Le saca a cada antepasado dado cualquier tecla que ya use un comando del catalogo. */
    public static void letCommandsWin(Commands commands, JComponent... ancestorsOfTheFocusedComponent) {
        commands.all().values().stream()
                .map(Command::accelerator)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(accelerator -> block(accelerator, ancestorsOfTheFocusedComponent));
    }

    private static void block(KeyStroke accelerator, JComponent[] ancestors) {
        for (JComponent ancestor : ancestors) {
            InputMap inputMap = ancestor.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            if (inputMap.get(accelerator) == null) {
                continue;
            }
            String name = "tabpro.reservado." + accelerator;
            inputMap.put(accelerator, name);
            ancestor.getActionMap().put(name, NO_HACE_NADA);
        }
    }
}
