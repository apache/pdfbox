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

import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Common functionality for embedding TrueType fonts.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
abstract class TrueTypeEmbedder
{
    private static final int ITALIC = 1;
    private static final int OBLIQUE = 256;

    protected final TrueTypeFont ttf;
    protected final PDFontDescriptor fontDescriptor;
    protected final CmapSubtable cmap;

    /**
     * Creates a new TrueType font for embedding.
     */
    TrueTypeEmbedder(PDDocument document, COSDictionary dict, InputStream ttfStream)
                           throws IOException
    {
        PDStream stream = new PDStream(document, ttfStream, false);
        stream.getStream().setInt(COSName.LENGTH1, stream.getByteArray().length);
        stream.addCompression();

        // as the stream was close within the PDStream constructor, we have to recreate it
        InputStream stream2 = null;
        PDFontDescriptor fd;
        try
        {
            stream2 = stream.createInputStream();
            ttf = new TTFParser().parse(stream2);
            fd = createFontDescriptor(ttf);
        }
        finally
        {
            IOUtils.closeQuietly(stream2);
        }

        fd.setFontFile2(stream);
        dict.setName(COSName.BASE_FONT, ttf.getName());

        fontDescriptor = fd;

        // choose a Unicode "cmap"
        cmap = getUnicodeCmap(ttf.getCmap());
    }

    /**
     * Creates a new font descriptor dictionary for the given TTF.
     */
    private PDFontDescriptor createFontDescriptor(TrueTypeFont ttf) throws IOException
    {
        PDFontDescriptor fd = new PDFontDescriptor();
        fd.setFontName(ttf.getName());

        OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
        PostScriptTable post = ttf.getPostScript();

        // Flags
        fd.setFixedPitch(post.getIsFixedPitch() > 0 ||
                         ttf.getHorizontalHeader().getNumberOfHMetrics() == 1);

        int fsSelection = os2.getFsSelection();
        fd.setItalic((fsSelection & ITALIC) == fsSelection ||
                     (fsSelection & OBLIQUE) == fsSelection);

        switch (os2.getFamilyClass())
        {
            case OS2WindowsMetricsTable.FAMILY_CLASS_CLAREDON_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_FREEFORM_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_MODERN_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_OLDSTYLE_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_SLAB_SERIFS:
                fd.setSerif(true);
                break;
            case OS2WindowsMetricsTable.FAMILY_CLASS_SCRIPTS:
                fd.setScript(true);
                break;
        }

        fd.setFontWeight(os2.getWeightClass());

        fd.setSymbolic(true);
        fd.setNonSymbolic(false);

        // ItalicAngle
        fd.setItalicAngle(post.getItalicAngle());

        // FontBBox
        HeaderTable header = ttf.getHeader();
        PDRectangle rect = new PDRectangle();
        float scaling = 1000f / header.getUnitsPerEm();
        rect.setLowerLeftX(header.getXMin() * scaling);
        rect.setLowerLeftY(header.getYMin() * scaling);
        rect.setUpperRightX(header.getXMax() * scaling);
        rect.setUpperRightY(header.getYMax() * scaling);
        fd.setFontBoundingBox(rect);

        // Ascent, Descent
        HorizontalHeaderTable hHeader = ttf.getHorizontalHeader();
        fd.setAscent(hHeader.getAscender() * scaling);
        fd.setDescent(hHeader.getDescender() * scaling);

        // CapHeight, XHeight
        if (os2.getVersion() >= 1.2)
        {
            fd.setCapHeight(os2.getCapHeight() / scaling);
            fd.setXHeight(os2.getHeight() / scaling);
        }
        else
        {
            // estimate by summing the typographical +ve ascender and -ve descender
            fd.setCapHeight((os2.getTypoAscender() + os2.getTypoDescender()) / scaling);

            // estimate by halfing the typographical ascender
            fd.setXHeight((os2.getTypoAscender() / 2) / scaling);
        }

        // StemV - there's no true TTF equivalent of this, so we estimate it
        fd.setStemV(fd.getFontBoundingBox().getWidth() * .13f);

        return fd;
    }

    /**
     * Returns the best Unicode from the font (the most general).
     */
    private CmapSubtable getUnicodeCmap(CmapTable cmapTable) throws IOException
    {
        CmapSubtable cmap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                                                  CmapTable.ENCODING_UNICODE_2_0_FULL);
        if (cmap == null)
        {
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                                         CmapTable.ENCODING_UNICODE_2_0_BMP);
        }
        if (cmap == null)
        {
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                                         CmapTable.ENCODING_WIN_UNICODE);
        }
        if (cmap == null)
        {
            // Microsoft's "Recommendations for OpenType Fonts" says that "Symbol" encoding
            // actually means "Unicode, non-standard character set"
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                                         CmapTable.ENCODING_WIN_SYMBOL);
        }
        if (cmap == null)
        {
            throw new IOException("The TrueType font does not contain a Unicode cmap");
        }
        return cmap;
    }


    /**
     * Returns the FontBox font.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }

    /**
     * Returns the font descriptor.
     */
    public PDFontDescriptor getFontDescriptor()
    {
        return fontDescriptor;
    }
}
