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
package org.apache.pdfbox.examples.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * This is an example on how to get some x/y coordinates of text and to show them in a rendered
 * image.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class DrawPrintTextLocations extends PDFTextStripper
{
    private AffineTransform flipAT;
    private AffineTransform rotateAT;
    private AffineTransform transAT;
    private final String filename;
    static final int SCALE = 4;
    private Graphics2D g2d;

    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @param document
     * @param filename
     * @throws IOException If there is an error loading the properties.
     */
    public DrawPrintTextLocations(final PDDocument document, final String filename) throws IOException
    {
        this.document = document; // must initialize here, base class initializes too late
        this.filename = filename;
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(final String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
        }
        else
        {
            try (PDDocument document = Loader.loadPDF(new File(args[0])))
            {
                final DrawPrintTextLocations stripper = new DrawPrintTextLocations(document, args[0]);
                stripper.setSortByPosition(true);

                for (int page = 0; page < document.getNumberOfPages(); ++page)
                {
                    stripper.stripPage(page);
                }
            }
        }
    }

    @Override
    protected void showGlyph(final Matrix textRenderingMatrix, final PDFont font, final int code, final Vector displacement)
            throws IOException
    {
        super.showGlyph(textRenderingMatrix, font, code, displacement);

        // in cyan:
        // show actual glyph bounds. This must be done here and not in writeString(),
        // because writeString processes only the glyphs with unicode, 
        // see e.g. the file in PDFBOX-3274
        Shape cyanShape = calculateGlyphBounds(textRenderingMatrix, font, code);

        if (cyanShape != null)
        {
            cyanShape = flipAT.createTransformedShape(cyanShape);
            cyanShape = rotateAT.createTransformedShape(cyanShape);
            cyanShape = transAT.createTransformedShape(cyanShape);

            g2d.setColor(Color.CYAN);
            g2d.draw(cyanShape);
        }
    }

    // this calculates the real (except for type 3 fonts) individual glyph bounds
    private Shape calculateGlyphBounds(final Matrix textRenderingMatrix, final PDFont font, final int code) throws IOException
    {
        GeneralPath path = null;
        final AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());
        if (font instanceof PDType3Font)
        {
            // It is difficult to calculate the real individual glyph bounds for type 3 fonts
            // because these are not vector fonts, the content stream could contain almost anything
            // that is found in page content streams.
            final PDType3Font t3Font = (PDType3Font) font;
            final PDType3CharProc charProc = t3Font.getCharProc(code);
            if (charProc != null)
            {
                final BoundingBox fontBBox = t3Font.getBoundingBox();
                final PDRectangle glyphBBox = charProc.getGlyphBBox();
                if (glyphBBox != null)
                {
                    // PDFBOX-3850: glyph bbox could be larger than the font bbox
                    glyphBBox.setLowerLeftX(Math.max(fontBBox.getLowerLeftX(), glyphBBox.getLowerLeftX()));
                    glyphBBox.setLowerLeftY(Math.max(fontBBox.getLowerLeftY(), glyphBBox.getLowerLeftY()));
                    glyphBBox.setUpperRightX(Math.min(fontBBox.getUpperRightX(), glyphBBox.getUpperRightX()));
                    glyphBBox.setUpperRightY(Math.min(fontBBox.getUpperRightY(), glyphBBox.getUpperRightY()));
                    path = glyphBBox.toGeneralPath();
                }
            }
        }
        else if (font instanceof PDVectorFont)
        {
            final PDVectorFont vectorFont = (PDVectorFont) font;
            path = vectorFont.getPath(code);

            if (font instanceof PDTrueTypeFont)
            {
                final PDTrueTypeFont ttFont = (PDTrueTypeFont) font;
                final int unitsPerEm = ttFont.getTrueTypeFont().getHeader().getUnitsPerEm();
                at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
            }
            if (font instanceof PDType0Font)
            {
                final PDType0Font t0font = (PDType0Font) font;
                if (t0font.getDescendantFont() instanceof PDCIDFontType2)
                {
                    final int unitsPerEm = ((PDCIDFontType2) t0font.getDescendantFont()).getTrueTypeFont().getHeader().getUnitsPerEm();
                    at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
                }
            }
        }
        else
        {
            // shouldn't happen, please open issue in JIRA
            System.out.println("Unknown font class: " + font.getClass());
        }
        if (path == null)
        {
            return null;
        }
        return at.createTransformedShape(path.getBounds2D());
    }

    private void stripPage(final int page) throws IOException
    {
        final PDFRenderer pdfRenderer = new PDFRenderer(document);
        final BufferedImage image = pdfRenderer.renderImage(page, SCALE);
        final PDPage pdPage = document.getPage(page);
        final PDRectangle cropBox = pdPage.getCropBox();

        // flip y-axis
        flipAT = new AffineTransform();
        flipAT.translate(0, pdPage.getBBox().getHeight());
        flipAT.scale(1, -1);

        // page may be rotated
        rotateAT = new AffineTransform();
        final int rotation = pdPage.getRotation();
        if (rotation != 0)
        {
            final PDRectangle mediaBox = pdPage.getMediaBox();
            switch (rotation)
            {
                case 90:
                    rotateAT.translate(mediaBox.getHeight(), 0);
                    break;
                case 270:
                    rotateAT.translate(0, mediaBox.getWidth());
                    break;
                case 180:
                    rotateAT.translate(mediaBox.getWidth(), mediaBox.getHeight());
                    break;
                default:
                    break;
            }
            rotateAT.rotate(Math.toRadians(rotation));
        }

        // cropbox
        transAT = AffineTransform.getTranslateInstance(-cropBox.getLowerLeftX(), cropBox.getLowerLeftY());

        g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(0.1f));
        g2d.scale(SCALE, SCALE);

        setStartPage(page + 1);
        setEndPage(page + 1);

        final Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);
        
        // beads in green
        g2d.setStroke(new BasicStroke(0.4f));
        final List<PDThreadBead> pageArticles = pdPage.getThreadBeads();
        for (final PDThreadBead bead : pageArticles)
        {
            if (bead == null)
            {
                continue;
            }
            final PDRectangle r = bead.getRectangle();
            Shape s = r.toGeneralPath().createTransformedShape(transAT);
            s = flipAT.createTransformedShape(s);
            s = rotateAT.createTransformedShape(s);
            g2d.setColor(Color.green);
            g2d.draw(s);
        }

        g2d.dispose();

        String imageFilename = filename;
        final int pt = imageFilename.lastIndexOf('.');
        imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";
        ImageIO.write(image, "png", new File(imageFilename));
    }

    /**
     * Override the default functionality of PDFTextStripper.
     */
    @Override
    protected void writeString(final String string, final List<TextPosition> textPositions) throws IOException
    {
        for (final TextPosition text : textPositions)
        {
            System.out.println("String[" + text.getXDirAdj() + ","
                    + text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale="
                    + text.getXScale() + " height=" + text.getHeightDir() + " space="
                    + text.getWidthOfSpace() + " width="
                    + text.getWidthDirAdj() + "]" + text.getUnicode());

            // glyph space -> user space
            // note: text.getTextMatrix() is *not* the Text Matrix, it's the Text Rendering Matrix
            final AffineTransform at = text.getTextMatrix().createAffineTransform();

            // in red:
            // show rectangles with the "height" (not a real height, but used for text extraction 
            // heuristics, it is 1/2 of the bounding box height and starts at y=0)
            Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 
                    text.getWidthDirAdj() / text.getTextMatrix().getScalingFactorX(),
                    text.getHeightDir() / text.getTextMatrix().getScalingFactorY());
            Shape s = at.createTransformedShape(rect);
            s = flipAT.createTransformedShape(s);
            s = rotateAT.createTransformedShape(s);
            g2d.setColor(Color.red);
            g2d.draw(s);

            // in blue:
            // show rectangle with the real vertical bounds, based on the font bounding box y values
            // usually, the height is identical to what you see when marking text in Adobe Reader
            final PDFont font = text.getFont();
            final BoundingBox bbox = font.getBoundingBox();

            // advance width, bbox height (glyph space)
            final float xadvance = font.getWidth(text.getCharacterCodes()[0]); // todo: should iterate all chars
            rect = new Rectangle2D.Float(0, bbox.getLowerLeftY(), xadvance, bbox.getHeight());
            
            if (font instanceof PDType3Font)
            {
                // bbox and font matrix are unscaled
                at.concatenate(font.getFontMatrix().createAffineTransform());
            }
            else
            {
                // bbox and font matrix are already scaled to 1000
                at.scale(1/1000f, 1/1000f);
            }
            s = at.createTransformedShape(rect);
            s = flipAT.createTransformedShape(s);
            s = rotateAT.createTransformedShape(s);

            g2d.setColor(Color.blue);
            g2d.draw(s);
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + DrawPrintTextLocations.class.getName() + " <input-pdf>");
    }
}
