package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.theme.Palette;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Las barras de herramientas, en las mismas tres filas que usa Guitar Pro: el
 * archivo y la edicion, la estructura y el sonido, y las figuras y los efectos.
 * Ver > Menus y barras deja elegir cada fila por separado, ademas del interruptor
 * general que las esconde a todas juntas.
 */
public final class ToolBars {

    private final Commands commands;
    private final JPanel rows = new JPanel();
    private final JPanel structureRowExtras = transparentRow();
    private final JComponent documentToolBar;
    private final JComponent structureToolBar;
    private final JComponent notationToolBar;

    public ToolBars(Commands commands) {
        this.commands = commands;
        documentToolBar = leftAligned(documentRow());
        structureToolBar = leftAligned(structureRow());
        notationToolBar = leftAligned(notationRow());
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Palette.panel());
        rows.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Palette.separator()));
        rows.add(documentToolBar);
        rows.add(structureToolBar);
        rows.add(notationToolBar);
    }

    public JComponent component() {
        return rows;
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

    /** Los extras que la ventana agrega a la fila del sonido, como el tempo. */
    public void addToSoundRow(JComponent component) {
        structureRowExtras.add(Box.createHorizontalStrut(8));
        structureRowExtras.add(component);
    }

    private JComponent documentRow() {
        JToolBar bar = emptyBar();
        add(bar, "file.new", "file.open", "file.save", "file.print");
        bar.addSeparator();
        add(bar, "edit.undo", "edit.redo");
        bar.addSeparator();
        add(bar, "edit.cut", "edit.copy", "edit.paste");
        bar.addSeparator();
        add(bar, "file.information", "file.pageSetup");
        bar.addSeparator();
        add(bar, "view.page", "view.parchment", "view.verticalScreen", "view.horizontalScreen");
        bar.addSeparator();
        add(bar, "view.zoomOut", "view.resetZoom", "view.zoomIn");
        bar.addSeparator();
        add(bar, "view.multitrack", "view.fretboard", "view.keyboard", "view.mixTable");
        return bar;
    }

    private JComponent structureRow() {
        JToolBar bar = emptyBar();
        add(bar, "track.add", "bar.insert", "bar.delete");
        bar.addSeparator();
        add(bar, "bar.keySignature", "bar.timeSignature", "bar.doubleBar",
                "bar.repeatOpen", "bar.repeatClose", "bar.alternateEndings", "marker.insert");
        bar.addSeparator();
        add(bar, "nav.firstBar", "nav.previousBar", "sound.play", "nav.nextBar", "nav.lastBar");
        bar.addSeparator();
        add(bar, "sound.loop", "sound.metronome", "sound.countDown");
        bar.addSeparator();
        add(bar, "tool.scales", "tool.tuner");
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(bar, BorderLayout.CENTER);
        row.add(structureRowExtras, BorderLayout.EAST);
        return row;
    }

    private JComponent notationRow() {
        JToolBar bar = emptyBar();
        add(bar, "note.value.WHOLE", "note.value.HALF", "note.value.QUARTER", "note.value.EIGHTH",
                "note.value.SIXTEENTH", "note.value.THIRTY_SECOND", "note.value.SIXTY_FOURTH");
        bar.addSeparator();
        add(bar, "note.dot", "note.triplet", "note.rest", "note.tie");
        bar.addSeparator();
        add(bar, "effect.deadNote", "effect.ghostNote", "effect.accent", "effect.staccato",
                "effect.palmMute", "effect.letRing");
        bar.addSeparator();
        add(bar, "effect.hammer", "effect.legatoSlide", "effect.bend", "effect.vibrato",
                "effect.wideVibrato", "effect.harmonics");
        bar.addSeparator();
        add(bar, "effect.tapping", "effect.slapping", "effect.popping",
                "effect.strokeDown", "effect.strokeUp");
        bar.addSeparator();
        add(bar, "note.chord", "effect.text", "note.mixTableChange");
        return bar;
    }

    /** Las filas arrancan pegadas a la izquierda, como en una barra de verdad. */
    private static JComponent leftAligned(JComponent row) {
        row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static JPanel transparentRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
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
