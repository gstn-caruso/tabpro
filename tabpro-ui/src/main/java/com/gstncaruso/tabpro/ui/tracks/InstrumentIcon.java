package com.gstncaruso.tabpro.ui.tracks;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/** El dibujito que identifica de un vistazo que instrumento toca una pista. */
public final class InstrumentIcon {

    public enum Family {
        KEYS,
        GUITAR,
        BASS,
        STRINGS,
        WIND,
        DRUMS,
        OTHER
    }

    private InstrumentIcon() {
    }

    public static Family familyOf(int program) {
        if (program < 0 || program > 127) {
            throw new IllegalArgumentException("program debe estar entre 0 y 127: " + program);
        }
        return switch (program / 8) {
            case 0, 1, 2 -> Family.KEYS;
            case 3 -> Family.GUITAR;
            case 4 -> Family.BASS;
            case 5, 6 -> Family.STRINGS;
            case 7, 8, 9 -> Family.WIND;
            case 10, 11 -> Family.KEYS;
            case 14 -> Family.DRUMS;
            default -> Family.OTHER;
        };
    }

    public static void paint(Graphics2D g, int program, Color color, double x, double y, double size) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (familyOf(program)) {
            case KEYS -> paintKeys(g, x, y, size);
            case GUITAR -> paintPlucked(g, x, y, size, 0.46);
            case BASS -> paintPlucked(g, x, y, size, 0.36);
            case STRINGS -> paintStrings(g, x, y, size);
            case WIND -> paintWind(g, x, y, size);
            case DRUMS -> paintDrums(g, x, y, size);
            case OTHER -> paintWave(g, x, y, size);
        }
    }

    private static void paintKeys(Graphics2D g, double x, double y, double s) {
        g.draw(new Rectangle2D.Double(x + 0.08 * s, y + 0.26 * s, 0.84 * s, 0.48 * s));
        for (int key = 1; key < 4; key++) {
            double keyX = x + 0.08 * s + key * 0.21 * s;
            g.draw(new Line2D.Double(keyX, y + 0.26 * s, keyX, y + 0.74 * s));
        }
        for (int black = 0; black < 3; black++) {
            double keyX = x + 0.20 * s + black * 0.21 * s;
            g.fill(new Rectangle2D.Double(keyX, y + 0.26 * s, 0.10 * s, 0.28 * s));
        }
    }

    private static void paintPlucked(Graphics2D g, double x, double y, double s, double bodyWidth) {
        double centerX = x + 0.36 * s;
        g.draw(new Ellipse2D.Double(
                centerX - bodyWidth * s / 2, y + 0.44 * s, bodyWidth * s, 0.46 * s));
        g.draw(new Ellipse2D.Double(
                centerX - bodyWidth * s / 2.6, y + 0.30 * s, bodyWidth * s / 1.3, 0.32 * s));
        g.draw(new Line2D.Double(centerX + 0.10 * s, y + 0.34 * s, x + 0.88 * s, y + 0.10 * s));
        g.fill(new Ellipse2D.Double(x + 0.80 * s, y + 0.06 * s, 0.14 * s, 0.14 * s));
    }

    private static void paintStrings(Graphics2D g, double x, double y, double s) {
        Path2D body = new Path2D.Double();
        body.moveTo(x + 0.34 * s, y + 0.28 * s);
        body.curveTo(x + 0.10 * s, y + 0.40 * s, x + 0.14 * s, y + 0.66 * s, x + 0.34 * s, y + 0.74 * s);
        body.curveTo(x + 0.58 * s, y + 0.82 * s, x + 0.62 * s, y + 0.34 * s, x + 0.34 * s, y + 0.28 * s);
        g.draw(body);
        g.draw(new Line2D.Double(x + 0.44 * s, y + 0.30 * s, x + 0.86 * s, y + 0.10 * s));
        g.draw(new Line2D.Double(x + 0.16 * s, y + 0.82 * s, x + 0.90 * s, y + 0.30 * s));
    }

    private static void paintWind(Graphics2D g, double x, double y, double s) {
        Path2D tube = new Path2D.Double();
        tube.moveTo(x + 0.16 * s, y + 0.22 * s);
        tube.curveTo(x + 0.62 * s, y + 0.20 * s, x + 0.30 * s, y + 0.60 * s, x + 0.58 * s, y + 0.66 * s);
        g.draw(tube);
        Path2D bell = new Path2D.Double();
        bell.moveTo(x + 0.56 * s, y + 0.50 * s);
        bell.lineTo(x + 0.92 * s, y + 0.40 * s);
        bell.lineTo(x + 0.92 * s, y + 0.86 * s);
        bell.lineTo(x + 0.56 * s, y + 0.76 * s);
        bell.closePath();
        g.draw(bell);
    }

    private static void paintDrums(Graphics2D g, double x, double y, double s) {
        g.draw(new Ellipse2D.Double(x + 0.14 * s, y + 0.22 * s, 0.72 * s, 0.26 * s));
        g.draw(new Line2D.Double(x + 0.14 * s, y + 0.35 * s, x + 0.14 * s, y + 0.66 * s));
        g.draw(new Line2D.Double(x + 0.86 * s, y + 0.35 * s, x + 0.86 * s, y + 0.66 * s));
        Path2D bottom = new Path2D.Double();
        bottom.moveTo(x + 0.14 * s, y + 0.66 * s);
        bottom.curveTo(x + 0.30 * s, y + 0.84 * s, x + 0.70 * s, y + 0.84 * s, x + 0.86 * s, y + 0.66 * s);
        g.draw(bottom);
    }

    private static void paintWave(Graphics2D g, double x, double y, double s) {
        Path2D wave = new Path2D.Double();
        wave.moveTo(x + 0.10 * s, y + 0.50 * s);
        wave.curveTo(x + 0.28 * s, y + 0.14 * s, x + 0.40 * s, y + 0.86 * s, x + 0.54 * s, y + 0.50 * s);
        wave.curveTo(x + 0.68 * s, y + 0.16 * s, x + 0.78 * s, y + 0.84 * s, x + 0.92 * s, y + 0.50 * s);
        g.draw(wave);
    }
}
