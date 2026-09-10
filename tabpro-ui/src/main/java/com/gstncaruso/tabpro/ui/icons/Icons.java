package com.gstncaruso.tabpro.ui.icons;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * El juego de iconos de las barras de herramientas: las acciones genericas vienen de un SVG
 * de Tabler Icons y los simbolos propios de la notacion musical se dibujan con Java2D.
 */
public final class Icons {

    public static final int SIZE = 18;

    /** SMuFL U+ECA2 "metNoteWhole": la redonda chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_WHOLE = "";
    /** SMuFL U+ECA3 "metNoteHalfUp": la blanca chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_HALF_UP = "";
    /** SMuFL U+ECA5 "metNoteQuarterUp": la negra chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_QUARTER_UP = "";
    /** SMuFL U+ECA7 "metNote8thUp": la corchea chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_8TH_UP = "";
    /** SMuFL U+ECA9 "metNote16thUp": la semicorchea chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_16TH_UP = "";
    /** SMuFL U+ECAB "metNote32ndUp": la fusa chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_32ND_UP = "";
    /** SMuFL U+ECAD "metNote64thUp": la semifusa chiquita del "figura = numero" de tempo. */
    private static final String MET_NOTE_64TH_UP = "";
    /** SMuFL U+E1E7 "augmentationDot": el puntillo que alarga una figura. */
    private static final String AUGMENTATION_DOT = "";
    /** SMuFL U+E4E5 "restQuarter": el silencio de negra. */
    private static final String REST_QUARTER = "";
    /** SMuFL U+E0A9 "noteheadXBlack": la cabeza en X de percusion, para las notas apagadas. */
    private static final String NOTEHEAD_X_BLACK = "";

    private Icons() {
    }

    // ---- archivo ----------------------------------------------------------

    public static Icon newScore() {
        return svgIcon("file-plus");
    }

    public static Icon open() {
        return svgIcon("folder-open");
    }

    public static Icon save() {
        return svgIcon("device-floppy");
    }

    public static Icon print() {
        return svgIcon("printer");
    }

    public static Icon scoreInformation() {
        return svgIcon("info-circle");
    }

    public static Icon pageSetup() {
        return svgIcon("file-settings");
    }

    // ---- edicion ----------------------------------------------------------

    public static Icon undo() {
        return svgIcon("arrow-back-up");
    }

    public static Icon redo() {
        return svgIcon("arrow-forward-up");
    }

    public static Icon cut() {
        return svgIcon("scissors");
    }

    public static Icon copy() {
        return svgIcon("copy");
    }

    public static Icon paste() {
        return svgIcon("clipboard");
    }

    // ---- compases ---------------------------------------------------------

