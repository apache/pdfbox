/*
 * Copyright 2016 The Apache Software Foundation.
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

package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Charsets;

/**
 * This is an example of how to create a page that uses patterns to paint areas.
 *
 * @author Tilman Hausherr
 */
public final class CreatePatternsPDF
{
    private CreatePatternsPDF()
    {
    }

    public static void main(String[] args) throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        page.setResources(new PDResources());

        PDPageContentStream pcs = new PDPageContentStream(doc, page);

        // Colored pattern, i.e. the pattern content stream will set its own color(s)
        PDColorSpace patternCS1 = new PDPattern(null, PDDeviceRGB.INSTANCE);

        // Table 75 spec
        PDTilingPattern tilingPattern1 = new PDTilingPattern();
        tilingPattern1.setBBox(new PDRectangle(0, 0, 10, 10));
        tilingPattern1.setPaintType(PDTilingPattern.PAINT_COLORED);
        tilingPattern1.setTilingType(PDTilingPattern.TILING_CONSTANT_SPACING);
        tilingPattern1.setXStep(10);
        tilingPattern1.setYStep(10);

        COSName patternName1 = page.getResources().add(tilingPattern1);
        //TODO 
        // there's no way to create something like a PDPageContentStream, 
        // so we'll do it the hard way
        OutputStream os1 = tilingPattern1.getContentStream().createOutputStream();
        // Set color, draw diagonal line + 2 more diagonals so that corners look good
        os1.write("1 0 0 RG 0 0 m 10 10 l -1 9 m 1 11 l 9 -1 m 11 1 l s".getBytes(Charsets.US_ASCII));
        os1.close();

        PDColor patternColor1 = new PDColor(patternName1, patternCS1);

        pcs.addRect(50, 500, 200, 200);        
        pcs.setNonStrokingColor(patternColor1);
        pcs.fill();

        // Uncolored pattern - the color is passed later
        PDTilingPattern tilingPattern2 = new PDTilingPattern();
        tilingPattern2.setBBox(new PDRectangle(0, 0, 10, 10));
        tilingPattern2.setPaintType(PDTilingPattern.PAINT_UNCOLORED);
        tilingPattern2.setTilingType(PDTilingPattern.TILING_NO_DISTORTION);
        tilingPattern2.setXStep(10);
        tilingPattern2.setYStep(10);

        COSName patternName2 = page.getResources().add(tilingPattern2);
        OutputStream os2 = tilingPattern2.getContentStream().createOutputStream();
        // draw a cross
        os2.write("0 5 m 10 5 l 5 0 m 5 10 l s".getBytes(Charsets.US_ASCII));
        os2.close();

        // Uncolored pattern colorspace needs to know the colorspace
        // for the color values that will be passed when painting the fill
        PDColorSpace patternCS2 = new PDPattern(null, PDDeviceRGB.INSTANCE);
        PDColor patternColor2green = new PDColor(
                new float[]{0,1,0},
                patternName2,
                patternCS2);

        pcs.addRect(300, 500, 100, 100);        
        pcs.setNonStrokingColor(patternColor2green);
        pcs.fill();

        // same pattern again but with different color + different pattern start position
        PDColor patternColor2blue = new PDColor(
                new float[]{0,0,1},
                patternName2,
                patternCS2);
        pcs.addRect(455, 505, 100, 100);        
        pcs.setNonStrokingColor(patternColor2blue);
        pcs.fill();

        pcs.close();
        doc.save("patterns.pdf");
        doc.close();
    }
}
