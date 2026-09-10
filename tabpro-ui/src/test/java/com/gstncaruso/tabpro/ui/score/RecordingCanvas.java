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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecordingCanvas extends Graphics2D {

    private record DrawOrder(double x, double y, double width, double height, Color color, Font font, String text) {

        boolean touches(Rectangle region) {
            return region.intersects(x, y, Math.max(width, 1), Math.max(height, 1));
        }
    }

    public record DrawnText(Font font, String text) {
    }

    private final List<DrawOrder> orders;
    private final Graphics2D delegate;

    public RecordingCanvas() {
        this(new ArrayList<>(), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics());
    }

    public RecordingCanvas(Rectangle clip) {
        this(new ArrayList<>(), clippedCanvas(clip));
    }

    private static Graphics2D clippedCanvas(Rectangle clip) {
        Graphics2D canvas = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        canvas.setClip(clip);
        return canvas;
    }

    private RecordingCanvas(List<DrawOrder> orders, Graphics2D delegate) {
        this.orders = orders;
        this.delegate = delegate;
    }

    public boolean drawsColor(Color color) {
        return orders.stream().anyMatch(order -> order.color().equals(color));
    }

    public boolean drawsColorInRegion(Color color, Rectangle region) {
        return orders.stream().anyMatch(order -> order.color().equals(color) && order.touches(region));
    }

    public boolean writesTextInRegion(String text, Rectangle region) {
        return orders.stream().anyMatch(order -> text.equals(order.text()) && order.touches(region));
    }

    public List<DrawnText> drawnTexts() {
        return orders.stream()
                .filter(order -> order.text() != null)
                .map(order -> new DrawnText(order.font(), order.text()))
                .toList();
    }

    public boolean matchesInRegion(RecordingCanvas other, Rectangle region) {
        return ordersInRegion(region).equals(other.ordersInRegion(region));
    }

    public boolean matches(RecordingCanvas other) {
        return orders.equals(other.orders);
    }

    private List<DrawOrder> ordersInRegion(Rectangle region) {
        return orders.stream().filter(order -> order.touches(region)).toList();
    }

    private void recordOrder(Rectangle2D localBounds, String text) {
        Rectangle2D absoluteBounds = delegate.getTransform().createTransformedShape(localBounds).getBounds2D();
        orders.add(new DrawOrder(absoluteBounds.getX(), absoluteBounds.getY(), absoluteBounds.getWidth(), absoluteBounds.getHeight(),
                delegate.getColor(), delegate.getFont(), text));
    }

    private void recordText(String text, double x, double y) {
        FontMetrics metrics = delegate.getFontMetrics();
        double width = Math.max(metrics.stringWidth(text), 1);
        double height = Math.max(metrics.getAscent() + metrics.getDescent(), 1);
        recordOrder(new Rectangle2D.Double(x, y - metrics.getAscent(), width, height), text);
    }

    @Override
    public void draw(Shape shape) {
        recordOrder(shape.getBounds2D(), null);
    }

    @Override
    public void fill(Shape shape) {
        recordOrder(shape.getBounds2D(), null);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        recordOrder(new Rectangle2D.Double(
                Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)), null);
    }

    @Override
    public void drawString(String text, int x, int y) {
        recordText(text, x, y);
    }

    @Override
    public void drawString(String text, float x, float y) {
        recordText(text, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator text, int x, int y) {
    }

    @Override
    public void drawString(AttributedCharacterIterator text, float x, float y) {
    }

    @Override
    public void drawGlyphVector(GlyphVector glyphs, float x, float y) {
    }

    @Override
    public boolean drawImage(Image image, AffineTransform transform, ImageObserver observer) {
        return true;
    }

    @Override
    public void drawImage(BufferedImage image, BufferedImageOp op, int x, int y) {
    }

    @Override
    public boolean drawImage(Image image, int x, int y, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image image, int x, int y, Color background, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(
            Image image, int x, int y, int width, int height, Color background, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(
            Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
            ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(
            Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
            Color background, ImageObserver observer) {
        return true;
    }

    @Override
    public void drawRenderedImage(RenderedImage image, AffineTransform transform) {
    }

    @Override
    public void drawRenderableImage(RenderableImage image, AffineTransform transform) {
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        recordOrder(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawPolyline(int[] xs, int[] ys, int points) {
    }

    @Override
    public void drawPolygon(int[] xs, int[] ys, int points) {
    }

    @Override
    public void fillPolygon(int[] xs, int[] ys, int points) {
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    @Override
    public boolean hit(Rectangle region, Shape shape, boolean onStroke) {
        return false;
    }

    @Override
    public Graphics create() {
        return new RecordingCanvas(orders, (Graphics2D) delegate.create());
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return delegate.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite composite) {
        delegate.setComposite(composite);
    }

    @Override
    public void setPaint(Paint paint) {
        delegate.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke stroke) {
        delegate.setStroke(stroke);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key key, Object value) {
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key key) {
        return null;
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
    }

    @Override
    public RenderingHints getRenderingHints() {
        return delegate.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        delegate.translate(x, y);
    }

    @Override
    public void translate(double x, double y) {
        delegate.translate(x, y);
    }

    @Override
    public void rotate(double theta) {
        delegate.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        delegate.rotate(theta, x, y);
    }

    @Override
    public void scale(double x, double y) {
        delegate.scale(x, y);
    }

    @Override
    public void shear(double x, double y) {
        delegate.shear(x, y);
    }

    @Override
    public void transform(AffineTransform transform) {
        delegate.transform(transform);
    }

    @Override
    public void setTransform(AffineTransform transform) {
        delegate.setTransform(transform);
    }

    @Override
    public AffineTransform getTransform() {
        return delegate.getTransform();
    }

    @Override
    public Paint getPaint() {
        return delegate.getPaint();
    }

    @Override
    public Composite getComposite() {
        return delegate.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        delegate.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return delegate.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return delegate.getStroke();
    }

    @Override
    public void clip(Shape shape) {
        delegate.clip(shape);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return delegate.getFontRenderContext();
    }

    @Override
    public Color getColor() {
        return delegate.getColor();
    }

    @Override
    public void setColor(Color color) {
        delegate.setColor(color);
    }

    @Override
    public void setPaintMode() {
        delegate.setPaintMode();
    }

    @Override
    public void setXORMode(Color color) {
        delegate.setXORMode(color);
    }

    @Override
    public Font getFont() {
        return delegate.getFont();
    }

    @Override
    public void setFont(Font font) {
        delegate.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return delegate.getFontMetrics(font);
    }

    @Override
    public Rectangle getClipBounds() {
        return delegate.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        delegate.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        delegate.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return delegate.getClip();
    }

    @Override
    public void setClip(Shape shape) {
        delegate.setClip(shape);
    }

    @Override
    public void dispose() {
    }
}
