package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public final class FretContextMenu {

    private FretContextMenu() {
    }

    public static JPopupMenu forTrack(Track track, IntConsumer onChosen) {
        JPopupMenu menu = new JPopupMenu();
        for (int number : numbersOf(track)) {
            JMenuItem item = new JMenuItem(labelFor(track, number));
            item.addActionListener(event -> onChosen.accept(number));
            menu.add(item);
        }
        return menu;
    }

    private static List<Integer> numbersOf(Track track) {
        if (track.isPercussion()) {
            return PercussionKit.sounds();
        }
        List<Integer> frets = new ArrayList<>();
        for (int fret = 0; fret <= Tuning.MAX_FRET; fret++) {
            frets.add(fret);
        }
        return frets;
    }

    private static String labelFor(Track track, int number) {
        if (track.isPercussion()) {
            return number + " – " + PercussionKit.nameOf(number).orElse("");
        }
        return String.valueOf(number);
    }
}
