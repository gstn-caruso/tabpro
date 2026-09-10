package com.gstncaruso.tabpro.ui.icons;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public final class Glyphs {

    private Glyphs() {
    }

    public static Shape noteHead(double centerX, double centerY, double width, boolean hollow) {
        double height = width * 0.72;
        Shape head = new Ellipse2D.Double(-width / 2, -height / 2, width, hollow ? height : height * 1.02);
        AffineTransform placed = AffineTransform.getTranslateInstance(centerX, centerY);
        placed.rotate(Math.toRadians(-20));
        return placed.createTransformedShape(head);
    }

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

    public static Shape arc(double fromX, double toX, double y, double height) {
        Path2D arc = new Path2D.Double();
        arc.moveTo(fromX, y);
        arc.quadTo((fromX + toX) / 2, y - height, toX, y);
        return arc;
    }

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
