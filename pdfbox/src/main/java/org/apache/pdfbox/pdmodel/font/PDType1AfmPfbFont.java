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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.CharMetric;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.pfb.PfbParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.AFMEncoding;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This is implementation of the Type1 Font with a afm and a pfb file.
 * 
 * @author <a href="mailto:m.g.n@gmx.de">Michael Niedermair</a>
 * 
 */
public class PDType1AfmPfbFont extends PDType1Font
{
    /**
     * the buffersize.
     */
    private static final int BUFFERSIZE = 0xffff;

    /**
     * The font metric.
     */
    private FontMetric metric;

    /**
     * Create a new object.
     * 
     * @param doc The PDF document that will hold the embedded font.
     * @param afmname The font filename.
     * @throws IOException If there is an error loading the data.
     */
    public PDType1AfmPfbFont(final PDDocument doc, final String afmname) throws IOException
    {

        super();
        InputStream afmin = new BufferedInputStream(new FileInputStream(afmname), BUFFERSIZE);
        String pfbname = afmname.replaceAll(".AFM", "").replaceAll(".afm", "") + ".pfb";
        InputStream pfbin = new BufferedInputStream(new FileInputStream(pfbname), BUFFERSIZE);
        load(doc, afmin, pfbin);
    }

    /**
     * Create a new object.
     * 
     * @param doc The PDF document that will hold the embedded font.
     * @param afm The afm input.
     * @param pfb The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    public PDType1AfmPfbFont(final PDDocument doc, final InputStream afm, final InputStream pfb) throws IOException
    {
        super();
        load(doc, afm, pfb);
    }

    /**
     * This will load a afm and pfb to be embedding into a document.
     * 
     * @param doc The PDF document that will hold the embedded font.
     * @param afm The afm input.
     * @param pfb The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    private void load(final PDDocument doc, final InputStream afm, final InputStream pfb) throws IOException
    {

        PDFontDescriptorDictionary fd = new PDFontDescriptorDictionary();
        setFontDescriptor(fd);

        // read the pfb
        PfbParser pfbparser = new PfbParser(pfb);
        pfb.close();

        PDStream fontStream = new PDStream(doc, pfbparser.getInputStream(), false);
        fontStream.getStream().setInt("Length", pfbparser.size());
        for (int i = 0; i < pfbparser.getLengths().length; i++)
        {
            fontStream.getStream().setInt("Length" + (i + 1), pfbparser.getLengths()[i]);
        }
        fontStream.addCompression();
        fd.setFontFile(fontStream);

        // read the afm
        AFMParser parser = new AFMParser(afm);
        metric = parser.parse();
        setFontEncoding(afmToDictionary(new AFMEncoding(metric)));

        // set the values
        setBaseFont(metric.getFontName());
        fd.setFontName(metric.getFontName());
        fd.setFontFamily(metric.getFamilyName());
        fd.setNonSymbolic(true);
        fd.setFontBoundingBox(new PDRectangle(metric.getFontBBox()));
        fd.setItalicAngle(metric.getItalicAngle());
        fd.setAscent(metric.getAscender());
        fd.setDescent(metric.getDescender());
        fd.setCapHeight(metric.getCapHeight());
        fd.setXHeight(metric.getXHeight());
        fd.setAverageWidth(metric.getAverageCharacterWidth());
        fd.setCharacterSet(metric.getCharacterSet());

        // get firstchar, lastchar
        int firstchar = 255;
        int lastchar = 0;

        // widths
        List<CharMetric> listmetric = metric.getCharMetrics();
        Encoding encoding = getFontEncoding();
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
                    // germandbls has 2 character codes !! Don't ask me why .....
                    // StandardEncoding = 0373 = 251
                    // WinANSIEncoding = 0337 = 223
                    if (m.getName().equals("germandbls") && n != 223)
                    {
                        widths.set(0337, width);
                    }
                }
            }
            else
            {
                // my AFMPFB-Fonts has no character-codes for german umlauts
                // so that I've to add them here by hand
                if (m.getName().equals("adieresis"))
                {
                    widths.set(0344, widths.get(encoding.getCode("a")));
                }
                else if (m.getName().equals("odieresis"))
                {
                    widths.set(0366, widths.get(encoding.getCode("o")));
                }
                else if (m.getName().equals("udieresis"))
                {
                    widths.set(0374, widths.get(encoding.getCode("u")));
                }
                else if (m.getName().equals("Adieresis"))
                {
                    widths.set(0304, widths.get(encoding.getCode("A")));
                }
                else if (m.getName().equals("Odieresis"))
                {
                    widths.set(0326, widths.get(encoding.getCode("O")));
                }
                else if (m.getName().equals("Udieresis"))
                {
                    widths.set(0334, widths.get(encoding.getCode("U")));
                }
            }
        }
        setFirstChar(0);
        setLastChar(255);
        setWidths(widths);
    }

    /*
     * This will generate a Encoding from the AFM-Encoding, because the AFM-Enconding isn't exported to the pdf and
     * consequently the StandardEncoding is used so that any special character is missing I've copied the code from the
     * pdfbox-forum posted by V0JT4 and made some additions concerning german umlauts see also
     * https://sourceforge.net/forum/message.php?msg_id=4705274
     */
    private DictionaryEncoding afmToDictionary(AFMEncoding encoding) throws java.io.IOException
    {
        COSArray array = new COSArray();
        array.add(COSInteger.ZERO);
        for (int i = 0; i < 256; i++)
        {
            array.add(COSName.getPDFName(encoding.getName(i)));
        }
        // my AFMPFB-Fonts has no character-codes for german umlauts
        // so that I've to add them here by hand
        array.set(0337 + 1, COSName.getPDFName("germandbls"));
        array.set(0344 + 1, COSName.getPDFName("adieresis"));
        array.set(0366 + 1, COSName.getPDFName("odieresis"));
        array.set(0374 + 1, COSName.getPDFName("udieresis"));
        array.set(0304 + 1, COSName.getPDFName("Adieresis"));
        array.set(0326 + 1, COSName.getPDFName("Odieresis"));
        array.set(0334 + 1, COSName.getPDFName("Udieresis"));

        COSDictionary dictionary = new COSDictionary();
        dictionary.setItem(COSName.NAME, COSName.ENCODING);
        dictionary.setItem(COSName.DIFFERENCES, array);
        dictionary.setItem(COSName.BASE_ENCODING, COSName.STANDARD_ENCODING);
        return new DictionaryEncoding(dictionary);
    }

}
