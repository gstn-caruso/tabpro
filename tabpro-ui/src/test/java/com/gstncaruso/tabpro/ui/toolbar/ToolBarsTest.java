package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class ToolBarsTest {

    private final Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final ToolBars toolBars = new ToolBars(editor, commands, new FakeZoomHolder());

    @Test
    void givenDoubleUiScale_whenBuildingToolbars_thenButtonsAndIconsRemainProportional() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object previousScale = UIManager.get("laf.scaleFactor");
            try {
                UIManager.put("laf.scaleFactor", 2f);
                Commands scaledCommands = new Commands(
                        editor, record(Ports.Document.class), record(Ports.Dialogs.class),
                        record(Ports.Playback.class), record(Ports.View.class));
                ToolBars scaledBars = new ToolBars(editor, scaledCommands, new FakeZoomHolder());
                List<AbstractButton> buttons = buttonsOf(scaledBars.component());

                // SVG, procedural drawing, musical glyph, and the sound-font toggle.
                assertAll(List.of("file.new", "bar.insert", "note.value.QUARTER", "sound.soundFont")
                        .stream().<Executable>map(name -> () -> {
                            AbstractButton button = buttons.stream()
                                    .filter(candidate -> candidate.getAction() == scaledCommands.get(name))
                                    .findFirst().orElseThrow();
                            assertAll(name,
                                    () -> assertEquals(new Dimension(52, 48), button.getPreferredSize(),
                                            "button surface follows the UI scale"),
                                    () -> assertEquals(new Dimension(52, 48), button.getMaximumSize(),
                                            "toolbar layout must not cap the scaled button"),
                                    () -> assertEquals(36, button.getIcon().getIconWidth(),
                                            "icon width follows the UI scale"),
                                    () -> assertEquals(36, button.getIcon().getIconHeight(),
                                            "icon height follows the UI scale"));
                        }));
            } finally {
                UIManager.put("laf.scaleFactor", previousScale);
            }
        });
    }

    @Test
    void theFourRowsStartVisible() {
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void hidingOneRowLeavesTheOtherThreeVisible() {
        toolBars.setStructureToolBarVisible(false);

        assertFalse(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void showingItAgainBringsItBack() {
        toolBars.setNotationToolBarVisible(false);
        toolBars.setNotationToolBarVisible(true);

        assertTrue(toolBars.isNotationToolBarVisible());
    }

    @Test
    void theStateOfEachRowIsIndependent() {
        toolBars.setDocumentToolBarVisible(false);
        toolBars.setStructureToolBarVisible(false);

        assertFalse(toolBars.isDocumentToolBarVisible());
        assertFalse(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void theEffectsRowHidesAndShowsAgainWithoutAffectingTheOthers() {
        toolBars.setEffectsToolBarVisible(false);

        assertFalse(toolBars.isEffectsToolBarVisible());
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());

        toolBars.setEffectsToolBarVisible(true);

        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void noButtonIsLeftWithoutAnAccessibleNameOrTooltip() {
        AccessibilityAssertions.assertNoViolations(toolBars.component());
        AccessibilityAssertions.assertNoViolations(toolBars.effectsComponent());
    }

    @Test
    void noButtonInAnyOfTheFourRowsIsLeftWithoutAnIcon() {
        for (Container row : new Container[] {
            toolBars.component(), toolBars.effectsComponent(),
        }) {
            for (AbstractButton button : buttonsOf(row)) {
                assertNotNull(button.getIcon(), button.getAccessibleContext().getAccessibleName() + " has no icon");
            }
        }
    }

    @Test
    void theSoundFontButtonStartsAndStaysSyncedWithTheActualPort() {
        boolean[] active = {true};
        List<String> called = new java.util.ArrayList<>();
        InvocationHandler handler = (proxy, method, args) -> {
            called.add(method.getName());
            if (method.getName().equals("toggleSoundFont")) {
                active[0] = !active[0];
                return null;
            }
            if (method.getName().equals("soundFontActive")) {
                return active[0];
            }
            return null;
        };
        Ports.Playback playback = (Ports.Playback) Proxy.newProxyInstance(
                Ports.Playback.class.getClassLoader(), new Class<?>[] {Ports.Playback.class}, handler);
        ToolBars anotherToolBar = new ToolBars(editor, new Commands(
                editor, record(Ports.Document.class), record(Ports.Dialogs.class), playback, record(Ports.View.class)),
                new FakeZoomHolder());
        JToggleButton button = toggleButtonNamed(anotherToolBar.structureToolBar, "Banco de sonido");

        assertTrue(button.isSelected(), "has to start showing that the sound font is on");

        button.doClick();
        assertFalse(button.isSelected());

        button.getAction().actionPerformed(null);
        assertTrue(button.isSelected(), "a trigger from outside the button (F2, the menu) has to sync it just the same");

        assertEquals(2, called.stream().filter("toggleSoundFont"::equals).count());
    }

    private JToggleButton toggleButtonNamed(Container root, String name) {
        for (AbstractButton button : buttonsOf(root)) {
            if (button instanceof JToggleButton toggle && name.equals(button.getAccessibleContext().getAccessibleName())) {
                return toggle;
            }
        }
        throw new AssertionError("found no toggle button named " + name);
    }

    private java.util.List<AbstractButton> buttonsOf(Container root) {
        java.util.List<AbstractButton> found = new java.util.ArrayList<>();
        for (Component child : root.getComponents()) {
            if (child instanceof AbstractButton button) {
                found.add(button);
            }
            if (child instanceof Container container && !(child instanceof javax.swing.JComboBox)) {
                found.addAll(buttonsOf(container));
            }
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }

    private static final class FakeZoomHolder implements ZoomHolder {
        @Override
        public Zoom zoom() {
            return Zoom.whole();
        }

        @Override
        public void setZoom(Zoom zoom) {
        }

        @Override
        public void onZoomChange(Runnable listener) {
        }
    }
}
