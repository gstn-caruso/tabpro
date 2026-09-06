package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * La lista F: los acordes propios del usuario, guardados entre sesiones con
 * java.util.prefs. Cada vez que cambia algo se reescribe entera -son pocos acordes,
 * y asi no hay que llevar la cuenta de que indices quedaron libres.
 */
public final class ChordLibrary {

    private static final String COUNT_KEY = "count";
    private static final String ITEMS_NODE = "items";
    private static final String SEPARATOR = ",";
    private static final String NO_FINGER = "-";

    private final Preferences store;

    public ChordLibrary(Preferences store) {
        this.store = store;
    }

    public static ChordLibrary userLibrary() {
        return new ChordLibrary(Preferences.userNodeForPackage(ChordLibrary.class).node("chordLibrary"));
    }

    public List<ChordDiagram> all() {
        int count = store.getInt(COUNT_KEY, 0);
        List<ChordDiagram> diagrams = new ArrayList<>(count);
        Preferences items = store.node(ITEMS_NODE);
        for (int i = 0; i < count; i++) {
            diagrams.add(decode(items.node(String.valueOf(i))));
        }
        return List.copyOf(diagrams);
    }

    public void add(ChordDiagram diagram) {
        List<ChordDiagram> updated = new ArrayList<>(all());
        updated.add(diagram);
        persist(updated);
    }

    public void remove(int index) {
        List<ChordDiagram> updated = new ArrayList<>(all());
        updated.remove(index);
        persist(updated);
    }

    /** El boton "actualizar": reemplaza el diagrama de esa posicion por el que se armo ahora. */
    public void update(int index, ChordDiagram diagram) {
        List<ChordDiagram> updated = new ArrayList<>(all());
        updated.set(index, diagram);
        persist(updated);
    }

    public void sortByName() {
        List<ChordDiagram> sorted = new ArrayList<>(all());
        sorted.sort(Comparator.comparing(ChordDiagram::name, String.CASE_INSENSITIVE_ORDER));
        persist(sorted);
    }

    private void persist(List<ChordDiagram> diagrams) {
        Preferences items = store.node(ITEMS_NODE);
        for (int i = 0; i < diagrams.size(); i++) {
            encode(items.node(String.valueOf(i)), diagrams.get(i));
        }
        for (int i = diagrams.size(); nodeExists(items, i); i++) {
            removeQuietly(items.node(String.valueOf(i)));
        }
        store.putInt(COUNT_KEY, diagrams.size());
    }

    private static boolean nodeExists(Preferences items, int index) {
        try {
            return items.nodeExists(String.valueOf(index));
        } catch (java.util.prefs.BackingStoreException e) {
            return false;
        }
    }

    private static void removeQuietly(Preferences node) {
        try {
            node.removeNode();
        } catch (java.util.prefs.BackingStoreException e) {
            throw new IllegalStateException("no se pudo limpiar la biblioteca de acordes", e);
        }
    }

    private static void encode(Preferences item, ChordDiagram diagram) {
        item.put("name", diagram.name());
        item.putInt("baseFret", diagram.baseFret());
        item.putBoolean("shown", diagram.shown());
        item.put("frets", join(diagram.frets(), String::valueOf));
        item.put("fingering", join(diagram.fingering(), finger -> finger == null ? NO_FINGER : finger.name()));
    }

    private static ChordDiagram decode(Preferences item) {
        String name = item.get("name", "");
        int baseFret = item.getInt("baseFret", 1);
        boolean shown = item.getBoolean("shown", true);
        List<Integer> frets = splitInts(item.get("frets", ""));
        List<Finger> fingering = splitFingers(item.get("fingering", ""));
        return new ChordDiagram(name, baseFret, frets, fingering, shown);
    }

    private static <T> String join(List<T> values, java.util.function.Function<T, String> toText) {
        return values.stream().map(toText).reduce((a, b) -> a + SEPARATOR + b).orElse("");
    }

    private static List<Integer> splitInts(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        return List.of(text.split(SEPARATOR)).stream().map(Integer::parseInt).toList();
    }

    private static List<Finger> splitFingers(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        return List.of(text.split(SEPARATOR, -1)).stream()
                .map(token -> NO_FINGER.equals(token) ? null : Finger.valueOf(token))
                .toList();
    }
}
