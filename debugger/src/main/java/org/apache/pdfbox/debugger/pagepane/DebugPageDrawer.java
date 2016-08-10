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

package org.apache.pdfbox.debugger.pagepane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * A custom PageDrawer with debugging overlays.
 * 
 * @author John Hewson
 */
final class DebugPageDrawer extends PageDrawer
{
    private final boolean showGlyphBounds;

    DebugPageDrawer(PageDrawerParameters parameters, boolean showGlyphBounds) throws IOException
    {
        super(parameters);
        this.showGlyphBounds = showGlyphBounds;
    }

    /**
     * Glyph bounding boxes.
     */
    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                             Vector displacement) throws IOException
    {
        // draw glyph
        super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);

        if (showGlyphBounds)
        {
            Shape bbox;

            // compute visual bounds
            if (font instanceof PDType3Font)
            {
                // todo: implement me
                return;
            }
            else
            {
                AffineTransform at = textRenderingMatrix.createAffineTransform();
                at.concatenate(font.getFontMatrix().createAffineTransform());

                // get the path
                PDVectorFont vectorFont = (PDVectorFont) font;
                GeneralPath path = vectorFont.getNormalizedPath(code);

                if (path == null)
                {
                    return;
                }

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

                Shape glyph = at.createTransformedShape(path);
                bbox = glyph.getBounds2D();
            }
            
            // save
            Graphics2D graphics = getGraphics();
            Color color = graphics.getColor();
            Stroke stroke = graphics.getStroke();
            Shape clip = graphics.getClip();

            // draw
            graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            graphics.setColor(Color.cyan);
            graphics.setStroke(new BasicStroke(.5f));
            graphics.draw(bbox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);
        }
    }
}
