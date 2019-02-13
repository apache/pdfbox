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

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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

            PDDocument doc = PDDocument.load(srcFile);
            for (PDPage page : doc.getPages())
            {
                PDFont font = PDType1Font.HELVETICA;
                addWatermarkText(doc, page, font, text);
            }
            doc.save(dstFile);
            doc.close();
        }
    }

    private static void addWatermarkText(PDDocument doc, PDPage page, PDFont font, String text)
            throws IOException
    {
        PDPageContentStream cs
                = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

        float fontHeight = 100; // arbitrary for short text
        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();
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

        // some API weirdness here. When int, range is 0..255.
        // when float, this would be 0..1f
        cs.setNonStrokingColor(255, 0, 0);
        cs.setStrokingColor(255, 0, 0);

        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        cs.close();
    }

    /**
     * This will print the usage.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + AddWatermarkText.class.getName() + " <input-pdf> <output-pdf> <short text>");
    }
}
