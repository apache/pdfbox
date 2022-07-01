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

package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;

import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.advanced.GlyphVectorAdvanced;
import org.apache.fontbox.ttf.advanced.api.AdvancedOTFParser;
import org.apache.fontbox.ttf.advanced.api.AdvancedOpenTypeFont;
import org.apache.fontbox.ttf.advanced.api.GlyphVector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;

/**
 * An example of using an embedded OpenType font with advanced glyph layout.
 *
 * @author Daniel Fickling
 */
public final class AdvancedTextLayout
{
    private AdvancedTextLayout()
    {
    }

    private static float getAdvance(float startX, GlyphVector vector, float fontSize) {
        return (((vector.getWidth() / 1000f) * fontSize) + startX);
    }

    private static Matrix createMatrix(float translateX, float translateY) {
        return Matrix.getTranslateInstance(translateX, translateY);
    }

    public static void main(String[] args) throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            //String dir = "C:\\Users\\daniel\\Desktop\\fonts\\";
            //String fontFile = "./pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";
            //String fontFile = dir + "IndieFlower-Regular.ttf";
            //String fontFile = dir + "SourceSansPro-Regular.ttf";
            String dir = "/home/volker/work/";
            String fontFile = dir + "NotoSans-Regular.ttf";


            AdvancedOTFParser fontParser = new AdvancedOTFParser();
            AdvancedOpenTypeFont otFont = fontParser.parse(fontFile);
            PDFont font = PDType0Font.load(document, otFont, true);

            GlyphVector vector = null;
            float x = 10;

            try (PDPageContentStream stream = new PDPageContentStream(document, page))
            {
                float fontSize = 20;
                stream.beginText();
                stream.setFont(font, fontSize);

                vector = otFont.createGlyphVector("PDFBox's Unicode with Embedded OpenType Font", (int)fontSize);
                stream.setTextMatrix(createMatrix(x, 200));
                stream.showGlyphVector(vector);
                x = getAdvance(10, vector, fontSize);

                vector = otFont.createGlyphVector("|AFTER (SIMPLE)", (int)fontSize);
                stream.setTextMatrix(createMatrix(x, 200));
                stream.showGlyphVector(vector);
                x = 10;

                String complex = "A̋L̦    a̋   N̂N̦B ўў 1/2";

                vector = otFont.createGlyphVector(complex, (int)fontSize);
                stream.setTextMatrix(createMatrix(x, 100));
                stream.showGlyphVector(vector);
                x = getAdvance(10, vector, fontSize);

                vector = otFont.createGlyphVector("|AFTER (COMPLEX)", (int)fontSize);
                stream.setTextMatrix(createMatrix(x, 100));
                stream.showGlyphVector(vector);

                stream.setTextMatrix(Matrix.getTranslateInstance(10, 160));
                stream.showText(complex);
                stream.setTextMatrix(Matrix.getTranslateInstance(10 + (font.getStringWidth(complex) / 1000f * fontSize), 160));
                stream.showText("|AFTER (BAD)");

                stream.endText();
            }

            document.save("advanced-text.pdf");
        }
    }
}
