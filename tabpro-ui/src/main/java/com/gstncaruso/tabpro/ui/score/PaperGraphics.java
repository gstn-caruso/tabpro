package com.gstncaruso.tabpro.ui.score;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * El lienzo de una hoja de papel: dibuja lo mismo que le piden, pero cada color que el pintor
 * elige de {@link ScoreColors} entra como se ve en la pantalla oscura y sale como se lee sobre la
 * hoja clara.
 *
 * <p>Los pintores de la partitura eligen sus colores pensando en el fondo oscuro de la pantalla y
 * no saben nada de hojas. El Modo Pagina los dejaba dibujar sobre un lienzo transparente e
 * invertia despues cada pixel, y esa inversion no sabia lo que estaba invirtiendo: servia
 * mientras toda la tinta fuera gris, pero el rectangulito rojo del cambio de parametro, un
 * marcador de color o el compas incompleto llegaban a la hoja con el color cambiado. Traducir el
 * color en el momento en que el pintor lo elige deja que cada cosa se dibuje directamente del
 * color que le toca, y que no quede nada para invertir despues.
 */
final class PaperGraphics extends Graphics2D {

    private final Graphics2D canvas;

    private PaperGraphics(Graphics2D canvas) {
        this.canvas = canvas;
    }

    /** Un pedazo de hoja, recortado y en el origen, listo para que la partitura se dibuje adentro. */
    static Graphics2D over(Graphics2D g, int x, int y, int width, int height) {
        return new PaperGraphics((Graphics2D) g.create(x, y, Math.max(1, width), Math.max(1, height)));
    }

    @Override
    public void setColor(Color color) {
        canvas.setColor(ScoreColors.onPaper(color));
    }

    @Override
    public void setBackground(Color color) {
        canvas.setBackground(ScoreColors.onPaper(color));
    }

    @Override
    public void setPaint(Paint paint) {
        canvas.setPaint(paint instanceof Color color ? ScoreColors.onPaper(color) : paint);
    }

    @Override
    public void setXORMode(Color color) {
        canvas.setXORMode(ScoreColors.onPaper(color));
    }

    @Override
    public Graphics create() {
        return new PaperGraphics((Graphics2D) canvas.create());
    }

