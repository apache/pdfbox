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
    private final Graphics2D groupG2D;
    private final Graphics2D alphaG2D;

    GroupGraphics(final BufferedImage groupImage, final Graphics2D groupGraphics)
    {
        this.groupImage = groupImage;
        this.groupG2D = groupGraphics;
        this.groupAlphaImage = new BufferedImage(groupImage.getWidth(), groupImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        this.alphaG2D = groupAlphaImage.createGraphics();
    }

    private GroupGraphics(final BufferedImage groupImage, final Graphics2D groupGraphics,
                          final BufferedImage groupAlphaImage, final Graphics2D alphaGraphics)
    {
        this.groupImage = groupImage;
        this.groupG2D = groupGraphics;
        this.groupAlphaImage = groupAlphaImage;
        this.alphaG2D = alphaGraphics;
    }

    @Override
    public void clearRect(final int x, final int y, final int width, final int height)
    {
        groupG2D.clearRect(x, y, width, height);
        alphaG2D.clearRect(x, y, width, height);
    }

    @Override
    public void clipRect(final int x, final int y, final int width, final int height)
    {
        groupG2D.clipRect(x, y, width, height);
        alphaG2D.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(final int x, final int y, final int width, final int height, final int dx, final int dy)
    {
        groupG2D.copyArea(x, y, width, height, dx, dy);
        alphaG2D.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public Graphics create()
    {
        final Graphics g = groupG2D.create();
        final Graphics a = alphaG2D.create();
        if (g instanceof Graphics2D && a instanceof Graphics2D)
        {
            return new GroupGraphics(groupImage, (Graphics2D)g, groupAlphaImage, (Graphics2D)a);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose()
    {
        groupG2D.dispose();
        alphaG2D.dispose();
    }

    @Override
    public void drawArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle)
    {
        groupG2D.drawArc(x, y, width, height, startAngle, arcAngle);
        alphaG2D.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public boolean drawImage(final Image img, final int x, final int y, final Color bgcolor, final ImageObserver observer)
    {
        groupG2D.drawImage(img, x, y, bgcolor, observer);
        return alphaG2D.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(final Image img, final int x, final int y, final ImageObserver observer)
    {
        groupG2D.drawImage(img, x, y, observer);
        return alphaG2D.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(final Image img, final int x, final int y, final int width, final int height,
                             final Color bgcolor, final ImageObserver observer)
    {
        groupG2D.drawImage(img, x, y, width, height, bgcolor, observer);
        return alphaG2D.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(final Image img, final int x, final int y, final int width, final int height, final ImageObserver observer)
    {
        groupG2D.drawImage(img, x, y, width, height, observer);
        return alphaG2D.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1,
                             final int sy1, final int sx2, final int sy2, final Color bgcolor, final ImageObserver observer)
    {
        groupG2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        return alphaG2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1,
                             final int sy1, final int sx2, final int sy2, final ImageObserver observer)
    {
        groupG2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        return alphaG2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2)
    {
        groupG2D.drawLine(x1, y1, x2, y2);
        alphaG2D.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawOval(final int x, final int y, final int width, final int height)
    {
        groupG2D.drawOval(x, y, width, height);
        alphaG2D.drawOval(x, y, width, height);
    }

    @Override
    public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        groupG2D.drawPolygon(xPoints, yPoints, nPoints);
        alphaG2D.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        groupG2D.drawPolyline(xPoints, yPoints, nPoints);
        alphaG2D.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight)
    {
        groupG2D.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        alphaG2D.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawString(final AttributedCharacterIterator iterator, final int x, final int y)
    {
        groupG2D.drawString(iterator, x, y);
        alphaG2D.drawString(iterator, x, y);
    }

    @Override
    public void drawString(final String str, final int x, final int y)
    {
        groupG2D.drawString(str, x, y);
        alphaG2D.drawString(str, x, y);
    }

    @Override
    public void fillArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle)
    {
        groupG2D.fillArc(x, y, width, height, startAngle, arcAngle);
        alphaG2D.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillOval(final int x, final int y, final int width, final int height)
    {
        groupG2D.fillOval(x, y, width, height);
        alphaG2D.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        groupG2D.fillPolygon(xPoints, yPoints, nPoints);
        alphaG2D.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillRect(final int x, final int y, final int width, final int height)
    {
        groupG2D.fillRect(x, y, width, height);
        alphaG2D.fillRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight)
    {
        groupG2D.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        alphaG2D.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public Shape getClip()
    {
        return groupG2D.getClip();
    }

    @Override
    public Rectangle getClipBounds()
    {
        return groupG2D.getClipBounds();
    }

    @Override
    public Color getColor()
    {
        return groupG2D.getColor();
    }

    @Override
    public Font getFont()
    {
        return groupG2D.getFont();
    }

    @Override
    public FontMetrics getFontMetrics(final Font f)
    {
        return groupG2D.getFontMetrics(f);
    }

    @Override
    public void setClip(final int x, final int y, final int width, final int height)
    {
        groupG2D.setClip(x, y, width, height);
        alphaG2D.setClip(x, y, width, height);
    }

    @Override
    public void setClip(final Shape clip)
    {
        groupG2D.setClip(clip);
        alphaG2D.setClip(clip);
    }

    @Override
    public void setColor(final Color c)
    {
        groupG2D.setColor(c);
        alphaG2D.setColor(c);
    }

    @Override
    public void setFont(final Font font)
    {
        groupG2D.setFont(font);
        alphaG2D.setFont(font);
    }

    @Override
    public void setPaintMode()
    {
        groupG2D.setPaintMode();
        alphaG2D.setPaintMode();
    }

    @Override
    public void setXORMode(final Color c1)
    {
        groupG2D.setXORMode(c1);
        alphaG2D.setXORMode(c1);
    }

    @Override
    public void translate(final int x, final int y)
    {
        groupG2D.translate(x, y);
        alphaG2D.translate(x, y);
    }

    @Override
    public void addRenderingHints(final Map<?,?> hints)
    {
        groupG2D.addRenderingHints(hints);
        alphaG2D.addRenderingHints(hints);
    }

    @Override
    public void clip(final Shape s)
    {
        groupG2D.clip(s);
        alphaG2D.clip(s);
    }

    @Override
    public void draw(final Shape s)
    {
        groupG2D.draw(s);
        alphaG2D.draw(s);
    }

    @Override
    public void drawGlyphVector(final GlyphVector g, final float x, final float y)
    {
        groupG2D.drawGlyphVector(g, x, y);
        alphaG2D.drawGlyphVector(g, x, y);
    }

    @Override
    public void drawImage(final BufferedImage img, final BufferedImageOp op, final int x, final int y)
    {
        groupG2D.drawImage(img, op, x, y);
        alphaG2D.drawImage(img, op, x, y);
    }

    @Override
    public boolean drawImage(final Image img, final AffineTransform xform, final ImageObserver obs)
    {
        groupG2D.drawImage(img, xform, obs);
        return alphaG2D.drawImage(img, xform, obs);
    }

    @Override
    public void drawRenderableImage(final RenderableImage img, final AffineTransform xform)
    {
        groupG2D.drawRenderableImage(img, xform);
        alphaG2D.drawRenderableImage(img, xform);
    }

    @Override
    public void drawRenderedImage(final RenderedImage img, final AffineTransform xform)
    {
        groupG2D.drawRenderedImage(img, xform);
        alphaG2D.drawRenderedImage(img, xform);
    }

    @Override
    public void drawString(final AttributedCharacterIterator iterator, final float x, final float y)
    {
        groupG2D.drawString(iterator, x, y);
        alphaG2D.drawString(iterator, x, y);
    }

    @Override
    public void drawString(final String str, final float x, final float y)
    {
        groupG2D.drawString(str, x, y);
        alphaG2D.drawString(str, x, y);
    }

    @Override
    public void fill(final Shape s)
    {
        groupG2D.fill(s);
        alphaG2D.fill(s);
    }

    @Override
    public Color getBackground()
    {
        return groupG2D.getBackground();
    }

    @Override
    public Composite getComposite()
    {
        return groupG2D.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        return groupG2D.getDeviceConfiguration();
    }

    @Override
    public FontRenderContext getFontRenderContext()
    {
        return groupG2D.getFontRenderContext();
    }

    @Override
    public Paint getPaint()
    {
        return groupG2D.getPaint();
    }

    @Override
    public Object getRenderingHint(final RenderingHints.Key hintKey)
    {
        return groupG2D.getRenderingHint(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints()
    {
        return groupG2D.getRenderingHints();
    }

    @Override
    public Stroke getStroke()
    {
        return groupG2D.getStroke();
    }

    @Override
    public AffineTransform getTransform()
    {
        return groupG2D.getTransform();
    }

    @Override
    public boolean hit(final Rectangle rect, final Shape s, final boolean onStroke)
    {
        return groupG2D.hit(rect, s, onStroke);
    }

    @Override
    public void rotate(final double theta)
    {
        groupG2D.rotate(theta);
        alphaG2D.rotate(theta);
    }

    @Override
    public void rotate(final double theta, final double x, final double y)
    {
        groupG2D.rotate(theta, x, y);
        alphaG2D.rotate(theta, x, y);
    }

    @Override
    public void scale(final double sx, final double sy)
    {
        groupG2D.scale(sx, sy);
        alphaG2D.scale(sx, sy);
    }

    @Override
    public void setBackground(final Color color)
    {
        groupG2D.setBackground(color);
        alphaG2D.setBackground(color);
    }

    @Override
    public void setComposite(final Composite comp)
    {
        groupG2D.setComposite(comp);
        alphaG2D.setComposite(comp);
    }

    @Override
    public void setPaint(final Paint paint)
    {
        groupG2D.setPaint(paint);
        alphaG2D.setPaint(paint);
    }

    @Override
    public void setRenderingHint(final RenderingHints.Key hintKey, final Object hintValue)
    {
        groupG2D.setRenderingHint(hintKey, hintValue);
        alphaG2D.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(final Map<?, ?> hints)
    {
        groupG2D.setRenderingHints(hints);
        alphaG2D.setRenderingHints(hints);
    }

    @Override
    public void setStroke(final Stroke s)
    {
        groupG2D.setStroke(s);
        alphaG2D.setStroke(s);
    }

    @Override
    public void setTransform(final AffineTransform tx)
    {
        groupG2D.setTransform(tx);
        alphaG2D.setTransform(tx);
    }

    @Override
    public void shear(final double shx, final double shy)
    {
        groupG2D.shear(shx, shy);
        alphaG2D.shear(shx, shy);
    }

    @Override
    public void transform(final AffineTransform tx)
    {
        groupG2D.transform(tx);
        alphaG2D.transform(tx);
    }

    @Override
    public void translate(final double tx, final double ty)
    {
        groupG2D.translate(tx, ty);
        alphaG2D.translate(tx, ty);
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
    void removeBackdrop(final BufferedImage backdrop, final int offsetX, final int offsetY)
    {
        final int groupWidth = groupImage.getWidth();
        final int groupHeight = groupImage.getHeight();
        final int backdropWidth = backdrop.getWidth();
        final int backdropHeight = backdrop.getHeight();
        final int groupType = groupImage.getType();
        final int groupAlphaType = groupAlphaImage.getType();
        final int backdropType = backdrop.getType();
        final DataBuffer groupDataBuffer = groupImage.getRaster().getDataBuffer();
        final DataBuffer groupAlphaDataBuffer = groupAlphaImage.getRaster().getDataBuffer();
        final DataBuffer backdropDataBuffer = backdrop.getRaster().getDataBuffer();

        if (groupType == BufferedImage.TYPE_INT_ARGB &&
            groupAlphaType == BufferedImage.TYPE_INT_ARGB &&
            (backdropType == BufferedImage.TYPE_INT_ARGB || backdropType == BufferedImage.TYPE_INT_RGB) &&
            groupDataBuffer instanceof DataBufferInt &&
            groupAlphaDataBuffer instanceof DataBufferInt &&
            backdropDataBuffer instanceof DataBufferInt)
        {
            // Optimized computation for int[] buffers.

            final int[] groupData = ((DataBufferInt)groupDataBuffer).getData();
            final int[] groupAlphaData = ((DataBufferInt)groupAlphaDataBuffer).getData();
            final int[] backdropData = ((DataBufferInt)backdropDataBuffer).getData();
            final boolean backdropHasAlpha = backdropType == BufferedImage.TYPE_INT_ARGB;

            for (int y = 0; y < groupHeight; y++)
            {
                for (int x = 0; x < groupWidth; x++)
                {
                    final int index = x + y * groupWidth;

                    // alphagn is the total alpha of the group contents excluding backdrop.
                    final int alphagn = (groupAlphaData[index] >> 24) & 0xFF;
                    if (alphagn == 0)
                    {
                        // Avoid division by 0 and set the result to fully transparent.
                        groupData[index] = 0;
                        continue;
                    }

                    final int backdropX = x + offsetX;
                    final int backdropY = y + offsetY;
                    final int backdropRGB; // color of backdrop pixel
                    final float alpha0; // alpha of backdrop pixel

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
                    final float alphaFactor = alpha0 / (float)alphagn - alpha0 / 255.0f;
                    final int groupRGB = groupData[index]; // color of group pixel

                    // Compute backdrop removal for RGB components.
                    final int r = backdropRemoval(groupRGB, backdropRGB, 16, alphaFactor);
                    final int g = backdropRemoval(groupRGB, backdropRGB, 8, alphaFactor);
                    final int b = backdropRemoval(groupRGB, backdropRGB, 0, alphaFactor);

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
                    final int alphagn = (groupAlphaImage.getRGB(x, y) >> 24) & 0xFF;
                    if (alphagn == 0)
                    {
                        groupImage.setRGB(x, y, 0);
                        continue;
                    }

                    final int backdropX = x + offsetX;
                    final int backdropY = y + offsetY;
                    final int backdropRGB;
                    final float alpha0;
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

                    final int groupRGB = groupImage.getRGB(x, y);
                    final float alphaFactor = alpha0 / alphagn - alpha0 / 255.0f;

                    final int r = backdropRemoval(groupRGB, backdropRGB, 16, alphaFactor);
                    final int g = backdropRemoval(groupRGB, backdropRGB, 8, alphaFactor);
                    final int b = backdropRemoval(groupRGB, backdropRGB, 0, alphaFactor);

                    groupImage.setRGB(x, y, (alphagn << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
    }

    /**
     * Computes the backdrop removal equation.
     * <code>C = Cn + (Cn - C0) * (alpha0 / alphagn - alpha0)</code>
     */
    private int backdropRemoval(final int groupRGB, final int backdropRGB, final int shift, final float alphaFactor)
    {
        final float cn = (groupRGB >> shift) & 0xFF;
        final float c0 = (backdropRGB >> shift) & 0xFF;
        final int c = Math.round(cn + (cn - c0) * alphaFactor);
        return (c < 0) ? 0 : (c > 255 ? 255 : c);
    }
}
