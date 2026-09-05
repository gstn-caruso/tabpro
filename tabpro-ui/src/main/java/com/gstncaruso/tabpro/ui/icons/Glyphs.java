package com.gstncaruso.tabpro.ui.icons;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/** Los trazos de los que estan hechos los simbolos musicales de los botones. */
public final class Glyphs {

    private Glyphs() {
    }

    /** Una cabeza de nota, inclinada como la escriben a mano. */
    public static Shape noteHead(double centerX, double centerY, double width, boolean hollow) {
        double height = width * 0.72;
        Shape head = new Ellipse2D.Double(-width / 2, -height / 2, width, hollow ? height : height * 1.02);
        AffineTransform placed = AffineTransform.getTranslateInstance(centerX, centerY);
        placed.rotate(Math.toRadians(-20));
        return placed.createTransformedShape(head);
    }

    /** Una figura completa: cabeza, plica y corchetes segun su valor. */
    public static void note(Graphics2D graphics, double centerX, double baseY, double scale, NoteValue value, boolean dotted) {
        boolean hollow = value == NoteValue.WHOLE || value == NoteValue.HALF;
        double headWidth = 4.4 * scale;
        Shape head = noteHead(centerX, baseY, headWidth, hollow);
        graphics.setStroke(new BasicStroke((float) (0.9 * scale)));
        if (hollow) {
            graphics.fill(head);
            graphics.setColor(graphics.getBackground());
            AffineTransform inner = AffineTransform.getTranslateInstance(centerX, baseY);
            inner.rotate(Math.toRadians(-20));
            inner.scale(0.5, 0.42);
            graphics.fill(inner.createTransformedShape(new Ellipse2D.Double(-headWidth / 2, -headWidth / 2, headWidth, headWidth)));
            graphics.setColor(graphics.getColor());
        } else {
            graphics.fill(head);
        }
        if (value != NoteValue.WHOLE) {
            double stemX = centerX + headWidth / 2 - 0.4 * scale;
            double stemTop = baseY - 11 * scale;
            graphics.draw(new java.awt.geom.Line2D.Double(stemX, baseY - 0.8 * scale, stemX, stemTop));
            flags(graphics, stemX, stemTop, scale, flagsOf(value));
        }
        if (dotted) {
            double dot = 1.5 * scale;
            graphics.fill(new Ellipse2D.Double(centerX + headWidth * 0.75, baseY - dot / 2, dot, dot));
        }
    }

    private static int flagsOf(NoteValue value) {
        return switch (value) {
            case WHOLE, HALF, QUARTER -> 0;
            case EIGHTH -> 1;
            case SIXTEENTH -> 2;
            case THIRTY_SECOND -> 3;
            case SIXTY_FOURTH -> 4;
        };
    }

    private static void flags(Graphics2D graphics, double stemX, double stemTop, double scale, int count) {
        for (int index = 0; index < count; index++) {
            double y = stemTop + index * 3.0 * scale;
            Path2D flag = new Path2D.Double();
            flag.moveTo(stemX, y);
            flag.quadTo(stemX + 4.5 * scale, y + 1.5 * scale, stemX + 3.2 * scale, y + 6 * scale);
            flag.quadTo(stemX + 3.6 * scale, y + 2.4 * scale, stemX, y + 2.4 * scale);
            flag.closePath();
            graphics.fill(flag);
        }
    }

    /** Un silencio de negra, el que se usa como simbolo de silencio en general. */
    public static void quarterRest(Graphics2D graphics, double centerX, double centerY, double scale) {
        Path2D rest = new Path2D.Double();
        rest.moveTo(centerX - 2 * scale, centerY - 7 * scale);
        rest.lineTo(centerX + 2.2 * scale, centerY - 2 * scale);
        rest.lineTo(centerX - 1.6 * scale, centerY + 1.4 * scale);
        rest.lineTo(centerX + 2.4 * scale, centerY + 6.6 * scale);
        rest.quadTo(centerX - 2.6 * scale, centerY + 3.4 * scale, centerX + 0.6 * scale, centerY + 2.2 * scale);
        rest.lineTo(centerX - 2.6 * scale, centerY - 1.8 * scale);
        rest.closePath();
        graphics.fill(rest);
    }

    /** Un pentagrama chiquito, para los iconos que hablan de la partitura. */
    public static void staff(Graphics2D graphics, double left, double top, double width, double spacing) {
        graphics.setStroke(new BasicStroke(1f));
        for (int line = 0; line < 5; line++) {
            double y = top + line * spacing;
            graphics.draw(new java.awt.geom.Line2D.Double(left, y, left + width, y));
        }
    }

    public static Shape barLine(double x, double top, double bottom, double width) {
        return new Rectangle2D.Double(x, top, width, bottom - top);
    }

    /** El arco de una ligadura o de un slide, segun se lo use. */
    public static Shape arc(double fromX, double toX, double y, double height) {
        Path2D arc = new Path2D.Double();
        arc.moveTo(fromX, y);
        arc.quadTo((fromX + toX) / 2, y - height, toX, y);
        return arc;
    }

    /** La ondita del vibrato. */
    public static Shape wave(double fromX, double toX, double y, double amplitude) {
        Path2D wave = new Path2D.Double();
        wave.moveTo(fromX, y);
        double step = (toX - fromX) / 4;
        for (int index = 0; index < 4; index++) {
            double x = fromX + index * step;
            wave.quadTo(x + step / 2, y + (index % 2 == 0 ? -amplitude : amplitude), x + step, y);
        }
        return wave;
    }

    /** Una flecha, para rasgueos, puas y navegacion. */
    public static Shape arrow(double x, double fromY, double toY, double width) {
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(x, fromY);
        arrow.lineTo(x, toY);
        double direction = Math.signum(toY - fromY);
        arrow.moveTo(x - width, toY - direction * width);
        arrow.lineTo(x, toY);
        arrow.lineTo(x + width, toY - direction * width);
        return arrow;
    }
}
