/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.debugger.pagepane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * Draws an overlay showing the locations of text found by PDFTextStripper and another heuristic.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 * @author John Hewson
 */
final class DebugTextOverlay
{
    private final PDDocument document;
    private final int pageIndex;
    private final float scale;
    private final boolean showTextStripper;
    private final boolean showTextStripperBeads;
    private final boolean showFontBBox;
    private final boolean showGlyphBounds;    

    private class DebugTextStripper extends PDFTextStripper
    {
        private final Graphics2D graphics;
        private AffineTransform flip;
        
        DebugTextStripper(final Graphics2D graphics) throws IOException
        {
            this.graphics = graphics;
        }

        public void stripPage(final PDDocument document, final PDPage page, final int pageIndex, final float scale) throws IOException
        {
            // flip y-axis
            final PDRectangle cropBox = page.getCropBox();
            this.flip = new AffineTransform();
            flip.translate(0, cropBox.getHeight());
            flip.scale(1, -1);
            
            // scale and rotate
            transform(graphics, page, scale);

            // set stroke width
            graphics.setStroke(new BasicStroke(0.5f));
            
            setStartPage(pageIndex + 1);
            setEndPage(pageIndex + 1);

            final Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            writeText(document, dummy);

            if (DebugTextOverlay.this.showTextStripperBeads)
            {
                // beads in green
                final List<PDThreadBead> pageArticles = page.getThreadBeads();
                for (final PDThreadBead bead : pageArticles)
                {
                    if (bead == null || bead.getRectangle() == null)
                    {
                        continue;
                    }
                    final PDRectangle r = bead.getRectangle();
                    final GeneralPath p = r.transform(Matrix.getTranslateInstance(-cropBox.getLowerLeftX(), cropBox.getLowerLeftY()));
                    final Shape s = flip.createTransformedShape(p);
                    graphics.setColor(Color.green);
                    graphics.draw(s);
                }
            }
        }

        // scale rotate translate
        private void transform(final Graphics2D graphics, final PDPage page, final float scale)
        {
            graphics.scale(scale, scale);
            
            final int rotationAngle = page.getRotation();
            final PDRectangle cropBox = page.getCropBox();

            if (rotationAngle != 0)
            {
                float translateX = 0;
                float translateY = 0;
                switch (rotationAngle)
                {
                    case 90:
                        translateX = cropBox.getHeight();
                        break;
                    case 270:
                        translateY = cropBox.getWidth();
                        break;
                    case 180:
                        translateX = cropBox.getWidth();
                        translateY = cropBox.getHeight();
                        break;
                    default:
                        break;
                }
                graphics.translate(translateX, translateY);
                graphics.rotate((float) Math.toRadians(rotationAngle));
            }
        }
        
        @Override
        protected void writeString(final String string, final List<TextPosition> textPositions) throws IOException
        {
            for (final TextPosition text : textPositions)
            {
                if (DebugTextOverlay.this.showTextStripper)
                {
                    final AffineTransform at = (AffineTransform) flip.clone();
                    at.concatenate(text.getTextMatrix().createAffineTransform());

                    // in red:
                    // show rectangles with the "height" (not a real height, but used for text extraction 
                    // heuristics, it is 1/2 of the bounding box height and starts at y=0)
                    final Rectangle2D.Float rect = new Rectangle2D.Float(0, 0,
                            text.getWidthDirAdj() / text.getTextMatrix().getScalingFactorX(),
                            text.getHeightDir() / text.getTextMatrix().getScalingFactorY());
                    graphics.setColor(Color.red);
                    graphics.draw(at.createTransformedShape(rect));
                }

                if (DebugTextOverlay.this.showFontBBox)
                {
                    // in blue:
                    // show rectangle with the real vertical bounds, based on the font bounding box y values
                    // usually, the height is identical to what you see when marking text in Adobe Reader
                    final PDFont font = text.getFont();
                    final BoundingBox bbox = font.getBoundingBox();

                    // advance width, bbox height (glyph space)
                    final float xadvance = font.getWidth(text.getCharacterCodes()[0]); // todo: should iterate all chars
                    final Rectangle2D rect = new Rectangle2D.Float(0, bbox.getLowerLeftY(), xadvance, bbox.getHeight());

                    // glyph space -> user space
                    // note: text.getTextMatrix() is *not* the Text Matrix, it's the Text Rendering Matrix
                    final AffineTransform at = (AffineTransform) flip.clone();
                    at.concatenate(text.getTextMatrix().createAffineTransform());

                    if (font instanceof PDType3Font)
                    {
                        // bbox and font matrix are unscaled
                        at.concatenate(font.getFontMatrix().createAffineTransform());
                    }
                    else
                    {
                        // bbox and font matrix are already scaled to 1000
                        at.scale(1 / 1000f, 1 / 1000f);
                    }

                    graphics.setColor(Color.blue);
                    graphics.draw(at.createTransformedShape(rect));
                }
            }
        }

