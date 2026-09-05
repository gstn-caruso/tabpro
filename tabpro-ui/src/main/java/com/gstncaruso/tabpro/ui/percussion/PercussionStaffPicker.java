package com.gstncaruso.tabpro.ui.percussion;

import com.gstncaruso.tabpro.ui.instruments.InstrumentColors;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.JComponent;

/**
 * La zona (2) del asistente de percusion: un pentagrama en miniatura donde cada
 * linea es un sonido. Un clic lo escucha, un doble clic lo agrega al beat, igual
 * que en la zona (1).
 */
public final class PercussionStaffPicker extends JComponent {

    public static final int PREFERRED_HEIGHT = 96;

    private static final int SIDE_MARGIN = 20;
    private static final int TOP_MARGIN = 12;
    private static final int STEP = 8;
    private static final int STAFF_LINES = 5;
    private static final int NOTE_RADIUS = 5;

    private boolean preferElectric;
    private Optional<PercussionLine> hovered = Optional.empty();

    /** Para geometria y tests: sin acciones al clickear. */
    public PercussionStaffPicker() {
        this(sound -> { }, line -> { });
    }

    /**
     * @param onPlay que hacer cuando un clic pide escuchar el sonido de una linea
     * @param onAdd que hacer cuando un doble clic pide agregarlo al beat
     */
    public PercussionStaffPicker(IntConsumer onPlay, Consumer<PercussionLine> onAdd) {
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(0, PREFERRED_HEIGHT));
        trackTheMouse();
        installClicking(onPlay, onAdd);
    }

    private void installClicking(IntConsumer onPlay, Consumer<PercussionLine> onAdd) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lineAt(e.getX(), e.getY()).ifPresent(line -> {
                    if (e.getClickCount() >= 2) {
                        onAdd.accept(line);
                    } else {
                        onPlay.accept(soundOf(line));
                    }
                });
            }
        });
    }

    public void setPreferElectric(boolean preferElectric) {
        this.preferElectric = preferElectric;
        repaint();
    }

    public boolean preferElectric() {
        return preferElectric;
    }

    public Optional<PercussionLine> hoveredLine() {
        return hovered;
    }

    /** El sonido que hoy le corresponde a esa linea, segun este o no activo el electrico. */
    public int soundOf(PercussionLine line) {
        return line.soundToUse(preferElectric);
    }

    public int yOf(PercussionLine line) {
        return bottomLineY() - line.staffSlot() * STEP;
    }

    private int bottomLineY() {
        return TOP_MARGIN + (STAFF_LINES - 1) * STEP * 2;
    }

    /** La linea del pentagrama que hay en ese punto, o nada si el punto cae afuera. */
    public Optional<PercussionLine> lineAt(int x, int y) {
        if (x < SIDE_MARGIN || x > getWidth() - SIDE_MARGIN) {
            return Optional.empty();
        }
        for (PercussionLine line : PercussionLine.values()) {
            if (Math.abs(y - yOf(line)) <= STEP / 2) {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    private void trackTheMouse() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = lineAt(e.getX(), e.getY());
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = Optional.empty();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintStaffLines(g);
        paintSounds(g);
    }

    private void paintStaffLines(Graphics2D g) {
        g.setColor(ScoreColors.STAFF_LINE);
        g.setStroke(new BasicStroke(1));
        for (int line = 0; line < STAFF_LINES; line++) {
            int y = TOP_MARGIN + line * STEP * 2;
            g.drawLine(SIDE_MARGIN, y, getWidth() - SIDE_MARGIN, y);
        }
    }

    private void paintSounds(Graphics2D g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        FontMetrics metrics = g.getFontMetrics();
        int x = getWidth() / 2;
        for (PercussionLine line : PercussionLine.values()) {
            int y = yOf(line);
            boolean isHovered = hovered.map(h -> h == line).orElse(false);
            g.setColor(isHovered ? InstrumentColors.HOVER : InstrumentColors.CONTEXT);
            g.fillOval(x - NOTE_RADIUS, y - NOTE_RADIUS, NOTE_RADIUS * 2, NOTE_RADIUS * 2);

            String sound = String.valueOf(soundOf(line));
            g.setColor(InstrumentColors.CONTEXT_INK);
            g.drawString(sound, x + NOTE_RADIUS + 4, y + metrics.getAscent() / 2 - 1);
        }
    }
}
