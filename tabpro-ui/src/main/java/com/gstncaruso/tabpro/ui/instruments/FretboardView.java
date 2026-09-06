package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.notation.PitchName;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;

/**
 * El mastil: las notas del beat marcadas donde se pisan, mas lo que sume el modo
 * de vista elegido. Respeta la cejilla, la cantidad de trastes y de cuerdas de la
 * pista activa, y se puede dar vuelta para zurdos.
 */
public final class FretboardView extends JComponent {

    public static final int PREFERRED_HEIGHT = 118;

    private static final int SIDE_MARGIN = 10;
    private static final int OPEN_COLUMN = 26;
    private static final int TOP_MARGIN = 12;
    private static final int NUMBERS_HEIGHT = 15;
    private static final Set<Integer> SINGLE_INLAYS = Set.of(3, 5, 7, 9, 15, 17, 19, 21);
    private static final Set<Integer> DOUBLE_INLAYS = Set.of(12, 24);

    private BeatLocation location = defaultLocation();
    private FretboardDisplayMode displayMode = FretboardDisplayMode.ONLY_BEAT;
    private NoteNameMode noteNameMode = NoteNameMode.BEAT_ONLY;
    private ScaleLabelMode scaleLabelMode = ScaleLabelMode.NAME;
    private FretboardType fretboardType = FretboardType.ELECTRIC;
    private Handedness handedness = Handedness.RIGHT_HANDED;
    private Optional<Scale> scale = Optional.empty();
    private Optional<Note> hovered = Optional.empty();

