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

/**
 * Un {@link Graphics2D} de mentira para tests de painters: en vez de rasterizar en un
 * {@link BufferedImage}, anota que se pidio dibujar, donde y de que color, y contesta preguntas
 * sobre eso -para que los asserts hablen del dibujo, no de pixeles sueltos.
 *
 * <p>Delega la parte geometrica (transform, clip, fuente) a un {@link Graphics2D} de verdad sobre
 * un lienzo de 1x1 que nunca se pinta: asi {@code getFontMetrics}, {@code getTransform} y el resto
 * siguen siendo exactos, y las coordenadas que se anotan ya vienen traducidas al espacio absoluto
 * del lienzo raiz, sin importar cuantos {@code create(x, y, w, h)} anidados haga el pintor.
 */
public final class LienzoDePrueba extends Graphics2D {

    private record OrdenDeDibujo(double x, double y, double width, double height, Color color, Font fuente, String texto) {

        boolean tocaA(Rectangle region) {
            return region.intersects(x, y, Math.max(width, 1), Math.max(height, 1));
        }
    }

    /** Un texto tal como se pidio dibujar, junto con la fuente que estaba puesta en ese momento. */
    public record TextoDibujado(Font fuente, String texto) {
    }

    private final List<OrdenDeDibujo> ordenes;
    private final Graphics2D delegado;

    public LienzoDePrueba() {
        this(new ArrayList<>(), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics());
    }

    private LienzoDePrueba(List<OrdenDeDibujo> ordenes, Graphics2D delegado) {
        this.ordenes = ordenes;
        this.delegado = delegado;
    }

    /** Si algo se dibujo con exactamente ese color, en cualquier lugar del lienzo. */
    public boolean dibujaColor(Color color) {
        return ordenes.stream().anyMatch(orden -> orden.color().equals(color));
    }

    /** Si algo se dibujo con exactamente ese color, tocando la region dada. */
    public boolean dibujaColorEnRegion(Color color, Rectangle region) {
        return ordenes.stream().anyMatch(orden -> orden.color().equals(color) && orden.tocaA(region));
    }

    /** Si ese texto exacto se escribio tocando la region dada. */
    public boolean escribeTextoEnRegion(String texto, Rectangle region) {
        return ordenes.stream().anyMatch(orden -> texto.equals(orden.texto()) && orden.tocaA(region));
    }

    /** Todo lo que se escribio, cada texto con la fuente que tenia puesta al pedirse. */
    public List<TextoDibujado> textosDibujados() {
        return ordenes.stream()
                .filter(orden -> orden.texto() != null)
                .map(orden -> new TextoDibujado(orden.fuente(), orden.texto()))
                .toList();
    }

    /** Si dentro de esa region se dibujo exactamente lo mismo -mismo orden, forma, color y texto- que en otro lienzo. */
    public boolean coincideEnRegionCon(LienzoDePrueba otro, Rectangle region) {
        return ordenesEnRegion(region).equals(otro.ordenesEnRegion(region));
    }

    /** Si se dibujo exactamente lo mismo -mismo orden, forma, color y texto- que en otro lienzo. */
    public boolean coincideCon(LienzoDePrueba otro) {
        return ordenes.equals(otro.ordenes);
    }

    private List<OrdenDeDibujo> ordenesEnRegion(Rectangle region) {
        return ordenes.stream().filter(orden -> orden.tocaA(region)).toList();
    }

    private void anotar(Rectangle2D limitesLocales, String texto) {
        Rectangle2D absolutos = delegado.getTransform().createTransformedShape(limitesLocales).getBounds2D();
        ordenes.add(new OrdenDeDibujo(absolutos.getX(), absolutos.getY(), absolutos.getWidth(), absolutos.getHeight(),
                delegado.getColor(), delegado.getFont(), texto));
    }

    private void anotarTexto(String texto, double x, double y) {
        FontMetrics metricas = delegado.getFontMetrics();
        double ancho = Math.max(metricas.stringWidth(texto), 1);
        double alto = Math.max(metricas.getAscent() + metricas.getDescent(), 1);
        anotar(new Rectangle2D.Double(x, y - metricas.getAscent(), ancho, alto), texto);
    }

    @Override
    public void draw(Shape shape) {
        anotar(shape.getBounds2D(), null);
    }

    @Override
    public void fill(Shape shape) {
        anotar(shape.getBounds2D(), null);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        anotar(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        anotar(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        anotar(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        anotar(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        anotar(new Rectangle2D.Double(
                Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)), null);
    }

    @Override
    public void drawString(String text, int x, int y) {
        anotarTexto(text, x, y);
    }

    @Override
    public void drawString(String text, float x, float y) {
        anotarTexto(text, x, y);
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
        anotar(new Rectangle2D.Double(x, y, width, height), null);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        anotar(new Rectangle2D.Double(x, y, width, height), null);
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
        return new LienzoDePrueba(ordenes, (Graphics2D) delegado.create());
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return delegado.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite composite) {
        delegado.setComposite(composite);
    }

    @Override
    public void setPaint(Paint paint) {
        delegado.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke stroke) {
        delegado.setStroke(stroke);
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
        return delegado.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        delegado.translate(x, y);
    }

    @Override
    public void translate(double x, double y) {
        delegado.translate(x, y);
    }

    @Override
    public void rotate(double theta) {
        delegado.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        delegado.rotate(theta, x, y);
    }

    @Override
    public void scale(double x, double y) {
        delegado.scale(x, y);
    }

    @Override
    public void shear(double x, double y) {
        delegado.shear(x, y);
    }

    @Override
    public void transform(AffineTransform transform) {
        delegado.transform(transform);
    }

    @Override
    public void setTransform(AffineTransform transform) {
        delegado.setTransform(transform);
    }

    @Override
    public AffineTransform getTransform() {
        return delegado.getTransform();
    }

    @Override
    public Paint getPaint() {
        return delegado.getPaint();
    }

    @Override
    public Composite getComposite() {
        return delegado.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        delegado.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return delegado.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return delegado.getStroke();
    }

    @Override
    public void clip(Shape shape) {
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return delegado.getFontRenderContext();
    }

    @Override
    public Color getColor() {
        return delegado.getColor();
    }

    @Override
    public void setColor(Color color) {
        delegado.setColor(color);
    }

    @Override
    public void setPaintMode() {
        delegado.setPaintMode();
    }

    @Override
    public void setXORMode(Color color) {
        delegado.setXORMode(color);
    }

    @Override
    public Font getFont() {
        return delegado.getFont();
    }

    @Override
    public void setFont(Font font) {
        delegado.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return delegado.getFontMetrics(font);
    }

    @Override
    public Rectangle getClipBounds() {
        return delegado.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
    }

    @Override
    public Shape getClip() {
        return delegado.getClip();
    }

    @Override
    public void setClip(Shape shape) {
    }

    @Override
    public void dispose() {
    }
}
