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

import java.awt.Color;
import java.io.IOException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPatternContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;

/**
 * This is an example of how to create a page that uses patterns to paint areas.
 *
 * @author Tilman Hausherr
 */
public final class CreatePatternsPDF {

    public static void main(String[] args) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            page.setResources(new PDResources());

            PatternDrawer drawer = new PatternDrawer(doc, page);

            // Colored pattern, i.e. the pattern content stream will set its own color(s)

            // Table 75 spec
            PDTilingPattern coloredTilingPattern = drawer.createTilingPattern(
                    new PDRectangle(0, 0, 10, 10),
                    PDTilingPattern.PAINT_COLORED,
                    PDTilingPattern.TILING_CONSTANT_SPACING,
                    10,
                    10);
            COSName coloredTilingPatternName = page.getResources().add(coloredTilingPattern);
            try (PDPatternContentStream cs = new PDPatternContentStream(coloredTilingPattern))
            {
                // Set color, draw diagonal line + 2 more diagonals so that corners look good
                cs.setStrokingColor(Color.red);
                cs.moveTo(0, 0);
                cs.lineTo(10, 10);
                cs.moveTo(-1, 9);
                cs.lineTo(1, 11);
                cs.moveTo(9, -1);
                cs.lineTo(11, 1);
                cs.stroke();
            }
            PDColorSpace coloredPatternCS = new PDPattern(null, PDDeviceRGB.INSTANCE);
            PDColor patternColor = new PDColor(coloredTilingPatternName, coloredPatternCS);
            drawer.drawPattern(50, 500, 200, 200, patternColor);

            // Uncolored pattern - the color is passed later

            PDTilingPattern uncoloredTilingPattern = drawer.createTilingPattern(
                    new PDRectangle(0, 0, 10, 10),
                    PDTilingPattern.PAINT_UNCOLORED,
                    PDTilingPattern.TILING_NO_DISTORTION,
                    10,
                    10);
            COSName uncoloredTilingPatternName = page.getResources().add(uncoloredTilingPattern);
            try (PDPatternContentStream cs = new PDPatternContentStream(uncoloredTilingPattern))
            {
                // draw a cross
                cs.moveTo(0, 5);
                cs.lineTo(10, 5);
                cs.moveTo(5, 0);
                cs.lineTo(5, 10);
                cs.stroke();
            }

            // Uncolored pattern colorspace needs to know the colorspace
            // for the color values that will be passed when painting the fill
            PDColorSpace uncoloredPatternCS = new PDPattern(null, PDDeviceRGB.INSTANCE);
            PDColor patternColor2green = new PDColor(new float[]{0, 1, 0}, uncoloredTilingPatternName, uncoloredPatternCS);
            drawer.drawPattern(300, 500, 100, 100, patternColor2green);

            // same pattern again but with different color + different pattern start position
            PDColor patternColor2blue = new PDColor(new float[]{0, 0, 1}, uncoloredTilingPatternName, uncoloredPatternCS);
            drawer.drawPattern(455, 505, 100, 100, patternColor2blue);

            doc.save("patterns.pdf");
        }
    }
}

final class PatternDrawer{
    private final PDDocument document;
    private final PDPage page;

    public PatternDrawer(PDDocument document, PDPage page) {
        this.document = document;
        this.page = page;
    }

    public void drawPattern(float x, float y, float width, float height, PDColor patternColor) throws IOException {
        try (PDPageContentStream pcs = new PDPageContentStream(document, page)) {
            pcs.addRect(x, y, width, height);
            pcs.setNonStrokingColor(patternColor);
            pcs.fill();
        }
    }

    public PDTilingPattern createTilingPattern(PDRectangle pdRectangle, int paintType, int tilingType, float xStep, float yStep){
        PDTilingPattern tilingPattern = new PDTilingPattern();
        tilingPattern.setBBox(pdRectangle);
        tilingPattern.setPaintType(paintType);
        tilingPattern.setTilingType(tilingType);
        tilingPattern.setXStep(xStep);
        tilingPattern.setYStep(yStep);
        return tilingPattern;
    }
}