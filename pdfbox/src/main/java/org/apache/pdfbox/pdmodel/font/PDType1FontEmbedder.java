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
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.fontbox.pfb.PfbParser;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.Type1Encoding;

/**
 * Embedded PDType1Font builder. Helper class to populate a PDType1Font from a PFB and AFM.
 *
 * @author Michael Niedermair
 */
class PDType1FontEmbedder
{
    private final Encoding fontEncoding;
    private final Type1Font type1;
    
    /**
     * This will load a PFB to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param dict The Font dictionary to write to.
     * @param pfbStream The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    PDType1FontEmbedder(PDDocument doc, COSDictionary dict, InputStream pfbStream,
                        Encoding encoding) throws IOException
    {
        dict.setItem(COSName.SUBTYPE, COSName.TYPE1);

        // read the pfb
        byte[] pfbBytes = IOUtils.toByteArray(pfbStream);
        PfbParser pfbParser = new PfbParser(pfbBytes);
        type1 = Type1Font.createWithPFB(pfbBytes);
        
        if (encoding == null)
        {
            fontEncoding = Type1Encoding.fromFontBox(type1.getEncoding());
        }
        else
        {
            fontEncoding = encoding;
        }

        // build font descriptor
        PDFontDescriptor fd = buildFontDescriptor(type1);

        PDStream fontStream = new PDStream(doc, pfbParser.getInputStream(), COSName.FLATE_DECODE);
        fontStream.getCOSObject().setInt("Length", pfbParser.size());
        for (int i = 0; i < pfbParser.getLengths().length; i++)
        {
            fontStream.getCOSObject().setInt("Length" + (i + 1), pfbParser.getLengths()[i]);
        }
        fd.setFontFile(fontStream);

        // set the values
        dict.setItem(COSName.FONT_DESC, fd);
        dict.setName(COSName.BASE_FONT, type1.getName());

        // widths
        List<Integer> widths = new ArrayList<Integer>(256);
        for (int code = 0; code <= 255; code++)
        {
            String name = fontEncoding.getName(code);
            int width = Math.round(type1.getWidth(name));
            widths.add(width);
        }
        
        dict.setInt(COSName.FIRST_CHAR, 0);
        dict.setInt(COSName.LAST_CHAR, 255);
        dict.setItem(COSName.WIDTHS, COSArrayList.converterToCOSArray(widths));
    }

    /**
     * Returns a PDFontDescriptor for the given PFB.
     */
    static PDFontDescriptor buildFontDescriptor(Type1Font type1)
    {
        boolean isSymbolic = type1.getEncoding()
                instanceof org.apache.fontbox.encoding.BuiltInEncoding;

        PDFontDescriptor fd = new PDFontDescriptor();
        fd.setFontName(type1.getName());
        fd.setFontFamily(type1.getFamilyName());
        fd.setNonSymbolic(!isSymbolic);
        fd.setSymbolic(isSymbolic);
        fd.setFontBoundingBox(new PDRectangle(type1.getFontBBox()));
        fd.setItalicAngle(type1.getItalicAngle());
        fd.setAscent(type1.getFontBBox().getUpperRightY());
        fd.setDescent(type1.getFontBBox().getLowerLeftY());
        fd.setCapHeight(type1.getBlueValues().get(2).floatValue());
        fd.setStemV(0); // for PDF/A
        return fd;
    }


    /**
     * Returns a PDFontDescriptor for the given AFM. Used only for Standard 14 fonts.
     *
     * @param metrics AFM
     */
    static PDFontDescriptor buildFontDescriptor(FontMetrics metrics)
    {
        boolean isSymbolic = metrics.getEncodingScheme().equals("FontSpecific");

        PDFontDescriptor fd = new PDFontDescriptor();
        fd.setFontName(metrics.getFontName());
        fd.setFontFamily(metrics.getFamilyName());
        fd.setNonSymbolic(!isSymbolic);
        fd.setSymbolic(isSymbolic);
        fd.setFontBoundingBox(new PDRectangle(metrics.getFontBBox()));
        fd.setItalicAngle(metrics.getItalicAngle());
        fd.setAscent(metrics.getAscender());
        fd.setDescent(metrics.getDescender());
        fd.setCapHeight(metrics.getCapHeight());
        fd.setXHeight(metrics.getXHeight());
        fd.setAverageWidth(metrics.getAverageCharacterWidth());
        fd.setCharacterSet(metrics.getCharacterSet());
        fd.setStemV(0); // for PDF/A
        return fd;
    }
    
    /**
     * Returns the font's encoding.
     */
    public Encoding getFontEncoding()
    {
        return fontEncoding;
    }

    /**
     * Returns the font's glyph list.
     */
    public GlyphList getGlyphList()
    {
        return GlyphList.getAdobeGlyphList();
    }

    /**
     * Returns the Type 1 font.
     */
    public Type1Font getType1Font()
    {
        return type1;
    }
}
