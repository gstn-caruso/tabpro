package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import javax.swing.JComponent;

/**
 * La zona B: el diagrama grande y clicable. Un clic en la grilla agrega o saca una nota; un
 * clic en el encabezado alterna cuerda al aire / muda. La geometria esta en metodos aparte
 * para poder testearla sin pintar nada, como el diapason y el teclado.
 */
public final class ChordDiagramCanvas extends JComponent {

    public static final int PREFERRED_WIDTH = 200;
    public static final int PREFERRED_HEIGHT = 240;
    public static final int MINIMUM_ROWS = 4;

    private static final int SIDE_MARGIN = 16;
    private static final int TOP_MARGIN = 10;
    private static final int HEADER_HEIGHT = 20;
    private static final int BOTTOM_MARGIN = 18;

    private Tuning tuning = Tuning.standard();
    private ChordDiagram diagram = ChordDiagram.justTheName("");

    private BiConsumer<Integer, Integer> onFretClick = (string, fret) -> { };
    private IntConsumer onHeaderClick = string -> { };
    private IntConsumer onFingerClick = string -> { };

    public ChordDiagramCanvas() {
        setOpaque(true);
        setBackground(ChordDiagramColors.BACKGROUND);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleClick(event.getX(), event.getY());
            }
        });
    }

    public void show(ChordDiagram diagram, Tuning tuning) {
        this.diagram = diagram;
        this.tuning = tuning;
        repaint();
    }

    public void onFretClick(BiConsumer<Integer, Integer> listener) {
        this.onFretClick = listener;
    }

    public void onHeaderClick(IntConsumer listener) {
        this.onHeaderClick = listener;
    }

    /** El clic en la fila de numeros de abajo: define o cambia la digitacion de esa cuerda. */
    public void onFingerClick(IntConsumer listener) {
        this.onFingerClick = listener;
    }

    private void handleClick(int x, int y) {
        OptionalInt string = stringAt(x);
        if (string.isEmpty()) {
            return;
        }
        if (isHeaderRow(y)) {
            onHeaderClick.accept(string.getAsInt());
            return;
        }
        if (isFingerRow(y)) {
            onFingerClick.accept(string.getAsInt());
            return;
        }
        fretAt(y).ifPresent(fret -> onFretClick.accept(string.getAsInt(), fret));
    }

    // ---- geometria: columnas (cuerdas) -------------------------------------

    public int stringCount() {
        return tuning.stringCount();
    }

    /** La cuerda 6 (la mas grave) queda a la izquierda, como en cualquier diagrama de acorde. */
    public int stringX(int string) {
        return SIDE_MARGIN + (int) Math.round((stringCount() - string) * stringGap());
    }

    public OptionalInt stringAt(int x) {
        int nearest = stringCount() - (int) Math.round((x - SIDE_MARGIN) / stringGap());
        if (nearest < 1 || nearest > stringCount()) {
            return OptionalInt.empty();
        }
        return Math.abs(x - stringX(nearest)) <= stringGap() / 2 ? OptionalInt.of(nearest) : OptionalInt.empty();
    }

    private double stringGap() {
        return stringCount() <= 1 ? 0 : (double) (getWidth() - 2 * SIDE_MARGIN) / (stringCount() - 1);
    }

    // ---- geometria: filas (trastes) ----------------------------------------

    /** Cuantas filas hace falta dibujar para que entre todo lo que ya esta pisado. */
    public int rowCount() {
        return Math.max(MINIMUM_ROWS, diagram.highestFret() - diagram.baseFret() + 1);
    }

    public boolean hasHeader() {
        return diagram.baseFret() == 1;
    }

    public int headerY() {
        return TOP_MARGIN + HEADER_HEIGHT / 2;
    }

    public boolean isHeaderRow(int y) {
        return y >= TOP_MARGIN && y < TOP_MARGIN + HEADER_HEIGHT;
    }

    /** El centro de la fila del traste "diagram.baseFret() + row". */
    public int fretRowY(int row) {
        return TOP_MARGIN + HEADER_HEIGHT + (int) Math.round((row + 0.5) * rowHeight());
    }

    /** El traste absoluto de esa fila, o nada si el clic cayo afuera de la grilla. */
    public OptionalInt fretAt(int y) {
        int gridTop = TOP_MARGIN + HEADER_HEIGHT;
        if (y < gridTop) {
            return OptionalInt.empty();
        }
        int row = (int) ((y - gridTop) / rowHeight());
        return row >= rowCount() ? OptionalInt.empty() : OptionalInt.of(diagram.baseFret() + row);
    }

    private double rowHeight() {
        int usable = getHeight() - TOP_MARGIN - HEADER_HEIGHT - BOTTOM_MARGIN;
        return (double) usable / rowCount();
    }

    // ---- geometria: fila de digitacion, debajo de la grilla ------------------

    /** El renglon con los numeros de dedo, debajo del ultimo traste dibujado. */
    public int fingerRowY() {
        return fretRowY(rowCount() - 1) + (int) Math.round(rowHeight() / 2) + BOTTOM_MARGIN / 2;
    }

    public boolean isFingerRow(int y) {
        return y >= fretRowY(rowCount() - 1) + Math.round(rowHeight() / 2);
    }

    // ---- pintura ------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ChordDiagramColors.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintPositionLabel(g);
        paintHeader(g);
        paintGrid(g);
        paintBarre(g);
        paintFingers(g);
        paintFingerRow(g);
    }

    private void paintPositionLabel(Graphics2D g) {
        if (hasHeader()) {
            return;
        }
        g.setColor(ChordDiagramColors.LABEL);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.drawString(diagram.baseFret() + "fr", 2, headerY() + 4);
    }

    private void paintHeader(Graphics2D g) {
        if (!hasHeader()) {
            return;
        }
        int radius = 5;
        for (int string = 1; string <= stringCount(); string++) {
            int x = stringX(string);
            int fret = diagram.fretOfString(string);
            if (fret == 0) {
                g.setColor(ChordDiagramColors.OPEN_STRING);
                g.drawOval(x - radius, headerY() - radius, radius * 2, radius * 2);
            } else if (fret == ChordDiagram.MUTED) {
                g.setColor(ChordDiagramColors.MUTED_STRING);
                g.drawLine(x - radius, headerY() - radius, x + radius, headerY() + radius);
                g.drawLine(x - radius, headerY() + radius, x + radius, headerY() - radius);
            }
        }
    }

    private void paintGrid(Graphics2D g) {
        g.setColor(ChordDiagramColors.GRID);
        int left = stringX(stringCount());
        int right = stringX(1);
        for (int string = 1; string <= stringCount(); string++) {
            int x = stringX(string);
            g.drawLine(x, fretRowY(0) - (int) (rowHeight() / 2), x, fretRowY(rowCount() - 1) + (int) (rowHeight() / 2));
        }
        for (int row = 0; row <= rowCount(); row++) {
            int y = fretRowY(0) - (int) (rowHeight() / 2) + row * (int) rowHeight();
            g.setColor(row == 0 && hasHeader() ? ChordDiagramColors.NUT : ChordDiagramColors.GRID);
            g.drawLine(left, y, right, y);
        }
    }

    private void paintBarre(Graphics2D g) {
        if (!diagram.requiresBarre()) {
            return;
        }
        int fret = diagram.barreFret().orElseThrow();
        int row = fret - diagram.baseFret();
        if (row < 0 || row >= rowCount()) {
            return;
        }
        int firstString = firstStringAtFret(fret);
        int lastString = lastStringAtFret(fret);
        int radius = fingerRadius();
        g.setColor(ChordDiagramColors.BARRE);
        g.fillRoundRect(
                stringX(firstString) - radius, fretRowY(row) - radius,
                stringX(lastString) - stringX(firstString) + radius * 2, radius * 2,
                radius * 2, radius * 2);
    }

    private int firstStringAtFret(int fret) {
        for (int string = stringCount(); string >= 1; string--) {
            if (diagram.fretOfString(string) == fret) {
                return string;
            }
        }
        return 1;
    }

    private int lastStringAtFret(int fret) {
        for (int string = 1; string <= stringCount(); string++) {
            if (diagram.fretOfString(string) == fret) {
                return string;
            }
        }
        return stringCount();
    }

    private void paintFingers(Graphics2D g) {
        int radius = fingerRadius();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        for (int string = 1; string <= stringCount(); string++) {
            int fret = diagram.fretOfString(string);
            if (fret <= 0) {
                continue;
            }
            int row = fret - diagram.baseFret();
            if (row < 0 || row >= rowCount()) {
                continue;
            }
            int x = stringX(string);
            int y = fretRowY(row);
            g.setColor(ChordDiagramColors.FINGER);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            String label = diagram.fingerOfString(string).map(Finger::leftHandSymbol).orElse("");
            if (!label.isEmpty()) {
                g.setColor(ChordDiagramColors.FINGER_INK);
                g.drawString(label, x - metrics.stringWidth(label) / 2, y + (metrics.getAscent() - metrics.getDescent()) / 2);
            }
        }
    }

    private int fingerRadius() {
        return Math.max(6, (int) (rowHeight() * 0.32));
    }

    /**
     * Los numeros clickeables de abajo: la digitacion de la mano izquierda de cada cuerda
     * pisada. Una cuerda al aire o muda no lleva numero, porque no se digita.
     */
    private void paintFingerRow(Graphics2D g) {
        int y = fingerRowY();
        g.setColor(ChordDiagramColors.LABEL);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        for (int string = 1; string <= stringCount(); string++) {
            if (diagram.fretOfString(string) <= 0) {
                continue;
            }
            String label = diagram.fingerOfString(string).map(Finger::leftHandSymbol).orElse("?");
            int x = stringX(string);
            g.drawString(label, x - metrics.stringWidth(label) / 2, y + (metrics.getAscent() - metrics.getDescent()) / 2);
        }
    }
}
