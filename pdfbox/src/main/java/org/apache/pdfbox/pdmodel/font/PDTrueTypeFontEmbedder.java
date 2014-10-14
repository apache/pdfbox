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
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Embedded PDTrueTypeFont builder. Helper class to populate a PDTrueTypeFont from a TTF.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
class PDTrueTypeFontEmbedder
{
    private final Encoding fontEncoding;
    private final TrueTypeFont ttf;
    private final PDFontDescriptor fontDescriptor;

    /**
     * Creates a new TrueType font for embedding.
     */
    PDTrueTypeFontEmbedder(PDDocument document, COSDictionary dict, InputStream ttfStream)
            throws IOException
    {
        dict.setItem(COSName.SUBTYPE, COSName.TRUE_TYPE);

        PDStream stream = new PDStream(document, ttfStream, false);
        stream.getStream().setInt(COSName.LENGTH1, stream.getByteArray().length); // todo: wrong?
        stream.addCompression();

        // only support winansi encoding right now, should really
        // just use Identity-H with unicode mapping
        Encoding encoding = new WinAnsiEncoding(); // fixme: read encoding from TTF

        this.fontEncoding = encoding;
        dict.setItem(COSName.ENCODING, encoding.getCOSObject());

        // as the stream was close within the PDStream constructor, we have to recreate it
        InputStream stream2 = null;
        PDFontDescriptor fd;
        try
        {
            stream2 = stream.createInputStream();
            ttf = new TTFParser().parse(stream2);
            fd = createFontDescriptor(dict, ttf);
        }
        finally
        {
            IOUtils.closeQuietly(stream2);
        }

        fd.setFontFile2(stream);
        dict.setItem(COSName.FONT_DESC, fd);
        fontDescriptor = fd;
    }