    public static Icon insertBar() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.1, size * 0.28, size * 0.8, size * 0.11);
            graphics.fill(Glyphs.barLine(size * 0.46, size * 0.28, size * 0.72, size * 0.08));
            plus(graphics, size * 0.78, size * 0.82, size * 0.16);
        });
    }

    public static Icon deleteBar() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.1, size * 0.28, size * 0.8, size * 0.11);
            graphics.fill(Glyphs.barLine(size * 0.46, size * 0.28, size * 0.72, size * 0.08));
            graphics.setStroke(thin());
            graphics.draw(new Line2D.Double(size * 0.68, size * 0.82, size * 0.9, size * 0.82));
        });
    }

    public static Icon repeatOpen() {
        return icon((graphics, size) -> repeat(graphics, size, true));
    }

    public static Icon repeatClose() {
        return icon((graphics, size) -> repeat(graphics, size, false));
    }

    public static Icon alternateEndings() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Line2D.Double(size * 0.16, size * 0.62, size * 0.16, size * 0.3));
            graphics.draw(new Line2D.Double(size * 0.16, size * 0.3, size * 0.84, size * 0.3));
            graphics.setFont(small(size));
            graphics.drawString("1.", (float) (size * 0.24), (float) (size * 0.58));
        });
    }

    public static Icon doubleBar() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.1, size * 0.28, size * 0.8, size * 0.11);
            graphics.fill(Glyphs.barLine(size * 0.56, size * 0.28, size * 0.72, size * 0.06));
            graphics.fill(Glyphs.barLine(size * 0.7, size * 0.28, size * 0.72, size * 0.06));
        });
    }

    public static Icon keySignature() {
        return icon((graphics, size) -> {
            graphics.setFont(big(size));
            graphics.drawString("♯", (float) (size * 0.3), (float) (size * 0.78));
        });
    }

    public static Icon timeSignature() {
        return icon((graphics, size) -> {
            graphics.setFont(small(size));
            graphics.drawString("4", (float) (size * 0.36), (float) (size * 0.46));
            graphics.drawString("4", (float) (size * 0.36), (float) (size * 0.86));
        });
    }

    public static Icon marker() {
        return svgIcon("flag-3");
    }

    // ---- figuras ----------------------------------------------------------

    public static Icon note(NoteValue value) {
        return new GlyphIcon(SIZE, metNoteGlyphOf(value));
    }

    public static Icon dottedNote() {
        return new GlyphIcon(SIZE, MET_NOTE_QUARTER_UP + AUGMENTATION_DOT);
    }

    public static Icon rest() {
        return new GlyphIcon(SIZE, REST_QUARTER);
    }

    /** El corchete de un n-tuplet con su numero: 3 para el tresillo, 5 para el quintillo, etc. */
    public static Icon tuplet(int enters) {
        String label = String.valueOf(enters);
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arc(size * 0.16, size * 0.84, size * 0.66, size * 0.3));
            graphics.setFont(small(size));
            double width = graphics.getFontMetrics().stringWidth(label);
            graphics.drawString(label, (float) (size * 0.5 - width / 2), (float) (size * 0.34));
        });
    }

    public static Icon tie() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arc(size * 0.2, size * 0.8, size * 0.6, size * 0.34));
            graphics.fill(Glyphs.noteHead(size * 0.2, size * 0.66, size * 0.24, false));
            graphics.fill(Glyphs.noteHead(size * 0.8, size * 0.66, size * 0.24, false));
        });
    }

    // ---- efectos ----------------------------------------------------------

    public static Icon letter(String text) {
        return icon((graphics, size) -> {
            graphics.setFont(small(size));
            double width = graphics.getFontMetrics().stringWidth(text);
            graphics.drawString(text, (float) ((size - width) / 2), (float) (size * 0.7));
        });
    }

    public static Icon deadNote() {
        return new GlyphIcon(SIZE, NOTEHEAD_X_BLACK);
    }

    public static Icon ghostNote() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arc(size * 0.22, size * 0.22, size * 0.8, size * 0.9));
            graphics.draw(Glyphs.arc(size * 0.78, size * 0.78, size * 0.8, -size * 0.9));
            graphics.fill(Glyphs.noteHead(size * 0.5, size * 0.5, size * 0.28, false));
        });
    }

    public static Icon accent() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            Path2D accent = new Path2D.Double();
            accent.moveTo(size * 0.22, size * 0.32);
            accent.lineTo(size * 0.78, size * 0.5);
            accent.lineTo(size * 0.22, size * 0.68);
            graphics.draw(accent);
        });
    }

    public static Icon staccato() {
        return icon((graphics, size) -> {
            graphics.fill(Glyphs.noteHead(size * 0.5, size * 0.66, size * 0.34, false));
            graphics.fill(new Ellipse2D.Double(size * 0.44, size * 0.2, size * 0.13, size * 0.13));
        });
    }

    public static Icon vibrato() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.wave(size * 0.14, size * 0.86, size * 0.5, size * 0.16));
        });
    }

    public static Icon wideVibrato() {
        return icon((graphics, size) -> {
            graphics.setStroke(new BasicStroke(size / 10f));
            graphics.draw(Glyphs.wave(size * 0.12, size * 0.88, size * 0.5, size * 0.28));
        });
    }

    public static Icon bend() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            Path2D curve = new Path2D.Double();
            curve.moveTo(size * 0.24, size * 0.82);
            curve.quadTo(size * 0.62, size * 0.78, size * 0.66, size * 0.24);
            graphics.draw(curve);
            graphics.draw(Glyphs.arrow(size * 0.66, size * 0.4, size * 0.16, size * 0.1));
        });
    }

    public static Icon slide() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Line2D.Double(size * 0.24, size * 0.72, size * 0.76, size * 0.3));
            graphics.fill(Glyphs.noteHead(size * 0.2, size * 0.76, size * 0.22, false));
            graphics.fill(Glyphs.noteHead(size * 0.8, size * 0.26, size * 0.22, false));
        });
    }

    public static Icon hammerOn() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arc(size * 0.2, size * 0.8, size * 0.62, size * 0.36));
            graphics.fill(Glyphs.noteHead(size * 0.2, size * 0.7, size * 0.22, false));
            graphics.fill(Glyphs.noteHead(size * 0.8, size * 0.7, size * 0.22, false));
        });
    }

    public static Icon harmonic() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            Path2D diamond = new Path2D.Double();
            diamond.moveTo(size * 0.5, size * 0.24);
            diamond.lineTo(size * 0.76, size * 0.5);
            diamond.lineTo(size * 0.5, size * 0.76);
            diamond.lineTo(size * 0.24, size * 0.5);
            diamond.closePath();
            graphics.draw(diamond);
        });
    }

    public static Icon strokeDown() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arrow(size * 0.5, size * 0.16, size * 0.84, size * 0.16));
        });
    }

    public static Icon strokeUp() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(Glyphs.arrow(size * 0.5, size * 0.84, size * 0.16, size * 0.16));
        });
    }

    public static Icon chordDiagram() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            for (int string = 0; string < 4; string++) {
                double x = size * (0.24 + string * 0.17);
                graphics.draw(new Line2D.Double(x, size * 0.26, x, size * 0.84));
            }
            for (int fret = 0; fret < 3; fret++) {
                double y = size * (0.26 + fret * 0.29);
                graphics.draw(new Line2D.Double(size * 0.24, y, size * 0.75, y));
            }
            graphics.fill(new Ellipse2D.Double(size * 0.36, size * 0.36, size * 0.14, size * 0.14));
        });
    }

    public static Icon text() {
        return svgIcon("typography");
    }

    // ---- sonido -----------------------------------------------------------

    public static Icon play() {
        return svgIcon("player-play");
    }

    public static Icon stop() {
        return svgIcon("player-stop");
    }

    public static Icon loop() {
        return svgIcon("repeat");
    }

    public static Icon metronome() {
        return svgIcon("metronome");
    }

    public static Icon countDown() {
        return svgIcon("clock");
    }

    // ---- navegacion y vista -----------------------------------------------

    public static Icon firstBar() {
        return svgIcon("player-skip-back");
    }

    public static Icon previousBar() {
        return svgIcon("player-track-prev");
    }

    public static Icon nextBar() {
        return svgIcon("player-track-next");
    }

    public static Icon lastBar() {
        return svgIcon("player-skip-forward");
    }

    public static Icon pageMode() {
        return icon((graphics, size) -> {
            page(graphics, size);
            Glyphs.staff(graphics, size * 0.3, size * 0.36, size * 0.4, size * 0.06);
        });
    }

    public static Icon parchmentMode() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new RoundRectangle2D.Double(size * 0.1, size * 0.22, size * 0.8, size * 0.56, 6, 6));
            Glyphs.staff(graphics, size * 0.22, size * 0.36, size * 0.56, size * 0.07);
        });
    }

    public static Icon verticalScreen() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.22, size * 0.12, size * 0.56, size * 0.76));
            Glyphs.staff(graphics, size * 0.3, size * 0.28, size * 0.4, size * 0.06);
            Glyphs.staff(graphics, size * 0.3, size * 0.6, size * 0.4, size * 0.06);
        });
    }

    public static Icon horizontalScreen() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.08, size * 0.26, size * 0.84, size * 0.48));
            Glyphs.staff(graphics, size * 0.16, size * 0.36, size * 0.68, size * 0.06);
        });
    }

    public static Icon zoomIn() {
        return svgIcon("zoom-in");
    }

    public static Icon zoomOut() {
        return svgIcon("zoom-out");
    }

    public static Icon zoomReset() {
        return svgIcon("zoom-reset");
    }

    public static Icon multitrack() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.12, size * 0.16, size * 0.76, size * 0.06);
            Glyphs.staff(graphics, size * 0.12, size * 0.56, size * 0.76, size * 0.06);
        });
    }

    public static Icon fretboard() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.1, size * 0.26, size * 0.8, size * 0.48));
            for (int fret = 1; fret < 4; fret++) {
                double x = size * (0.1 + fret * 0.2);
                graphics.draw(new Line2D.Double(x, size * 0.26, x, size * 0.74));
            }
            graphics.fill(new Ellipse2D.Double(size * 0.44, size * 0.44, size * 0.12, size * 0.12));
        });
    }

    public static Icon keyboard() {
        return svgIcon("piano");
    }

    public static Icon tuner() {
        return svgIcon("gauge");
    }

    public static Icon scales() {
        return icon((graphics, size) -> {
            for (int step = 0; step < 4; step++) {
                double x = size * (0.2 + step * 0.2);
                double y = size * (0.78 - step * 0.14);
                graphics.fill(Glyphs.noteHead(x, y, size * 0.2, false));
            }
        });
    }

    public static Icon mixTable() {
        return svgIcon("adjustments");
    }

    public static Icon addTrack() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.08, size * 0.2, size * 0.6, size * 0.09);
            plus(graphics, size * 0.78, size * 0.72, size * 0.18);
        });
    }

    // ---- trazos compartidos -----------------------------------------------

    private static String metNoteGlyphOf(NoteValue value) {
        return switch (value) {
            case WHOLE -> MET_NOTE_WHOLE;
            case HALF -> MET_NOTE_HALF_UP;
            case QUARTER -> MET_NOTE_QUARTER_UP;
            case EIGHTH -> MET_NOTE_8TH_UP;
            case SIXTEENTH -> MET_NOTE_16TH_UP;
            case THIRTY_SECOND -> MET_NOTE_32ND_UP;
            case SIXTY_FOURTH -> MET_NOTE_64TH_UP;
        };
    }

    private static void page(Graphics2D graphics, int size) {
        graphics.setStroke(thin());
        graphics.draw(new Rectangle2D.Double(size * 0.2, size * 0.1, size * 0.6, size * 0.8));
    }

    private static void plus(Graphics2D graphics, double centerX, double centerY, double arm) {
        graphics.setStroke(new BasicStroke((float) (arm * 0.55)));
        graphics.draw(new Line2D.Double(centerX - arm, centerY, centerX + arm, centerY));
        graphics.draw(new Line2D.Double(centerX, centerY - arm, centerX, centerY + arm));
    }

    private static void repeat(Graphics2D graphics, int size, boolean opening) {
        double x = opening ? size * 0.24 : size * 0.66;
        double dots = opening ? size * 0.56 : size * 0.34;
        Glyphs.staff(graphics, size * 0.1, size * 0.26, size * 0.8, size * 0.12);
        graphics.fill(Glyphs.barLine(x, size * 0.26, size * 0.74, size * 0.09));
        graphics.fill(Glyphs.barLine(x + (opening ? size * 0.12 : -size * 0.06), size * 0.26, size * 0.74, size * 0.05));
        graphics.fill(new Ellipse2D.Double(dots, size * 0.4, size * 0.1, size * 0.1));
        graphics.fill(new Ellipse2D.Double(dots, size * 0.56, size * 0.1, size * 0.1));
    }

    private static BasicStroke thin() {
        return new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Font small(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size * 0.62f));
    }

    private static Font big(int size) {
        return new Font(Font.SERIF, Font.BOLD, Math.round(size * 0.86f));
    }

    private static Icon icon(ToolIcon.Drawing drawing) {
        return new ToolIcon(SIZE, drawing);
    }

    private static Icon svgIcon(String name) {
        return new SvgIcon("/icons/tabler/" + name + ".svg", SIZE);
    }
}
