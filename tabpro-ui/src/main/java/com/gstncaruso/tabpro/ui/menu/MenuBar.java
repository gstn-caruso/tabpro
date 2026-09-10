package com.gstncaruso.tabpro.ui.menu;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import java.awt.event.InputEvent;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/** La barra de menu de tabpro, con los mismos menus que describe el manual. */
public final class MenuBar {

    private final Commands commands;
    private final Supplier<List<Path>> recentFiles;
    private final Consumer<Path> openRecentFile;
    private final Map<JMenu, MnemonicAssigner> itemMnemonics = new IdentityHashMap<>();

    /** Sin archivos recientes que ofrecer, como en un uso puramente programatico o de test. */
    public MenuBar(Commands commands) {
        this(commands, List::of, path -> { });
    }

    public MenuBar(Commands commands, Supplier<List<Path>> recentFiles, Consumer<Path> openRecentFile) {
        this.commands = commands;
        this.recentFiles = recentFiles;
        this.openRecentFile = openRecentFile;
    }

    public JMenuBar build() {
        JMenuBar bar = new JMenuBar();
        MnemonicAssigner topLevelMnemonics = new MnemonicAssigner();
        reserveAltLetterAccelerators(topLevelMnemonics);
        addMenu(bar, topLevelMnemonics, fileMenu());
        addMenu(bar, topLevelMnemonics, editMenu());
        addMenu(bar, topLevelMnemonics, barMenu());
        addMenu(bar, topLevelMnemonics, trackMenu());
        addMenu(bar, topLevelMnemonics, noteMenu());
        addMenu(bar, topLevelMnemonics, effectsMenu());
        addMenu(bar, topLevelMnemonics, markersMenu());
        addMenu(bar, topLevelMnemonics, toolsMenu());
        addMenu(bar, topLevelMnemonics, soundMenu());
        addMenu(bar, topLevelMnemonics, viewMenu());
        addMenu(bar, topLevelMnemonics, optionsMenu());
        addMenu(bar, topLevelMnemonics, helpMenu());
        return bar;
    }

    private void addMenu(JMenuBar bar, MnemonicAssigner assigner, JMenu menu) {
        assigner.applyTo(menu);
        bar.add(menu);
    }

    private void reserveAltLetterAccelerators(MnemonicAssigner assigner) {
        for (Command command : commands.all().values()) {
            KeyStroke accelerator = command.accelerator();
            if (isAltLetterAccelerator(accelerator)) {
                assigner.reserve((char) accelerator.getKeyCode());
            }
        }
    }

    private boolean isAltLetterAccelerator(KeyStroke accelerator) {
        if (accelerator == null) {
            return false;
        }
        int modifiers = accelerator.getModifiers();
        int allowedBits = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK;
        boolean onlyAlt = (modifiers & InputEvent.ALT_DOWN_MASK) != 0 && (modifiers & ~allowedBits) == 0;
        return onlyAlt && Character.isLetter((char) accelerator.getKeyCode());
    }

    private JMenu fileMenu() {
        JMenu menu = new JMenu("Archivo");
        add(menu, "file.new", "file.open", "file.browse");
        recentFilesMenu().ifPresent(recent -> addSubmenu(menu, recent));
        menu.addSeparator();
        add(menu, "file.save", "file.saveAs");
        menu.addSeparator();
        JMenu importMenu = new JMenu("Importar");
        add(importMenu, "file.importGuitarPro", "file.importTabEdit", "file.importPowerTab", "file.importMidi",
                "file.importAscii", "file.importMusicXml");
        addSubmenu(menu, importMenu);
        JMenu exportMenu = new JMenu("Exportar");
        add(exportMenu, "file.exportMidi", "file.exportWave", "file.exportAscii", "file.exportMusicXml",
                "file.exportGuitarPro",
                "file.exportImage", "file.exportPdf");
        addSubmenu(menu, exportMenu);
        menu.addSeparator();
        add(menu, "file.information", "file.pageSetup", "file.print");
        menu.addSeparator();
        add(menu, "file.quit");
        return menu;
    }

    private JMenu editMenu() {
        JMenu menu = new JMenu("Editar");
        add(menu, "edit.undo", "edit.redo");
        menu.addSeparator();
        add(menu, "edit.cut", "edit.copy", "edit.copyTrack", "edit.paste", "edit.pasteOptions", "edit.selectAll");
        menu.addSeparator();
        JMenu voices = new JMenu("Voces");
        add(voices, "edit.leadVoice", "edit.bassVoice");
        addSubmenu(menu, voices);
        menu.addSeparator();
        add(menu, "bar.insert", "bar.delete", "edit.emptyBar", "edit.emptyBarEveryTrack");
        return menu;
    }

