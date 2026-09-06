package com.gstncaruso.tabpro.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Test;

/** El menu Archivo ofrece los archivos recientes, tal como los recuerda Preferences. */
class MenuBarTest {

    private final Editor editor = new Editor(Score.blank());
    private final List<Path> opened = new ArrayList<>();
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void sinArchivosRecientesElMenuArchivoNoOfreceElSubmenu() {
        JMenuBar bar = new MenuBar(commands, List::of, opened::add).build();

        assertNull(recentFilesMenuOf(bar));
    }

    @Test
    void ofreceCadaArchivoRecienteConSuNombre() {
        List<Path> recent = List.of(Path.of("/tmp/una.tabpro"), Path.of("/tmp/otra.tabpro"));
        JMenuBar bar = new MenuBar(commands, () -> recent, opened::add).build();

        JMenu recentMenu = recentFilesMenuOf(bar);

        assertEquals(2, recentMenu.getItemCount());
        assertEquals("una.tabpro", recentMenu.getItem(0).getText());
        assertEquals("otra.tabpro", recentMenu.getItem(1).getText());
    }

    @Test
    void alElegirUnArchivoRecienteSeLoAbre() {
        Path path = Path.of("/tmp/una.tabpro");
        JMenuBar bar = new MenuBar(commands, () -> List.of(path), opened::add).build();

        recentFilesMenuOf(bar).getItem(0).doClick();

        assertEquals(List.of(path), opened);
    }

    /**
     * Que un comando tenga un acelerador declarado no prueba que la ventana lo escuche: si
     * nadie lo cuelga de un menu, el atajo queda muerto sin que nada lo avise. Este test recorre
     * la barra de menus entera y confirma que todo comando con atajo aparece en algun item.
     */
    @Test
    void todoComandoConAceleradorCuelgaDeAlgunMenu() {
        JMenuBar bar = new MenuBar(commands).build();
        Set<Command> enElMenu = new HashSet<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            recolectar(bar.getMenu(i), enElMenu);
        }

        commands.all().forEach((nombre, comando) -> {
            if (comando.accelerator() != null) {
                assertTrue(enElMenu.contains(comando), "el atajo de " + nombre + " no cuelga de ningun menu");
            }
        });
    }

    private void recolectar(JMenu menu, Set<Command> encontrados) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            if (item instanceof JMenu submenu) {
                recolectar(submenu, encontrados);
            } else if (item.getAction() instanceof Command comando) {
                encontrados.add(comando);
            }
        }
    }

    private JMenu recentFilesMenuOf(JMenuBar bar) {
        JMenu fileMenu = (JMenu) bar.getMenu(0);
        for (int i = 0; i < fileMenu.getItemCount(); i++) {
            JMenuItem item = fileMenu.getItem(i);
            if (item instanceof JMenu menu && "Abrir reciente".equals(menu.getText())) {
                return menu;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}