        @Override
        protected void showGlyph(final Matrix textRenderingMatrix, final PDFont font, final int code, final Vector displacement) throws IOException
        {
            super.showGlyph(textRenderingMatrix, font, code, displacement);

            if (!DebugTextOverlay.this.showGlyphBounds)
            {
                return;
            }

            final AffineTransform at = textRenderingMatrix.createAffineTransform();
            final Shape bbox = calculateGlyphBounds(at, font, code, displacement);
            if (bbox == null)
            {
                return;
            }

            final Shape transformedBBox = flip.createTransformedShape(bbox);

            // save
            final Color color = graphics.getColor();
            final Stroke stroke = graphics.getStroke();
            final Shape clip = graphics.getClip();

            // draw
            graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            graphics.setColor(Color.cyan);
            graphics.setStroke(new BasicStroke(.5f));                
            graphics.draw(transformedBBox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);
        }

        private Shape calculateGlyphBounds(
                final AffineTransform at, final PDFont font, final int code, final Vector displacement) throws IOException
        {
            at.concatenate(font.getFontMatrix().createAffineTransform());
            // compute glyph path
            final GeneralPath path;
            if (font instanceof PDType3Font)
            {
                // It is difficult to calculate the real individual glyph bounds for type 3 fonts
                // because these are not vector fonts, the content stream could contain almost anything
                // that is found in page content streams.
                final PDType3Font t3Font = (PDType3Font) font;
                final PDType3CharProc charProc = t3Font.getCharProc(code);
                if (charProc == null)
                {
                    return null;
                }

                final BoundingBox fontBBox = t3Font.getBoundingBox();
                final PDRectangle glyphBBox = charProc.getGlyphBBox();
                if (glyphBBox == null)
                {
                    return null;
                }

                // PDFBOX-3850: glyph bbox could be larger than the font bbox
                glyphBBox.setLowerLeftX(Math.max(fontBBox.getLowerLeftX(), glyphBBox.getLowerLeftX()));
                glyphBBox.setLowerLeftY(Math.max(fontBBox.getLowerLeftY(), glyphBBox.getLowerLeftY()));
                glyphBBox.setUpperRightX(Math.min(fontBBox.getUpperRightX(), glyphBBox.getUpperRightX()));
                glyphBBox.setUpperRightY(Math.min(fontBBox.getUpperRightY(), glyphBBox.getUpperRightY()));
                path = glyphBBox.toGeneralPath();
            }
            else
            {
                final PDVectorFont vectorFont = (PDVectorFont) font;
                path = vectorFont.getNormalizedPath(code);

                if (path == null)
                {
                    return null;
                }

                // stretch non-embedded glyph if it does not match the width contained in the PDF
                if (!font.isEmbedded() && !font.isVertical() && !font.isStandard14() && font.hasExplicitWidth(code))
                {
                    final float fontWidth = font.getWidthFromFont(code);
                    if (fontWidth > 0 && // ignore spaces
                            Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001)
                    {
                        final float pdfWidth = displacement.getX() * 1000;
                        at.scale(pdfWidth / fontWidth, 1);
                    }
                }
            }
            // compute visual bounds
            return at.createTransformedShape(path.getBounds2D());
        }
    }

    DebugTextOverlay(final PDDocument document, final int pageIndex, final float scale,
                     final boolean showTextStripper, final boolean showTextStripperBeads,
                     final boolean showFontBBox, final boolean showGlyphBounds)
    {
        this.document = document;
        this.pageIndex = pageIndex;
        this.scale = scale;
        this.showTextStripper = showTextStripper;
        this.showTextStripperBeads = showTextStripperBeads;
        this.showFontBBox = showFontBBox;
        this.showGlyphBounds = showGlyphBounds;
    }
    
    public void renderTo(final Graphics2D graphics) throws IOException
    {
        final DebugTextStripper stripper = new DebugTextStripper(graphics);
        stripper.stripPage(this.document, this.document.getPage(pageIndex), this.pageIndex, this.scale);
    }
}