    private JMenu barMenu() {
        JMenu menu = new JMenu("Compás");
        add(menu, "bar.keySignature", "bar.timeSignature", "bar.tripletFeel");
        menu.addSeparator();
        add(menu, "bar.doubleBar", "bar.repeatOpen", "bar.repeatClose", "bar.alternateEndings", "bar.directions");
        menu.addSeparator();
        JMenu lineBreaks = new JMenu("Salto de línea");
        add(lineBreaks, "bar.forceLineBreak", "bar.preventLineBreak", "bar.resetLineBreak");
        addSubmenu(menu, lineBreaks);
        JMenu octave = new JMenu("Octava");
        add(octave, "bar.octave8va", "bar.octave8vb", "bar.octave15ma", "bar.octave15mb", "bar.octaveNone");
        addSubmenu(menu, octave);
        return menu;
    }

    private JMenu trackMenu() {
        JMenu menu = new JMenu("Pista");
        add(menu, "track.add", "track.addGuitar", "track.addBass", "track.addPercussion", "track.delete");
        menu.addSeparator();
        add(menu, "track.moveUp", "track.moveDown");
        menu.addSeparator();
        add(menu, "track.properties", "track.instrument");
        menu.addSeparator();
        add(menu, "track.previous", "track.next");
        return menu;
    }

    private JMenu noteMenu() {
        JMenu menu = new JMenu("Nota");
        JMenu durations = new JMenu("Duración");
        add(durations, "note.value.WHOLE", "note.value.HALF", "note.value.QUARTER", "note.value.EIGHTH",
                "note.value.SIXTEENTH", "note.value.THIRTY_SECOND", "note.value.SIXTY_FOURTH");
        durations.addSeparator();
        add(durations, "note.longer", "note.shorter", "note.dot", "note.triplet",
                "note.tuplet.5", "note.tuplet.6", "note.tuplet.7",
                "note.tuplet.9", "note.tuplet.10", "note.tuplet.11", "note.tuplet.12", "note.tuplet.13");
        addSubmenu(menu, durations);
        menu.addSeparator();
        add(menu, "note.rest", "note.tie", "note.tieBeat");
        menu.addSeparator();
        add(menu, "note.insertBeat", "note.deleteNote", "note.deleteBeat", "note.repeatToEndOfBar");
        menu.addSeparator();
        add(menu, "note.up", "note.down", "note.toUpperString", "note.toLowerString");
        menu.addSeparator();
        add(menu, "note.dynamics",
                "note.dynamic.PIANO_PIANISSIMO", "note.dynamic.PIANISSIMO", "note.dynamic.PIANO",
                "note.dynamic.MEZZO_PIANO", "note.dynamic.MEZZO_FORTE", "note.dynamic.FORTE",
                "note.dynamic.FORTISSIMO", "note.dynamic.FORTE_FORTISSIMO",
                "note.soundDuration", "note.fingering", "note.chord", "note.mixTableChange");
        menu.addSeparator();
        JMenu beams = new JMenu("Barra de unión");
        add(beams, "note.forceBeamBreak", "note.preventBeamBreak", "note.resetBeamBreak");
        addSubmenu(menu, beams);
        JMenu stems = new JMenu("Plica");
        add(stems, "note.stemUp", "note.stemDown", "note.stemAutomatic");
        addSubmenu(menu, stems);
        return menu;
    }

    private JMenu effectsMenu() {
        JMenu menu = new JMenu("Efectos");
        add(menu, "effect.hammer", "effect.legatoSlide", "effect.shiftSlide", "effect.slideInFromBelow",
                "effect.slideInFromAbove", "effect.slideOutDownwards", "effect.slideOutUpwards", "effect.noSlide");
        menu.addSeparator();
        add(menu, "effect.bend", "effect.tremoloBar", "effect.vibrato", "effect.wideVibrato",
                "effect.trill", "effect.tremoloPicking");
        menu.addSeparator();
        add(menu, "effect.palmMute", "effect.letRing", "effect.staccato",
                "effect.deadNote", "effect.ghostNote", "effect.accent", "effect.heavyAccent", "effect.fadeIn");
        menu.addSeparator();
        add(menu, "effect.graceNote", "effect.harmonics", "effect.naturalHarmonic", "effect.artificialHarmonic",
                "effect.tapping", "effect.slapping", "effect.popping");
        menu.addSeparator();
        JMenu strokes = new JMenu("Rasgueo y púa");
        add(strokes, "effect.strokeDown", "effect.strokeUp", "effect.strokeOptions",
                "effect.pickstrokeDown", "effect.pickstrokeUp");
        addSubmenu(menu, strokes);
        JMenu wah = new JMenu("Wah-wah");
        add(wah, "effect.wahOpen", "effect.wahClosed", "effect.wahOff");
        addSubmenu(menu, wah);
        menu.addSeparator();
        add(menu, "effect.text");
        return menu;
    }

    private JMenu markersMenu() {
        JMenu menu = new JMenu("Marcadores");
        add(menu, "marker.insert", "marker.list");
        menu.addSeparator();
        add(menu, "marker.previous", "marker.next");
        return menu;
    }

