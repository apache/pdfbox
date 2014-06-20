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
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.font.Glyph2D;
import org.apache.pdfbox.pdfviewer.font.TTFGlyph2D;
import org.apache.pdfbox.pdfviewer.font.Type1Glyph2D;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.FontManager;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0Font;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.blend.SoftMaskPaint;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This will paint a page in a PDF document to a graphics context.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PageDrawer extends PDFStreamEngine
{
    private static final Log LOG = LogFactory.getLog(PageDrawer.class);
    private static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

    // parent document renderer
    private final PDFRenderer renderer;

    private Graphics2D graphics;

    // clipping winding rule used for the clipping path.
    private int clippingWindingRule = -1;

    private GeneralPath linePath = new GeneralPath();

    private Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();
    private Map<PDFont, Font> awtFonts = new HashMap<PDFont, Font>();

    private int pageHeight;
    
    /**
     * Default constructor, loads properties from file.
     * 
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer(PDFRenderer renderer) throws IOException
    {
        super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PageDrawer.properties", true));
        this.renderer = renderer;
    }

    /**
     * Returns the parent renderer.
     * @return the parent renderer.
     */
    public PDFRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Returns the page height.
     * @return the page height
     */
    public int getPageHeight()
    {
        return pageHeight;
    }

    /**
     * This will draw the page to the requested context.
     * 
     * @param g The graphics context to draw onto.
     * @param page The page to draw.
     * @param pageSize The size of the page to draw.
     * 
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage(Graphics g, PDPage page, PDRectangle pageSize) throws IOException
    {
        graphics = (Graphics2D) g;
        pageHeight = (int)pageSize.getHeight();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.translate(0, pageHeight);
        graphics.scale(1, -1);
        // TODO use getStroke() to set the initial stroke
        graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // Only if there is some content, we have to process it.
        // Otherwise we are done here and we will produce an empty page
        if (page.getContents() != null)
        {
            PDResources resources = page.findResources();
            processStream(resources, page.getContents().getStream(), page.findCropBox(), page.findRotation());
        }

        List<PDAnnotation> annotations = page.getAnnotations();
        for (int i = 0; i < annotations.size(); i++)
        {
            PDAnnotation annot = (PDAnnotation) annotations.get(i);
            PDRectangle rect = annot.getRectangle();
            String appearanceName = annot.getAppearanceStream();
            PDAppearanceDictionary appearDictionary = annot.getAppearance();
            if (appearDictionary != null)
            {
                if (appearanceName == null)
                {
                    appearanceName = "default";
                }
                Map<String, PDAppearanceStream> appearanceMap = appearDictionary.getNormalAppearance();
                if (appearanceMap != null)
                {
                    PDAppearanceStream appearance = (PDAppearanceStream) appearanceMap.get(appearanceName);
                    if (appearance != null)
                    {
                        Point2D point = new Point2D.Float(rect.getLowerLeftX(), rect.getLowerLeftY());
                        Matrix matrix = appearance.getMatrix();
                        if (matrix != null)
                        {
                            // transform the rectangle using the given matrix
                            AffineTransform at = matrix.createAffineTransform();
                            at.scale(1, -1);
                            at.transform(point, point);
                        }
                        graphics.translate((int) point.getX(), -(int) point.getY());
                        processSubStream(appearance.getResources(), appearance.getStream());
                        graphics.translate(-(int) point.getX(), (int) point.getY());
                    }
                }
            }
        }
        graphics = null;
    }

    /**
     * This will draw the pattern stream to the requested context.
     *
     * @param g The graphics context to draw onto.
     * @param pattern The tiling pattern to be used.
     * @param pageDimension The size of the page to draw.
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawTilingPattern(Graphics2D g, PDTilingPattern pattern, PDRectangle pageDimension,
                                  Matrix matrix, PDColorSpace colorSpace, PDColor color)
                                  throws IOException
    {
        graphics = g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        initStream(pageDimension, 0);

        // transform ctm
        Matrix concat = matrix.multiply(getGraphicsState().getCurrentTransformationMatrix());
        getGraphicsState().setCurrentTransformationMatrix(concat);

        // color
        if (colorSpace != null)
        {
            getGraphicsState().setNonStrokingColorSpace(colorSpace);
            getGraphicsState().setNonStrokingColor(color);
            getGraphicsState().setStrokingColorSpace(colorSpace);
            getGraphicsState().setStrokingColor(color);
        }

        processSubStream(pattern.getResources(), (COSStream)pattern.getCOSObject());
    }

    /**
     * Remove all cached resources.
     */
    public void dispose()
    {
        super.dispose();
        if (fontGlyph2D != null)
        {
            Iterator<Glyph2D> iter = fontGlyph2D.values().iterator();
            while (iter.hasNext())
            {
                iter.next().dispose();
            }
            fontGlyph2D.clear();
            fontGlyph2D = null;
        }
        if (awtFonts != null)
        {
            awtFonts.clear();
        }
        graphics = null;
        linePath = null;
    }

    /**
     * You should override this method if you want to perform an action when a text is being processed.
     * 
     * @param text The text to process
     */
    protected void processTextPosition(TextPosition text)
    {
        try
        {
            PDGraphicsState graphicsState = getGraphicsState();
            Composite composite;
            Paint paint;
            switch (graphicsState.getTextState().getRenderingMode())
            {
            case PDTextState.RENDERING_MODE_FILL_TEXT:
                composite = graphicsState.getNonStrokeJavaComposite();
                paint = getNonStrokingPaint();
                break;
            case PDTextState.RENDERING_MODE_STROKE_TEXT:
                composite = graphicsState.getStrokeJavaComposite();
                paint = getStrokingPaint();
                break;
            case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
                // basic support for text rendering mode "invisible"
                // TODO why are we drawing anything at all?
                paint = COLOR_TRANSPARENT;
                composite = graphicsState.getStrokeJavaComposite();
                break;
            default:
                // TODO : need to implement....
                LOG.debug("Unsupported RenderingMode " + this.getGraphicsState().getTextState().getRenderingMode()
                        + " in PageDrawer.processTextPosition()." + " Using RenderingMode "
                        + PDTextState.RENDERING_MODE_FILL_TEXT + " instead");
                composite = graphicsState.getNonStrokeJavaComposite();
                paint = getNonStrokingPaint();
            }
            graphics.setComposite(composite);
            graphics.setPaint(paint);

            PDFont font = text.getFont();
            AffineTransform at = text.getTextPos().createAffineTransform();
            PDMatrix fontMatrix = font.getFontMatrix();
            // TODO setClip() is a massive performance hot spot. Investigate optimization possibilities
            graphics.setClip(graphicsState.getCurrentClippingPath());

            // use different methods to draw the string
            if (font.isType3Font())
            {
                // Type3 fonts don't use the same units within the font matrix as all the other fonts
                at.scale(fontMatrix.getValue(0, 0), fontMatrix.getValue(1, 1));
                // Type3 fonts are using streams for each character
                drawType3String((PDType3Font) font, text, at);
            }
            else
            {
                Glyph2D glyph2D = createGlyph2D(font);
                if (glyph2D != null)
                {
                    AffineTransform fontMatrixAT = new AffineTransform(fontMatrix.getValue(0, 0), fontMatrix.getValue(
                            0, 1), fontMatrix.getValue(1, 0), fontMatrix.getValue(1, 1), fontMatrix.getValue(2, 0),
                            fontMatrix.getValue(2, 1));
                    at.concatenate(fontMatrixAT);
                    // Let PDFBox render the font if supported
                    drawGlyph2D(glyph2D, text.getCodePoints(), at);
                }
                else
                {
                    // Use AWT to render the font (standard14 fonts, substituted embedded fonts)
                    // TODO to be removed in the long run
                    drawString(font, text.getCharacter(), at);
                }
            }
        }
        catch (IOException io)
        {
            LOG.error (io, io);
        }
    }

    /**
     * Render the font using the Glyph2d interface.
     * 
     * @param glyph2D the Glyph2D implementation provided a GeneralPath for each glyph
     * @param codePoints the string to be rendered
     * @param at the transformation
     * @throws IOException if something went wrong
     */
    private void drawGlyph2D(Glyph2D glyph2D, int[] codePoints, AffineTransform at) throws IOException
    {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < codePoints.length; i++)
        {
            GeneralPath path = glyph2D.getPathForCharacterCode(codePoints[i]);
            if (path != null)
            {
                AffineTransform atInverse = null;
                if (!at.isIdentity())
                {
                    try
                    {
                        atInverse = at.createInverse();
                    }
                    catch (NoninvertibleTransformException exception)
                    {
                        LOG.error("Can't invert the given affine transformation", exception);
                    }
                }
                if (atInverse != null)
                {
                    graphics.transform(at);
                }
                graphics.fill(path);
                if (atInverse != null)
                {
                    graphics.transform(atInverse);
                }
            }
        }
    }

    /**
     * Render the text using a type 3 font.
     * 
     * @param font the type3 font
     * @param text the text to be rendered
     * @param at the transformation
     * 
     * @throws IOException if something went wrong
     */
    private void drawType3String(PDType3Font font, TextPosition text, AffineTransform at) throws IOException
    {
        int[] codePoints = text.getCodePoints();
        int textLength = codePoints.length;
        for (int i = 0; i < textLength; i++)
        {
            COSStream stream = font.getCharStream((char)codePoints[i]);
            if (stream != null)
            {
                // save the current graphics state and matrices
                saveGraphicsState();
                Matrix textMatrix = getTextMatrix();
                Matrix textLineMatrix = getTextLineMatrix();
                
                Matrix ctm = new Matrix();
                ctm.setFromAffineTransform(at);
                getGraphicsState().setCurrentTransformationMatrix(ctm);
                processSubStream(font.getType3Resources(), stream);

                // restore the saved graphics state and matrices
                restoreGraphicsState();
                setTextLineMatrix(textLineMatrix);
                setTextMatrix(textMatrix);
                 
            }
            else
            {
                LOG.debug("drawType3String: stream for character " + (char)codePoints[i] + " not found");
            }
        }
    }

    /**
     * This will draw a string on a canvas using the font.
     *
     * @param font the font to be used to draw the string
     * @param string The string to draw.
     * @param at The transformation matrix with all information for scaling and shearing of the font.
     *
     * @throws IOException If there is an error drawing the specific string.
     */
    private void drawString(PDFont font, String string, AffineTransform at) throws IOException
    {
        Font awtFont = createAWTFont(font);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector glyphs = awtFont.createGlyphVector(frc, string);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        writeFont(at, glyphs);
    }

    private void writeFont(final AffineTransform at, final GlyphVector glyphs)
    {
        // Convert from PDF, where glyphs are upright when direction is from
        // bottom to top, to AWT, where this is the other way around
        
        // PDFBOX-2141: do not use graphics.transform(), because this prevents
        // the correct rendering of shading patterns
        // don't apply the translation to each glyph, only scale and shear
        AffineTransform atRS = new AffineTransform(at.getScaleX(), at.getShearY(), 
                -at.getShearX(), -at.getScaleY(), 0, 0);

        for (int i = 0; i < glyphs.getNumGlyphs(); i++)
        {
            glyphs.setGlyphTransform(i, atRS);
        }
        graphics.drawGlyphVector(glyphs, (float) at.getTranslateX(), (float) at.getTranslateY());
    }

    /**
     * Provides an AWT font for the given PDFont.
     * 
     * @param font the font which needs an AWT font
     * @return the corresponding AWT font
     * @throws IOException if something went wrong
     */
    private Font createAWTFont(PDFont font) throws IOException
    {
        Font awtFont = null;
        // Is there already a AWTFont for the given font?
        if (awtFonts.containsKey(font))
        {
            awtFont = awtFonts.get(font);
        }
        else
        {
            if (font instanceof PDType1Font)
            {
                PDType1Font type1Font = (PDType1Font) font;
                PDFontDescriptor fd = type1Font.getFontDescriptor();
                if (fd instanceof PDFontDescriptorDictionary)
                {
                    PDFontDescriptorDictionary fdDictionary = (PDFontDescriptorDictionary) fd;
                    if (fdDictionary.getFontFile() == null)
                    {
                        // check if the font is part of our environment
                        if (fd.getFontName() != null)
                        {
                            awtFont = FontManager.getAwtFont(fd.getFontName());
                        }
                        if (awtFont == null)
                        {
                            LOG.info("Can't find the specified font " + fd.getFontName());
                        }
                    }
                }
                else
                {
                    // check if the font is part of our environment
                    String baseFont = type1Font.getBaseFont();
                    awtFont = FontManager.getAwtFont(baseFont);
                    if (awtFont == null)
                    {
                        LOG.info("Can't find the specified basefont " + baseFont);
                    }
                }
            }
            else
            {
                LOG.info("Unsupported type of font " + font.getClass().getName());
            }
            if (awtFont == null)
            {
                // Fallback: we can't find anything, so we have to use the standard font
                awtFont = FontManager.getStandardFont();
                LOG.info("Using font " + awtFont.getName() + " instead of " + font.getBaseFont());
            }
            awtFonts.put(font, awtFont);
        }
        return awtFont;
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
        Glyph2D glyph2D = null;
        // Is there already a Glyph2D for the given font?
        if (fontGlyph2D.containsKey(font))
        {
            glyph2D = fontGlyph2D.get(font);
        }
        else
        {
            // check if the given font is supported
            if (font instanceof PDTrueTypeFont)
            {
                PDTrueTypeFont ttfFont = (PDTrueTypeFont) font;
                // get the true type font raw data
                TrueTypeFont ttf = ttfFont.getTTFFont();
                if (ttf != null)
                {
                    glyph2D = new TTFGlyph2D(ttfFont);
                }
            }
            else if (font instanceof PDType1Font)
            {
                PDType1Font pdType1Font = (PDType1Font) font;
                PDType1CFont type1CFont = pdType1Font.getType1CFont();
                if (type1CFont != null)
                {
                    // get the cffFont raw data
                    CFFFont cffFont = type1CFont.getCFFFont();
                    if (cffFont != null)
                    {
                        glyph2D = new Type1Glyph2D(cffFont, type1CFont.getFontEncoding());
                    }
                }
                else
                {
                    // get the pfb raw data
                    Type1Font type1Font = pdType1Font.getType1Font();
                    if (type1Font != null)
                    {
                        glyph2D = new Type1Glyph2D(type1Font, pdType1Font.getFontEncoding());
                    }
                }
            }
            else if (font instanceof PDType0Font)
            {
                PDType0Font type0Font = (PDType0Font) font;
                if (type0Font.getDescendantFont() instanceof PDCIDFontType2Font)
                {
                    // a Type2 CIDFont contains a TTF font
                    PDCIDFontType2Font cidType2Font = (PDCIDFontType2Font) type0Font.getDescendantFont();
                    // get the true type font raw data
                    TrueTypeFont ttf = cidType2Font.getTTFFont();
                    if (ttf != null)
                    {
                        glyph2D = new TTFGlyph2D(type0Font);
                    }
                }
                else if (type0Font.getDescendantFont() instanceof PDCIDFontType0Font)
                {
                    // a Type0 CIDFont contains CFF font
                    PDCIDFontType0Font cidType2Font = (PDCIDFontType0Font) type0Font.getDescendantFont();
                    PDType1CFont type1CFont = cidType2Font.getType1CFont();
                    if (type1CFont != null)
                    {
                        // get the cffFont raw data
                        CFFFont cffFont = type1CFont.getCFFFont();
                        if (cffFont != null)
                        {
                            glyph2D = new Type1Glyph2D(cffFont, type1CFont.getFontEncoding());
                        }
                    }
                }
            }
            // cache the Glyph2D instance
            if (glyph2D != null)
            {
                fontGlyph2D.put(font, glyph2D);
            }
        }
        return glyph2D;
    }

    /**
     * Get the current line path to be drawn.
     * 
     * @return The current line path to be drawn.
     */
    public GeneralPath getLinePath()
    {
        return linePath;
    }

    /**
     * Generates awt raster for a soft mask
     * 
     * @param context
     * @return
     * @throws IOException
     */
    private Raster createSoftMaskRaster(PDSoftMask softMask) throws IOException
    {
        PageDrawer.Group result = createPageDrawerGroup(softMask.getGroup());
        COSName sMaskSubType = softMask.getSubType();
        if (COSName.ALPHA.equals(sMaskSubType))
        {
            return result.getAlphaRaster();
        }
        else if (COSName.LUMINOSITY.equals(sMaskSubType))
        {
            return result.getLuminosityRaster();
        }
        else
        {
            throw new IOException("Invalid soft mask subtype.");
        }
    }

    private Paint applySoftMaskToPaint(Paint parentPaint, PDSoftMask softMask) throws IOException  
    {
        if (softMask != null) 
        {
            return new SoftMaskPaint(parentPaint, createSoftMaskRaster(softMask));
        }
        else 
        {
            return parentPaint;
        }
    }

    // returns the stroking AWT Paint
    private Paint getStrokingPaint() throws IOException
    {
        PDGraphicsState graphicsState = getGraphicsState();
        return applySoftMaskToPaint(graphicsState.getStrokingColorSpace()
                .toPaint(renderer, graphicsState.getStrokingColor(), pageHeight), graphicsState.getSoftMask());
    }

    // returns the non-stroking AWT Paint
    private Paint getNonStrokingPaint() throws IOException
    {
        return getGraphicsState().getNonStrokingColorSpace()
                .toPaint(renderer, getGraphicsState().getNonStrokingColor(), pageHeight);
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
                dashArray[i] = transformWidth(dashArray[i]);
            }
            phaseStart = (int)transformWidth(phaseStart);

            // empty dash array is illegal
            if (dashArray.length == 0)
            {
                dashArray = null;
            }
        }
        return new BasicStroke(lineWidth, state.getLineCap(), state.getLineJoin(),
                               state.getMiterLimit(), dashArray, phaseStart);
    }

    /**
     * Stroke the path.
     * 
     * @throws IOException If there is an IO error while stroking the path.
     */
    public void strokePath() throws IOException
    {
        graphics.setComposite(getGraphicsState().getStrokeJavaComposite());
        Paint strokingPaint = getStrokingPaint();
        if (strokingPaint == null)
        {
            LOG.info("ColorSpace " + getGraphicsState().getStrokingColorSpace().getName() +
                     " doesn't provide a stroking color, using white instead!");
            strokingPaint = Color.WHITE;// ((PageDrawer)context).strokePath();
        }
        graphics.setPaint(strokingPaint);
        graphics.setStroke(getStroke());
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        graphics.draw(linePath);
        linePath.reset();
    }

    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     *
     * @throws IOException If there is an IO error while filling the path.
     */
    public void fillPath(int windingRule) throws IOException
    {
        graphics.setComposite(getGraphicsState().getNonStrokeJavaComposite());
        Paint nonStrokingPaint = getNonStrokingPaint();
        if (nonStrokingPaint == null)
        {
            LOG.info("ColorSpace " + getGraphicsState().getNonStrokingColorSpace().getName() +
                    " doesn't provide a non-stroking color, using white instead!");
            nonStrokingPaint = Color.WHITE;
        }
        graphics.setPaint(nonStrokingPaint);
        linePath.setWindingRule(windingRule);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        graphics.fill(linePath);
        linePath.reset();
    }

    /**
     * Fills and then strokes the path.
     *
     * @param windingRule The winding rule this path will use.
     *
     * @throws IOException If there is an IO error while filling the path.
     */
    public void fillAndStrokePath(int windingRule) throws IOException
    {
        // TODO can we avoid cloning the path?
        GeneralPath path = (GeneralPath)linePath.clone();
        fillPath(windingRule);
        linePath = path;
        strokePath();
    }

    // This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
    /**
     * use the current transformation matrix to transform a single point.
     *
     * @param x x-coordinate of the point to be transform
     * @param y y-coordinate of the point to be transform
     * @return the transformed coordinates as Point2D.Double
     */
    public Point2D.Double transformedPoint(double x, double y)
    {
        double[] position = { x, y };
        getGraphicsState().getCurrentTransformationMatrix().createAffineTransform()
                .transform(position, 0, position, 0, 1);
        return new Point2D.Double(position[0], position[1]);
    }

    // transforms a width using the CTM
    private float transformWidth(float width)
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

        if (ctm == null)
        {
            // TODO does the CTM really need to use null?
            return width;
        }

        float x = ctm.getValue(0, 0) + ctm.getValue(1, 0);
        float y = ctm.getValue(0, 1) + ctm.getValue(1, 1);
        return width * (float)Math.sqrt((x * x + y * y) * 0.5);
    }

    /**
     * Set the clipping winding rule.
     * 
     * @param windingRule The winding rule which will be used for clipping.
     * 
     */
    public void setClippingWindingRule(int windingRule)
    {
        clippingWindingRule = windingRule;
    }

    /**
     * Set the clipping Path.
     * 
     */
    public void endPath()
    {
        if (clippingWindingRule > -1)
        {
            PDGraphicsState graphicsState = getGraphicsState();
            GeneralPath clippingPath = (GeneralPath) linePath.clone();  // TODO do we really need to clone this? isn't the line path reset anyway?
            clippingPath.setWindingRule(clippingWindingRule);
            // If there is already set a clipping path, we have to intersect the new with the existing one
            if (graphicsState.getCurrentClippingPath() != null)
            {
                Area currentArea = new Area(getGraphicsState().getCurrentClippingPath());
                Area newArea = new Area(clippingPath);
                currentArea.intersect(newArea);
                graphicsState.setCurrentClippingPath(currentArea);
            }
            else
            {
                graphicsState.setCurrentClippingPath(clippingPath);
            }
            clippingWindingRule = -1;
        }
        linePath.reset();
    }

    /**
     * Draw the AWT image. Called by Invoke. Moved into PageDrawer so that Invoke doesn't have to reach in here for
     * Graphics as that breaks extensibility.
     * 
     * @param awtImage The image to draw.
     * @param at The transformation to use when drawing.
     * 
     */
    public void drawImage(Image awtImage, AffineTransform at) throws IOException
    {
        graphics.setComposite(getGraphicsState().getNonStrokeJavaComposite());
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if( softMask != null ) 
        {
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(1, -1);
            imageTransform.translate(0, -1);
            Paint awtPaint = new TexturePaint((BufferedImage)awtImage, 
                    new Rectangle2D.Double(imageTransform.getTranslateX(), imageTransform.getTranslateY(),
                            imageTransform.getScaleX(), imageTransform.getScaleY())); 
            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
            graphics.setPaint(awtPaint);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            Rectangle2D unitRect = new Rectangle2D.Float(0, 0, 1, 1);
            graphics.fill(at.createTransformedShape(unitRect));
        }
        else 
        {
            int width = awtImage.getWidth(null);
            int height = awtImage.getHeight(null);
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(1.0 / width, -1.0 / height);
            imageTransform.translate(0, -height);
            graphics.drawImage(awtImage, imageTransform, null);
        }
    }

    /**
     * Fill with Shading. Called by SHFill operator.
     *
     * @param shadingName The name of the Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the clipping area.
     */
    public void shFill(COSName shadingName) throws IOException
    {
        PDShading shading = getResources().getShadings().get(shadingName.getName());
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Paint paint = shading.toPaint(ctm, pageHeight);

        graphics.setComposite(getGraphicsState().getNonStrokeJavaComposite());
        graphics.setPaint(paint);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(null); // PDFBOX-2153 don't use obsolete clipping path
        graphics.fill(getGraphicsState().getCurrentClippingPath());
    }

    /**
     * Creates a buffered image for a transparency group result.
     *
     * @param clippingPath clipping path (in current graphics2D coordinates)
     * @param resources Global resources
     * @param content Content of the transparency group to create
     * @return {@link Group} object
     */
    private Group createGroup(GeneralPath clippingPath, PDResources resources, COSStream content) throws IOException {
        return new Group(clippingPath, resources, content);
    }


    /**
     * Draws the transparency group into a {@link BufferedImage} object and returns it together with the transformation matrix
     *
     * @param context {@link PageDrawer} object
     * @return PageDrawer.Group
     * @throws IOException
     */
    public PageDrawer.Group createPageDrawerGroup(PDFormXObject form) throws IOException {
        // save the graphics state
        saveGraphicsState();

        try {
            PDResources pdResources = form.getResources();
            if (pdResources == null) {
                pdResources = getResources();
            }

            // if there is an optional form matrix, we have to
            // map the form space to the user space
            Matrix matrix = form.getMatrix();
            if(matrix != null)
            {
                Matrix xobjectCTM = matrix.multiply(getGraphicsState().getCurrentTransformationMatrix());
                getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
            }

            PDRectangle bBox = form.getBBox();

            float x1 = bBox.getLowerLeftX();
            float y1 = bBox.getLowerLeftY();
            float x2 = bBox.getUpperRightX();
            float y2 = bBox.getUpperRightY();

            Point2D p0 = transformedPoint(x1, y1);
            Point2D p1 = transformedPoint(x2, y1);
            Point2D p2 = transformedPoint(x2, y2);
            Point2D p3 = transformedPoint(x1, y2);

            GeneralPath path = new GeneralPath();
            path.moveTo((float) p0.getX(), (float) p0.getY());
            path.lineTo((float) p1.getX(), (float) p1.getY());
            path.lineTo((float) p2.getX(), (float) p2.getY());
            path.lineTo((float) p3.getX(), (float) p3.getY());
            path.closePath();

            return createGroup(path, pdResources, form.getCOSStream());
        }
        finally {
            // restore the graphics state
            restoreGraphicsState();
        }

    }

    /**
     * Create for rendering transparency groups...
     *
     **/
    public class Group {
        /**
         * {@link BufferedImage} object to draw into...
         */
        private final BufferedImage mImage;
        /**
         * Matrix for drawing the result
         */
        private final Matrix mResultMatrix;


        private final int minX;
        private final int minY;
        private final int width;
        private final int height;

        /**
         * Creates a group object. The group can now be created only if the underlying {@link Graphics2D} implementation
         * is SunGraphics2D (i.e. rendering to bitmap). For all other implementations, this throws
         * an {@link UnsupportedOperationException}.
         *
         * @param image
         * @param g2d
         */
        private Group(GeneralPath clippingPath, PDResources resources, COSStream content) throws IOException {
            Graphics2D g2dOriginal = graphics;

            // Check underlying g2d
            double unitSize = 1.0;

            Area resultClippingArea = new Area(getGraphicsState().getCurrentClippingPath());
            if(clippingPath != null) {
                Area newArea = new Area(clippingPath);            
                resultClippingArea.intersect(newArea);
            }

            AffineTransform at = g2dOriginal.getTransform();
            at.scale(unitSize, unitSize);
            Shape clippingPathInPixels = at.createTransformedShape(resultClippingArea);
            Rectangle2D bounds2D = clippingPathInPixels.getBounds2D();

            minX = (int) Math.floor(bounds2D.getMinX());
            minY = (int) Math.floor(bounds2D.getMinY());
            int maxX = (int) Math.floor(bounds2D.getMaxX()) + 1;
            int maxY = (int) Math.floor(bounds2D.getMaxY()) + 1;

            width = maxX - minX;
            height = maxY - minY;
            mImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);     // FIXME - color space
            Graphics2D groupG2D = mImage.createGraphics();
            groupG2D.translate(-minX, -minY);
            groupG2D.transform(at);
            groupG2D.setClip(resultClippingArea);

            AffineTransform atInv = null;
            Matrix tmpResultMatrix = null;
            try {
                atInv = groupG2D.getTransform().createInverse();
                atInv.scale(width, -height);
                atInv.translate(0, -1);
                tmpResultMatrix = new Matrix();
                tmpResultMatrix.setFromAffineTransform(atInv);
            }
            catch (NoninvertibleTransformException e) {
                LOG.warn("Non-invertible transform when rendering a transparency group.", e);
            }
            mResultMatrix = tmpResultMatrix;

            PDGraphicsState gs = getGraphicsState();
            gs.setBlendMode(BlendMode.NORMAL);
            gs.setAlphaConstants(1.0);
            gs.setNonStrokeAlphaConstants(1.0);
            gs.setSoftMask(null);
            graphics = groupG2D;
            try {
                processSubStream(resources, content);
            }
            finally 
            {
                graphics = g2dOriginal;
            }
        }

        public BufferedImage getImage() {
            return mImage;
        }

        /**
         * @return the resultMatrix
         */
        public Matrix getResultMatrix() {
            return mResultMatrix;
        }

        public void drawResult() throws IOException {
            if (mResultMatrix != null) {
                saveGraphicsState();
                drawImage(mImage, mResultMatrix.createAffineTransform());
                restoreGraphicsState();
            }
        }

        public Raster getAlphaRaster() {
            return mImage.getAlphaRaster().createTranslatedChild(minX, minY);
        }

        public Raster getLuminosityRaster() {
            BufferedImage tmpImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = tmpImage.getGraphics();
            g.drawImage(mImage, 0, 0, null);

            WritableRaster result = tmpImage.getRaster();
            return result.createTranslatedChild(minX, minY);
        }
    }
}
