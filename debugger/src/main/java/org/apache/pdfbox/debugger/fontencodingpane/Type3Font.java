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

package org.apache.pdfbox.debugger.fontencodingpane;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Matrix;

/**
 * @author Khyrul Bashar
 * @author Tilman Hausherr
 * 
 * A class that shows the glyph table along with unicode characters for PDType3Font.
 */
class Type3Font extends FontPane
{
    public static final String NO_GLYPH = "No glyph";
    private final FontEncodingView view;
    private int totalAvailableGlyph = 0;
    private PDRectangle fontBBox;
    private final PDResources resources;

    /**
     * Constructor.
     * @param font PDSimpleFont instance.
     * @throws IOException If fails to parse unicode characters.
     */
    Type3Font(PDType3Font font, PDResources resources) throws IOException
    {
        this.resources = resources;

        calcBBox(font);
        
        Object[][] tableData = getGlyphs(font);

        Map<String, String> attributes = new LinkedHashMap<String, String>();
        attributes.put("Font", font.getName());
        attributes.put("Encoding", getEncodingName(font));
        attributes.put("Glyphs", Integer.toString(totalAvailableGlyph));

        view = new FontEncodingView(tableData, attributes, 
                new String[] {"Code", "Glyph Name", "Unicode Character", "Glyph"}, null);
    }
    
    private void calcBBox(PDType3Font font) throws IOException
    {
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        for (int index = 0; index <= 255; ++index)
        {
            PDType3CharProc charProc = font.getCharProc(index);
            if (charProc == null)
            {
                continue;
            }
            PDRectangle glyphBBox = charProc.getGlyphBBox();
            if (glyphBBox == null)
            {
                continue;
            }
            minX = Math.min(minX, glyphBBox.getLowerLeftX());
            maxX = Math.max(maxX, glyphBBox.getUpperRightX());
            minY = Math.min(minY, glyphBBox.getLowerLeftY());
            maxY = Math.max(maxY, glyphBBox.getUpperRightY());
        }
        fontBBox = new PDRectangle((float) minX, (float) minY, (float) (maxX - minX), (float) (maxY - minY));
        if (fontBBox.getWidth() <= 0 || fontBBox.getHeight() <= 0)
        {
            // less reliable, but good as a fallback solution for PDF.js issue 10717
            BoundingBox boundingBox = font.getBoundingBox();
            fontBBox = new PDRectangle(boundingBox.getLowerLeftX(), 
                                       boundingBox.getLowerLeftY(),
                                       boundingBox.getWidth(),
                                       boundingBox.getHeight());
        }
    }

    private Object[][] getGlyphs(PDType3Font font) throws IOException
    {
        boolean isEmpty = fontBBox.toGeneralPath().getBounds2D().isEmpty();
        Object[][] glyphs = new Object[256][4];

        // map needed to lessen memory footprint for files with duplicates
        // e.g. PDF.js issue 10717
        Map<String, BufferedImage> map = new HashMap<String, BufferedImage>();

        for (int index = 0; index <= 255; index++)
        {
            glyphs[index][0] = index;
            if (font.getEncoding().contains(index) || font.toUnicode(index) != null)
            {
                String name = font.getEncoding().getName(index);
                glyphs[index][1] = name;
                glyphs[index][2] = font.toUnicode(index);
                if (isEmpty)
                {
                    glyphs[index][3] = NO_GLYPH;
                }
                else if (map.containsKey(name))
                {
                    glyphs[index][3] = map.get(name);
                }
                else
                {
                    BufferedImage image = renderType3Glyph(font, index);
                    map.put(name, image);
                    glyphs[index][3] = image;
                }
                totalAvailableGlyph++;
            }
            else
            {
                glyphs[index][1] = NO_GLYPH;
                glyphs[index][2] = NO_GLYPH;
                glyphs[index][3] = NO_GLYPH;
            }
        }
        return glyphs;
    }

    // Kindof an overkill to create a PDF for one glyph, but there is no better way at this time.
    // Isn't called if no bounds are available
    private BufferedImage renderType3Glyph(PDType3Font font, int index) throws IOException
    {
        PDDocument doc = new PDDocument();
        int scale = 1;
        if (fontBBox.getWidth() < 72 || fontBBox.getHeight() < 72)
        {
            // e.g. T4 font of PDFBOX-2959
            scale = (int) (72 / Math.min(fontBBox.getWidth(), fontBBox.getHeight()));
        }
        PDPage page = new PDPage(new PDRectangle(fontBBox.getWidth() * scale, fontBBox.getHeight() * scale));
        page.setResources(resources);

        PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);
        try
        {
            // any changes here must be done carefully and each file must be tested again
            // just inverting didn't work with
            // https://www.treasury.gov/ofac/downloads/sdnlist.pdf (has rotated matrix)
            // also PDFBOX-4228-type3.pdf (identity matrix)
            // Root/Pages/Kids/[0]/Resources/XObject/X1/Resources/XObject/X3/Resources/Font/F10
            // PDFBOX-1794-vattenfall.pdf (scale 0.001)
            float scalingFactorX = font.getFontMatrix().getScalingFactorX();
            float scalingFactorY = font.getFontMatrix().getScalingFactorY();
            float translateX = scalingFactorX > 0 ? -fontBBox.getLowerLeftX() : fontBBox.getUpperRightX();
            float translateY = scalingFactorY > 0 ? -fontBBox.getLowerLeftY() : fontBBox.getUpperRightY();
            cs.transform(Matrix.getTranslateInstance(translateX * scale, translateY * scale));
            cs.beginText();
            cs.setFont(font, scale / Math.min(Math.abs(scalingFactorX), Math.abs(scalingFactorY)));
            // can't use showText() because there's no guarantee we have the unicode
            cs.appendRawCommands(String.format("<%02X> Tj\n", index).getBytes(Charsets.ISO_8859_1));
            cs.endText();
            cs.close();
            doc.addPage(page);
            return new PDFRenderer(doc).renderImage(0);
        }
        finally
        {
            doc.close();
        }
    }

    private String getEncodingName(PDSimpleFont font)
    {
        return font.getEncoding().getClass().getSimpleName();
    }

    @Override
    public JPanel getPanel()
    {
        return view.getPanel();
    }
}
