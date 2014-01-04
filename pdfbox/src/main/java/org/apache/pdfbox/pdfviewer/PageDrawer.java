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
package org.apache.pdfbox.pdfviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.font.CFFGlyph2D;
import org.apache.pdfbox.pdfviewer.font.Glyph2D;
import org.apache.pdfbox.pdfviewer.font.TTFGlyph2D;
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
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.AxialShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingResources;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.pdmodel.graphics.shading.RadialShadingPaint;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;

/**
 * This will paint a page in a PDF document to a graphics context.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PageDrawer extends PDFStreamEngine
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PageDrawer.class);

    private Graphics2D graphics;

    /**
     * clipping winding rule used for the clipping path.
     */
    private int clippingWindingRule = -1;

    /**
     * Size of the page.
     */
    protected Dimension pageSize;
    /**
     * Current page to be rendered.
     */
    protected PDPage page;

    private GeneralPath linePath = new GeneralPath();

    private Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();
    private Map<PDFont, Font> awtFonts = new HashMap<PDFont, Font>();

    /**
     * Default constructor, loads properties from file.
     * 
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer() throws IOException
    {
        super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PageDrawer.properties", true));
    }

    /**
     * This will draw the page to the requested context.
     * 
     * @param g The graphics context to draw onto.
     * @param p The page to draw.
     * @param pageDimension The size of the page to draw.
     * 
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage(Graphics g, PDPage p, Dimension pageDimension) throws IOException
    {
        graphics = (Graphics2D) g;
        page = p;
        pageSize = pageDimension;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.translate(0, pageSize.height);
        graphics.scale(1, -1);
        // initialize the used stroke with CAP_BUTT instead of CAP_SQUARE
        graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        // Only if there is some content, we have to process it.
        // Otherwise we are done here and we will produce an empty page
        if (page.getContents() != null)
        {
            PDResources resources = page.findResources();
            processStream(page, resources, page.getContents().getStream());
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
                        processSubStream(page, appearance.getResources(), appearance.getStream());
                        graphics.translate(-(int) point.getX(), (int) point.getY());
                    }
                }
            }
        }
    }

    /**
     * Remove all cached resources.
     */
    public void dispose()
    {
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
        page = null;
        pageSize = null;
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
                paint = graphicsState.getNonStrokingColor().getJavaColor();
                if (paint == null)
                {
                    paint = graphicsState.getNonStrokingColor().getPaint(pageSize.height);
                }
                break;
            case PDTextState.RENDERING_MODE_STROKE_TEXT:
                composite = graphicsState.getStrokeJavaComposite();
                paint = graphicsState.getStrokingColor().getJavaColor();
                if (paint == null)
                {
                    paint = graphicsState.getStrokingColor().getPaint(pageSize.height);
                }
                break;
            case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
                // basic support for text rendering mode "invisible"
                Color nsc = graphicsState.getStrokingColor().getJavaColor();
                float[] components = { Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue() };
                paint = new Color(nsc.getColorSpace(), components, 0f);
                composite = graphicsState.getStrokeJavaComposite();
                break;
            default:
                // TODO : need to implement....
                LOG.debug("Unsupported RenderingMode " + this.getGraphicsState().getTextState().getRenderingMode()
                        + " in PageDrawer.processTextPosition()." + " Using RenderingMode "
                        + PDTextState.RENDERING_MODE_FILL_TEXT + " instead");
                composite = graphicsState.getNonStrokeJavaComposite();
                paint = graphicsState.getNonStrokingColor().getJavaColor();
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
                    // Use AWT to render the font (Type1 fonts, standard14 fonts, if the embedded font is substituted)
                    // TODO to be removed in the long run?
                    drawString((PDSimpleFont) font, text.getCharacter(), at);
                }
            }
        }
        catch (IOException io)
        {
            io.printStackTrace();
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
            GeneralPath path = glyph2D.getPathForCharactercode(codePoints[i]);
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
                // save the current graphics state
                getGraphicsStack().push((PDGraphicsState) getGraphicsState().clone());

                Matrix ctm = new Matrix();
                ctm.setFromAffineTransform(at);
                getGraphicsState().setCurrentTransformationMatrix(ctm);
                processSubStream(page, font.getType3Resources(), stream);

                // restore the saved graphics state
                setGraphicsState((PDGraphicsState) getGraphicsStack().pop());
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
     * @param g The graphics to draw onto.
     * @param at The transformation matrix with all information for scaling and shearing of the font.
     * 
     * @throws IOException If there is an error drawing the specific string.
     */
    private void drawString(PDSimpleFont font, String string, AffineTransform at) throws IOException
    {
        Font awtFont = createAWTFont(font);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector glyphs = awtFont.createGlyphVector(frc, string);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        writeFont(at, glyphs);
    }

    private void writeFont(final AffineTransform at, final GlyphVector glyphs)
    {
        try
        {
            // Convert from PDF, where glyphs are upright when direction is from
            // bottom to top, to AWT, where this is the other way around
            at.scale(1, -1);
            AffineTransform atInverse = at.createInverse();
            graphics.transform(at);
            graphics.drawGlyphVector(glyphs, 0, 0);
            graphics.transform(atInverse);
        }
        catch (NoninvertibleTransformException exception)
        {
            LOG.error("Can't invert the given affine transformation", exception);
        }
    }

    /**
     * Provides an AWT font for the given PDFont.
     * 
     * @param font the font which needs an AWT font
     * @return the corresponding AWT font
     * @throws IOException if something went wrong
     */

    private Font createAWTFont(PDSimpleFont font) throws IOException
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
                    if (fdDictionary.getFontFile() != null)
                    {
                        try
                        {
                            // create a type1 font with the embedded data
                            awtFont = Font.createFont(Font.TYPE1_FONT, fdDictionary.getFontFile().createInputStream());
                        }
                        catch (FontFormatException e)
                        {
                            LOG.info("Can't read the embedded type1 font " + fd.getFontName());
                        }
                    }
                    if (awtFont == null)
                    {
                        // check if the font is part of our environment
                        awtFont = FontManager.getAwtFont(fd.getFontName());
                        if (awtFont != null)
                        {
                            type1Font.setIsFontSubstituted(true);
                        }
                        else
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
                LOG.info("Using font " + awtFont.getName() + " instead");
                font.setIsFontSubstituted(true);
            }
            if (awtFont != null)
            {
                awtFonts.put(font, awtFont);
            }
        }
        return awtFont;
    }

    /**
     * Provide a Glyh2d for the given font if supported.
     * 
     * @param font the font
     * @return the implementation of the Glyph2D interface for the given font if supported
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
                    glyph2D = new TTFGlyph2D(ttf, font);
                }
            }
            else if (font instanceof PDType1Font)
            {
                PDType1Font type1Font = (PDType1Font) font;
                PDType1CFont type1CFont = type1Font.getType1CFont();
                if (type1CFont != null)
                {
                    // get the cffFont raw data
                    CFFFont cffFont = type1CFont.getCFFFont();
                    if (cffFont != null)
                    {
                        glyph2D = new CFFGlyph2D(cffFont, type1CFont.getFontEncoding());
                    }
                }
            }
            else if (font instanceof PDType0Font)
            {
                PDType0Font type0Font = (PDType0Font) font;
                if (type0Font.getDescendantFont() instanceof PDCIDFontType2Font)
                {
                    // a CIDFontType2Font contains TTF font
                    PDCIDFontType2Font cidType2Font = (PDCIDFontType2Font) type0Font.getDescendantFont();
                    // get the true type font raw data
                    TrueTypeFont ttf = cidType2Font.getTTFFont();
                    if (ttf != null)
                    {
                        glyph2D = new TTFGlyph2D(ttf, font, cidType2Font);
                    }
                }
                else if (type0Font.getDescendantFont() instanceof PDCIDFontType0Font)
                {
                    // a CIDFontType2Font contains TTF font
                    PDCIDFontType0Font cidType2Font = (PDCIDFontType0Font) type0Font.getDescendantFont();
                    PDType1CFont type1CFont = cidType2Font.getType1CFont();
                    if (type1CFont != null)
                    {
                        // get the cffFont raw data
                        CFFFont cffFont = type1CFont.getCFFFont();
                        if (cffFont != null)
                        {
                            glyph2D = new CFFGlyph2D(cffFont, type1CFont.getFontEncoding());
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
     * Get the graphics that we are currently drawing on.
     * 
     * @return The graphics we are drawing on.
     */
    public Graphics2D getGraphics()
    {
        return graphics;
    }

    /**
     * Get the page that is currently being drawn.
     * 
     * @return The page that is being drawn.
     */
    public PDPage getPage()
    {
        return page;
    }

    /**
     * Get the size of the page that is currently being drawn.
     * 
     * @return The size of the page that is being drawn.
     */
    public Dimension getPageSize()
    {
        return pageSize;
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
     * Set the line path to draw.
     * 
     * @param newLinePath Set the line path to draw.
     */
    public void setLinePath(GeneralPath newLinePath)
    {
        if (linePath == null || linePath.getCurrentPoint() == null)
        {
            linePath = newLinePath;
        }
        else
        {
            linePath.append(newLinePath, false);
        }
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
        Paint nonStrokingPaint = getGraphicsState().getNonStrokingColor().getJavaColor();
        if (nonStrokingPaint == null)
        {
            nonStrokingPaint = getGraphicsState().getNonStrokingColor().getPaint(pageSize.height);
        }
        if (nonStrokingPaint == null)
        {
            LOG.info("ColorSpace " + getGraphicsState().getNonStrokingColor().getColorSpace().getName()
                    + " doesn't provide a non-stroking color, using white instead!");
            nonStrokingPaint = Color.WHITE;
        }
        graphics.setPaint(nonStrokingPaint);
        getLinePath().setWindingRule(windingRule);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        graphics.fill(getLinePath());
        getLinePath().reset();
    }

    /**
     * This will set the current stroke.
     * 
     * @param newStroke The current stroke.
     * 
     */
    public void setStroke(BasicStroke newStroke)
    {
        getGraphics().setStroke(newStroke);
    }

    /**
     * This will return the current stroke.
     * 
     * @return The current stroke.
     * 
     */
    public BasicStroke getStroke()
    {
        return (BasicStroke) getGraphics().getStroke();
    }

    /**
     * Stroke the path.
     * 
     * @throws IOException If there is an IO error while stroking the path.
     */
    public void strokePath() throws IOException
    {
        graphics.setComposite(getGraphicsState().getStrokeJavaComposite());
        Paint strokingPaint = getGraphicsState().getStrokingColor().getJavaColor();
        if (strokingPaint == null)
        {
            strokingPaint = getGraphicsState().getStrokingColor().getPaint(pageSize.height);
        }
        if (strokingPaint == null)
        {
            LOG.info("ColorSpace " + getGraphicsState().getStrokingColor().getColorSpace().getName()
                    + " doesn't provide a stroking color, using white instead!");
            strokingPaint = Color.WHITE;
        }
        graphics.setPaint(strokingPaint);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        GeneralPath path = getLinePath();
        graphics.draw(path);
        path.reset();
    }

    /**
     * Called when the color changed.
     * 
     * @param bStroking true for the stroking color, false for the non-stroking color
     * @throws IOException if an I/O error occurs
     */
    @Deprecated
    public void colorChanged(boolean bStroking) throws IOException
    {
        // logger().info("changing " + (bStroking ? "" : "non") + "stroking color");
    }

    // This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
    /**
     * use the current transformation matrix to transform a single point.
     * 
     * @param x x-coordinate of the point to be transform
     * @param y y-coordinate of the point to be transform
     * @return the transformed coordinates as Point2D.Double
     */
    public java.awt.geom.Point2D.Double transformedPoint(double x, double y)
    {
        double[] position = { x, y };
        getGraphicsState().getCurrentTransformationMatrix().createAffineTransform()
                .transform(position, 0, position, 0, 1);
        return new Point2D.Double(position[0], position[1]);
    }

    /**
     * Set the clipping Path.
     * 
     * @param windingRule The winding rule this path will use.
     * 
     * @deprecated use {@link #setClippingWindingRule(int)} instead
     * 
     */
    public void setClippingPath(int windingRule)
    {
        setClippingWindingRule(windingRule);
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
            GeneralPath clippingPath = (GeneralPath) getLinePath().clone();
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
        getLinePath().reset();
    }

    /**
     * Draw the AWT image. Called by Invoke. Moved into PageDrawer so that Invoke doesn't have to reach in here for
     * Graphics as that breaks extensibility.
     * 
     * @param awtImage The image to draw.
     * @param at The transformation to use when drawing.
     * 
     */
    public void drawImage(Image awtImage, AffineTransform at)
    {
        graphics.setComposite(getGraphicsState().getStrokeJavaComposite());
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        int width = awtImage.getWidth(null);
        int height = awtImage.getHeight(null);
        AffineTransform imageTransform = new AffineTransform(at);
        imageTransform.scale(1.0 / width, -1.0 / height);
        imageTransform.translate(0, -height);
        graphics.drawImage(awtImage, imageTransform, null);
    }

    /**
     * Fill with Shading. Called by SHFill operator.
     * 
     * @param ShadingName The name of the Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     * 
     * @deprecated use {@link #shFill(COSName)) instead.
     */
    public void SHFill(COSName ShadingName) throws IOException
    {
        shFill(ShadingName);
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
        PDShadingResources shading = getResources().getShadings().get(shadingName.getName());
        LOG.debug("Shading = " + shading.toString());
        int shadingType = shading.getShadingType();
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Paint paint = null;
        switch (shadingType)
        {
        case 1:
            // TODO
            LOG.debug("Function based shading not yet supported");
            break;
        case 2:
            paint = new AxialShadingPaint((PDShadingType2) shading, ctm, pageSize.height);
            break;
        case 3:
            paint = new RadialShadingPaint((PDShadingType3) shading, ctm, pageSize.height);
            break;
        case 4:
        case 5:
        case 6:
        case 7:
            // TODO
            LOG.debug("Shading type " + shadingType + " not yet supported");
            break;
        default:
            throw new IOException("Invalid ShadingType " + shadingType + " for Shading " + shadingName);
        }
        graphics.setComposite(getGraphicsState().getNonStrokeJavaComposite());
        graphics.setPaint(paint);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.fill(getGraphicsState().getCurrentClippingPath());
    }

    /**
     * Fill with a Function-based gradient / shading. If extending the class, override this and its siblings, not the
     * public SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Function(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with an Axial Shading. If extending the class, override this and its siblings, not the public SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Axial(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");

    }

    /**
     * Fill with a Radial gradient / shading. If extending the class, override this and its siblings, not the public
     * SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Radial(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with a Free-form Gourad-shaded triangle mesh. If extending the class, override this and its siblings, not
     * the public SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_FreeGourad(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with a Lattice-form Gourad-shaded triangle mesh. If extending the class, override this and its siblings, not
     * the public SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_LatticeGourad(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with a Coons patch mesh If extending the class, override this and its siblings, not the public SHFill
     * method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_CoonsPatch(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with a Tensor-product patch mesh. If extending the class, override this and its siblings, not the public
     * SHFill method.
     * 
     * @param Shading The Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_TensorPatch(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
}
