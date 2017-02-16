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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * Paints a page in a PDF document to a Graphics context. May be subclassed to provide custom
 * rendering.
 * 
 * <p>If you want to do custom graphics processing rather than Graphics2D rendering, then you should
 * subclass PDFGraphicsStreamEngine instead. Subclassing PageDrawer is only suitable for cases
 * where the goal is to render onto a Graphics2D surface.
 * 
 * @author Ben Litchfield
 */
public class PageDrawer extends PDFGraphicsStreamEngine
{
    private static final Log LOG = LogFactory.getLog(PageDrawer.class);

    // parent document renderer - note: this is needed for not-yet-implemented resource caching
    private final PDFRenderer renderer;
    
    // the graphics device to draw to, xform is the initial transform of the device (i.e. DPI)
    private Graphics2D graphics;
    private AffineTransform xform;

    // the page box to draw (usually the crop box but may be another)
    private PDRectangle pageSize;

    private int pageRotation;

    // whether image of a transparency group must be flipped
    // needed when in a tiling pattern
    private boolean flipTG = false;

    // clipping winding rule used for the clipping path
    private int clipWindingRule = -1;
    private GeneralPath linePath = new GeneralPath();

    // last clipping path
    private Area lastClip;

    // buffered clipping area for text being drawn
    private Area textClippingArea;

    // glyph cache
    private final Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();
    
    private final TilingPaintFactory tilingPaintFactory = new TilingPaintFactory(this);

    /**
     * Constructor.
     *
     * @param parameters Parameters for page drawing.
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer(PageDrawerParameters parameters) throws IOException
    {
        super(parameters.getPage());
        this.renderer = parameters.getRenderer();
    }

    /**
     * Returns the parent renderer.
     */
    public final PDFRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Returns the underlying Graphics2D. May be null if drawPage has not yet been called.
     */
    protected final Graphics2D getGraphics()
    {
        return graphics;
    }

    /**
     * Returns the current line path. This is reset to empty after each fill/stroke.
     */
    protected final GeneralPath getLinePath()
    {
        return linePath;
    }

    /**
     * Sets high-quality rendering hints on the current Graphics2D.
     */
    private void setRenderingHints()
    {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Draws the page to the requested context.
     * 
     * @param g The graphics context to draw onto.
     * @param pageSize The size of the page to draw.
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage(Graphics g, PDRectangle pageSize) throws IOException
    {
        graphics = (Graphics2D) g;
        xform = graphics.getTransform();
        this.pageSize = pageSize;
        pageRotation = getPage().getRotation() % 360;

        setRenderingHints();

        graphics.translate(0, pageSize.getHeight());
        graphics.scale(1, -1);

        // TODO use getStroke() to set the initial stroke
        graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // adjust for non-(0,0) crop box
        graphics.translate(-pageSize.getLowerLeftX(), -pageSize.getLowerLeftY());

        processPage(getPage());

        for (PDAnnotation annotation : getPage().getAnnotations())
        {
            showAnnotation(annotation);
        }

        graphics = null;
    }

    /**
     * Draws the pattern stream to the requested context.
     *
     * @param g The graphics context to draw onto.
     * @param pattern The tiling pattern to be used.
     * @param colorSpace color space for this tiling.
     * @param color color for this tiling.
     * @param patternMatrix the pattern matrix
     * @throws IOException If there is an IO error while drawing the page.
     */
    void drawTilingPattern(Graphics2D g, PDTilingPattern pattern, PDColorSpace colorSpace,
                                  PDColor color, Matrix patternMatrix) throws IOException
    {
        Graphics2D oldGraphics = graphics;
        graphics = g;

        GeneralPath oldLinePath = linePath;
        linePath = new GeneralPath();
        int oldClipWindingRule = clipWindingRule;
        clipWindingRule = -1;

        Area oldLastClip = lastClip;
        lastClip = null;

        boolean oldFlipTG = flipTG;
        flipTG = true;

        setRenderingHints();
        processTilingPattern(pattern, color, colorSpace, patternMatrix);

        flipTG = oldFlipTG;
        graphics = oldGraphics;
        linePath = oldLinePath;
        lastClip = oldLastClip;
        clipWindingRule = oldClipWindingRule;
    }

    private float clampColor(float color)
    {
        return color < 0 ? 0 : (color > 1 ? 1 : color);        
    }

    /**
     * Returns an AWT paint for the given PDColor.
     * 
     * @param color The color to get a paint for. This can be an actual color or a pattern.
     * @throws IOException
     */
    protected Paint getPaint(PDColor color) throws IOException
    {
        PDColorSpace colorSpace = color.getColorSpace();
        if (!(colorSpace instanceof PDPattern))
        {
            float[] rgb = colorSpace.toRGB(color.getComponents());
            return new Color(clampColor(rgb[0]), clampColor(rgb[1]), clampColor(rgb[2]));
        }
        else
        {
            PDPattern patternSpace = (PDPattern)colorSpace;
            PDAbstractPattern pattern = patternSpace.getPattern(color);
            if (pattern instanceof PDTilingPattern)
            {
                PDTilingPattern tilingPattern = (PDTilingPattern) pattern;

                if (tilingPattern.getPaintType() == PDTilingPattern.PAINT_COLORED)
                {
                    // colored tiling pattern
                    return tilingPaintFactory.create(tilingPattern, null, null, xform);
                }
                else
                {
                    // uncolored tiling pattern
                    return tilingPaintFactory.create(tilingPattern, 
                            patternSpace.getUnderlyingColorSpace(), color, xform);
                }
            }
            else
            {
                PDShadingPattern shadingPattern = (PDShadingPattern)pattern;
                PDShading shading = shadingPattern.getShading();
                if (shading == null)
                {
                    LOG.error("shadingPattern is null, will be filled with transparency");
                    return new Color(0,0,0,0);
                }
                return shading.toPaint(Matrix.concatenate(getInitialMatrix(),
                                                          shadingPattern.getMatrix()));

            }
        }
    }

    // sets the clipping path using caching for performance, we track lastClip manually because
    // Graphics2D#getClip() returns a new object instead of the same one passed to setClip
    private void setClip()
    {
        Area clippingPath = getGraphicsState().getCurrentClippingPath();
        if (clippingPath != lastClip)
        {
            graphics.setClip(clippingPath);
            lastClip = clippingPath;
        }
    }

    @Override
    public void beginText() throws IOException
    {
        setClip();
        beginTextClip();
    }

    @Override
    public void endText() throws IOException
    {
        endTextClip();
    }
    
    /**
     * Begin buffering the text clipping path, if any.
     */
    private void beginTextClip()
    {
        // buffer the text clip because it represents a single clipping area
        textClippingArea = new Area();        
    }

    /**
     * End buffering the text clipping path, if any.
     */
    private void endTextClip()
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();
        
        // apply the buffered clip as one area
        if (renderingMode.isClip() && !textClippingArea.isEmpty())
        {
            state.intersectClippingPath(textClippingArea);
            textClippingArea = null;

            // PDFBOX-3681: lastClip needs to be reset, because after intersection it is still the same 
            // object, thus setClip() would believe that it is cached.
            lastClip = null;
        }
    }

