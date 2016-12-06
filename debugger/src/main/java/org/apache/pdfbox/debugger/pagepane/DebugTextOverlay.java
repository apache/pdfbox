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
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

/**
 * Draws an overlay showing the locations of text found by PDFTextStripper and another heuristic.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 * @author John Hewson
 */
final class DebugTextOverlay
{
    private PDDocument document;
    private int pageIndex;
    private float scale;
    private boolean showTextStripper;
    private boolean showTextStripperBeads;
    private boolean showFontBBox;
        
    private class DebugTextStripper extends PDFTextStripper
    {
        private Graphics2D graphics;
        private AffineTransform flip;
        
        public DebugTextStripper(Graphics2D graphics) throws IOException
        {
            this.graphics = graphics;
        }

        public void stripPage(PDDocument document, PDPage page, int pageIndex, float scale) throws IOException
        {
            // flip y-axis
            PDRectangle cropBox = page.getCropBox();
            this.flip = new AffineTransform();
            flip.translate(0, cropBox.getHeight());
            flip.scale(1, -1);
            
            // scale and rotate
            transform(graphics, page, scale);

            // set stroke width
            graphics.setStroke(new BasicStroke(0.5f));
            
            setStartPage(pageIndex + 1);
            setEndPage(pageIndex + 1);

            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            writeText(document, dummy);

            if (DebugTextOverlay.this.showTextStripperBeads)
            {
                // beads in green
                List<PDThreadBead> pageArticles = page.getThreadBeads();
                for (PDThreadBead bead : pageArticles)
                {
                    PDRectangle r = bead.getRectangle();
                    GeneralPath p = r.transform(Matrix.getTranslateInstance(-cropBox.getLowerLeftX(), cropBox.getLowerLeftY()));
                    Shape s = flip.createTransformedShape(p);
                    graphics.setColor(Color.green);
                    graphics.draw(s);
                }
            }
        }

        // scale rotate translate
        private void transform(Graphics2D graphics, PDPage page, float scale)
        {
            graphics.scale(scale, scale);
            
            int rotationAngle = page.getRotation();
            PDRectangle cropBox = page.getCropBox();

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
                }
                graphics.translate(translateX, translateY);
                graphics.rotate((float) Math.toRadians(rotationAngle));
            }
        }
        
        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException
        {
            for (TextPosition text : textPositions)
            {
                if (DebugTextOverlay.this.showTextStripper)
                {
                    AffineTransform at = (AffineTransform) flip.clone();
                    at.concatenate(text.getTextMatrix().createAffineTransform());

                    // in red:
                    // show rectangles with the "height" (not a real height, but used for text extraction 
                    // heuristics, it is 1/2 of the bounding box height and starts at y=0)
                    Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 
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
                    PDFont font = text.getFont();
                    BoundingBox bbox = font.getBoundingBox();

                    // advance width, bbox height (glyph space)
                    float xadvance = font.getWidth(text.getCharacterCodes()[0]); // todo: should iterate all chars
                    Rectangle2D rect = new Rectangle2D.Float(0, bbox.getLowerLeftY(), xadvance, bbox.getHeight());

                    // glyph space -> user space
                    // note: text.getTextMatrix() is *not* the Text Matrix, it's the Text Rendering Matrix
                    AffineTransform at = (AffineTransform) flip.clone();
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
    }

    public DebugTextOverlay(PDDocument document, int pageIndex, float scale,
                            boolean showTextStripper, boolean showTextStripperBeads,
                            boolean showFontBBox)
    {
        this.document = document;
        this.pageIndex = pageIndex;
        this.scale = scale;
        this.showTextStripper = showTextStripper;
        this.showTextStripperBeads = showTextStripperBeads;
        this.showFontBBox = showFontBBox;
    }
    
    public void renderTo(Graphics2D graphics) throws IOException
    {
        DebugTextStripper stripper = new DebugTextStripper(graphics);
        stripper.stripPage(this.document, this.document.getPage(pageIndex), this.pageIndex, this.scale);
    }
}
