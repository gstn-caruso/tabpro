package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.theme.Palette;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Las barras de herramientas, con los mismos cuatro grupos que usa Guitar Pro 5: arriba, el
 * archivo y la edicion, la estructura y el sonido, y las figuras; abajo, pegada a la mesa de
 * mezcla, la barra de efectos ({@link #effectsComponent()}). Ver > Menus y barras deja elegir
 * cada fila por separado, ademas del interruptor general que esconde las tres de arriba juntas.
 */
public final class ToolBars {

    private final Commands commands;
    private final JPanel rows = new JPanel();
    final JToolBar documentToolBar;
    final JToolBar structureToolBar;
    final JToolBar notationToolBar;
    final JToolBar effectsToolBar;

    public ToolBars(Commands commands) {
        this.commands = commands;
        documentToolBar = leftAligned(documentRow());
        structureToolBar = leftAligned(structureRow());
        notationToolBar = leftAligned(notationRow());
        effectsToolBar = leftAligned(effectsRow());
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Palette.panel());
        rows.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Palette.separator()));
        rows.add(documentToolBar);
        rows.add(structureToolBar);
        rows.add(notationToolBar);
    }

    /** Las tres filas de arriba: documento y edicion, estructura y sonido, figuras. */
    public JComponent component() {
        return rows;
    }

    /**
     * La barra de efectos, la cuarta fila del manual: en Guitar Pro 5 va abajo de la partitura,
     * pegada a la mesa de mezcla, no arriba junto a las otras tres.
     */
    public JComponent effectsComponent() {
        return effectsToolBar;
    }

    public void setVisible(boolean visible) {
        rows.setVisible(visible);
    }

    public boolean isVisible() {
        return rows.isVisible();
    }

    /** Ver > Menus y barras: la fila del archivo y la edicion, elegible por separado. */
    public void setDocumentToolBarVisible(boolean visible) {
        documentToolBar.setVisible(visible);
    }

    public boolean isDocumentToolBarVisible() {
        return documentToolBar.isVisible();
    }

    /** Ver > Menus y barras: la fila de la estructura y el sonido, elegible por separado. */
    public void setStructureToolBarVisible(boolean visible) {
        structureToolBar.setVisible(visible);
    }

    public boolean isStructureToolBarVisible() {
        return structureToolBar.isVisible();
    }

    /** Ver > Menus y barras: la fila de las figuras y los efectos, elegible por separado. */
    public void setNotationToolBarVisible(boolean visible) {
        notationToolBar.setVisible(visible);
    }

    public boolean isNotationToolBarVisible() {
        return notationToolBar.isVisible();
    }

    /** Ver > Menus y barras: la fila de efectos, elegible por separado como las otras tres. */
    public void setEffectsToolBarVisible(boolean visible) {
        effectsToolBar.setVisible(visible);
    }

    public boolean isEffectsToolBarVisible() {
        return effectsToolBar.isVisible();
    }

    /**
     * Los extras que la ventana agrega a la fila del sonido, como el tempo: Guitar Pro 5 no los
     * trae en esta fila, asi que {@link #structureRow()} ya dejo un separador antes de ellos.
     */
    public void addToSoundRow(JComponent component) {
        structureToolBar.add(Box.createHorizontalStrut(4));
        structureToolBar.add(component);
    }

    /**
     * Guitar Pro 5, manual pagina 14, fila 1: archivo, edicion, pistas, compases, vistas, zoom
     * y paneles. Copiar y pegar no estan en esta fila del manual; se conservan al final, que es
     * donde ya vivian antes de este orden.
     */
    private JToolBar documentRow() {
        JToolBar bar = emptyBar();
        add(bar, "file.new", "file.open", "file.save");
        bar.addSeparator();
        add(bar, "file.information");
        bar.addSeparator();
        add(bar, "file.pageSetup", "file.print");
        bar.addSeparator();
        add(bar, "edit.undo", "edit.redo");
        bar.addSeparator();
        add(bar, "track.add", "track.properties", "track.moveUp", "track.moveDown", "track.delete",
                "tool.checkBarDurations");
        bar.addSeparator();
        add(bar, "bar.insert", "bar.delete");
        bar.addSeparator();
        add(bar, "edit.cut", "options.preferences");
        bar.addSeparator();
        add(bar, "view.multitrack");
        bar.addSeparator();
        add(bar, "view.page", "view.parchment", "view.verticalScreen", "view.horizontalScreen");
        bar.addSeparator();
        add(bar, "view.zoomOut", "view.resetZoom", "view.zoomIn");
        bar.addSeparator();
        add(bar, "view.fretboard", "view.keyboard", "view.mixTable");
        bar.addSeparator();
        add(bar, "edit.copy", "edit.paste");
        return bar;
    }

    /**
     * Guitar Pro 5, manual pagina 14, fila 2: atributos del compas, barras, marcadores,
     * transporte. La pista anterior/siguiente y las herramientas de escalas y afinador no estan
     * en esta fila del manual; se conservan al final, que es donde ya vivian antes de este orden.
     */
    private JToolBar structureRow() {
        JToolBar bar = emptyBar();
        add(bar, "bar.keySignature", "bar.timeSignature");
        bar.addSeparator();
        add(bar, "bar.repeatOpen", "bar.repeatClose");
        bar.addSeparator();
        add(bar, "bar.doubleBar");
        bar.addSeparator();
        add(bar, "bar.alternateEndings", "bar.forceLineBreak", "bar.preventLineBreak");
        bar.addSeparator();
        add(bar, "marker.insert", "marker.previous", "marker.next", "marker.list");
        bar.addSeparator();
        add(bar, "sound.play", "nav.firstBar", "nav.lastBar", "sound.metronome", "sound.countDown",
                "sound.loop");
        bar.addSeparator();
        add(bar, "nav.previousBar", "nav.nextBar", "tool.scales", "tool.tuner");
        bar.addSeparator();
        return bar;
    }

    /**
     * Guitar Pro 5, manual pagina 14, fila 3: figuras y su notacion (los efectos de la nota
     * tienen su propia barra, ver {@link #effectsRow()}).
     */
    private JToolBar notationRow() {
        JToolBar bar = emptyBar();
        add(bar, "note.value.WHOLE", "note.value.HALF", "note.value.QUARTER", "note.value.EIGHTH",
                "note.value.SIXTEENTH", "note.value.THIRTY_SECOND", "note.value.SIXTY_FOURTH");
        bar.addSeparator();
        add(bar, "note.dot", "note.triplet", "note.tieBeat");
        bar.addSeparator();
        add(bar, "note.rest");
        bar.addSeparator();
        add(bar, "note.tie", "note.soundDuration");
        bar.addSeparator();
        add(bar, "bar.octave8va", "bar.octave8vb", "bar.octave15ma", "bar.octave15mb");
        bar.addSeparator();
        add(bar, "note.preventBeamBreak", "note.forceBeamBreak", "note.resetBeamBreak");
        return bar;
    }

    /**
     * Guitar Pro 5, manual pagina 14, barra de efectos: va abajo de la partitura, pegada a la
     * mesa de mezcla, no junto a las otras tres filas de arriba.
     */
    private JToolBar effectsRow() {
        JToolBar bar = emptyBar();
        add(bar, "effect.deadNote", "effect.graceNote", "effect.ghostNote", "effect.accent",
                "effect.heavyAccent", "effect.letRing", "effect.harmonics");
        bar.addSeparator();
        add(bar, "effect.hammer", "effect.legatoSlide", "effect.bend", "effect.tremoloBar",
                "effect.vibrato", "effect.wideVibrato");
        bar.addSeparator();
        add(bar, "effect.trill", "effect.tremoloPicking", "effect.palmMute", "effect.staccato");
        bar.addSeparator();
        add(bar, "effect.tapping", "effect.slapping", "effect.popping");
        bar.addSeparator();
        add(bar, "effect.fadeIn");
        bar.addSeparator();
        add(bar, "note.chord", "effect.text", "note.mixTableChange", "note.fingering");
        bar.addSeparator();
        add(bar, "effect.strokeUp", "effect.strokeDown");
        return bar;
    }

    /** Las filas arrancan pegadas a la izquierda, como en una barra de verdad. */
    private static <T extends JComponent> T leftAligned(T row) {
        row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JToolBar emptyBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return bar;
    }

    private void add(JToolBar bar, String... names) {
        for (String name : names) {
            bar.add(button(commands.get(name)));
        }
    }

    /** Un boton de barra: solo el icono, plano, con la ayuda y el atajo en el tooltip. */
    private static JButton button(Command command) {
        JButton button = new JButton(command);
        button.setText(null);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText(tooltipOf(command));
        button.getAccessibleContext().setAccessibleName(command.label());
        button.setPreferredSize(new Dimension(26, 24));
        button.setMaximumSize(new Dimension(26, 24));
        button.addChangeListener(event -> button.setContentAreaFilled(
                button.getModel().isRollover() || button.getModel().isPressed()));
        return button;
    }

    private static String tooltipOf(Command command) {
        String shortcut = command.acceleratorText();
        return shortcut.isEmpty() ? command.description() : command.description() + "  [" + shortcut + "]";
    }
}