    @Override
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException
    {
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());

        Glyph2D glyph2D = createGlyph2D(font);
        drawGlyph2D(glyph2D, font, code, displacement, at);
    }

    /**
     * Render the font using the Glyph2D interface.
     * 
     * @param glyph2D the Glyph2D implementation provided a GeneralPath for each glyph
     * @param font the font
     * @param code character code
     * @param displacement the glyph's displacement (advance)
     * @param at the transformation
     * @throws IOException if something went wrong
     */
    private void drawGlyph2D(Glyph2D glyph2D, PDFont font, int code, Vector displacement,
                             AffineTransform at) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();

        GeneralPath path = glyph2D.getPathForCharacterCode(code);
        if (path != null)
        {
            // stretch non-embedded glyph if it does not match the width contained in the PDF
            if (!font.isEmbedded())
            {
                float fontWidth = font.getWidthFromFont(code);
                if (fontWidth > 0 && // ignore spaces
                        Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001)
                {
                    float pdfWidth = displacement.getX() * 1000;
                    at.scale(pdfWidth / fontWidth, 1);
                }
            }

            // render glyph
            Shape glyph = at.createTransformedShape(path);

            if (renderingMode.isFill())
            {
                graphics.setComposite(state.getNonStrokingJavaComposite());
                graphics.setPaint(getNonStrokingPaint());
                setClip();
                graphics.fill(glyph);
            }

            if (renderingMode.isStroke())
            {
                graphics.setComposite(state.getStrokingJavaComposite());
                graphics.setPaint(getStrokingPaint());
                graphics.setStroke(getStroke());
                setClip();
                graphics.draw(glyph);
            }

            if (renderingMode.isClip())
            {
                textClippingArea.add(new Area(glyph));
            }
        }
    }

    /**
     * Provide a Glyph2D for the given font.
     * 
     * @param font the font
     * @return the implementation of the Glyph2D interface for the given font
     * @throws IOException if something went wrong
     */
    private Glyph2D createGlyph2D(PDFont font) throws IOException
    {
        Glyph2D glyph2D = fontGlyph2D.get(font);
        // Is there already a Glyph2D for the given font?
        if (glyph2D != null)
        {
            return glyph2D;
        }

        if (font instanceof PDTrueTypeFont)
        {
            PDTrueTypeFont ttfFont = (PDTrueTypeFont)font;
            glyph2D = new TTFGlyph2D(ttfFont);  // TTF is never null
        }
        else if (font instanceof PDType1Font)
        {
            PDType1Font pdType1Font = (PDType1Font)font;
            glyph2D = new Type1Glyph2D(pdType1Font); // T1 is never null
        }
        else if (font instanceof PDType1CFont)
        {
            PDType1CFont type1CFont = (PDType1CFont)font;
            glyph2D = new Type1Glyph2D(type1CFont);
        }
        else if (font instanceof PDType0Font)
        {
            PDType0Font type0Font = (PDType0Font) font;
            if (type0Font.getDescendantFont() instanceof PDCIDFontType2)
            {
                glyph2D = new TTFGlyph2D(type0Font); // TTF is never null
            }
            else if (type0Font.getDescendantFont() instanceof PDCIDFontType0)
            {
                // a Type0 CIDFont contains CFF font
                PDCIDFontType0 cidType0Font = (PDCIDFontType0)type0Font.getDescendantFont();
                glyph2D = new CIDType0Glyph2D(cidType0Font); // todo: could be null (need incorporate fallback)
            }
        }
        else
        {
            throw new IllegalStateException("Bad font type: " + font.getClass().getSimpleName());
        }

        // cache the Glyph2D instance
        if (glyph2D != null)
        {
            fontGlyph2D.put(font, glyph2D);
        }

        if (glyph2D == null)
        {
            // todo: make sure this never happens
            throw new UnsupportedOperationException("No font for " + font.getName());
        }

        return glyph2D;
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)
    {
        // to ensure that the path is created in the right direction, we have to create
        // it by combining single lines instead of creating a simple rectangle
        linePath.moveTo((float) p0.getX(), (float) p0.getY());
        linePath.lineTo((float) p1.getX(), (float) p1.getY());
        linePath.lineTo((float) p2.getX(), (float) p2.getY());
        linePath.lineTo((float) p3.getX(), (float) p3.getY());

        // close the subpath instead of adding the last line so that a possible set line
        // cap style isn't taken into account at the "beginning" of the rectangle
        linePath.closePath();
    }

    //TODO: move soft mask apply to getPaint()?
    private Paint applySoftMaskToPaint(Paint parentPaint, PDSoftMask softMask) throws IOException
    {
        if (softMask == null || softMask.getGroup() == null)
        {
            return parentPaint;
        }
        PDColor backdropColor = null;
        if (COSName.LUMINOSITY.equals(softMask.getSubType()))
        {
            COSArray backdropColorArray = softMask.getBackdropColor();
            PDColorSpace colorSpace = softMask.getGroup().getGroup().getColorSpace();
            if (colorSpace != null && backdropColorArray != null)
            {
                backdropColor = new PDColor(backdropColorArray, colorSpace);
            }
        }
        TransparencyGroup transparencyGroup = new TransparencyGroup(softMask.getGroup(), true, 
                softMask.getInitialTransformationMatrix(), backdropColor);
        BufferedImage image = transparencyGroup.getImage();
        if (image == null)
        {
            // Adobe Reader ignores empty softmasks instead of using bc color
            // sample file: PDFJS-6967_reduced_outside_softmask.pdf
            return parentPaint;
        }
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        if (COSName.ALPHA.equals(softMask.getSubType()))
        {
            gray.setData(image.getAlphaRaster());
        }
        else if (COSName.LUMINOSITY.equals(softMask.getSubType()))
        {
            Graphics g = gray.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        else
        {
            throw new IOException("Invalid soft mask subtype.");
        }
        gray = getRotatedImage(gray);
        Rectangle2D tpgBounds = transparencyGroup.getBounds();
        adjustRectangle(tpgBounds);
        return new SoftMask(parentPaint, gray, tpgBounds, backdropColor, softMask.getTransferFunction());
    }

    // this adjusts the rectangle to the rotated image to put the soft mask at the correct position
    //TODO after all transparency problems have been solved:
    // 1. shouldn't this be done in transparencyGroup.getBounds() ?
    // 2. change transparencyGroup.getBounds() to getOrigin(), because size isn't used in SoftMask
    // 3. Is it possible to create the softmask and transparency group in the correct rotation?
    //    (needs rendering identity testing before committing!)
    private void adjustRectangle(Rectangle2D r)
    {
        Matrix m = new Matrix(xform);
        if (pageRotation == 90)
        {
            r.setRect(pageSize.getHeight() * m.getScalingFactorY() - r.getY() - r.getHeight(), 
                      r.getX(), 
                      r.getWidth(), 
                      r.getHeight());
        }
        if (pageRotation == 180)
        {
            r.setRect(pageSize.getWidth() * m.getScalingFactorX() - r.getX() - r.getWidth(),
                      pageSize.getHeight() * m.getScalingFactorY() - r.getY() - r.getHeight(),
                      r.getWidth(),
                      r.getHeight());
        }
        if (pageRotation == 270)
        {
            r.setRect(r.getY(), 
                      pageSize.getWidth() * m.getScalingFactorX() - r.getX() - r.getWidth(), 
                      r.getWidth(), 
                      r.getHeight());
        }
    }

    // return quadrant-rotated image with adjusted size
    private BufferedImage getRotatedImage(BufferedImage gray) throws IOException
    {
        BufferedImage gray2;
        AffineTransform at;
        switch (pageRotation % 360)
        {
            case 90:
                gray2 = new BufferedImage(gray.getHeight(), gray.getWidth(), BufferedImage.TYPE_BYTE_GRAY);
                at = AffineTransform.getQuadrantRotateInstance(1, gray.getHeight() / 2d, gray.getHeight() / 2d);
                break;
            case 180:
                gray2 = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                at = AffineTransform.getQuadrantRotateInstance(2, gray.getWidth()/ 2d, gray.getHeight() / 2d);
                break;
            case 270:
                gray2 = new BufferedImage(gray.getHeight(), gray.getWidth(), BufferedImage.TYPE_BYTE_GRAY);
                at = AffineTransform.getQuadrantRotateInstance(3, gray.getWidth()/ 2d, gray.getWidth() / 2d);
                break;
            default:
                return gray;
        }
        Graphics2D g2 = (Graphics2D) gray2.getGraphics();
        g2.drawImage(gray, at, null);
        g2.dispose();
        return gray2;
    }

    // returns the stroking AWT Paint
    private Paint getStrokingPaint() throws IOException
    {
        return applySoftMaskToPaint(
                getPaint(getGraphicsState().getStrokingColor()),
                getGraphicsState().getSoftMask());
    }

    // returns the non-stroking AWT Paint
    private Paint getNonStrokingPaint() throws IOException
    {
        return applySoftMaskToPaint(
                getPaint(getGraphicsState().getNonStrokingColor()),
                getGraphicsState().getSoftMask());
    }

    // create a new stroke based on the current CTM and the current stroke
    private BasicStroke getStroke()
    {
        PDGraphicsState state = getGraphicsState();

        // apply the CTM
        float lineWidth = transformWidth(state.getLineWidth());

        // minimum line width as used by Adobe Reader
        if (lineWidth < 0.25)
        {
            lineWidth = 0.25f;
        }

        PDLineDashPattern dashPattern = state.getLineDashPattern();
        int phaseStart = dashPattern.getPhase();
        float[] dashArray = dashPattern.getDashArray();
        if (dashArray != null)
        {
            // apply the CTM
            for (int i = 0; i < dashArray.length; ++i)
            {
                // minimum line dash width avoids JVM crash, see PDFBOX-2373, PDFBOX-2929, PDFBOX-3204
                float w = transformWidth(dashArray[i]);
                if (w != 0)
                {
                    dashArray[i] = Math.max(w, 0.035f);
                }
            }
            phaseStart = (int)transformWidth(phaseStart);

            // empty dash array is illegal
            // avoid also infinite and NaN values (PDFBOX-3360)
            if (dashArray.length == 0 || Float.isInfinite(phaseStart) || Float.isNaN(phaseStart))
            {
                dashArray = null;
            }
            else
            {
                for (int i = 0; i < dashArray.length; ++i)
                {
                    if (Float.isInfinite(dashArray[i]) || Float.isNaN(dashArray[i]))
                    {
                        dashArray = null;
                        break;
                    }
                }
            }
        }
        return new BasicStroke(lineWidth, state.getLineCap(), state.getLineJoin(),
                               state.getMiterLimit(), dashArray, phaseStart);
    }

    @Override
    public void strokePath() throws IOException
    {
        graphics.setComposite(getGraphicsState().getStrokingJavaComposite());
        graphics.setPaint(getStrokingPaint());
        graphics.setStroke(getStroke());
        setClip();
        //TODO bbox of shading pattern should be used here? (see fillPath)
        graphics.draw(linePath);
        linePath.reset();
    }

    @Override
    public void fillPath(int windingRule) throws IOException
    {
        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        graphics.setPaint(getNonStrokingPaint());
        setClip();
        linePath.setWindingRule(windingRule);

        // disable anti-aliasing for rectangular paths, this is a workaround to avoid small stripes
        // which occur when solid fills are used to simulate piecewise gradients, see PDFBOX-2302
        // note that we ignore paths with a width/height under 1 as these are fills used as strokes,
        // see PDFBOX-1658 for an example
        Rectangle2D bounds = linePath.getBounds2D();
        boolean noAntiAlias = isRectangular(linePath) && bounds.getWidth() > 1 &&
                                                         bounds.getHeight() > 1;
        if (noAntiAlias)
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        if (!(graphics.getPaint() instanceof Color))
        {
            // apply clip to path to avoid oversized device bounds in shading contexts (PDFBOX-2901)
            Area area = new Area(linePath);
            area.intersect(new Area(graphics.getClip()));
            intersectShadingBBox(getGraphicsState().getNonStrokingColor(), area);
            graphics.fill(area);
        }
        else
        {
            graphics.fill(linePath);
        }
        
        linePath.reset();

        if (noAntiAlias)
        {
            // JDK 1.7 has a bug where rendering hints are reset by the above call to
            // the setRenderingHint method, so we re-set all hints, see PDFBOX-2302
            setRenderingHints();
        }
    }

    // checks whether this is a shading pattern and if yes,
    // get the transformed BBox and intersect with current paint area
    // need to do it here and not in shading getRaster() because it may have been rotated
    private void intersectShadingBBox(PDColor color, Area area) throws IOException
    {
        if (color.getColorSpace() instanceof PDPattern)
        {
            PDColorSpace colorSpace = color.getColorSpace();
            PDAbstractPattern pat = ((PDPattern) colorSpace).getPattern(color);
            if (pat instanceof PDShadingPattern)
            {
                PDShading shading = ((PDShadingPattern) pat).getShading();
                PDRectangle bbox = shading.getBBox();
                if (bbox != null)
                {
                    Matrix m = Matrix.concatenate(getInitialMatrix(), pat.getMatrix());
                    Area bboxArea = new Area(bbox.transform(m));
                    area.intersect(bboxArea);
                }
            }
        }
    }

    /**
     * Returns true if the given path is rectangular.
     */
    private boolean isRectangular(GeneralPath path)
    {
        PathIterator iter = path.getPathIterator(null);
        double[] coords = new double[6];
        int count = 0;
        int[] xs = new int[4];
        int[] ys = new int[4];
        while (!iter.isDone())
        {
            switch(iter.currentSegment(coords))
            {
                case PathIterator.SEG_MOVETO:
                    if (count == 0)
                    {
                        xs[count] = (int)Math.floor(coords[0]);
                        ys[count] = (int)Math.floor(coords[1]);
                    }
                    else
                    {
                        return false;
                    }
                    count++;
                    break;

                case PathIterator.SEG_LINETO:
                    if (count < 4)
                    {
                        xs[count] = (int)Math.floor(coords[0]);
                        ys[count] = (int)Math.floor(coords[1]);
                    }
                    else
                    {
                        return false;
                    }
                    count++;
                    break;

                case PathIterator.SEG_CUBICTO:
                    return false;

                case PathIterator.SEG_CLOSE:
                    break;
            }
            iter.next();
        }

        if (count == 4)
        {
            return xs[0] == xs[1] || xs[0] == xs[2] ||
                   ys[0] == ys[1] || ys[0] == ys[3];
        }
        return false;
    }

    /**
     * Fills and then strokes the path.
     *
     * @param windingRule The winding rule this path will use.
     * @throws IOException If there is an IO error while filling the path.
     */
    @Override
    public void fillAndStrokePath(int windingRule) throws IOException
    {
        // TODO can we avoid cloning the path?
        GeneralPath path = (GeneralPath)linePath.clone();
        fillPath(windingRule);
        linePath = path;
        strokePath();
    }

    @Override
    public void clip(int windingRule)
    {
        // the clipping path will not be updated until the succeeding painting operator is called
        clipWindingRule = windingRule;
    }

    @Override
    public void moveTo(float x, float y)
    {
        linePath.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y)
    {
        linePath.lineTo(x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
    {
        linePath.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public Point2D getCurrentPoint()
    {
        return linePath.getCurrentPoint();
    }

    @Override
    public void closePath()
    {
        linePath.closePath();
    }

    @Override
    public void endPath()
    {
        if (clipWindingRule != -1)
        {
            linePath.setWindingRule(clipWindingRule);
            getGraphicsState().intersectClippingPath(linePath);
            clipWindingRule = -1;
        }
        linePath.reset();
    }
    
    @Override
    public void drawImage(PDImage pdImage) throws IOException
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();

        if (!pdImage.getInterpolate())
        {
            Matrix m = new Matrix(xform);
            m.concatenate(ctm);
            boolean isScaledUp = pdImage.getWidth() < Math.round(Math.abs(m.getScalingFactorX())) ||
                                pdImage.getHeight() < Math.round(Math.abs(m.getScalingFactorY()));

            // if the image is scaled down, we use smooth interpolation, eg PDFBOX-2364
            // only when scaled up do we use nearest neighbour, eg PDFBOX-2302 / mori-cvpr01.pdf
            // stencils are excluded from this rule (see survey.pdf)
            if (isScaledUp || pdImage.isStencil())
            {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }
        }

        if (pdImage.isStencil())
        {
            if (getGraphicsState().getNonStrokingColor().getColorSpace() instanceof PDPattern)
            {
                // The earlier code for stencils (see "else") doesn't work with patterns because the
                // CTM is not taken into consideration.
                // this code is based on the fact that it is easily possible to draw the mask and 
                // the paint at the correct place with the existing code, but not in one step.
                // Thus what we do is to draw both in separate images, then combine the two and draw
                // the result. 
                // Note that the device scale is not used. In theory, some patterns can get better
                // at higher resolutions but the stencil would become more and more "blocky".
                // If anybody wants to do this, have a look at the code in showTransparencyGroup().

                // draw the paint
                Paint paint = getNonStrokingPaint();
                Rectangle2D unitRect = new Rectangle2D.Float(0, 0, 1, 1);
                Rectangle2D bounds = at.createTransformedShape(unitRect).getBounds2D();
                BufferedImage renderedPaint = 
                        new BufferedImage((int) Math.ceil(bounds.getWidth()), 
                                          (int) Math.ceil(bounds.getHeight()), 
                                           BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) renderedPaint.getGraphics();
                g.translate(-bounds.getMinX(), -bounds.getMinY());
                g.setPaint(paint);
                g.fill(bounds);
                g.dispose();

                // draw the mask
                BufferedImage mask = pdImage.getImage();
                BufferedImage renderedMask = new BufferedImage((int) Math.ceil(bounds.getWidth()), 
                                                               (int) Math.ceil(bounds.getHeight()), 
                                                               BufferedImage.TYPE_INT_RGB);
                g = (Graphics2D) renderedMask.getGraphics();
                g.translate(-bounds.getMinX(), -bounds.getMinY());
                AffineTransform imageTransform = new AffineTransform(at);
                imageTransform.scale(1.0 / mask.getWidth(), -1.0 / mask.getHeight());
                imageTransform.translate(0, -mask.getHeight());
                g.drawImage(mask, imageTransform, null);
                g.dispose();

                // apply the mask
                final int[] transparent = new int[4];
                int[] alphaPixel = null;
                WritableRaster raster = renderedPaint.getRaster();
                WritableRaster alpha = renderedMask.getRaster();
                int h = renderedMask.getRaster().getHeight();
                int w = renderedMask.getRaster().getWidth();
                for (int y = 0; y < h; y++)
                {
                    for (int x = 0; x < w; x++)
                    {
                        alphaPixel = alpha.getPixel(x, y, alphaPixel);
                        if (alphaPixel[0] == 255)
                        {
                            raster.setPixel(x, y, transparent);
                        }
                    }
                }
                
                // draw the image
                setClip();
                graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
                graphics.drawImage(renderedPaint, 
                        AffineTransform.getTranslateInstance(bounds.getMinX(), bounds.getMinY()), 
                        null);
            }
            else
            {
                // fill the image with stenciled paint
                BufferedImage image = pdImage.getStencilImage(getNonStrokingPaint());

                // draw the image
                drawBufferedImage(image, at);
            }
        }
        else
        {
            // draw the image
            drawBufferedImage(pdImage.getImage(), at);
        }

        if (!pdImage.getInterpolate())
        {
            // JDK 1.7 has a bug where rendering hints are reset by the above call to
            // the setRenderingHint method, so we re-set all hints, see PDFBOX-2302
            setRenderingHints();
        }
    }

    private void drawBufferedImage(BufferedImage image, AffineTransform at) throws IOException
    {
        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        setClip();
        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if( softMask != null )
        {
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(1, -1);
            imageTransform.translate(0, -1);
            Paint awtPaint = new TexturePaint(image,
                    new Rectangle2D.Double(imageTransform.getTranslateX(), imageTransform.getTranslateY(),
                            imageTransform.getScaleX(), imageTransform.getScaleY()));
            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
            graphics.setPaint(awtPaint);
            Rectangle2D unitRect = new Rectangle2D.Float(0, 0, 1, 1);
            graphics.fill(at.createTransformedShape(unitRect));
        }
        else
        {
            COSBase transfer = getGraphicsState().getTransfer();
            if (transfer instanceof COSArray || transfer instanceof COSDictionary)
            {
                image = applyTransferFunction(image, transfer);
            }

            int width = image.getWidth(null);
            int height = image.getHeight(null);
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(1.0 / width, -1.0 / height);
            imageTransform.translate(0, -height);
            graphics.drawImage(image, imageTransform, null);
        }
    }

    private BufferedImage applyTransferFunction(BufferedImage image, COSBase transfer) throws IOException
    {
        BufferedImage bim;
        if (image.getColorModel().hasAlpha())
        {
            bim = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            bim = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        // prepare transfer functions (either one per color or one for all) 
        // and maps (actually arrays[256] to be faster) to avoid calculating values several times
        Integer rMap[], gMap[], bMap[];
        PDFunction rf, gf, bf;
        if (transfer instanceof COSArray)
        {
            COSArray ar = (COSArray) transfer;
            rf = PDFunction.create(ar.getObject(0));
            gf = PDFunction.create(ar.getObject(1));
            bf = PDFunction.create(ar.getObject(2));
            rMap = new Integer[256];
            gMap = new Integer[256];
            bMap = new Integer[256];
        }
        else
        {
            rf = PDFunction.create(transfer);
            gf = rf;
            bf = rf;
            rMap = new Integer[256];
            gMap = rMap;
            bMap = rMap;
        }

        // apply the transfer function to each color, but keep alpha
        float[] input = new float[1];
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                int rgb = image.getRGB(x, y);
                int ri = (rgb >> 16) & 0xFF;
                int gi = (rgb >> 8) & 0xFF;
                int bi = rgb & 0xFF;
                int ro, go, bo;
                if (rMap[ri] != null)
                {
                    ro = rMap[ri];
                }
                else
                {
                    input[0] = (ri & 0xFF) / 255f;
                    ro = (int) (rf.eval(input)[0] * 255);
                    rMap[ri] = ro;
                }
                if (gMap[gi] != null)
                {
                    go = gMap[gi];
                }
                else
                {
                    input[0] = (gi & 0xFF) / 255f;
                    go = (int) (gf.eval(input)[0] * 255);
                    gMap[gi] = go;
                }
                if (bMap[bi] != null)
                {
                    bo = bMap[bi];
                }
                else
                {
                    input[0] = (bi & 0xFF) / 255f;
                    bo = (int) (bf.eval(input)[0] * 255);
                    bMap[bi] = bo;
                }
                bim.setRGB(x, y, (rgb & 0xFF000000) | (ro << 16) | (go << 8) | bo);
            }
        }
        return bim;
    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException
    {
        PDShading shading = getResources().getShading(shadingName);
        if (shading == null)
        {
            LOG.error("shading " + shadingName + " does not exist in resources dictionary");
            return;
        }
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Paint paint = shading.toPaint(ctm);
        paint = applySoftMaskToPaint(paint, getGraphicsState().getSoftMask());

        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        graphics.setPaint(paint);
        graphics.setClip(null);
        lastClip = null;

        // get the transformed BBox and intersect with current clipping path
        // need to do it here and not in shading getRaster() because it may have been rotated
        PDRectangle bbox = shading.getBBox();
        if (bbox != null)
        {
            Area bboxArea = new Area(bbox.transform(ctm));
            bboxArea.intersect(getGraphicsState().getCurrentClippingPath());
            graphics.fill(bboxArea);
        }
        else
        {
            graphics.fill(getGraphicsState().getCurrentClippingPath());
        }
    }

    @Override
    public void showAnnotation(PDAnnotation annotation) throws IOException
    {
        lastClip = null;
        //TODO support more annotation flags (Invisible, NoZoom, NoRotate)
        // Example for NoZoom can be found in p5 of PDFBOX-2348
        int deviceType = graphics.getDeviceConfiguration().getDevice().getType();
        if (deviceType == GraphicsDevice.TYPE_PRINTER && !annotation.isPrinted())
        {
            return;
        }
        if (deviceType == GraphicsDevice.TYPE_RASTER_SCREEN && annotation.isNoView())
        {
            return;
        }
        if (annotation.isHidden())
        {
            return;
        }
        super.showAnnotation(annotation);

        if (annotation.getAppearance() == null)
        {
            if (annotation instanceof PDAnnotationLink)
            {
                drawAnnotationLinkBorder((PDAnnotationLink) annotation);
            }

            if (annotation instanceof PDAnnotationMarkup && annotation.getSubtype().equals(PDAnnotationMarkup.SUB_TYPE_INK))
            {
                drawAnnotationInk((PDAnnotationMarkup) annotation);
            }
        }
    }

    private static class AnnotationBorder
    {
        private float[] dashArray = null;
        private boolean underline = false;
        private float width = 0;
        private PDColor color;
    }
    
    // return border info. BorderStyle must be provided as parameter because
    // method is not available in the base class
    private AnnotationBorder getAnnotationBorder(PDAnnotation annotation, 
            PDBorderStyleDictionary borderStyle)
    {
        AnnotationBorder ab = new AnnotationBorder();
        COSArray border = annotation.getBorder();
        if (borderStyle == null)
        {
            if (border.get(2) instanceof COSNumber)
            {
                ab.width = ((COSNumber) border.getObject(2)).floatValue();
            }
            if (border.size() > 3)
            {
                COSBase base3 = border.getObject(3);
                if (base3 instanceof COSArray)
                {
                    ab.dashArray = ((COSArray) base3).toFloatArray();
                }
            }
        }
        else
        {
            ab.width = borderStyle.getWidth();
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_DASHED))
            {
                ab.dashArray = borderStyle.getDashStyle().getDashArray();
            }
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_UNDERLINE))
            {
                ab.underline = true;
            }
        }
        ab.color = annotation.getColor();
        if (ab.color == null)
        {
            // spec is unclear, but black seems to be the right thing to do
            ab.color = new PDColor(new float[] { 0 }, PDDeviceGray.INSTANCE);
        }
        if (ab.dashArray != null)
        {
            boolean allZero = true;
            for (float f : ab.dashArray)
            {
                if (f != 0)
                {
                    allZero = false;
                    break;
                }
            }
            if (allZero)
            {
                ab.dashArray = null;
            }
        }
        return ab;
    }

    private void drawAnnotationLinkBorder(PDAnnotationLink link) throws IOException
    {
        AnnotationBorder ab = getAnnotationBorder(link, link.getBorderStyle());
        if (ab.width == 0 || ab.color.getComponents().length == 0)
        {
            return;
        }
        PDRectangle rectangle = link.getRectangle();
        Stroke oldStroke = graphics.getStroke();
        graphics.setPaint(getPaint(ab.color));
        BasicStroke stroke = new BasicStroke(ab.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, ab.dashArray, 0);
        graphics.setStroke(stroke);
        graphics.setClip(null);
        if (ab.underline)
        {
            graphics.drawLine((int) rectangle.getLowerLeftX(), (int) rectangle.getLowerLeftY(),
                    (int) (rectangle.getLowerLeftX() + rectangle.getWidth()), (int) rectangle.getLowerLeftY());
        }
        else
        {
            graphics.drawRect((int) rectangle.getLowerLeftX(), (int) rectangle.getLowerLeftY(),
                    (int) rectangle.getWidth(), (int) rectangle.getHeight());
        }
        graphics.setStroke(oldStroke);
    }

    private void drawAnnotationInk(PDAnnotationMarkup inkAnnotation) throws IOException
    {
        if (!inkAnnotation.getCOSObject().containsKey(COSName.INKLIST))
        {
            return;
        }
        //TODO there should be an InkAnnotation class with a getInkList method
        COSBase base = inkAnnotation.getCOSObject().getDictionaryObject(COSName.INKLIST);
        if (!(base instanceof COSArray))
        {
            return;
        }
        // PDF spec does not mention /Border for ink annotations, but it is used if /BS is not available
        AnnotationBorder ab = getAnnotationBorder(inkAnnotation, inkAnnotation.getBorderStyle());
        if (ab.width == 0 || ab.color.getComponents().length == 0)
        {
            return;
        }
        graphics.setPaint(getPaint(ab.color));
        Stroke oldStroke = graphics.getStroke();
        BasicStroke stroke = 
                new BasicStroke(ab.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, ab.dashArray, 0);
        graphics.setStroke(stroke);
        graphics.setClip(null);
        COSArray pathsArray = (COSArray) base;
        for (COSBase baseElement : pathsArray.toList())
        {
            if (!(baseElement instanceof COSArray))
            {
                continue;
            }
            COSArray pathArray = (COSArray) baseElement;
            int nPoints = pathArray.size() / 2;
            
            // "When drawn, the points shall be connected by straight lines or curves 
            // in an implementation-dependent way" - we do lines.
            GeneralPath path = new GeneralPath();
            for (int i = 0; i < nPoints; ++i)
            {
                COSBase bx = pathArray.getObject(i * 2);
                COSBase by = pathArray.getObject(i * 2 + 1);
                if (bx instanceof COSNumber && by instanceof COSNumber)
                {
                    float x = ((COSNumber) bx).floatValue();
                    float y = ((COSNumber) by).floatValue();
                    if (i == 0)
                    {
                        path.moveTo(x, y);
                    }
                    else
                    {
                        path.lineTo(x, y);
                    }
                }
            }
            graphics.draw(path);
        }
        graphics.setStroke(oldStroke);
    }

    @Override
    public void showTransparencyGroup(PDTransparencyGroup form) throws IOException
    {
        TransparencyGroup group =
                new TransparencyGroup(form, false, getGraphicsState().getCurrentTransformationMatrix(), null);
        BufferedImage image = group.getImage();
        if (image == null)
        {
            // image is empty, don't bother
            return;
        }

        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        setClip();

        // both the DPI xform and the CTM were already applied to the group, so all we do
        // here is draw it directly onto the Graphics2D device at the appropriate position
        PDRectangle bbox = group.getBBox();
        AffineTransform prev = graphics.getTransform();

        Matrix m = new Matrix(xform);
        float xScale = Math.abs(m.getScalingFactorX());
        float yScale = Math.abs(m.getScalingFactorY());
        
        // adjust the initial translation (includes the translation used to "help" the rotation)
        graphics.setTransform(AffineTransform.getTranslateInstance(xform.getTranslateX(), xform.getTranslateY()));

        graphics.rotate(Math.toRadians(pageRotation));

        // adjust bbox (x,y) position at the initial scale + cropbox
        float x = bbox.getLowerLeftX() - pageSize.getLowerLeftX();
        float y = pageSize.getUpperRightY() - bbox.getUpperRightY();
        graphics.translate(x * xScale, y * yScale);

        if (flipTG)
        {
            graphics.translate(0, image.getHeight());
            graphics.scale(1, -1);
        }

        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if (softMask != null)
        {
            Paint awtPaint = new TexturePaint(image,
                    new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
            graphics.setPaint(awtPaint);
            graphics.fill(
                    new Rectangle2D.Float(0, 0, bbox.getWidth() * xScale, bbox.getHeight() * yScale));
        }
        else
        {
            graphics.drawImage(image, null, null);
        }

        graphics.setTransform(prev);
    }

    /**
     * Transparency group.
     **/
    private final class TransparencyGroup
    {
        private final BufferedImage image;
        private final PDRectangle bbox;

        private final int minX;
        private final int minY;
        private final int width;
        private final int height;

        /**
         * Creates a buffered image for a transparency group result.
         *
         * @param form the transparency group of the form or soft mask.
         * @param isSoftMask true if this is a soft mask.
         * @param ctm the relevant current transformation matrix. For soft masks, this is the CTM at
         * the time the soft mask is set (not at the time the soft mask is used for fill/stroke!),
         * for forms, this is the CTM at the time the form is invoked.
         * @param backdropColor the color according to the /bc entry to be used for luminosity soft
         * masks.
         * @throws IOException
         */
        private TransparencyGroup(PDTransparencyGroup form, boolean isSoftMask, Matrix ctm, 
                PDColor backdropColor) throws IOException
        {
            Graphics2D g2dOriginal = graphics;
            Area lastClipOriginal = lastClip;

            // get the CTM x Form Matrix transform
            Matrix transform = Matrix.concatenate(ctm, form.getMatrix());

            // transform the bbox
            GeneralPath transformedBox = form.getBBox().transform(transform);

            // clip the bbox to prevent giant bboxes from consuming all memory
            Area clip = (Area)getGraphicsState().getCurrentClippingPath().clone();
            clip.intersect(new Area(transformedBox));
            Rectangle2D clipRect = clip.getBounds2D();
            if (clipRect.isEmpty())
            {
                image = null;
                bbox = null;
                minX = 0;
                minY = 0;
                width = 0;
                height = 0;
                return;
            }
            this.bbox = new PDRectangle((float)clipRect.getX(), (float)clipRect.getY(),
                                        (float)clipRect.getWidth(), (float)clipRect.getHeight());

            // apply the underlying Graphics2D device's DPI transform
            Matrix m = new Matrix(xform);
            AffineTransform dpiTransform = AffineTransform.getScaleInstance(Math.abs(m.getScalingFactorX()), Math.abs(m.getScalingFactorY()));
            Rectangle2D bounds = dpiTransform.createTransformedShape(clip.getBounds2D()).getBounds2D();

            minX = (int) Math.floor(bounds.getMinX());
            minY = (int) Math.floor(bounds.getMinY());
            int maxX = (int) Math.floor(bounds.getMaxX()) + 1;
            int maxY = (int) Math.floor(bounds.getMaxY()) + 1;

            width = maxX - minX;
            height = maxY - minY;

            // FIXME - color space
            if (isGray(form.getGroup().getColorSpace()))
            {
                image = create2ByteGrayAlphaImage(width, height);
            }
            else
            {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g = image.createGraphics();
            if (isSoftMask && backdropColor != null)
            {
                // "If the subtype is Luminosity, the transparency group XObject G shall be 
                // composited with a fully opaque backdrop whose colour is everywhere defined 
                // by the soft-mask dictionary's BC entry."
                g.setBackground(new Color(backdropColor.toRGB()));
                g.clearRect(0, 0, width, height);
            }

            // flip y-axis
            g.translate(0, image.getHeight());
            g.scale(1, -1);

            boolean oldFlipTG = flipTG;
            flipTG = false;

            // apply device transform (DPI)
            // the initial translation is ignored, because we're not writing into the initial graphics device
            g.transform(dpiTransform);

            AffineTransform xformOriginal = xform;
            xform = AffineTransform.getScaleInstance(m.getScalingFactorX(), m.getScalingFactorY());
            PDRectangle pageSizeOriginal = pageSize;
            pageSize = new PDRectangle(minX / Math.abs(m.getScalingFactorX()), 
                                       minY / Math.abs(m.getScalingFactorY()),
                        (float) bounds.getWidth() / Math.abs(m.getScalingFactorX()),
                        (float) bounds.getHeight() / Math.abs(m.getScalingFactorY()));
            int pageRotationOriginal = pageRotation;
            pageRotation = 0;
            int clipWindingRuleOriginal = clipWindingRule;
            clipWindingRule = -1;
            GeneralPath linePathOriginal = linePath;
            linePath = new GeneralPath();

            // adjust the origin
            g.translate(-clipRect.getX(), -clipRect.getY());

            graphics = g;
            setRenderingHints();
            try
            {
                if (isSoftMask)
                {
                    processSoftMask(form);
                }
                else
                {
                    processTransparencyGroup(form);
                }
            }
            finally 
            {
                flipTG = oldFlipTG;
                lastClip = lastClipOriginal;
                graphics.dispose();
                graphics = g2dOriginal;
                clipWindingRule = clipWindingRuleOriginal;
                linePath = linePathOriginal;
                pageSize = pageSizeOriginal;
                xform = xformOriginal;
                pageRotation = pageRotationOriginal;
            }
        }

        // http://stackoverflow.com/a/21181943/535646
        private BufferedImage create2ByteGrayAlphaImage(int width, int height) 
        {
            /**
             * gray + alpha
             */
            int[] bandOffsets = new int[] {1, 0};
            int bands = bandOffsets.length;
            
            /**
             * Color Model usesd for raw GRAY + ALPHA
             */
            final ColorModel CM_GRAY_ALPHA
                = new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

            // Init data buffer of type byte
            DataBuffer buffer = new DataBufferByte(width * height * bands);

            // Wrap the data buffer in a raster
            WritableRaster raster =
                    Raster.createInterleavedRaster(buffer, width, height,
                            width * bands, bands, bandOffsets, new Point(0, 0));

            // Create a custom BufferedImage with the raster and a suitable color model
            return new BufferedImage(CM_GRAY_ALPHA, raster, false, null);
        }

        private boolean isGray(PDColorSpace colorSpace)
        {
            if (colorSpace instanceof PDDeviceGray)
            {
                return true;
            }
            if (colorSpace instanceof PDICCBased)
            {
                try
                {
                    return ((PDICCBased) colorSpace).getAlternateColorSpace() instanceof PDDeviceGray;
                }
                catch (IOException ex)
                {
                    return false;
                }
            }
            return false;
        }

        public BufferedImage getImage()
        {
            return image;
        }

        public PDRectangle getBBox()
        {
            return bbox;
        }

        public Rectangle2D getBounds()
        {
            Point2D size = new Point2D.Double(pageSize.getWidth(), pageSize.getHeight());
            // apply the underlying Graphics2D device's DPI transform and y-axis flip
            Matrix m = new Matrix(xform);
            AffineTransform dpiTransform = AffineTransform.getScaleInstance(Math.abs(m.getScalingFactorX()), Math.abs(m.getScalingFactorY()));
            size = dpiTransform.transform(size, size);
            // Flip y
            return new Rectangle2D.Double(minX - pageSize.getLowerLeftX() * m.getScalingFactorX(),
                    size.getY() - minY - height + pageSize.getLowerLeftY() * m.getScalingFactorY(),
                    width, height);
        }
    }
}
