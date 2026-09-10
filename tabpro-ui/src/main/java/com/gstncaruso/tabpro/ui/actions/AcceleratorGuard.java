package com.gstncaruso.tabpro.ui.actions;

import java.util.Objects;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/**
 * JScrollPane and JSplitPane ship with their own shortcuts (Ctrl+Home/End for scrolling, F6/F8
 * to navigate and resize the split, Ctrl+Tab to move its focus): Swing checks them as soon as it
 * finds, walking up from the focused component, an ancestor that has them registered
 * (WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) -- even before reaching a menu shortcut
 * (WHEN_IN_FOCUSED_WINDOW). The score lives inside both, so if a catalog shortcut uses that same
 * key, while the score holds focus -the normal situation while editing- that shortcut stays dead
 * without anything reporting it.
 *
 * <p>This sweep, on each given ancestor, leaves unresolved any key the catalog already uses
 * ({@code inputMap.put(key, "none")}, Swing's convention for "no action registered under this
 * name"): {@code processKeyBinding} finds a binding but no Action for it, so it returns false and
 * the key keeps climbing up to the menu shortcut. It touches nothing shared between instances:
 * each Swing component has its own InputMap and ActionMap, so a JScrollPane not passed to
 * {@link #letCommandsWin} keeps the default behavior.
 */
public final class AcceleratorGuard {

    private static final String SIN_ACCION_REGISTRADA = "none";

    private AcceleratorGuard() {
    }

    public static void letCommandsWin(Commands commands, JComponent... ancestorsOfTheFocusedComponent) {
        blockEachAccelerator(commands, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, ancestorsOfTheFocusedComponent);
    }

    /**
     * F10 without a modifier activates the menu bar out of the box (BasicMenuBarUI installs it on
     * the JMenuBar's own WHEN_IN_FOCUSED_WINDOW, for any L&amp;F derived from BasicLookAndFeel):
     * this strips that key from the bar so the matching catalog shortcut can keep climbing.
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
