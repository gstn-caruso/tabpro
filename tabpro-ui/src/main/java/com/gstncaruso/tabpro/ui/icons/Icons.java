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

/** El juego de iconos de las barras de herramientas, dibujado con Java2D. */
public final class Icons {

    public static final int SIZE = 18;

    private Icons() {
    }

    // ---- archivo ----------------------------------------------------------

    public static Icon newScore() {
        return icon((graphics, size) -> {
            page(graphics, size);
            plus(graphics, size * 0.68, size * 0.7, size * 0.18);
        });
    }

    public static Icon open() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            Path2D folder = new Path2D.Double();
            folder.moveTo(size * 0.12, size * 0.78);
            folder.lineTo(size * 0.12, size * 0.26);
            folder.lineTo(size * 0.42, size * 0.26);
            folder.lineTo(size * 0.52, size * 0.38);
            folder.lineTo(size * 0.88, size * 0.38);
            folder.lineTo(size * 0.88, size * 0.78);
            folder.closePath();
            graphics.draw(folder);
        });
    }

    public static Icon save() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new RoundRectangle2D.Double(size * 0.15, size * 0.15, size * 0.7, size * 0.7, 3, 3));
            graphics.fill(new Rectangle2D.Double(size * 0.32, size * 0.15, size * 0.36, size * 0.24));
            graphics.draw(new Rectangle2D.Double(size * 0.3, size * 0.55, size * 0.4, size * 0.3));
        });
    }

    public static Icon print() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.28, size * 0.12, size * 0.44, size * 0.22));
            graphics.draw(new RoundRectangle2D.Double(size * 0.12, size * 0.34, size * 0.76, size * 0.34, 3, 3));
            graphics.draw(new Rectangle2D.Double(size * 0.28, size * 0.6, size * 0.44, size * 0.28));
        });
    }

    public static Icon scoreInformation() {
        return icon((graphics, size) -> {
            page(graphics, size);
            graphics.setStroke(thin());
            for (int line = 0; line < 3; line++) {
                double y = size * (0.42 + line * 0.15);
                graphics.draw(new Line2D.Double(size * 0.3, y, size * 0.7, y));
            }
        });
    }

    public static Icon pageSetup() {
        return icon((graphics, size) -> {
            page(graphics, size);
            graphics.setStroke(dashed());
            graphics.draw(new Rectangle2D.Double(size * 0.32, size * 0.28, size * 0.36, size * 0.46));
        });
    }

    // ---- edicion ----------------------------------------------------------

    public static Icon undo() {
        return icon((graphics, size) -> curvedArrow(graphics, size, true));
    }

    public static Icon redo() {
        return icon((graphics, size) -> curvedArrow(graphics, size, false));
    }

    public static Icon cut() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Line2D.Double(size * 0.28, size * 0.14, size * 0.68, size * 0.66));
            graphics.draw(new Line2D.Double(size * 0.72, size * 0.14, size * 0.32, size * 0.66));
            graphics.draw(new Ellipse2D.Double(size * 0.16, size * 0.66, size * 0.22, size * 0.22));
            graphics.draw(new Ellipse2D.Double(size * 0.62, size * 0.66, size * 0.22, size * 0.22));
        });
    }

    public static Icon copy() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.14, size * 0.14, size * 0.48, size * 0.56));
            graphics.draw(new Rectangle2D.Double(size * 0.34, size * 0.3, size * 0.48, size * 0.56));
        });
    }

    public static Icon paste() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new RoundRectangle2D.Double(size * 0.18, size * 0.2, size * 0.64, size * 0.68, 3, 3));
            graphics.fill(new Rectangle2D.Double(size * 0.36, size * 0.1, size * 0.28, size * 0.16));
        });
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
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Line2D.Double(size * 0.24, size * 0.12, size * 0.24, size * 0.88));
            Path2D flag = new Path2D.Double();
            flag.moveTo(size * 0.24, size * 0.16);
            flag.lineTo(size * 0.82, size * 0.28);
            flag.lineTo(size * 0.24, size * 0.44);
            flag.closePath();
            graphics.fill(flag);
        });
    }

    // ---- figuras ----------------------------------------------------------

    public static Icon note(NoteValue value) {
        return icon((graphics, size) -> Glyphs.note(graphics, size * 0.36, size * 0.8, size * 0.34, value, false));
    }

    public static Icon dottedNote() {
        return icon((graphics, size) ->
                Glyphs.note(graphics, size * 0.32, size * 0.8, size * 0.34, NoteValue.QUARTER, true));
    }

    public static Icon rest() {
        return icon((graphics, size) -> Glyphs.quarterRest(graphics, size * 0.5, size * 0.5, size / 13.0));
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
        return icon((graphics, size) -> {
            graphics.setStroke(new BasicStroke(size / 9f));
            graphics.draw(new Line2D.Double(size * 0.28, size * 0.28, size * 0.72, size * 0.72));
            graphics.draw(new Line2D.Double(size * 0.72, size * 0.28, size * 0.28, size * 0.72));
        });
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
        return icon((graphics, size) -> {
            graphics.setFont(big(size));
            graphics.drawString("T", (float) (size * 0.32), (float) (size * 0.78));
        });
    }

    // ---- sonido -----------------------------------------------------------

    public static Icon play() {
        return icon((graphics, size) -> {
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(size * 0.28, size * 0.18);
            triangle.lineTo(size * 0.84, size * 0.5);
            triangle.lineTo(size * 0.28, size * 0.82);
            triangle.closePath();
            graphics.fill(triangle);
        });
    }

    public static Icon stop() {
        return icon((graphics, size) ->
                graphics.fill(new RoundRectangle2D.Double(size * 0.24, size * 0.24, size * 0.52, size * 0.52, 2, 2)));
    }

    public static Icon loop() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new RoundRectangle2D.Double(size * 0.16, size * 0.28, size * 0.68, size * 0.44, size * 0.4, size * 0.4));
            Path2D head = new Path2D.Double();
            head.moveTo(size * 0.52, size * 0.14);
            head.lineTo(size * 0.72, size * 0.28);
            head.lineTo(size * 0.52, size * 0.42);
            head.closePath();
            graphics.fill(head);
        });
    }

    public static Icon metronome() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            Path2D body = new Path2D.Double();
            body.moveTo(size * 0.34, size * 0.16);
            body.lineTo(size * 0.66, size * 0.16);
            body.lineTo(size * 0.84, size * 0.86);
            body.lineTo(size * 0.16, size * 0.86);
            body.closePath();
            graphics.draw(body);
            graphics.draw(new Line2D.Double(size * 0.44, size * 0.82, size * 0.66, size * 0.26));
        });
    }

    public static Icon countDown() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Ellipse2D.Double(size * 0.16, size * 0.16, size * 0.68, size * 0.68));
            graphics.draw(new Line2D.Double(size * 0.5, size * 0.5, size * 0.5, size * 0.28));
            graphics.draw(new Line2D.Double(size * 0.5, size * 0.5, size * 0.68, size * 0.6));
        });
    }

    // ---- navegacion y vista -----------------------------------------------

    public static Icon firstBar() {
        return icon((graphics, size) -> skip(graphics, size, true, true));
    }

    public static Icon previousBar() {
        return icon((graphics, size) -> skip(graphics, size, true, false));
    }

    public static Icon nextBar() {
        return icon((graphics, size) -> skip(graphics, size, false, false));
    }

    public static Icon lastBar() {
        return icon((graphics, size) -> skip(graphics, size, false, true));
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
        return icon((graphics, size) -> magnifier(graphics, size, 1));
    }

    public static Icon zoomOut() {
        return icon((graphics, size) -> magnifier(graphics, size, -1));
    }

    public static Icon zoomReset() {
        return icon((graphics, size) -> magnifier(graphics, size, 0));
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
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new Rectangle2D.Double(size * 0.08, size * 0.3, size * 0.84, size * 0.42));
            for (int key = 1; key < 5; key++) {
                double x = size * (0.08 + key * 0.168);
                graphics.draw(new Line2D.Double(x, size * 0.3, x, size * 0.72));
            }
            graphics.fill(new Rectangle2D.Double(size * 0.2, size * 0.3, size * 0.09, size * 0.24));
            graphics.fill(new Rectangle2D.Double(size * 0.53, size * 0.3, size * 0.09, size * 0.24));
        });
    }

    public static Icon tuner() {
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            graphics.draw(new java.awt.geom.Arc2D.Double(
                    size * 0.12, size * 0.24, size * 0.76, size * 0.76, 20, 140, java.awt.geom.Arc2D.OPEN));
            graphics.draw(new Line2D.Double(size * 0.5, size * 0.78, size * 0.62, size * 0.32));
        });
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
        return icon((graphics, size) -> {
            graphics.setStroke(thin());
            for (int slider = 0; slider < 3; slider++) {
                double x = size * (0.25 + slider * 0.25);
                graphics.draw(new Line2D.Double(x, size * 0.14, x, size * 0.86));
                double knob = size * (0.3 + slider * 0.18);
                graphics.fill(new RoundRectangle2D.Double(x - size * 0.09, knob, size * 0.18, size * 0.1, 2, 2));
            }
        });
    }

    public static Icon addTrack() {
        return icon((graphics, size) -> {
            Glyphs.staff(graphics, size * 0.08, size * 0.2, size * 0.6, size * 0.09);
            plus(graphics, size * 0.78, size * 0.72, size * 0.18);
        });
    }

    // ---- trazos compartidos -----------------------------------------------

    /** La lupa del zoom: con mas, con menos, o vacia para volver al 100%. */
    private static void magnifier(Graphics2D graphics, int size, int sign) {
        graphics.setStroke(thin());
        double diameter = size * 0.52;
        graphics.draw(new Ellipse2D.Double(size * 0.12, size * 0.12, diameter, diameter));
        graphics.draw(new Line2D.Double(size * 0.6, size * 0.6, size * 0.86, size * 0.86));
        double centerX = size * 0.12 + diameter / 2;
        double centerY = size * 0.12 + diameter / 2;
        double arm = size * 0.13;
        if (sign != 0) {
            graphics.draw(new Line2D.Double(centerX - arm, centerY, centerX + arm, centerY));
        }
        if (sign > 0) {
            graphics.draw(new Line2D.Double(centerX, centerY - arm, centerX, centerY + arm));
        }
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

    private static void curvedArrow(Graphics2D graphics, int size, boolean backwards) {
        graphics.setStroke(thin());
        java.awt.geom.Arc2D arc = new java.awt.geom.Arc2D.Double(
                size * 0.18, size * 0.26, size * 0.64, size * 0.5,
                backwards ? 20 : 160, backwards ? 140 : -140, java.awt.geom.Arc2D.OPEN);
        graphics.draw(arc);
        double tipX = backwards ? size * 0.22 : size * 0.78;
        Path2D head = new Path2D.Double();
        head.moveTo(tipX, size * 0.34);
        head.lineTo(tipX + (backwards ? size * 0.18 : -size * 0.18), size * 0.4);
        head.lineTo(tipX + (backwards ? size * 0.04 : -size * 0.04), size * 0.56);
        head.closePath();
        graphics.fill(head);
    }

    private static void skip(Graphics2D graphics, int size, boolean backwards, boolean toTheEnd) {
        double direction = backwards ? -1 : 1;
        double center = size * 0.5;
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(center - direction * size * 0.22, size * 0.2);
        triangle.lineTo(center + direction * size * 0.24, size * 0.5);
        triangle.lineTo(center - direction * size * 0.22, size * 0.8);
        triangle.closePath();
        graphics.fill(triangle);
        if (toTheEnd) {
            double wall = backwards ? size * 0.18 : size * 0.74;
            graphics.fill(new Rectangle2D.Double(wall, size * 0.2, size * 0.08, size * 0.6));
        }
    }

    private static BasicStroke thin() {
        return new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static BasicStroke dashed() {
        return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {2f, 2f}, 0f);
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
}