    private JMenu toolsMenu() {
        JMenu menu = new JMenu("Herramientas");
        add(menu, "tool.letRingOptions", "tool.palmMuteOptions", "tool.dynamicOptions");
        menu.addSeparator();
        add(menu, "tool.arrangeBars", "tool.completeBars", "tool.automaticFingering");
        menu.addSeparator();
        add(menu, "tool.transpose", "tool.checkBarDurations");
        menu.addSeparator();
        add(menu, "tool.scales", "tool.tuner");
        return menu;
    }

    private JMenu soundMenu() {
        JMenu menu = new JMenu("Sonido");
        add(menu, "sound.play", "sound.playFromStart", "sound.loop");
        menu.addSeparator();
        add(menu, "sound.tempo", "sound.relativeTempo", "sound.metronome", "sound.metronomeSettings",
                "sound.countDown");
        menu.addSeparator();
        add(menu, "sound.stepBack", "sound.stepForward", "sound.midiInput", "sound.soundFont");
        menu.addSeparator();
        add(menu, "nav.firstBar", "nav.previousBar", "nav.nextBar", "nav.lastBar", "nav.nextNote");
        return menu;
    }

    private JMenu viewMenu() {
        JMenu menu = new JMenu("Ver");
        add(menu, "view.page", "view.parchment", "view.verticalScreen", "view.horizontalScreen");
        menu.addSeparator();
        add(menu, "view.zoomIn", "view.zoomOut", "view.resetZoom");
        menu.addSeparator();
        add(menu, "view.multitrack", "view.hideStandardNotation", "view.hideTablature",
                "view.grayInactiveVoice", "view.dynamicNotes");
        menu.addSeparator();
        add(menu, "view.fretboard", "view.keyboard", "view.percussion", "view.mixTable", "view.toggleView");
        menu.addSeparator();
        add(menu, "view.toolBars");
        addSubmenu(menu, toolBarsMenu());
        return menu;
    }

    /** Ver > Menus y barras: un casillero por fila, para elegir cuales se ven. */
    private JMenu toolBarsMenu() {
        JMenu menu = new JMenu("Menús y barras");
        addCheckbox(menu, "view.toolBars.document");
        addCheckbox(menu, "view.toolBars.structure");
        addCheckbox(menu, "view.toolBars.notation");
        addCheckbox(menu, "view.toolBars.effects");
        return menu;
    }

    private JMenu optionsMenu() {
        JMenu menu = new JMenu("Opciones");
        themesMenu().ifPresent(theme -> addSubmenu(menu, theme));
        add(menu, "options.midiSetup", "options.preferences");
        return menu;
    }

    /** El menu de temas solo aparece si la aplicacion ofrece alguno. */
    private java.util.Optional<JMenu> themesMenu() {
        java.util.List<String> names = commands.all().keySet().stream()
                .filter(name -> name.startsWith("view.theme."))
                .sorted()
                .toList();
        if (names.isEmpty()) {
            return java.util.Optional.empty();
        }
        JMenu menu = new JMenu("Tema");
        add(menu, names.toArray(String[]::new));
        return java.util.Optional.of(menu);
    }

    /** El submenu "Abrir reciente" solo aparece si hay algun archivo que ofrecer. */
    private Optional<JMenu> recentFilesMenu() {
        List<Path> paths = recentFiles.get();
        if (paths.isEmpty()) {
            return Optional.empty();
        }
        JMenu menu = new JMenu("Abrir reciente");
        for (Path path : paths) {
            JMenuItem item = new JMenuItem(path.getFileName().toString());
            item.setToolTipText(path.toString());
            item.addActionListener(event -> openRecentFile.accept(path));
            menu.add(item);
        }
        return Optional.of(menu);
    }

    private JMenu helpMenu() {
        JMenu menu = new JMenu("Ayuda");
        add(menu, "help.contents", "help.about");
        return menu;
    }

    private void add(JMenu menu, String... names) {
        MnemonicAssigner assigner = itemMnemonicsOf(menu);
        for (String name : names) {
            Command command = commands.get(name);
            JMenuItem item = new JMenuItem(command);
            item.setIcon(null);
            assigner.applyTo(item);
            menu.add(item);
        }
    }

    private void addCheckbox(JMenu menu, String name) {
        Command command = commands.get(name);
        javax.swing.JCheckBoxMenuItem item = new javax.swing.JCheckBoxMenuItem(command);
        item.setIcon(null);
        itemMnemonicsOf(menu).applyTo(item);
        menu.add(item);
    }

    private void addSubmenu(JMenu parent, JMenu submenu) {
        itemMnemonicsOf(parent).applyTo(submenu);
        parent.add(submenu);
    }

    private MnemonicAssigner itemMnemonicsOf(JMenu menu) {
        return itemMnemonics.computeIfAbsent(menu, key -> new MnemonicAssigner());
    }
}
