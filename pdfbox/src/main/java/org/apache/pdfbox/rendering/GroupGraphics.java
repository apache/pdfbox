/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.rendering;

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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Graphics implementation for non-isolated transparency groups.
 * <p>
 * Non-isolated groups require that the group backdrop (copied from parent group or
 * page) is used as the initial contents of the image to which the group is rendered.
 * This allows blend modes to blend the group contents with the graphics behind
 * the group. Finally when the group rendering is done, backdrop removal must be
 * computed (see {@link #removeBackdrop(java.awt.image.BufferedImage, int, int) removeBackdrop}).
 * It ensures the backdrop is not rendered twice on the parent but it leaves the
 * effects of blend modes.
 * <p>
 * This class renders the group contents to two images. <code>groupImage</code> is
 * initialized with the backdrop and group contents are drawn over it.
 * <code>groupAlphaImage</code> is initially fully transparent and it accumulates
 * the total alpha of the group contents excluding backdrop.
 * <p>
 * If a non-isolated group uses only the blend mode Normal, it can be optimized
 * and rendered like an isolated group; backdrop usage and removal are not needed.
 */

class GroupGraphics extends Graphics2D
{
    private final BufferedImage groupImage;
    private final BufferedImage groupAlphaImage;
    private final Graphics2D groupGraphics;
    private final Graphics2D alphaGraphics;

    GroupGraphics(BufferedImage groupImage, Graphics2D groupGraphics)
    {
        this.groupImage = groupImage;
        this.groupGraphics = groupGraphics;
        this.groupAlphaImage = new BufferedImage(groupImage.getWidth(), groupImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        this.alphaGraphics = groupAlphaImage.createGraphics();
    }

    private GroupGraphics(BufferedImage groupImage, Graphics2D groupGraphics,
        BufferedImage groupAlphaImage, Graphics2D alphaGraphics)
    {
        this.groupImage = groupImage;
        this.groupGraphics = groupGraphics;
        this.groupAlphaImage = groupAlphaImage;
        this.alphaGraphics = alphaGraphics;
    }

    @Override
    public void clearRect(int x, int y, int width, int height)
    {
        groupGraphics.clearRect(x, y, width, height);
        alphaGraphics.clearRect(x, y, width, height);
    }

    @Override
    public void clipRect(int x, int y, int width, int height)
    {
        groupGraphics.clipRect(x, y, width, height);
        alphaGraphics.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy)
    {
        groupGraphics.copyArea(x, y, width, height, dx, dy);
        alphaGraphics.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public Graphics create()
    {
        Graphics g = groupGraphics.create();
        Graphics a = alphaGraphics.create();
        if (g instanceof Graphics2D && a instanceof Graphics2D)
        {
            return new GroupGraphics(groupImage, (Graphics2D)g, groupAlphaImage, (Graphics2D)a);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose()
    {
        groupGraphics.dispose();
        alphaGraphics.dispose();
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        groupGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
        alphaGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer)
    {
        groupGraphics.drawImage(img, x, y, bgcolor, observer);
        return alphaGraphics.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer)
    {
        groupGraphics.drawImage(img, x, y, observer);
        return alphaGraphics.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
        Color bgcolor, ImageObserver observer)
    {
        groupGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
        return alphaGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
    {
        groupGraphics.drawImage(img, x, y, width, height, observer);
        return alphaGraphics.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1,
        int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer)
    {
        groupGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        return alphaGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1,
            int sy1, int sx2, int sy2, ImageObserver observer)
    {
        groupGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        return alphaGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2)
    {
        groupGraphics.drawLine(x1, y1, x2, y2);
        alphaGraphics.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawOval(int x, int y, int width, int height)
    {
        groupGraphics.drawOval(x, y, width, height);
        alphaGraphics.drawOval(x, y, width, height);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints)
    {
        groupGraphics.drawPolygon(xPoints, yPoints, nPoints);
        alphaGraphics.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints)
    {
        groupGraphics.drawPolyline(xPoints, yPoints, nPoints);
        alphaGraphics.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        groupGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        alphaGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y)
    {
        groupGraphics.drawString(iterator, x, y);
        alphaGraphics.drawString(iterator, x, y);
    }

    @Override
    public void drawString(String str, int x, int y)
    {
        groupGraphics.drawString(str, x, y);
        alphaGraphics.drawString(str, x, y);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        groupGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
        alphaGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillOval(int x, int y, int width, int height)
    {
        groupGraphics.fillOval(x, y, width, height);
        alphaGraphics.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints)
    {
        groupGraphics.fillPolygon(xPoints, yPoints, nPoints);
        alphaGraphics.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillRect(int x, int y, int width, int height)
    {
        groupGraphics.fillRect(x, y, width, height);
        alphaGraphics.fillRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        groupGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        alphaGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public Shape getClip()
    {
        return groupGraphics.getClip();
    }

    @Override
    public Rectangle getClipBounds()
    {
        return groupGraphics.getClipBounds();
    }

    @Override
    public Color getColor()
    {
        return groupGraphics.getColor();
    }

    @Override
    public Font getFont()
    {
        return groupGraphics.getFont();
    }

    @Override
    public FontMetrics getFontMetrics(Font f)
    {
        return groupGraphics.getFontMetrics(f);
    }

    @Override
    public void setClip(int x, int y, int width, int height)
    {
        groupGraphics.setClip(x, y, width, height);
        alphaGraphics.setClip(x, y, width, height);
    }

    @Override
    public void setClip(Shape clip)
    {
        groupGraphics.setClip(clip);
        alphaGraphics.setClip(clip);
    }

    @Override
    public void setColor(Color c)
    {
        groupGraphics.setColor(c);
        alphaGraphics.setColor(c);
    }

    @Override
    public void setFont(Font font)
    {
        groupGraphics.setFont(font);
        alphaGraphics.setFont(font);
    }

    @Override
    public void setPaintMode()
    {
        groupGraphics.setPaintMode();
        alphaGraphics.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1)
    {
        groupGraphics.setXORMode(c1);
        alphaGraphics.setXORMode(c1);
    }

    @Override
    public void translate(int x, int y)
    {
        groupGraphics.translate(x, y);
        alphaGraphics.translate(x, y);
    }

    @Override
    public void addRenderingHints(Map<?,?> hints)
    {
        groupGraphics.addRenderingHints(hints);
        alphaGraphics.addRenderingHints(hints);
    }

    @Override
    public void clip(Shape s)
    {
        groupGraphics.clip(s);
        alphaGraphics.clip(s);
    }

    @Override
    public void draw(Shape s)
    {
        groupGraphics.draw(s);
        alphaGraphics.draw(s);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y)
    {
        groupGraphics.drawGlyphVector(g, x, y);
        alphaGraphics.drawGlyphVector(g, x, y);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y)
    {
        groupGraphics.drawImage(img, op, x, y);
        alphaGraphics.drawImage(img, op, x, y);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs)
    {
        groupGraphics.drawImage(img, xform, obs);
        return alphaGraphics.drawImage(img, xform, obs);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform)
    {
        groupGraphics.drawRenderableImage(img, xform);
        alphaGraphics.drawRenderableImage(img, xform);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform)
    {
        groupGraphics.drawRenderedImage(img, xform);
        alphaGraphics.drawRenderedImage(img, xform);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y)
    {
        groupGraphics.drawString(iterator, x, y);
        alphaGraphics.drawString(iterator, x, y);
    }

    @Override
    public void drawString(String str, float x, float y)
    {
        groupGraphics.drawString(str, x, y);
        alphaGraphics.drawString(str, x, y);
    }

    @Override
    public void fill(Shape s)
    {
        groupGraphics.fill(s);
        alphaGraphics.fill(s);
    }

    @Override
    public Color getBackground()
    {
        return groupGraphics.getBackground();
    }

    @Override
    public Composite getComposite()
    {
        return groupGraphics.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        return groupGraphics.getDeviceConfiguration();
    }

    @Override
    public FontRenderContext getFontRenderContext()
    {
        return groupGraphics.getFontRenderContext();
    }

    @Override
    public Paint getPaint()
    {
        return groupGraphics.getPaint();
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey)
    {
        return groupGraphics.getRenderingHint(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints()
    {
        return groupGraphics.getRenderingHints();
    }

    @Override
    public Stroke getStroke()
    {
        return groupGraphics.getStroke();
    }

    @Override
    public AffineTransform getTransform()
    {
        return groupGraphics.getTransform();
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke)
    {
        return groupGraphics.hit(rect, s, onStroke);
    }

    @Override
    public void rotate(double theta)
    {
        groupGraphics.rotate(theta);
        alphaGraphics.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y)
    {
        groupGraphics.rotate(theta, x, y);
        alphaGraphics.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy)
    {
        groupGraphics.scale(sx, sy);
        alphaGraphics.scale(sx, sy);
    }

    @Override
    public void setBackground(Color color)
    {
        groupGraphics.setBackground(color);
        alphaGraphics.setBackground(color);
    }

    @Override
    public void setComposite(Composite comp)
    {
        groupGraphics.setComposite(comp);
        alphaGraphics.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint)
    {
        groupGraphics.setPaint(paint);
        alphaGraphics.setPaint(paint);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue)
    {
        groupGraphics.setRenderingHint(hintKey, hintValue);
        alphaGraphics.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints)
    {
        groupGraphics.setRenderingHints(hints);
        alphaGraphics.setRenderingHints(hints);
    }

    @Override
    public void setStroke(Stroke s)
    {
        groupGraphics.setStroke(s);
        alphaGraphics.setStroke(s);
    }

    @Override
    public void setTransform(AffineTransform tx)
    {
        groupGraphics.setTransform(tx);
        alphaGraphics.setTransform(tx);
    }

    @Override
    public void shear(double shx, double shy)
    {
        groupGraphics.shear(shx, shy);
        alphaGraphics.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform tx)
    {
        groupGraphics.transform(tx);
        alphaGraphics.transform(tx);
    }

    @Override
    public void translate(double tx, double ty)
    {
        groupGraphics.translate(tx, ty);
        alphaGraphics.translate(tx, ty);
    }

    /**
     * Computes backdrop removal.
     * The backdrop removal equation is given in section 11.4.4 in the PDF 32000-1:2008
     * standard. It returns the final color <code>C</code> for each pixel in the group:<br>
     *     <code>C = Cn + (Cn - C0) * (alpha0 / alphagn - alpha0)</code><br>
     * where<br>
     *     <code>Cn</code> is the group color including backdrop (read from <code>groupImage</code>),<br>
     *     <code>C0</code> is the backdrop color,<br>
     *     <code>alpha0</code> is the backdrop alpha,<br>
     *     <code>alphagn</code> is the group alpha excluding backdrop (read the
     *           alpha channel from <code>groupAlphaImage</code>)<br>
     * <p>
     * The alpha of the result is equal to <code>alphagn</code>, i.e., the alpha
     * channel of <code>groupAlphaImage</code>.
     * <p>
     * The <code>backdrop</code> image may be much larger than <code>groupImage</code> if,
     * for example, the current page is used as the backdrop. Only a specific rectangular
     * region of <code>backdrop</code> is used in the backdrop removal: upper-left corner
     * is at <code>(offsetX, offsetY)</code>; width and height are equal to those of
     * <code>groupImage</code>.
     *
     * @param backdrop group backdrop
     * @param offsetX backdrop left X coordinate
     * @param offsetY backdrop upper Y coordinate
     */
    void removeBackdrop(BufferedImage backdrop, int offsetX, int offsetY)
    {
        int groupWidth = groupImage.getWidth();
        int groupHeight = groupImage.getHeight();
        int backdropWidth = backdrop.getWidth();
        int backdropHeight = backdrop.getHeight();
        int groupType = groupImage.getType();
        int groupAlphaType = groupAlphaImage.getType();
        int backdropType = backdrop.getType();
        DataBuffer groupDataBuffer = groupImage.getRaster().getDataBuffer();
        DataBuffer groupAlphaDataBuffer = groupAlphaImage.getRaster().getDataBuffer();
        DataBuffer backdropDataBuffer = backdrop.getRaster().getDataBuffer();

        if (groupType == BufferedImage.TYPE_INT_ARGB &&
            groupAlphaType == BufferedImage.TYPE_INT_ARGB &&
            (backdropType == BufferedImage.TYPE_INT_ARGB || backdropType == BufferedImage.TYPE_INT_RGB) &&
            groupDataBuffer instanceof DataBufferInt &&
            groupAlphaDataBuffer instanceof DataBufferInt &&
            backdropDataBuffer instanceof DataBufferInt)
        {
            // Optimized computation for int[] buffers.

            int[] groupData = ((DataBufferInt)groupDataBuffer).getData();
            int[] groupAlphaData = ((DataBufferInt)groupAlphaDataBuffer).getData();
            int[] backdropData = ((DataBufferInt)backdropDataBuffer).getData();
            boolean backdropHasAlpha = backdropType == BufferedImage.TYPE_INT_ARGB;

            for (int y = 0; y < groupHeight; y++)
            {
                for (int x = 0; x < groupWidth; x++)
                {
                    int index = x + y * groupWidth;

                    // alphagn is the total alpha of the group contents excluding backdrop.
                    int alphagn = (groupAlphaData[index] >> 24) & 0xFF;
                    if (alphagn == 0)
                    {
                        // Avoid division by 0 and set the result to fully transparent.
                        groupData[index] = 0;
                        continue;
                    }

                    int backdropX = x + offsetX;
                    int backdropY = y + offsetY;
                    int backdropRGB; // color of backdrop pixel
                    float alpha0; // alpha of backdrop pixel

                    if (backdropX >= 0 && backdropX < backdropWidth &&
                        backdropY >= 0 && backdropY < backdropHeight)
                    {
                        backdropRGB = backdropData[backdropX + backdropY * backdropWidth];
                        alpha0 = backdropHasAlpha ? ((backdropRGB >> 24) & 0xFF) : 255;
                    }
                    else
                    {
                        // Backdrop pixel is out of bounds. Use a transparent value.
                        backdropRGB = 0;
                        alpha0 = 0;
                    }

                    // Alpha factor alpha0 / alphagn - alpha0 is in range 0.0-1.0.
                    float alphaFactor = alpha0 / (float)alphagn - alpha0 / 255.0f;
                    int groupRGB = groupData[index]; // color of group pixel

                    // Compute backdrop removal for RGB components.
                    int r = backdropRemoval(groupRGB, backdropRGB, 16, alphaFactor);
                    int g = backdropRemoval(groupRGB, backdropRGB, 8, alphaFactor);
                    int b = backdropRemoval(groupRGB, backdropRGB, 0, alphaFactor);

                    // Copy the result back to groupImage. The alpha of the result
                    // is equal to alphagn.
                    groupData[index] = (alphagn << 24) | (r << 16) | (g << 8) | b;
                }
            }
        }
        else
        {
            // Non-optimized computation for other types of color spaces and pixel buffers.

            for (int y = 0; y < groupHeight; y++)
            {
                for (int x = 0; x < groupWidth; x++)
                {
                    int alphagn = (groupAlphaImage.getRGB(x, y) >> 24) & 0xFF;
                    if (alphagn == 0)
                    {
                        groupImage.setRGB(x, y, 0);
                        continue;
                    }

                    int backdropX = x + offsetX;
                    int backdropY = y + offsetY;
                    int backdropRGB;
                    float alpha0;
                    if (backdropX >= 0 && backdropX < backdropWidth &&
                        backdropY >= 0 && backdropY < backdropHeight)
                    {
                        backdropRGB = backdrop.getRGB(backdropX, backdropY);
                        alpha0 = (backdropRGB >> 24) & 0xFF;
                    }
                    else
                    {
                        backdropRGB = 0;
                        alpha0 = 0;
                    }

                    int groupRGB = groupImage.getRGB(x, y);
                    float alphaFactor = alpha0 / alphagn - alpha0 / 255.0f;

                    int r = backdropRemoval(groupRGB, backdropRGB, 16, alphaFactor);
                    int g = backdropRemoval(groupRGB, backdropRGB, 8, alphaFactor);
                    int b = backdropRemoval(groupRGB, backdropRGB, 0, alphaFactor);

                    groupImage.setRGB(x, y, (alphagn << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
    }

    /**
     * Computes the backdrop removal equation.
     * <code>C = Cn + (Cn - C0) * (alpha0 / alphagn - alpha0)</code>
     */
    private int backdropRemoval(int groupRGB, int backdropRGB, int shift, float alphaFactor)
    {
        float cn = (groupRGB >> shift) & 0xFF;
        float c0 = (backdropRGB >> shift) & 0xFF;
        int c = Math.round(cn + (cn - c0) * alphaFactor);
        return (c < 0) ? 0 : (c > 255 ? 255 : c);
    }
}