    // creates a new font descriptor dictionary for the given TTF
    private PDFontDescriptor createFontDescriptor(COSDictionary dict, TrueTypeFont ttf)
            throws IOException
    {
        PDFontDescriptor fd = new PDFontDescriptor();

        NamingTable naming = ttf.getNaming();
        List<NameRecord> records = naming.getNameRecords();
        for (NameRecord nr : records)
        {
            if (nr.getNameId() == NameRecord.NAME_POSTSCRIPT_NAME)
            {
                dict.setName(COSName.BASE_FONT, nr.getString());
                fd.setFontName(nr.getString());
            }
            else if (nr.getNameId() == NameRecord.NAME_FONT_FAMILY_NAME)
            {
                fd.setFontFamily(nr.getString());
            }
        }

        OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
        boolean isSymbolic = false;
        switch (os2.getFamilyClass())
        {
            case OS2WindowsMetricsTable.FAMILY_CLASS_SYMBOLIC:
                isSymbolic = true;
                break;
            case OS2WindowsMetricsTable.FAMILY_CLASS_SCRIPTS:
                fd.setScript(true);
                break;
            case OS2WindowsMetricsTable.FAMILY_CLASS_CLAREDON_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_FREEFORM_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_MODERN_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_OLDSTYLE_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_SLAB_SERIFS:
                fd.setSerif(true);
                break;
        }

        switch (os2.getWidthClass())
        {
            case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_CONDENSED:
                fd.setFontStretch("UltraCondensed");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_CONDENSED:
                fd.setFontStretch("ExtraCondensed");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_CONDENSED:
                fd.setFontStretch("Condensed");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_CONDENSED:
                fd.setFontStretch("SemiCondensed");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_MEDIUM:
                fd.setFontStretch("Normal");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_EXPANDED:
                fd.setFontStretch("SemiExpanded");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_EXPANDED:
                fd.setFontStretch("Expanded");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_EXPANDED:
                fd.setFontStretch("ExtraExpanded");
                break;
            case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_EXPANDED:
                fd.setFontStretch("UltraExpanded");
                break;
        }
        fd.setFontWeight(os2.getWeightClass());
        fd.setSymbolic(isSymbolic);
        fd.setNonSymbolic(!isSymbolic);

        // todo retval.setItalic
        // todo retval.setAllCap
        // todo retval.setSmallCap
        // todo retval.setForceBold

        HeaderTable header = ttf.getHeader();
        PDRectangle rect = new PDRectangle();
        float scaling = 1000f / header.getUnitsPerEm();
        rect.setLowerLeftX(header.getXMin() * scaling);
        rect.setLowerLeftY(header.getYMin() * scaling);
        rect.setUpperRightX(header.getXMax() * scaling);
        rect.setUpperRightY(header.getYMax() * scaling);
        fd.setFontBoundingBox(rect);

        HorizontalHeaderTable hHeader = ttf.getHorizontalHeader();
        fd.setAscent(hHeader.getAscender() * scaling);
        fd.setDescent(hHeader.getDescender() * scaling);

        GlyphTable glyphTable = ttf.getGlyph();
        GlyphData[] glyphs = glyphTable.getGlyphs();

        PostScriptTable ps = ttf.getPostScript();
        fd.setFixedPitch(ps.getIsFixedPitch() > 0);
        fd.setItalicAngle(ps.getItalicAngle());

        String[] names = ps.getGlyphNames();

        if (names != null)
        {
            for (int i = 0; i < names.length; i++)
            {
                // if we have a capital H then use that, otherwise use the tallest letter
                if (names[i].equals("H"))
                {
                    fd.setCapHeight(glyphs[i].getBoundingBox().getUpperRightY() / scaling);
                }
                if (names[i].equals("x"))
                {
                    fd.setXHeight(glyphs[i].getBoundingBox().getUpperRightY() / scaling);
                }
            }
        }

        // hmm there does not seem to be a clear definition for StemV,
        // this is close enough and I am told it doesn't usually get used.
        fd.setStemV(fd.getFontBoundingBox().getWidth() * .13f);

        CmapTable cmapTable = ttf.getCmap();
        CmapSubtable uniMap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                CmapTable.ENCODING_UNICODE_2_0_FULL);
        if (uniMap == null)
        {
            uniMap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                    CmapTable.ENCODING_UNICODE_2_0_BMP);
        }
        if (uniMap == null)
        {
            uniMap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                    CmapTable.ENCODING_WIN_UNICODE);
        }
        if (uniMap == null)
        {
            // Microsoft's "Recommendations for OpenType Fonts" says that "Symbol" encoding
            // actually means "Unicode, non-standard character set"
            uniMap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                    CmapTable.ENCODING_WIN_SYMBOL);
        }
        if (uniMap == null)
        {
            // there should always be a usable cmap, if this happens we haven't tried hard enough
            // to find one. Furthermore, if we loaded the font from disk then we should've checked
            // first to see that it had a suitable cmap before calling createFontDescriptor
            throw new IllegalArgumentException("ttf: no suitable cmap for font '" +
                    ttf.getNaming().getFontFamily() + "', found: " +
                    Arrays.toString(cmapTable.getCmaps()));
        }

        if (this.getFontEncoding() == null)
        {
            // todo: calling this.getFontEncoding() doesn't work if the font is loaded
            //       from the local system, because it relies on the FontDescriptor!
            //       We make do for now by returning an incomplete descriptor pending further
            //       refactoring of PDFont#determineEncoding().
            return fd;
        }

        Map<Integer, String> codeToName = this.getFontEncoding().getCodeToNameMap();

        int firstChar = Collections.min(codeToName.keySet());
        int lastChar = Collections.max(codeToName.keySet());

        HorizontalMetricsTable hMet = ttf.getHorizontalMetrics();
        int[] widthValues = hMet.getAdvanceWidth();
        // some monospaced fonts provide only one value for the width
        // instead of an array containing the same value for every glyphid
        boolean isMonospaced = fd.isFixedPitch() || widthValues.length == 1;
        int nWidths = lastChar - firstChar + 1;
        List<Integer> widths = new ArrayList<Integer>(nWidths);
        // use the first width as default
        // proportional fonts -> width of the .notdef character
        // monospaced-fonts -> the first width
        int defaultWidth = Math.round(widthValues[0] * scaling);
        for (int i = 0; i < nWidths; i++)
        {
            widths.add(defaultWidth);
        }

        // A character code is mapped to a glyph name via the provided font encoding
        // Afterwards, the glyph name is translated to a glyph ID.
        // For details, see PDFReference16.pdf, Section 5.5.5, p.401
        //
        for (Map.Entry<Integer, String> e : codeToName.entrySet())
        {
            String name = e.getValue();
            // pdf code to unicode by glyph list.
            if (!name.equals(".notdef"))
            {
                String c = GlyphList.getAdobeGlyphList().toUnicode(name); // todo: we're supposed to use the 'provided font encoding'
                int charCode = c.codePointAt(0);
                int gid = uniMap.getGlyphId(charCode);
                if (gid != 0)
                {
                    if (isMonospaced)
                    {
                        widths.set(e.getKey() - firstChar, defaultWidth);
                    }
                    else
                    {
                        widths.set(e.getKey() - firstChar,
                                Math.round(widthValues[gid] * scaling));
                    }
                }
            }
        }
        dict.setItem(COSName.WIDTHS, COSArrayList.converterToCOSArray(widths));
        dict.setInt(COSName.FIRST_CHAR, firstChar);
        dict.setInt(COSName.LAST_CHAR, lastChar);

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
