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

package org.pdfbox.pdmodel.font;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fontbox.afm.AFMParser;
import org.fontbox.afm.CharMetric;
import org.fontbox.afm.FontMetric;

import org.fontbox.pfb.PfbParser;

import org.pdfbox.encoding.AFMEncoding;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This is implementation of the Type1 Font
 * with a afm and a pfb file.
 *
 * @author <a href="mailto:m.g.n@gmx.de">Michael Niedermair</a>
 * @version $Revision: 1.5 $
 */
public class PDType1AfmPfbFont extends PDType1Font 
{
    /**
     * the buffersize.
     */
    private static final int BUFFERSIZE = 0xffff;
    
    /**
     * The font descriptor.
     */
    private PDFontDescriptorDictionary fd;

    /**
     * The font metric.
     */
    private FontMetric metric;

    /**
     * Create a new object.
     * @param doc       The PDF document that will hold the embedded font.
     * @param afmname   The font filename.
     * @throws IOException If there is an error loading the data.
     */
    public PDType1AfmPfbFont(final PDDocument doc, final String afmname)
        throws IOException 
    {

        super();

        InputStream afmin = new BufferedInputStream(
                new FileInputStream(afmname), BUFFERSIZE);
        String pfbname = afmname.replaceAll(".AFM", "").replaceAll(".afm", "")
                + ".pfb";
        InputStream pfbin = new BufferedInputStream(
                new FileInputStream(pfbname), BUFFERSIZE);

        load(doc, afmin, pfbin);
    }

    /**
     * Create a new object.
     * @param doc       The PDF document that will hold the embedded font.
     * @param afm       The afm input.
     * @param pfb       The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    public PDType1AfmPfbFont(final PDDocument doc, final InputStream afm, final InputStream pfb) 
        throws IOException 
    {
        super();

        load(doc, afm, pfb);
    }

    /**
     * This will load a afm and pfb to be embedding into a document.
     * 
     * @param doc   The PDF document that will hold the embedded font. 
     * @param afm   The afm input.
     * @param pfb   The pfb input.
     * @throws IOException If there is an error loading the data.
     */
    private void load(final PDDocument doc, final InputStream afm,
            final InputStream pfb) throws IOException 
    {

        fd = new PDFontDescriptorDictionary();
        setFontDescriptor(fd);

        // read the pfb 
        PfbParser pfbparser = new PfbParser(pfb);
        pfb.close();

        PDStream fontStream = new PDStream(doc, pfbparser.getInputStream(),
                false);
        fontStream.getStream().setInt("Length", pfbparser.size());
        for (int i = 0; i < pfbparser.getLengths().length; i++) 
        {
            fontStream.getStream().setInt("Length" + (i + 1),
                    pfbparser.getLengths()[i]);
        }
        fontStream.addCompression();
        fd.setFontFile(fontStream);

        // read the afm
        AFMParser parser = new AFMParser(afm);
        parser.parse();
        metric = parser.getResult();
        setEncoding(new AFMEncoding(metric));

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
        List listmetric = metric.getCharMetrics();

        int maxWidths = 256;
        List widths = new ArrayList(maxWidths);
        Integer zero = new Integer(0);
        Iterator iter = listmetric.iterator();
        while (iter.hasNext()) 
        {
            CharMetric m = (CharMetric) iter.next();
            int n = m.getCharacterCode();
            if (n > 0) 
            {
                firstchar = Math.min(firstchar, n);
                lastchar = Math.max(lastchar, n);
                if (m.getWx() > 0) 
                {
                    float width = m.getWx();
                    widths.add(new Float(width));
                } 
                else 
                {
                    widths.add(zero);
                }
            }
        }
        setFirstChar(firstchar);
        setLastChar(lastchar);
        setWidths(widths);

    }

    /**
     * {@inheritDoc}
     */
    public PDFontDescriptor getFontDescriptor() throws IOException 
    {
        return fd;
    }

}