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

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

/**
 * Add a diagonal watermark text to each page of a PDF.
 *
 * @author Tilman Hausherr
 */
public class AddWatermarkText
{
    private AddWatermarkText()
    {
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            usage();
        }
        else
        {
            File srcFile = new File(args[0]);
            File dstFile = new File(args[1]);
            String text = args[2];

            try (PDDocument doc = Loader.loadPDF(srcFile))
            {
                for (PDPage page : doc.getPages())
                {
                    PDFont font = new PDType1Font(FontName.HELVETICA);
                    addWatermarkText(doc, page, font, text);
                }
                doc.save(dstFile);
            }
        }
    }

    private static void addWatermarkText(PDDocument doc, PDPage page, PDFont font, String text)
            throws IOException
    {
        try (PDPageContentStream cs
                = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true))
        {
            float fontHeight = 100; // arbitrary for short text
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            
            int rotation = page.getRotation();
            switch (rotation)
            {
                case 90:
                    width = page.getMediaBox().getHeight();
                    height = page.getMediaBox().getWidth();
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(90), height, 0));
                    break;
                case 180:
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(180), width, height));
                    break;
                case 270:
                    width = page.getMediaBox().getHeight();
                    height = page.getMediaBox().getWidth();
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(270), 0, width));
                    break;
                default:
                    break;
            }

            float stringWidth = font.getStringWidth(text) / 1000 * fontHeight;
            float diagonalLength = (float) Math.sqrt(width * width + height * height);
            float angle = (float) Math.atan2(height, width);
            float x = (diagonalLength - stringWidth) / 2; // "horizontal" position in rotated world
            float y = -fontHeight / 4; // 4 is a trial-and-error thing, this lowers the text a bit
            cs.transform(Matrix.getRotateInstance(angle, 0, 0));
            cs.setFont(font, fontHeight);
            // cs.setRenderingMode(RenderingMode.STROKE) // for "hollow" effect

            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.2f);
            gs.setStrokingAlphaConstant(0.2f);
            gs.setBlendMode(BlendMode.MULTIPLY);
            gs.setLineWidth(3f);
            cs.setGraphicsStateParameters(gs);

            cs.setNonStrokingColor(Color.red);
            cs.setStrokingColor(Color.red);

            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(text);
            cs.endText();
        }
    }

    /**
     * This will print the usage.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + AddWatermarkText.class.getName() + " <input-pdf> <output-pdf> <short text>");
    }
}
