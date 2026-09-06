package com.gstncaruso.tabpro.ui.menu;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** La barra de menu de tabpro, con los mismos menus que describe el manual. */
public final class MenuBar {

    private final Commands commands;

    public MenuBar(Commands commands) {
        this.commands = commands;
    }

    public JMenuBar build() {
        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu());
        bar.add(editMenu());
        bar.add(barMenu());
        bar.add(trackMenu());
        bar.add(noteMenu());
        bar.add(effectsMenu());
        bar.add(markersMenu());
        bar.add(toolsMenu());
        bar.add(soundMenu());
        bar.add(viewMenu());
        bar.add(optionsMenu());
        bar.add(helpMenu());
        return bar;
    }

    private JMenu fileMenu() {
        JMenu menu = new JMenu("Archivo");
        add(menu, "file.new", "file.open", "file.browse");
        menu.addSeparator();
        add(menu, "file.save", "file.saveAs");
        menu.addSeparator();
        JMenu importMenu = new JMenu("Importar");
        add(importMenu, "file.importGuitarPro", "file.importMidi", "file.importAscii", "file.importMusicXml");
        menu.add(importMenu);
        JMenu exportMenu = new JMenu("Exportar");
        add(exportMenu, "file.exportMidi", "file.exportWave", "file.exportAscii", "file.exportMusicXml",
                "file.exportImage", "file.exportPdf");
        menu.add(exportMenu);
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
        menu.add(voices);
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
        menu.add(lineBreaks);
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
        menu.add(durations);
        menu.addSeparator();
        add(menu, "note.rest", "note.tie", "note.tieBeat");
        menu.addSeparator();
        add(menu, "note.insertBeat", "note.deleteNote", "note.deleteBeat", "note.repeatToEndOfBar");
        menu.addSeparator();
        add(menu, "note.up", "note.down", "note.toUpperString", "note.toLowerString");
        menu.addSeparator();
        add(menu, "note.dynamics", "note.soundDuration", "note.fingering", "note.chord", "note.mixTableChange");
        return menu;
    }

    private JMenu effectsMenu() {
        JMenu menu = new JMenu("Efectos");
        add(menu, "effect.hammer", "effect.legatoSlide", "effect.shiftSlide", "effect.noSlide");
        menu.addSeparator();
        add(menu, "effect.bend", "effect.tremoloBar", "effect.vibrato", "effect.wideVibrato",
                "effect.trill", "effect.tremoloPicking");
        menu.addSeparator();
        add(menu, "effect.palmMute", "effect.letRing", "effect.staccato",
                "effect.deadNote", "effect.ghostNote", "effect.accent", "effect.heavyAccent", "effect.fadeIn");
        menu.addSeparator();
        add(menu, "effect.graceNote", "effect.harmonics", "effect.tapping", "effect.slapping", "effect.popping");
        menu.addSeparator();
        JMenu strokes = new JMenu("Rasgueo y púa");
        add(strokes, "effect.strokeDown", "effect.strokeUp", "effect.strokeOptions",
                "effect.pickstrokeDown", "effect.pickstrokeUp");
        menu.add(strokes);
        JMenu wah = new JMenu("Wah-wah");
        add(wah, "effect.wahOpen", "effect.wahClosed", "effect.wahOff");
        menu.add(wah);
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
        add(menu, "sound.tempo", "sound.relativeTempo", "sound.metronome", "sound.countDown");
        menu.addSeparator();
        add(menu, "sound.stepBack", "sound.stepForward", "sound.midiInput");
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
                "view.grayInactiveVoice");
        menu.addSeparator();
        add(menu, "view.fretboard", "view.keyboard", "view.percussion", "view.mixTable", "view.toolBars");
        return menu;
    }

    private JMenu optionsMenu() {
        JMenu menu = new JMenu("Opciones");
        themesMenu().ifPresent(menu::add);
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

    private JMenu helpMenu() {
        JMenu menu = new JMenu("Ayuda");
        add(menu, "help.contents", "help.about");
        return menu;
    }

    private void add(JMenu menu, String... names) {
        for (String name : names) {
            Command command = commands.get(name);
            JMenuItem item = new JMenuItem(command);
            item.setIcon(null);
            menu.add(item);
        }
    }
}