    // De aca para abajo es todo plomeria: cada mensaje va tal cual al lienzo de abajo.

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        canvas.addRenderingHints(hints);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        canvas.clearRect(x, y, width, height);
    }

    @Override
    public void clip(Shape shape) {
        canvas.clip(shape);
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        canvas.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        canvas.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void dispose() {
        canvas.dispose();
    }

    @Override
    public void draw(Shape shape) {
        canvas.draw(shape);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        canvas.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawGlyphVector(GlyphVector glyphs, float x, float y) {
        canvas.drawGlyphVector(glyphs, x, y);
    }

    @Override
    public boolean drawImage(Image image, AffineTransform transform, ImageObserver observer) {
        return canvas.drawImage(image, transform, observer);
    }

    @Override
    public void drawImage(BufferedImage image, BufferedImageOp operation, int x, int y) {
        canvas.drawImage(image, operation, x, y);
    }

    @Override
    public boolean drawImage(Image image, int x, int y, ImageObserver observer) {
        return canvas.drawImage(image, x, y, observer);
    }

    @Override
    public boolean drawImage(Image image, int x, int y, Color background, ImageObserver observer) {
        return canvas.drawImage(image, x, y, background, observer);
    }

    @Override
    public boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver observer) {
        return canvas.drawImage(image, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image image, int x, int y, int width, int height, Color background, ImageObserver observer) {
        return canvas.drawImage(image, x, y, width, height, background, observer);
    }

    @Override
    public boolean drawImage(Image image, int toX1, int toY1, int toX2, int toY2, int fromX1, int fromY1, int fromX2, int fromY2, ImageObserver observer) {
        return canvas.drawImage(image, toX1, toY1, toX2, toY2, fromX1, fromY1, fromX2, fromY2, observer);
    }

    @Override
    public boolean drawImage(Image image, int toX1, int toY1, int toX2, int toY2, int fromX1, int fromY1, int fromX2, int fromY2, Color background, ImageObserver observer) {
        return canvas.drawImage(image, toX1, toY1, toX2, toY2, fromX1, fromY1, fromX2, fromY2, background, observer);
    }

    @Override
    public void drawLine(int fromX, int fromY, int toX, int toY) {
        canvas.drawLine(fromX, fromY, toX, toY);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        canvas.drawOval(x, y, width, height);
    }

    @Override
    public void drawPolygon(int[] xs, int[] ys, int points) {
        canvas.drawPolygon(xs, ys, points);
    }

    @Override
    public void drawPolyline(int[] xs, int[] ys, int points) {
        canvas.drawPolyline(xs, ys, points);
    }

    @Override
    public void drawRenderableImage(RenderableImage image, AffineTransform transform) {
        canvas.drawRenderableImage(image, transform);
    }

    @Override
    public void drawRenderedImage(RenderedImage image, AffineTransform transform) {
        canvas.drawRenderedImage(image, transform);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        canvas.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawString(String text, float x, float y) {
        canvas.drawString(text, x, y);
    }

    @Override
    public void drawString(String text, int x, int y) {
        canvas.drawString(text, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator text, float x, float y) {
        canvas.drawString(text, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator text, int x, int y) {
        canvas.drawString(text, x, y);
    }

    @Override
    public void fill(Shape shape) {
        canvas.fill(shape);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        canvas.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        canvas.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(int[] xs, int[] ys, int points) {
        canvas.fillPolygon(xs, ys, points);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        canvas.fillRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        canvas.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public Color getBackground() {
        return canvas.getBackground();
    }

    @Override
    public Shape getClip() {
        return canvas.getClip();
    }

    @Override
    public Rectangle getClipBounds() {
        return canvas.getClipBounds();
    }

    @Override
    public Color getColor() {
        return canvas.getColor();
    }

    @Override
    public Composite getComposite() {
        return canvas.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return canvas.getDeviceConfiguration();
    }

    @Override
    public Font getFont() {
        return canvas.getFont();
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return canvas.getFontMetrics(font);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return canvas.getFontRenderContext();
    }

    @Override
    public Paint getPaint() {
        return canvas.getPaint();
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key key) {
        return canvas.getRenderingHint(key);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return canvas.getRenderingHints();
    }

    @Override
    public Stroke getStroke() {
        return canvas.getStroke();
    }

    @Override
    public AffineTransform getTransform() {
        return canvas.getTransform();
    }

    @Override
    public boolean hit(Rectangle area, Shape shape, boolean onStroke) {
        return canvas.hit(area, shape, onStroke);
    }

    @Override
    public void rotate(double radians) {
        canvas.rotate(radians);
    }

    @Override
    public void rotate(double radians, double x, double y) {
        canvas.rotate(radians, x, y);
    }

    @Override
    public void scale(double x, double y) {
        canvas.scale(x, y);
    }

    @Override
    public void setClip(Shape shape) {
        canvas.setClip(shape);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        canvas.setClip(x, y, width, height);
    }

    @Override
    public void setComposite(Composite composite) {
        canvas.setComposite(composite);
    }

    @Override
    public void setFont(Font font) {
        canvas.setFont(font);
    }

    @Override
    public void setPaintMode() {
        canvas.setPaintMode();
    }

    @Override
    public void setRenderingHint(RenderingHints.Key key, Object value) {
        canvas.setRenderingHint(key, value);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        canvas.setRenderingHints(hints);
    }

    @Override
    public void setStroke(Stroke stroke) {
        canvas.setStroke(stroke);
    }

    @Override
    public void setTransform(AffineTransform transform) {
        canvas.setTransform(transform);
    }

    @Override
    public void shear(double x, double y) {
        canvas.shear(x, y);
    }

    @Override
    public void transform(AffineTransform transform) {
        canvas.transform(transform);
    }

    @Override
    public void translate(double x, double y) {
        canvas.translate(x, y);
    }

    @Override
    public void translate(int x, int y) {
        canvas.translate(x, y);
    }
}
