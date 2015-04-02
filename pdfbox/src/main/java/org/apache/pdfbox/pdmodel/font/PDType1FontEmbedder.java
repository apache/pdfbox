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

import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.CharMetric;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.fontbox.pfb.PfbParser;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.Type1Encoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Embedded PDType1Font builder. Helper class to populate a PDType1Font from a PFB and AFM.
 *
 * @author Michael Niedermair
 */
class PDType1FontEmbedder
{
    private final Encoding fontEncoding;
    private final FontMetrics metrics;
    private final Type1Font type1;

    /**
     * This will load a afm and pfb to be embedding into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param dict The Font dictionary to write to.
     * @param afmStream The afm input.
     * @param pfbStream The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    PDType1FontEmbedder(PDDocument doc, COSDictionary dict, InputStream afmStream,
                               InputStream pfbStream) throws IOException
    {
        dict.setItem(COSName.SUBTYPE, COSName.TYPE1);

        // read the afm
        AFMParser afmParser = new AFMParser(afmStream);
        metrics = afmParser.parse();
        this.fontEncoding = encodingFromAFM(metrics);

        // build font descriptor
        PDFontDescriptor fd = buildFontDescriptor(metrics);

        // read the pfb
        byte[] pfbBytes = IOUtils.toByteArray(pfbStream);
        PfbParser pfbParser = new PfbParser(new ByteArrayInputStream(pfbBytes));
        type1 = Type1Font.createWithPFB(new ByteArrayInputStream(pfbBytes));

        PDStream fontStream = new PDStream(doc, pfbParser.getInputStream(), false);
        fontStream.getStream().setInt("Length", pfbParser.size());
        for (int i = 0; i < pfbParser.getLengths().length; i++)
        {
            fontStream.getStream().setInt("Length" + (i + 1), pfbParser.getLengths()[i]);
        }
        fontStream.addCompression();
        fd.setFontFile(fontStream);

        // set the values
        dict.setItem(COSName.FONT_DESC, fd);
        dict.setName(COSName.BASE_FONT, metrics.getFontName());

        // get firstchar, lastchar
        int firstchar = 255;
        int lastchar = 0;

        // widths
        List<CharMetric> listmetric = metrics.getCharMetrics();
        int maxWidths = 256;
        List<Integer> widths = new ArrayList<Integer>(maxWidths);
        int zero = 250;

        Iterator<CharMetric> iter = listmetric.iterator();
        for (int i = 0; i < maxWidths; i++)
        {
            widths.add(zero);
        }

        while (iter.hasNext())
        {
            CharMetric m = iter.next();
            int n = m.getCharacterCode();
            if (n > 0)
            {
                firstchar = Math.min(firstchar, n);
                lastchar = Math.max(lastchar, n);
                if (m.getWx() > 0)
                {
                    int width = Math.round(m.getWx());
                    widths.set(n, width);
                }
            }
        }
        dict.setInt(COSName.FIRST_CHAR, 0);
        dict.setInt(COSName.LAST_CHAR, 255);
        dict.setItem(COSName.WIDTHS, COSArrayList.converterToCOSArray(widths));
    }

    /**
     * Returns a PDFontDescriptor for the given AFM.
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

    // This will generate a Encoding from the AFM-Encoding, because the AFM-Enconding isn't exported
    // to the pdf and consequently the StandardEncoding is used so that any special character is
    // missing I've copied the code from the pdfbox-forum posted by V0JT4 and made some additions
    // concerning german umlauts see also https://sourceforge.net/forum/message.php?msg_id=4705274
    private DictionaryEncoding encodingFromAFM(FontMetrics metrics) throws IOException
    {
        Type1Encoding encoding = new Type1Encoding(metrics);

        COSArray differences = new COSArray();
        differences.add(COSInteger.ZERO);
        for (int i = 0; i < 256; i++)
        {
            differences.add(COSName.getPDFName(encoding.getName(i)));
        }
        // my AFMPFB-Fonts has no character-codes for german umlauts
        // so that I've to add them here by hand
        differences.set(0337 + 1, COSName.getPDFName("germandbls"));
        differences.set(0344 + 1, COSName.getPDFName("adieresis"));
        differences.set(0366 + 1, COSName.getPDFName("odieresis"));
        differences.set(0374 + 1, COSName.getPDFName("udieresis"));
        differences.set(0304 + 1, COSName.getPDFName("Adieresis"));
        differences.set(0326 + 1, COSName.getPDFName("Odieresis"));
        differences.set(0334 + 1, COSName.getPDFName("Udieresis"));

        return new DictionaryEncoding(COSName.STANDARD_ENCODING, differences);
    }

    /**
     * Returns the font's encoding.
     */
    public Encoding getFontEncoding()
    {
        return fontEncoding;
    }

    /**
     * Returns the font's metrics.
     */
    public FontMetrics getFontMetrics()
    {
        return metrics;
    }

    /**
     * Returns the Type 1 font.
     */
    public Type1Font getType1Font()
    {
        return type1;
    }
}