    public FretboardView() {
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(0, PREFERRED_HEIGHT));
        trackTheMouse();
    }

    private static BeatLocation defaultLocation() {
        return new BeatLocation(Track.standardGuitar("Guitarra"), 0, VoicePart.LEAD, 0);
    }

    // ---- lo que se muestra -------------------------------------------------

    public void show(BeatLocation location) {
        this.location = location;
        repaint();
    }

    public void setDisplayMode(FretboardDisplayMode displayMode) {
        this.displayMode = displayMode;
        repaint();
    }

    public FretboardDisplayMode displayMode() {
        return displayMode;
    }

    public void setNoteNameMode(NoteNameMode noteNameMode) {
        this.noteNameMode = noteNameMode;
        repaint();
    }

    public NoteNameMode noteNameMode() {
        return noteNameMode;
    }

    /** Que muestran las notas de la escala en modo "Beat y escala": nombre, intervalo o grado. */
    public void setScaleLabelMode(ScaleLabelMode scaleLabelMode) {
        this.scaleLabelMode = scaleLabelMode;
        repaint();
    }

    public ScaleLabelMode scaleLabelMode() {
        return scaleLabelMode;
    }

    public void setFretboardType(FretboardType fretboardType) {
        this.fretboardType = fretboardType;
        repaint();
    }

    public FretboardType fretboardType() {
        return fretboardType;
    }

    public void setHandedness(Handedness handedness) {
        this.handedness = handedness;
        repaint();
    }

    public Handedness handedness() {
        return handedness;
    }

    public void setScale(Scale scale) {
        this.scale = Optional.ofNullable(scale);
        repaint();
    }

    /** La nota que esta bajo el mouse en este momento, sin que haga falta clickear. */
    public Optional<Note> hoveredNote() {
        return hovered;
    }

    // ---- geometria ----------------------------------------------------------

    public int stringCount() {
        return location.track().stringCount();
    }

    /** Cuantos trastes se dibujan: los que admite la pista activa, no siempre los mismos. */
    public int fretCount() {
        return location.track().settings().fretCount();
    }

    public int nutX() {
        return handedness.mirror(logicalNutX(), getWidth());
    }

    public int fretCenterX(int fret) {
        return handedness.mirror(logicalFretCenterX(fret), getWidth());
    }

    public int stringY(int string) {
        return TOP_MARGIN + (int) Math.round((string - 1) * stringGap());
    }

    /** La nota que se pisa en ese punto del mastil, o nada si el punto cae afuera. */
    public Optional<Note> noteAt(int x, int y) {
        int logicalX = handedness.mirror(x, getWidth());
        return stringAt(y).flatMap(string -> logicalFretAt(logicalX).map(fret -> new Note(string, fret)));
    }

    private int logicalNutX() {
        return SIDE_MARGIN + OPEN_COLUMN;
    }

    private int logicalFretCenterX(int fret) {
        if (fret == 0) {
            return SIDE_MARGIN + OPEN_COLUMN / 2;
        }
        return (int) Math.round(logicalNutX() + (fret - 0.5) * fretWidth());
    }

    private Optional<Integer> stringAt(int y) {
        int string = (int) Math.round((y - TOP_MARGIN) / stringGap()) + 1;
        if (string < 1 || string > stringCount()) {
            return Optional.empty();
        }
        boolean onTheNeck = Math.abs(y - stringY(string)) <= stringGap() / 2;
        return onTheNeck ? Optional.of(string) : Optional.empty();
    }

    private Optional<Integer> logicalFretAt(int x) {
        if (x < SIDE_MARGIN) {
            return Optional.empty();
        }
        if (x < logicalNutX()) {
            return Optional.of(0);
        }
        int fret = (int) Math.floor((x - logicalNutX()) / fretWidth()) + 1;
        return fret > fretCount() ? Optional.empty() : Optional.of(fret);
    }

    private double fretWidth() {
        return (double) (getWidth() - SIDE_MARGIN - logicalNutX()) / fretCount();
    }

    private double stringGap() {
        int usable = getHeight() - TOP_MARGIN - NUMBERS_HEIGHT - TOP_MARGIN;
        return stringCount() <= 1 ? 0 : (double) usable / (stringCount() - 1);
    }

    private int neckPadding() {
        return (int) Math.round(stringGap() / 2 * fretboardType.neckWidthFactor());
    }

    /** Como se llama la nota de esa posicion, ya con la cejilla de la pista sumada. */
    public String labelFor(FretPosition position) {
        return PitchName.of(location.track().pitchOf(new Note(position.string(), position.fret()))).text();
    }

    /**
     * El texto que se dibuja para esa posicion. Las notas de contexto de "Beat y escala"
     * respetan el modo de etiqueta elegido (nombre, intervalo o grado); todo lo demas -incluida
     * la nota que se esta pisando ahora mismo- siempre se llama por su nombre.
     */
    public String labelFor(FretPosition position, MarkKind kind) {
        String name = labelFor(position);
        if (kind != MarkKind.SECONDARY || displayMode != FretboardDisplayMode.BEAT_AND_SCALE) {
            return name;
        }
        return scale.map(chosen -> scaleLabelMode.textFor(name, midiOf(position), chosen)).orElse(name);
    }

    private int midiOf(FretPosition position) {
        return location.track().pitchOf(new Note(position.string(), position.fret())).midiNumber();
    }

    // ---- el mouse -----------------------------------------------------------

    private void trackTheMouse() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = noteAt(e.getX(), e.getY());
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

    // ---- dibujo -----------------------------------------------------------

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        withHandedTransform(g, gg -> {
            paintNeck(gg);
            paintInlays(gg);
            paintFretWires(gg);
            paintStrings(gg);
        });
        paintFretNumbers(g);
        paintMarkedNotes(g);
        paintHover(g);
    }

    /** Lo unico que se dibuja mirando al mastil al reves para zurdos: nada de texto. */
    private void withHandedTransform(Graphics2D g, Consumer<Graphics2D> painting) {
        if (handedness == Handedness.RIGHT_HANDED) {
            painting.accept(g);
            return;
        }
        Graphics2D mirrored = (Graphics2D) g.create();
        mirrored.translate(getWidth(), 0);
        mirrored.scale(-1, 1);
        painting.accept(mirrored);
        mirrored.dispose();
    }

    private void paintNeck(Graphics2D g) {
        int padding = neckPadding();
        int top = stringY(1) - padding;
        int bottom = stringY(stringCount()) + padding;
        g.setColor(fretboardType.woodColor());
        g.fillRect(logicalNutX(), top, getWidth() - SIDE_MARGIN - logicalNutX(), bottom - top);
        g.setColor(fretboardType.edgeColor());
        g.drawRect(logicalNutX(), top, getWidth() - SIDE_MARGIN - logicalNutX(), bottom - top);
    }

    private void paintInlays(Graphics2D g) {
        int middle = (stringY(1) + stringY(stringCount())) / 2;
        int radius = 4;
        g.setColor(InstrumentColors.INLAY);
        for (int fret = 1; fret <= fretCount(); fret++) {
            int x = logicalFretCenterX(fret);
            if (SINGLE_INLAYS.contains(fret)) {
                fretboardType.inlayStyle().draw(g, x, middle, radius);
            } else if (DOUBLE_INLAYS.contains(fret)) {
                int offset = (int) stringGap();
                fretboardType.inlayStyle().draw(g, x, middle - offset, radius);
                fretboardType.inlayStyle().draw(g, x, middle + offset, radius);
            }
        }
    }

    private void paintFretWires(Graphics2D g) {
        int padding = neckPadding();
        int top = stringY(1) - padding;
        int bottom = stringY(stringCount()) + padding;

        g.setColor(InstrumentColors.FRET_WIRE);
        g.setStroke(new BasicStroke(1));
        for (int fret = 1; fret <= fretCount(); fret++) {
            int x = (int) Math.round(logicalNutX() + fret * fretWidth());
            g.drawLine(x, top, x, bottom);
        }

        g.setColor(InstrumentColors.NUT);
        g.setStroke(new BasicStroke(3));
        g.drawLine(logicalNutX(), top, logicalNutX(), bottom);
    }

    private void paintStrings(Graphics2D g) {
        g.setColor(InstrumentColors.STRING);
        for (int string = 1; string <= stringCount(); string++) {
            float thickness = 0.8f + (string - 1) * 0.22f;
            g.setStroke(new BasicStroke(thickness));
            int y = stringY(string);
            g.drawLine(logicalNutX(), y, getWidth() - SIDE_MARGIN, y);
        }
    }

    private void paintFretNumbers(Graphics2D g) {
        g.setColor(InstrumentColors.FRET_NUMBER);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        FontMetrics metrics = g.getFontMetrics();
        int y = getHeight() - 4;
        for (int fret : List.of(3, 5, 7, 9, 12, 15, 17, 19, 21, 24)) {
            if (fret > fretCount()) {
                continue;
            }
            String text = String.valueOf(fret);
            g.drawString(text, fretCenterX(fret) - metrics.stringWidth(text) / 2, y);
        }
    }

    private void paintMarkedNotes(Graphics2D g) {
        FretMarks marks = currentMarks();
        int radius = Math.max(7, (int) (stringGap() * 0.44));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));

        paintMarks(g, marks.secondary(), MarkKind.SECONDARY, (int) Math.round(radius * 0.8));
        paintMarks(g, marks.primary(), MarkKind.PRIMARY, radius);
    }

    private void paintMarks(Graphics2D g, Set<FretPosition> positions, MarkKind kind, int radius) {
        FontMetrics metrics = g.getFontMetrics();
        boolean primary = kind == MarkKind.PRIMARY;
        for (FretPosition position : positions) {
            if (position.string() > stringCount() || position.fret() > fretCount()) {
                continue;
            }
            int x = fretCenterX(position.fret());
            int y = stringY(position.string());
            g.setColor(primary ? InstrumentColors.PRESSED : InstrumentColors.CONTEXT);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            if (noteNameMode.shows(kind)) {
                String name = labelFor(position, kind);
                g.setColor(primary ? InstrumentColors.PRESSED_INK : InstrumentColors.CONTEXT_INK);
                g.drawString(
                        name,
                        x - metrics.stringWidth(name) / 2,
                        y + (metrics.getAscent() - metrics.getDescent()) / 2);
            }
        }
    }

    private void paintHover(Graphics2D g) {
        hovered.ifPresent(note -> {
            if (note.string() > stringCount()) {
                return;
            }
            int radius = Math.max(8, (int) (stringGap() * 0.5));
            int x = fretCenterX(note.fret());
            int y = stringY(note.string());
            g.setColor(InstrumentColors.HOVER);
            g.setStroke(new BasicStroke(1.4f));
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        });
    }

    private FretMarks currentMarks() {
        return displayMode.marks(location, fretCount(), scale);
    }
}
