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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.TYPE_FLIP;
import static java.awt.geom.AffineTransform.TYPE_MASK_SCALE;
import static java.awt.geom.AffineTransform.TYPE_TRANSLATION;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup.RenderState;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentMembershipDictionary;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationUnknown;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * Paints a page in a PDF document to a Graphics context. May be subclassed to provide custom
 * rendering.
 *
 * <p>
 * If you want to do custom graphics processing rather than Graphics2D rendering, then you should
 * subclass {@link PDFGraphicsStreamEngine} instead. Subclassing PageDrawer is only suitable for
 * cases where the goal is to render onto a {@link Graphics2D} surface. In that case you'll also
 * have to subclass {@link PDFRenderer} and override
 * {@link PDFRenderer#createPageDrawer(PageDrawerParameters)}. See the <i>OpaquePDFRenderer.java</i>
 * example in the source code download on how to do this.
 *
 * @author Ben Litchfield
 */
public class PageDrawer extends PDFGraphicsStreamEngine
{
    private static final Logger LOG = LogManager.getLogger(PageDrawer.class);

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("windows");
    private static final boolean IS_LINUX = OS_NAME.startsWith("linux");

    // parent document renderer - note: this is needed for not-yet-implemented resource caching
    private final PDFRenderer renderer;
    
    private final boolean subsamplingAllowed;
    
    // the graphics device to draw to, xform is the initial transform of the device (i.e. DPI)
    private Graphics2D graphics;
    private AffineTransform xform;
    private float xformScalingFactorX;
    private float xformScalingFactorY;
    
    // the page box to draw (usually the crop box but may be another)
    private PDRectangle pageSize;

    // whether image of a transparency group must be flipped
    // needed when in a tiling pattern
    private boolean flipTG = false;

    // clipping winding rule used for the clipping path
    private int clipWindingRule = -1;
    private GeneralPath linePath = new GeneralPath();
    
    // last clipping path
    private List<Path2D> lastClips;

    // clip when drawPage() is called, can be null, must be intersected when clipping
    private Shape initialClip;
    
    // shapes of glyphs being drawn to be used for clipping
    private List<Shape> textClippings;

    // glyph caches
    private final Map<PDFont, GlyphCache> glyphCaches = new HashMap<>();

    private final TilingPaintFactory tilingPaintFactory = new TilingPaintFactory(this);
    
    private final Deque<TransparencyGroup> transparencyGroupStack = new ArrayDeque<>();

    // if greater zero the content is hidden and will not be rendered
    private int nestedHiddenOCGCount;

    private final RenderDestination destination;
    private final RenderingHints renderingHints;
    private final float imageDownscalingOptimizationThreshold;
    private LookupTable invTable = null;
    private final Map<COSBase,Boolean> blendModeMap = new HashMap<>();

    /**
    * Default annotations filter, returns all annotations
    */
    private AnnotationFilter annotationFilter = annotation -> true;

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
        this.subsamplingAllowed = parameters.isSubsamplingAllowed();
        this.destination = parameters.getDestination();
        this.renderingHints = parameters.getRenderingHints();
        this.imageDownscalingOptimizationThreshold =
                parameters.getImageDownscalingOptimizationThreshold();
    }

    /**
     * Return the AnnotationFilter.
     * 
     * @return the AnnotationFilter
     */
    public AnnotationFilter getAnnotationFilter()
    {
        return annotationFilter;
    }

    /**
     * Set the AnnotationFilter.
     * 
     * <p>Allows to only render annotation accepted by the filter.
     * 
     * @param annotationFilter the AnnotationFilter
     */
    public void setAnnotationFilter(AnnotationFilter annotationFilter)
    {
        this.annotationFilter = annotationFilter;
    }
    
    /**
     * Returns the parent renderer.
     * 
     * @return the parent renderer
     */
    public final PDFRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Returns the underlying Graphics2D. May be null if drawPage has not yet been called.
     * 
     * @return the underlying Graphics2D
     */
    protected final Graphics2D getGraphics()
    {
        return graphics;
    }

    /**
     * Returns the current line path. This is reset to empty after each fill/stroke.
     * 
     * @return the current line path
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
        graphics.addRenderingHints(renderingHints);
    }

    /**
     * Draws the page to the requested context.
     * 
     * @param g The graphics context to draw onto.
     * @param pageSize The size of the page to draw.
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage(Graphics2D g, PDRectangle pageSize) throws IOException
    {
        graphics = g;
        xform = graphics.getTransform();
        Matrix m = new Matrix(xform);
        xformScalingFactorX = Math.abs(m.getScalingFactorX());
        xformScalingFactorY = Math.abs(m.getScalingFactorY());
        initialClip = graphics.getClip();
        this.pageSize = pageSize;

        setRenderingHints();

        graphics.translate(0, pageSize.getHeight());
        graphics.scale(1, -1);

        // adjust for non-(0,0) crop box
        graphics.translate(-pageSize.getLowerLeftX(), -pageSize.getLowerLeftY());

        processPage(getPage());

        for (PDAnnotation annotation : getPage().getAnnotations(annotationFilter))
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
        Graphics2D savedGraphics = graphics;
        graphics = g;

        GeneralPath savedLinePath = linePath;
        linePath = new GeneralPath();
        int savedClipWindingRule = clipWindingRule;
        clipWindingRule = -1;

        List<Path2D> savedLastClips = lastClips;
        lastClips = null;
        Shape savedInitialClip = initialClip;
        initialClip = null;
        
        boolean savedFlipTG = flipTG;
        flipTG = true;

        setRenderingHints();
        processTilingPattern(pattern, color, colorSpace, patternMatrix);
        
        flipTG = savedFlipTG;
        graphics = savedGraphics;
        linePath = savedLinePath;
        lastClips = savedLastClips;
        initialClip = savedInitialClip;
        clipWindingRule = savedClipWindingRule;
    }

    private float clampColor(float color)
    {
        return color < 0 ? 0 : (color > 1 ? 1 : color);        
    }

    /**
     * Returns an AWT paint for the given PDColor.
     * 
     * @param color The color to get a paint for. This can be an actual color or a pattern.
     * @return an AWT paint for the given PDColor
     * 
     * @throws IOException if the AWT paint could not be created
     */
    protected Paint getPaint(PDColor color) throws IOException
    {
        PDColorSpace colorSpace = color.getColorSpace();
        if (colorSpace == null) // PDFBOX-5782
        {
            LOG.error("colorSpace is null, will be rendered as transparency");
            return new Color(0, 0, 0, 0);
        }
        else if (colorSpace instanceof PDSeparation &&
                "None".equals(((PDSeparation) colorSpace).getColorantName()))
        {
            // PDFBOX-4900: "The special colorant name None shall not produce any visible output"
            //TODO better solution needs to be found for all occurences where toRGB is called
            return new Color(0, 0, 0, 0);
        }
        else if (!(colorSpace instanceof PDPattern))
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

    /**
     * Sets the clipping path using caching for performance. We track lastClip manually because
     * {@link Graphics2D#getClip()} returns a new object instead of the same one passed to
     * {@link Graphics2D#setClip(java.awt.Shape) setClip()}. You may need to call this if you override
     * {@link #showGlyph(Matrix, PDFont, int, Vector) showGlyph()}. See
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5093">PDFBOX-5093</a> for more.
     */
    protected final void setClip()
    {
        List<Path2D> clippingPaths = getGraphicsState().getCurrentClippingPaths();
        if (clippingPaths != lastClips)
        {
            transferClip(graphics);
            if (initialClip != null)
            {
                // apply the remembered initial clip, but transform it first
                //TODO see PDFBOX-4583
            }
            lastClips = clippingPaths;
        }
    }

    /**
     * Transfer clip to the destination device. Override this if you want to avoid to do slow
     * intersecting operations but want the destination device to do this (e.g. SVG). You can get
     * the individual clippings via {@link PDGraphicsState#getCurrentClippingPaths()}. See
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5258">PDFBOX-5258</a> for sample code.
     *
     * @param graphics graphics device
     */
    protected void transferClip(Graphics2D graphics)
    {
        Area clippingPath = getGraphicsState().getCurrentClippingPath();
        if (clippingPath.getPathIterator(null).isDone())
        {
            // PDFBOX-4821: avoid bug with java printing that empty clipping path is ignored by
            // replacing with empty rectangle, works because this is not an empty path
            graphics.setClip(new Rectangle());
        }
        else
        {
            graphics.setClip(clippingPath);
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
        // buffer the text clippings because they represents a single clipping area
        textClippings = new ArrayList<>();
    }

    /**
     * End buffering the text clipping path, if any.
     */
    private void endTextClip()
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();
        
        // apply the buffered clip as one area
        if (renderingMode.isClip() && !textClippings.isEmpty())
        {
            // PDFBOX-4150: this is much faster than using textClippingArea.add(new Area(glyph))
            // https://stackoverflow.com/questions/21519007/fast-union-of-shapes-in-java
            GeneralPath path = new GeneralPath(Path2D.WIND_NON_ZERO, textClippings.size());
            for (Shape shape : textClippings)
            {
                path.append(shape, false);
            }
            state.intersectClippingPath(path);
            textClippings = new ArrayList<>();

            // PDFBOX-3681: lastClip needs to be reset, because after intersection it is still the same 
            // object, thus setClip() would believe that it is cached.
            lastClips = null;
        }
    }

    @Override
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code,
            Vector displacement) throws IOException
    {
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());

        // create cache if it does not exist
        PDVectorFont vectorFont = (PDVectorFont) font;
        GlyphCache cache = glyphCaches.get(font);
        if (cache == null)
        {
            cache = new GlyphCache(vectorFont);
            glyphCaches.put(font, cache);
        }

        GeneralPath path = cache.getPathForCharacterCode(code);
        drawGlyph(path, font, code, displacement, at);
    }

    /**
     * Renders a glyph.
     * 
     * @param path the GeneralPath for the glyph
     * @param font the font
     * @param code character code
     * @param displacement the glyph's displacement (advance)
     * @param at the transformation
     * @throws IOException if something went wrong
     */
    private void drawGlyph(GeneralPath path, PDFont font, int code, Vector displacement, AffineTransform at) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();

        if (path != null)
        {
            // Stretch non-embedded glyph if it does not match the height/width contained in the PDF.
            // Vertical fonts have zero X displacement, so the following code scales to 0 if we don't skip it.
            // TODO: How should vertical fonts be handled?
            if (!font.isEmbedded() && !font.isVertical() && !font.isStandard14() && font.hasExplicitWidth(code))
            {
                float fontWidth = font.getWidthFromFont(code);
                if (displacement.getX() > 0 && // PDFBOX-5611: ignore zero widths
                        fontWidth > 0 && // ignore spaces
                        Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001)
                {
                    float pdfWidth = displacement.getX() * 1000;
                    at.scale(pdfWidth / fontWidth, 1);
                }
            }

            // render glyph
            Shape glyph = at.createTransformedShape(path);

            if (isContentRendered())
            {
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
            }

            if (renderingMode.isClip())
            {
                textClippings.add(glyph);
            }
        }
    }

    @Override
    protected void showType3Glyph(Matrix textRenderingMatrix, PDType3Font font, int code,
            Vector displacement) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();
        if (RenderingMode.NEITHER != renderingMode)
        {
            super.showType3Glyph(textRenderingMatrix, font, code, displacement);
        }
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
            if (backdropColorArray != null)
            {
                PDTransparencyGroup form = softMask.getGroup();
                PDColorSpace colorSpace = form.getGroup().getColorSpace(form.getResources());
                if (colorSpace != null &&
                    colorSpace.getNumberOfComponents() == backdropColorArray.size()) // PDFBOX-5795
                {
                    backdropColor = new PDColor(backdropColorArray, colorSpace);
                }
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
        gray = adjustImage(gray);
        
        Rectangle2D tpgBounds = transparencyGroup.getBounds();
        return new SoftMask(parentPaint, gray, tpgBounds, backdropColor, softMask.getTransferFunction());
    }

    // returns the image adjusted for applySoftMaskToPaint().
    private BufferedImage adjustImage(BufferedImage gray)
    {
        AffineTransform at = new AffineTransform(xform);
        at.scale(1.0 / xformScalingFactorX, 1.0 / xformScalingFactorY);

        Rectangle originalBounds = new Rectangle(gray.getWidth(), gray.getHeight());
        Rectangle2D transformedBounds = at.createTransformedShape(originalBounds).getBounds2D();
        at.preConcatenate(AffineTransform.getTranslateInstance(-transformedBounds.getMinX(), 
                -transformedBounds.getMinY()));

        int width = (int) Math.ceil(transformedBounds.getWidth());
        int height = (int) Math.ceil(transformedBounds.getHeight());

        if (width == gray.getWidth() && height == gray.getHeight() && at.isIdentity())
        {
            return gray;
        }

        BufferedImage transformedGray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = (Graphics2D) transformedGray.getGraphics();
        g2.drawImage(gray, at, null);
        g2.dispose();
        return transformedGray;
    }

    // returns the stroking AWT Paint
    private Paint getStrokingPaint() throws IOException
    {
        PDGraphicsState graphicsState = getGraphicsState();
        return applySoftMaskToPaint(
                getPaint(graphicsState.getStrokingColor()), graphicsState.getSoftMask());
    }

    /**
     * Returns the non-stroking AWT Paint. You may need to call this if you override
     * {@link #showGlyph(Matrix, PDFont, int, Vector) showGlyph()}. See
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5093">PDFBOX-5093</a> for more.
     *
     * @return The non-stroking AWT Paint.
     * @throws IOException if the non-stroking AWT Paint could not be created
     */
    protected final Paint getNonStrokingPaint() throws IOException
    {
        PDGraphicsState graphicsState = getGraphicsState();
        return applySoftMaskToPaint(
                getPaint(graphicsState.getNonStrokingColor()), graphicsState.getSoftMask());
    }

    // create a new stroke based on the current CTM and the current stroke
    private Stroke getStroke()
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
        // PDFBOX-5168: show an all-zero dash array line invisible like Adobe does
        // must do it here because getDashArray() sets minimum width because of JVM bugs
        float[] dashArray = dashPattern.getDashArray();
        if (isAllZeroDash(dashArray))
        {
            return (Shape p) -> new Area();
        }
        float phaseStart = dashPattern.getPhase();
        dashArray = getDashArray(dashPattern);
        phaseStart = transformWidth(phaseStart);

        int lineCap = Math.min(2, Math.max(0, state.getLineCap())); // legal values 0..2
        int lineJoin = Math.min(2, Math.max(0, state.getLineJoin()));
        float miterLimit = state.getMiterLimit();
        if (miterLimit < 1)
        {
            LOG.warn("Miter limit must be >= 1, value {} is ignored", miterLimit);
            miterLimit = 10;
        }
        return new BasicStroke(lineWidth, lineCap, lineJoin,
                               miterLimit, dashArray, phaseStart);
    }

    private boolean isAllZeroDash(float[] dashArray)
    {
        if (dashArray.length > 0)
        {
            for (int i = 0; i < dashArray.length; ++i)
            {
                if (dashArray[i] != 0)
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private float[] getDashArray(PDLineDashPattern dashPattern)
    {
        float[] dashArray = dashPattern.getDashArray();
        // avoid empty, infinite and NaN values (PDFBOX-3360)
        if (dashArray.length == 0)
        {
            return null;
        }
        for (int i = 0; i < dashArray.length; ++i)
        {
            if (Float.isInfinite(dashArray[i]) || Float.isNaN(dashArray[i]))
            {
                return null;
            }
        }
        for (int i = 0; i < dashArray.length; ++i)
        {
            // apply the CTM
            float w = transformWidth(dashArray[i]);
            // minimum line dash width avoids JVM crash,
            // see PDFBOX-2373, PDFBOX-2929, PDFBOX-3204, PDFBOX-3813
            // also avoid 0 in array like "[ 0 1000 ] 0 d", see PDFBOX-3724
            if (xformScalingFactorX < 0.5f)
            {
                // PDFBOX-4492
                dashArray[i] = Math.max(w, 0.2f);
            }
            else
            {
                dashArray[i] = Math.max(w, 0.062f);
            }
        }
        return dashArray;
    }

    @Override
    public void strokePath() throws IOException
    {
        if (isContentRendered())
        {
            graphics.setComposite(getGraphicsState().getStrokingJavaComposite());
            graphics.setPaint(getStrokingPaint());
            graphics.setStroke(getStroke());
            setClip();
            graphics.draw(linePath);
        }
        linePath.reset();
    }

    @Override
    public void fillPath(int windingRule) throws IOException
    {
        PDGraphicsState graphicsState = getGraphicsState();
        graphics.setComposite(graphicsState.getNonStrokingJavaComposite());
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

        Shape shape;
        if (graphicsState.getNonStrokingColorSpace() instanceof PDPattern)
        {
            // apply clip to path to avoid oversized device bounds in shading contexts (PDFBOX-2901)
            Area area = new Area(linePath);
            Shape clip = graphics.getClip();
            if (clip != null)
            {
                area.intersect(new Area(clip));
            }
            intersectShadingBBox(graphicsState.getNonStrokingColor(), area);
            shape = area;
        }
        else
        {
            shape = linePath;
        }
        if (isContentRendered() && !shape.getPathIterator(null).isDone())
        {
            // creating Paint is sometimes a costly operation, so avoid if possible
            graphics.setPaint(getNonStrokingPaint());
            graphics.fill(shape);
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

                default:
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
        // Cloning needed because fillPath() resets linePath
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

            if (!linePath.getPathIterator(null).isDone())
            {
                // PDFBOX-4949 / PDF.js 12306: don't clip if "W n" only
                getGraphicsState().intersectClippingPath(adjustClip(linePath));
            }

            // PDFBOX-3836: lastClip needs to be reset, because after intersection it is still the same 
            // object, thus setClip() would believe that it is cached.
            lastClips = null;

            clipWindingRule = -1;
        }
        linePath.reset();
    }
    
    /**
     * PDFBOX-5715 / PR#73: This was added to fix a problem with missing fine lines when printing
     * on MacOS. Lines vanish because CPrinterJob sets graphics scale to 1 for Printable so after
     * scaling lines often have a width smaller than 1 after scaling and clipping. This change
     * enlarges the clip bounds to cover at least 1 point plus 0.5 on one and another side in the
     * device space to allow to draw the linePath inside the clip. The linePath can consists from
     * different lines but when its bounds width or height is less than 1.0 it seems safe to use a
     * rectangle as a clip instead of the real path. A more detailed explanation can be read
     * <a href="https://github.com/apache/pdfbox/pull/173">here</a>.
     *
     * @param linePath
     * @return 
     */
    private GeneralPath adjustClip(GeneralPath linePath)
    {
        AffineTransform tx = graphics.getTransform();
        int type = tx.getType();

        if ((type & ~(TYPE_TRANSLATION | TYPE_FLIP)) == 0)
        {
            return linePath;
        }
        else if ((type & ~(TYPE_TRANSLATION | TYPE_FLIP | TYPE_MASK_SCALE)) == 0)
        {
            double sx = Math.abs(tx.getScaleX());
            double sy = Math.abs(tx.getScaleY());
            if (sx > 1.0 && sy > 1.0)
            {
                return linePath;
            }

            Rectangle2D bounds = linePath.getBounds();
            double w = bounds.getWidth();
            double h = bounds.getHeight();
            double sw = sx * w;
            double sh = sy * h;
            final double minSize = 2.0;
            if (sw < minSize || sh < minSize)
            {
                double x = bounds.getX();
                double y = bounds.getY();
                if (sw < minSize)
                {
                    w = minSize / sx;
                    x = bounds.getCenterX() - w / 2;
                }
                if (sh < minSize)
                {
                    h = minSize / sy;
                    y = bounds.getCenterY() - h / 2;
                }
                return new GeneralPath(new Rectangle2D.Double(x, y, w, h));
            }
        }
        return linePath;
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException
    {
        if (pdImage instanceof PDImageXObject &&
            isHiddenOCG(((PDImageXObject) pdImage).getOptionalContent()))
        {
            return;
        }
        if (!isContentRendered())
        {
            return;
        }
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();

        if (!pdImage.getInterpolate())
        {
            // if the image is scaled down, we use smooth interpolation, eg PDFBOX-2364
            // only when scaled up do we use nearest neighbour, eg PDFBOX-2302 / mori-cvpr01.pdf
            // PDFBOX-4930: we use the sizes of the ARGB image. These can be different
            // than the original sizes of the base image, when the mask is bigger.
            // PDFBOX-5091: also consider subsampling, the sizes are different too.
            BufferedImage bim;
            if (subsamplingAllowed)
            {
                bim = pdImage.getImage(null, getSubsampling(pdImage, at));
            }
            else
            {
                bim = pdImage.getImage();
            }
            boolean isScaledUp =
                    bim.getWidth() <= Math.abs(Math.round(ctm.getScalingFactorX() * xformScalingFactorX)) ||
                    bim.getHeight() <= Math.abs(Math.round(ctm.getScalingFactorY() * xformScalingFactorY));
            if (isScaledUp)
            {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }
        }

        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        setClip();

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
                int w = (int) Math.ceil(bounds.getWidth());
                int h = (int) Math.ceil(bounds.getHeight());
                BufferedImage renderedPaint = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) renderedPaint.getGraphics();
                g.translate(-bounds.getMinX(), -bounds.getMinY());
                g.setPaint(paint);
                g.setRenderingHints(graphics.getRenderingHints());
                g.fill(bounds);
                g.dispose();

                // draw the mask
                BufferedImage mask = pdImage.getImage();
                AffineTransform imageTransform = new AffineTransform(at);
                imageTransform.scale(1.0 / mask.getWidth(), -1.0 / mask.getHeight());
                imageTransform.translate(0, -mask.getHeight());
                AffineTransform full = new AffineTransform(g.getTransform());
                full.concatenate(imageTransform);
                Matrix m = new Matrix(full);
                double scaleX = Math.abs(m.getScalingFactorX());
                double scaleY = Math.abs(m.getScalingFactorY());

                boolean smallMask = mask.getWidth() <= 8 && mask.getHeight() <= 8;
                if (mask.getWidth() == 1 && mask.getHeight() == 1)
                {
                    // PDFBOX-5802: force usage of the lookup table if it is only 1 pixel
                    // (See the comment for PDFBOX-5403 that it isn't done for some
                    // cases based purely on the rendering result of one file!)
                    smallMask = false;
                }
                if (!smallMask)
                {
                    // PDFBOX-5403:
                    // The mask is copied to RGB because this supports a smooth scaling, so we
                    // get a mask with 255 values instead of just 0 and 255.
                    // Inverting is done because when we don't do it, the getScaledInstance() call
                    // produces a black line in many masks. With the inversion we have a white line
                    // which is neutral. Because of the inversion we don't have to substract from 255
                    // in the "apply the mask" segment when rasterPixel[3] is assigned.

                    // The inversion is not done for very small ones, because of
                    // PDFBOX-2171-002-002710-p14.pdf where the "New Harmony Consolidated" and
                    // "Sailor Springs" patterns became almost invisible.
                    // (We may have to decide this differently in the future, e.g. on b/w relationship)
                    BufferedImage tmp = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_RGB);
                    mask = new LookupOp(getInvLookupTable(), graphics.getRenderingHints()).filter(mask, tmp);
                }

                BufferedImage renderedMask = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                g = (Graphics2D) renderedMask.getGraphics();
                g.translate(-bounds.getMinX(), -bounds.getMinY());
                g.setRenderingHints(graphics.getRenderingHints());

                if (smallMask)
                {
                    g.drawImage(mask, imageTransform, null);
                }
                else if (scaleX != 0 && scaleY != 0)
                {
                    while (scaleX < 0.25 || Math.round(mask.getWidth() * scaleX) < 1)
                    {
                        scaleX *= 2.0;
                    }
                    while (scaleY < 0.25 || Math.round(mask.getHeight() * scaleY) < 1)
                    {
                        scaleY *= 2.0;
                    }
                    int w2 = (int) Math.round(mask.getWidth() * scaleX);
                    int h2 = (int) Math.round(mask.getHeight() * scaleY);

                    Image scaledMask = mask.getScaledInstance(w2, h2, Image.SCALE_SMOOTH);
                    imageTransform.scale(1f / Math.abs(scaleX), 1f / Math.abs(scaleY));
                    g.drawImage(scaledMask, imageTransform, null);
                }
                g.dispose();

                // apply the mask
                int[] alphaPixel = null;
                int[] rasterPixel = null;
                WritableRaster raster = renderedPaint.getRaster();
                WritableRaster alpha = renderedMask.getRaster();
                for (int y = 0; y < h; y++)
                {
                    for (int x = 0; x < w; x++)
                    {
                        alphaPixel = alpha.getPixel(x, y, alphaPixel);
                        rasterPixel = raster.getPixel(x, y, rasterPixel);
                        rasterPixel[3] = alphaPixel[0];
                        raster.setPixel(x, y, rasterPixel);
                    }
                }

                // draw the image
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
            if (subsamplingAllowed)
            {
                int subsampling = getSubsampling(pdImage, at);
                // draw the subsampled image
                drawBufferedImage(pdImage.getImage(null, subsampling), at);
            }
            else
            {
                // subsampling not allowed, draw the image
                drawBufferedImage(pdImage.getImage(), at);
            }
        }

        if (!pdImage.getInterpolate())
        {
            // JDK 1.7 has a bug where rendering hints are reset by the above call to
            // the setRenderingHint method, so we re-set all hints, see PDFBOX-2302
            setRenderingHints();
        }
    }

    /**
     * Calculates the subsampling frequency for a given PDImage based on the current transformation
     * and its calculated transform. Extend this method if you want to use your own strategy.
     *
     * @param pdImage PDImage to be drawn
     * @param at Transform that will be applied to the image when drawing
     * @return The rounded-down ratio of image pixels to drawn pixels. Returned value will always be
     * &gt;=1.
     */
    protected int getSubsampling(PDImage pdImage, AffineTransform at)
    {
        // calculate subsampling according to the resulting image size
        double scale = Math.abs(at.getDeterminant() * xform.getDeterminant());

        int subsampling = (int) Math.floor(Math.sqrt(pdImage.getWidth() * pdImage.getHeight() / scale));
        if (subsampling > 8)
        {
            subsampling = 8;
        }
        if (subsampling < 1)
        {
            subsampling = 1;
        }
        if (subsampling > pdImage.getWidth() || subsampling > pdImage.getHeight())
        {
            // For very small images it is possible that the subsampling would imply 0 size.
            // To avoid problems, the subsampling is set to no less than the smallest dimension.
            subsampling = Math.min(pdImage.getWidth(), pdImage.getHeight());
        }
        return subsampling;
    }

    private void drawBufferedImage(BufferedImage image, AffineTransform at) throws IOException
    {
        AffineTransform originalTransform = graphics.getTransform();
        AffineTransform imageTransform = new AffineTransform(at);
        int width = image.getWidth();
        int height = image.getHeight();
        imageTransform.scale(1.0 / width, -1.0 / height);
        imageTransform.translate(0, -height);

        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if( softMask != null )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(0, 0, width, height);
            Paint awtPaint = new TexturePaint(image, rectangle);
            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
            graphics.setPaint(awtPaint);
            graphics.transform(imageTransform);
            graphics.fill(rectangle);
            graphics.setTransform(originalTransform);
        }
        else
        {
            COSBase transfer = getGraphicsState().getTransfer();
            if (transfer instanceof COSArray || transfer instanceof COSDictionary)
            {
                image = applyTransferFunction(image, transfer);
            }

            // PDFBOX-4516, PDFBOX-4527, PDFBOX-4815, PDFBOX-4886, PDFBOX-4863:
            // graphics.drawImage() has terrible quality when scaling down, even when
            // RenderingHints.VALUE_INTERPOLATION_BICUBIC, VALUE_ALPHA_INTERPOLATION_QUALITY,
            // VALUE_COLOR_RENDER_QUALITY and VALUE_RENDER_QUALITY are all set.
            // A workaround is to get a pre-scaled image with Image.getScaledInstance()
            // and then draw that one. To reduce differences in testing
            // (partly because the method needs integer parameters), only smaller scalings
            // will trigger the workaround. Because of the slowness we only do it if the user
            // expects quality rendering and interpolation.
            Matrix imageTransformMatrix = new Matrix(imageTransform);
            Matrix graphicsTransformMatrix = new Matrix(originalTransform);    
            float scaleX = Math.abs(imageTransformMatrix.getScalingFactorX() * graphicsTransformMatrix.getScalingFactorX());
            float scaleY = Math.abs(imageTransformMatrix.getScalingFactorY() * graphicsTransformMatrix.getScalingFactorY());

            if ((scaleX < imageDownscalingOptimizationThreshold || scaleY < imageDownscalingOptimizationThreshold) &&
                RenderingHints.VALUE_RENDER_QUALITY.equals(graphics.getRenderingHint(RenderingHints.KEY_RENDERING)) &&
                RenderingHints.VALUE_INTERPOLATION_BICUBIC.equals(graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION)))
            {
                int w = Math.round(image.getWidth() * scaleX);
                int h = Math.round(image.getHeight() * scaleY);
                if (w < 1 || h < 1)
                {
                    graphics.drawImage(image, imageTransform, null);
                    return;
                }
                Image imageToDraw = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                // remove the scale (extracted from w and h, to have it from the rounded values
                // hoping to reverse the rounding: without this, we get an horizontal line
                // when rendering PDFJS-8860-Pattern-Size1.pdf at 100% )
                imageTransform.scale(1f / w * image.getWidth(), 1f / h * image.getHeight());
                imageTransform.preConcatenate(originalTransform);
                graphics.setTransform(new AffineTransform());
                graphics.drawImage(imageToDraw, imageTransform, null);
                graphics.setTransform(originalTransform);
            }
            else
            {
                GraphicsConfiguration graphicsConfiguration = graphics.getDeviceConfiguration();
                int deviceType = GraphicsDevice.TYPE_RASTER_SCREEN;
                if (graphicsConfiguration != null)
                {
                    GraphicsDevice graphicsDevice = graphicsConfiguration.getDevice();
                    if (graphicsDevice != null)
                    {
                        deviceType = graphicsDevice.getType();
                    }
                }
                if (deviceType == GraphicsDevice.TYPE_PRINTER &&
                    image.getType() != BufferedImage.TYPE_4BYTE_ABGR &&
                    (IS_WINDOWS || IS_LINUX))
                {
                    // PDFBOX-5601, PDFBOX-4010, JDK-8308099, JDK-8191800:
                    // workaround to avoid terrible / missing output on printer unless TYPE_4BYTE_ABGR
                    BufferedImage bim = new BufferedImage(
                            image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics g = bim.getGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                    image = bim;
                }
                graphics.drawImage(image, imageTransform, null);
            }
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
        Integer[] rMap;
        Integer[] gMap;
        Integer[] bMap;
        PDFunction rf;
        PDFunction gf;
        PDFunction bf;
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
                int ro;
                int go;
                int bo;
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
        if (!isContentRendered())
        {
            return;
        }
        PDShading shading = getResources().getShading(shadingName);
        if (shading == null)
        {
            LOG.error("shading {} does not exist in resources dictionary", shadingName);
            return;
        }
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        Shape savedClip = graphics.getClip();
        graphics.setClip(null);
        lastClips = null;

        // get the transformed BBox and intersect with current clipping path
        // need to do it here and not in shading getRaster() because it may have been rotated
        PDRectangle bbox = shading.getBBox();
        Area area;
        if (bbox != null)
        {
            area = new Area(bbox.transform(ctm));
            area.intersect(getGraphicsState().getCurrentClippingPath());
        }
        else
        {
            Rectangle2D bounds = shading.getBounds(new AffineTransform(), ctm);
            if (bounds != null)
            {
                bounds.add(new Point2D.Double(Math.floor(bounds.getMinX() - 1),
                        Math.floor(bounds.getMinY() - 1)));
                bounds.add(new Point2D.Double(Math.ceil(bounds.getMaxX() + 1),
                        Math.ceil(bounds.getMaxY() + 1)));
                area = new Area(bounds);
                area.intersect(getGraphicsState().getCurrentClippingPath());
            }
            else
            {
                area = getGraphicsState().getCurrentClippingPath();
            }
        }
        if (!area.isEmpty())
        {
            // creating Paint is sometimes a costly operation, so avoid if possible
            Paint paint = shading.toPaint(ctm);
            paint = applySoftMaskToPaint(paint, getGraphicsState().getSoftMask());
            graphics.setPaint(paint);
            graphics.fill(area);
        }
        graphics.setClip(savedClip);
    }

    @Override
    public void showAnnotation(PDAnnotation annotation) throws IOException
    {
        lastClips = null;
        int deviceType = -1;
        GraphicsConfiguration graphicsConfiguration = graphics.getDeviceConfiguration();
        if (graphicsConfiguration != null)
        {
            GraphicsDevice graphicsDevice = graphicsConfiguration.getDevice();
            if (graphicsDevice != null)
            {
                deviceType = graphicsDevice.getType();
            }
        }
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
        if (annotation.isInvisible() && annotation instanceof PDAnnotationUnknown)
        {
            // "If set, do not display the annotation if it does not belong to one
            // of the standard annotation types and no annotation handler is available."
            return;
        }
        //TODO support NoZoom, example can be found in p5 of PDFBOX-2348

        if (isHiddenOCG(annotation.getOptionalContent()))
        {
            return;
        }

        PDAppearanceDictionary appearance = annotation.getAppearance();
        if (appearance == null || appearance.getNormalAppearance() == null)
        {
            annotation.constructAppearances(renderer.document);
        }
        if (annotation.isNoRotate() && getCurrentPage().getRotation() != 0)
        {
            PDRectangle rect = annotation.getRectangle();
            AffineTransform savedTransform = graphics.getTransform();
            // "The upper-left corner of the annotation remains at the same point in
            //  default user space; the annotation pivots around that point."
            graphics.rotate(Math.toRadians(getCurrentPage().getRotation()),
                    rect.getLowerLeftX(), rect.getUpperRightY());
            super.showAnnotation(annotation);
            graphics.setTransform(savedTransform);
        }
        else
        {
            super.showAnnotation(annotation);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForm(PDFormXObject form) throws IOException
    {
        if (isHiddenOCG(form.getOptionalContent()))
        {
            return;
        }
        if (isContentRendered())
        {
            GeneralPath savedLinePath = linePath;
            linePath = new GeneralPath();
            super.showForm(form);
            linePath = savedLinePath;
        }
    }

    @Override
    public void showTransparencyGroup(PDTransparencyGroup form) throws IOException
    {
        showTransparencyGroupOnGraphics(form, graphics);
    }

    /**
     * For advanced users, to extract the transparency group into a separate graphics device.
     * 
     * @param form the transparency group to be extracted
     * @param graphics the target graphics device
     * @throws IOException if the transparency group could not be extracted
     */
    protected void showTransparencyGroupOnGraphics(PDTransparencyGroup form, Graphics2D graphics)
        throws IOException
    {
        if (isHiddenOCG(form.getOptionalContent()))
        {
            return;
        }
        if (!isContentRendered())
        {
            return;
        }
        TransparencyGroup group
                = new TransparencyGroup(form, false, getGraphicsState().getCurrentTransformationMatrix(), null);
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
        AffineTransform savedTransform = graphics.getTransform();
        AffineTransform transform = new AffineTransform(xform);
        transform.scale(1.0 / xformScalingFactorX, 1.0 / xformScalingFactorY);
        graphics.setTransform(transform);

        // adjust bbox (x,y) position at the initial scale + cropbox
        PDRectangle bbox = group.getBBox();
        float x = bbox.getLowerLeftX() - pageSize.getLowerLeftX();
        float y = pageSize.getUpperRightY() - bbox.getUpperRightY();

        if (flipTG)
        {
            graphics.translate(0, image.getHeight());
            graphics.scale(1, -1);
        }
        else
        {
            graphics.translate(x * xformScalingFactorX, y * xformScalingFactorY);
        }

        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if (softMask != null)
        {
            Paint awtPaint = new TexturePaint(image,
                    new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
            graphics.setPaint(awtPaint);
            graphics.fill(
                    new Rectangle2D.Float(0, 0, bbox.getWidth() * xformScalingFactorX, bbox.getHeight() * xformScalingFactorY));
        }
        else
        {
            try
            {
                graphics.drawImage(image, null, null);
            }
            catch (InternalError ie)
            {
                LOG.error("Exception drawing image, see JDK-6689349, " +
                          "try rendering into a BufferedImage instead", ie);
            }
        }

        graphics.setTransform(savedTransform);
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
        private final int maxX;
        private final int maxY;
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
            Graphics2D savedGraphics = graphics;
            List<Path2D> savedLastClips = lastClips;
            Shape savedInitialClip = initialClip;

            // get the CTM x Form Matrix transform
            Matrix transform = Matrix.concatenate(ctm, form.getMatrix());

            // transform the bbox
            PDRectangle formBBox = form.getBBox();
            if (formBBox == null)
            {
                // PDFBOX-5471
                // check done here and not in caller to avoid getBBox() creating rectangle twice
                LOG.warn("transparency group ignored because BBox is null");
                formBBox = new PDRectangle();
            }
            GeneralPath transformedBox = formBBox.transform(transform);

            // clip the bbox to prevent giant bboxes from consuming all memory
            Area transformed = new Area(transformedBox);
            transformed.intersect(getGraphicsState().getCurrentClippingPath());
            Rectangle2D clipRect = transformed.getBounds2D();
            if (clipRect.isEmpty())
            {
                image = null;
                bbox = null;
                minX = 0;
                minY = 0;
                maxX = 0;
                maxY = 0;
                width = 0;
                height = 0;
                return;
            }
            this.bbox = new PDRectangle((float)clipRect.getX(), (float)clipRect.getY(),
                                        (float)clipRect.getWidth(), (float)clipRect.getHeight());

            // apply the underlying Graphics2D device's DPI transform
            AffineTransform xformOriginal = xform;
            xform = AffineTransform.getScaleInstance(xformScalingFactorX, xformScalingFactorY);
            Rectangle2D bounds = xform.createTransformedShape(clipRect).getBounds2D();

            minX = (int) Math.floor(bounds.getMinX());
            minY = (int) Math.floor(bounds.getMinY());
            maxX = (int) Math.floor(bounds.getMaxX()) + 1;
            maxY = (int) Math.floor(bounds.getMaxY()) + 1;

            width = maxX - minX;
            height = maxY - minY;

            // FIXME - color space
            if (isGray(form.getGroup().getColorSpace(form.getResources())))
            {
                image = create2ByteGrayAlphaImage(width, height);
            }
            else
            {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            boolean needsBackdrop = !isSoftMask && !form.getGroup().isIsolated() &&
                hasBlendMode(form, new HashSet<>());
            BufferedImage backdropImage = null;
            // Position of this group in parent group's coordinates
            int backdropX = 0;
            int backdropY = 0;
            if (needsBackdrop)
            {
                if (transparencyGroupStack.isEmpty())
                {
                    // Use the current page as the parent group.
                    backdropImage = renderer.getPageImage();
                    if (backdropImage == null)
                    {
                        needsBackdrop = false;
                    }
                    else
                    {
                        backdropX = minX;
                        backdropY = backdropImage.getHeight() - maxY;
                    }
                }
                else
                {
                    TransparencyGroup parentGroup = transparencyGroupStack.peek();
                    backdropImage = parentGroup.image;
                    backdropX = minX - parentGroup.minX;
                    backdropY = parentGroup.maxY - maxY;
                }
            }

            Graphics2D g = image.createGraphics();
            if (needsBackdrop)
            {
                // backdropImage must be included in group image but not in group alpha.
                g.drawImage(backdropImage, 0, 0, width, height,
                    backdropX, backdropY, backdropX + width, backdropY + height, null);
                g = new GroupGraphics(image, g);
            }
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

            boolean savedFlipTG = flipTG;
            flipTG = false;

            // apply device transform (DPI)
            // the initial translation is ignored, because we're not writing into the initial graphics device
            g.transform(xform);

            PDRectangle pageSizeOriginal = pageSize;
            pageSize = new PDRectangle(minX / xformScalingFactorX,
                                       minY / xformScalingFactorY,
                                       (float) (bounds.getWidth() / xformScalingFactorX),
                                        (float) (bounds.getHeight() / xformScalingFactorY));
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
                    transparencyGroupStack.push(this);
                    processTransparencyGroup(form);
                    if (!transparencyGroupStack.isEmpty())
                    {
                        transparencyGroupStack.pop();
                    }
                }

                if (needsBackdrop)
                {
                    ((GroupGraphics) graphics).removeBackdrop(backdropImage, backdropX, backdropY);
                }
            }
            finally 
            {
                flipTG = savedFlipTG;
                lastClips = savedLastClips;
                graphics.dispose();
                graphics = savedGraphics;
                initialClip = savedInitialClip;
                clipWindingRule = clipWindingRuleOriginal;
                linePath = linePathOriginal;
                pageSize = pageSizeOriginal;
                xform = xformOriginal;
            }
        }

        // http://stackoverflow.com/a/21181943/535646
        private BufferedImage create2ByteGrayAlphaImage(int width, int height) 
        {
            // gray + alpha
            int[] bandOffsets = {1, 0};
            int bands = bandOffsets.length;

            // Color Model used for raw GRAY + ALPHA
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
                    LOG.debug("Couldn't get an alternate ColorSpace", ex);
                    return false;
                }
            }
            return false;
        }

        BufferedImage getImage()
        {
            return image;
        }

        PDRectangle getBBox()
        {
            return bbox;
        }

        Rectangle2D getBounds()
        {
            // apply the underlying Graphics2D device's DPI transform and y-axis flip
            Rectangle2D r = 
                    new Rectangle2D.Double(
                            minX - pageSize.getLowerLeftX() * xformScalingFactorX,
                            (pageSize.getLowerLeftY() + pageSize.getHeight()) * xformScalingFactorY - minY - height,
                            width,
                            height);
            // this adjusts the rectangle to the rotated image to put the soft mask at the correct position
            //TODO
            // 1. change transparencyGroup.getBounds() to getOrigin(), because size isn't used in SoftMask,
            // 2. Is it possible to create the softmask and transparency group in the correct rotation?
            //    (needs rendering identity testing before committing!)
            AffineTransform adjustedTransform = new AffineTransform(xform);
            adjustedTransform.scale(1.0 / xformScalingFactorX, 1.0 / xformScalingFactorY);
            return adjustedTransform.createTransformedShape(r).getBounds2D();
        }
    }

    private boolean hasBlendMode(PDTransparencyGroup group, Set<COSBase> groupsDone)
    {
        if (groupsDone.contains(group.getCOSObject()))
        {
            // The group is being processed. Avoid endless recursion.
            return false;
        }
        groupsDone.add(group.getCOSObject());

        Boolean val = blendModeMap.get(group.getCOSObject());
        if (val != null)
        {
            return val;
        }

        PDResources resources = group.getResources();
        if (resources == null)
        {
            blendModeMap.put(group.getCOSObject(), false);
            return false;
        }
        for (COSName name : resources.getExtGStateNames())
        {
            PDExtendedGraphicsState extGState = resources.getExtGState(name);
            if (extGState == null)
            {
                continue;
            }
            BlendMode blendMode = extGState.getBlendMode();
            if (blendMode != BlendMode.NORMAL)
            {
                blendModeMap.put(group.getCOSObject(), true);
                return true;
            }
        }

        // Recursively process nested transparency groups
        for (COSName name : resources.getXObjectNames())
        {
            PDXObject xObject;
            try
            {
                xObject = resources.getXObject(name);
            }
            catch (IOException ex)
            {
                continue;
            }
            if (xObject instanceof PDTransparencyGroup &&
                hasBlendMode((PDTransparencyGroup)xObject, groupsDone))
            {
                blendModeMap.put(group.getCOSObject(), true);
                return true;
            }
        }

        blendModeMap.put(group.getCOSObject(), false);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginMarkedContentSequence(COSName tag, COSDictionary properties)
    {
        if (nestedHiddenOCGCount > 0)
        {
            nestedHiddenOCGCount++;
            return;
        }
        if (tag == null || getResources() == null)
        {
            return;
        }
        if (isHiddenOCG(getResources().getProperties(tag)))
        {
            nestedHiddenOCGCount = 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endMarkedContentSequence()
    {
        if (nestedHiddenOCGCount > 0)
        {
            nestedHiddenOCGCount--;
        }
    }

    private boolean isContentRendered()
    {
        return nestedHiddenOCGCount <= 0;
    }

    private boolean isHiddenOCG(PDPropertyList propertyList)
    {
        if (propertyList instanceof PDOptionalContentGroup)
        {
            PDOptionalContentGroup group = (PDOptionalContentGroup) propertyList;
            RenderState printState = group.getRenderState(destination);
            if (printState == null)
            {
                if (!getRenderer().isGroupEnabled(group))
                {
                    return true;
                }
            }
            else if (RenderState.OFF == printState)
            {
                return true;
            }
        }
        else if (propertyList instanceof PDOptionalContentMembershipDictionary)
        {
            return isHiddenOCMD((PDOptionalContentMembershipDictionary) propertyList);
        }
        return false;
    }

    private boolean isHiddenOCMD(PDOptionalContentMembershipDictionary ocmd)
    {
        COSArray veArray = ocmd.getCOSObject().getCOSArray(COSName.VE);
        if (veArray != null && !veArray.isEmpty())
        {
            return isHiddenVisibilityExpression(veArray);
        }
        List<PDPropertyList> oCGs = ocmd.getOCGs();
        if (oCGs.isEmpty())
        {
            return false;
        }
        List<Boolean> visibles = new ArrayList<>();
        oCGs.forEach(prop -> visibles.add(!isHiddenOCG(prop)));
        COSName visibilityPolicy = ocmd.getVisibilityPolicy();
        
        // visible if any of the entries in OCGs are OFF
        if (COSName.ANY_OFF.equals(visibilityPolicy))
        {
            return visibles.stream().allMatch(v -> v);
        }

        // visible only if all of the entries in OCGs are ON
        if (COSName.ALL_ON.equals(visibilityPolicy))
        {
            return visibles.stream().anyMatch(v -> !v);
        }

        // visible only if all of the entries in OCGs are OFF
        if (COSName.ALL_OFF.equals(visibilityPolicy))
        {
            return visibles.stream().anyMatch(v -> v);
        }

        // visible if any of the entries in OCGs are ON
        // AnyOn is default
        return visibles.stream().noneMatch(v -> v);
    }

    private boolean isHiddenVisibilityExpression(COSArray veArray)
    {
        if (veArray.isEmpty())
        {
            return false;
        }
        String op = veArray.getName(0);
        if (op == null)
        {
            return false;
        }
        switch (op)
        {
            case "And":
                return isHiddenAndVisibilityExpression(veArray);
            case "Or":
                return isHiddenOrVisibilityExpression(veArray);
            case "Not":
                return isHiddenNotVisibilityExpression(veArray);
            default:
                return false;
        }
    }

    private boolean isHiddenAndVisibilityExpression(COSArray veArray)
    {
        // hidden if at least one isn't visible
        for (int i = 1; i < veArray.size(); ++i)
        {
            COSBase base = veArray.getObject(i);
            if (base instanceof COSArray)
            {
                // Another VE
                boolean isHidden = isHiddenVisibilityExpression((COSArray) base);
                if (isHidden)
                {
                    return true;
                }
            }
            else if (base instanceof COSDictionary)
            {
                // Another OCG
                PDPropertyList prop = PDPropertyList.create((COSDictionary) base);
                boolean isHidden = isHiddenOCG(prop);
                if (isHidden)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHiddenOrVisibilityExpression(COSArray veArray)
    {
        // hidden only if all are hidden
        for (int i = 1; i < veArray.size(); ++i)
        {
            COSBase base = veArray.getObject(i);
            if (base instanceof COSArray)
            {
                // Another VE
                boolean isHidden = isHiddenVisibilityExpression((COSArray) base);
                if (!isHidden)
                {
                    return false;
                }
            }
            else if (base instanceof COSDictionary)
            {
                // Another OCG
                PDPropertyList prop = PDPropertyList.create((COSDictionary) base);
                boolean isHidden = isHiddenOCG(prop);
                if (!isHidden)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHiddenNotVisibilityExpression(COSArray veArray)
    {
        if (veArray.size() != 2)
        {
            return false;
        }
        COSBase base = veArray.getObject(1);
        if (base instanceof COSArray)
        {
            // Another VE
            return !isHiddenVisibilityExpression((COSArray) base);
        }
        else if (base instanceof COSDictionary)
        {
            // Another OCG
            PDPropertyList prop = PDPropertyList.create((COSDictionary) base);
            return !isHiddenOCG(prop);
        }
        return false;
    }

    private LookupTable getInvLookupTable()
    {
        if (invTable == null)
        {
            byte[] inv = new byte[256];
            for (int i = 0; i < inv.length; i++)
            {
                inv[i] = (byte) (255 - i);
            }
            invTable = new ByteLookupTable(0, inv);
        }
        return invTable;
    }
}
