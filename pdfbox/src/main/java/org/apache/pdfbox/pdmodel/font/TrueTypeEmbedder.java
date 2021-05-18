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

import java.awt.geom.GeneralPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TTFSubsetter;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Common functionality for embedding TrueType fonts.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
abstract class TrueTypeEmbedder implements Subsetter
{
    private static final int ITALIC = 1;
    private static final int OBLIQUE = 512;
    private static final String BASE25 = "BCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final PDDocument document;
    protected TrueTypeFont ttf;
    protected PDFontDescriptor fontDescriptor;

    protected final CmapLookup cmapLookup;
    private final Set<Integer> subsetCodePoints = new HashSet<>();
    private final boolean embedSubset;

    private final Set<Integer> allGlyphIds = new HashSet<>();

    /**
     * Creates a new TrueType font for embedding.
     */
    TrueTypeEmbedder(PDDocument document, COSDictionary dict, TrueTypeFont ttf,
                     boolean embedSubset) throws IOException
    {
        this.document = document;
        this.embedSubset = embedSubset;
        this.ttf = ttf;
        fontDescriptor = createFontDescriptor(ttf);

        if (!isEmbeddingPermitted(ttf))
        {
            throw new IOException("This font does not permit embedding");
        }

        if (!embedSubset)
        {
            // full embedding
            
            // TrueType collections are not supported
            InputStream is = ttf.getOriginalData();
            byte[] b = new byte[4];
            is.mark(b.length);
            if (is.read(b) == b.length && new String(b).equals("ttcf"))
            {
                is.close();
                throw new IOException("Full embedding of TrueType font collections not supported");
            }
            if (is.markSupported())
            {
                is.reset();
            }
            else
            {
                is.close();
                is = ttf.getOriginalData();
            }
            PDStream stream = new PDStream(document, is, COSName.FLATE_DECODE);
            stream.getCOSObject().setLong(COSName.LENGTH1, ttf.getOriginalDataSize());
            fontDescriptor.setFontFile2(stream);
        }

        dict.setName(COSName.BASE_FONT, ttf.getName());

        // choose a Unicode "cmap"
        cmapLookup = ttf.getUnicodeCmapLookup();
    }

    public final void buildFontFile2(InputStream ttfStream) throws IOException
    {
        PDStream stream = new PDStream(document, ttfStream, COSName.FLATE_DECODE);

        // as the stream was closed within the PDStream constructor, we have to recreate it
        try (InputStream input = stream.createInputStream())
        {
            ttf = new TTFParser().parseEmbedded(input);
            if (!isEmbeddingPermitted(ttf))
            {
                throw new IOException("This font does not permit embedding");
            }
            if (fontDescriptor == null)
            {
                fontDescriptor = createFontDescriptor(ttf);
            }
        }
        stream.getCOSObject().setLong(COSName.LENGTH1, ttf.getOriginalDataSize());
        fontDescriptor.setFontFile2(stream);
    }

    /**
     * Returns true if the fsType in the OS/2 table permits embedding.
     */
    boolean isEmbeddingPermitted(TrueTypeFont ttf) throws IOException
    {
        if (ttf.getOS2Windows() != null)
        {
            int fsType = ttf.getOS2Windows().getFsType();
            int maskedFsType = fsType & 0x000F;
            // PDFBOX-5191: don't check the bit because permissions are exclusive
            if (maskedFsType == OS2WindowsMetricsTable.FSTYPE_RESTRICTED)
            {
                // restricted License embedding
                return false;
            }
            else if ((fsType & OS2WindowsMetricsTable.FSTYPE_BITMAP_ONLY) ==
                                 OS2WindowsMetricsTable.FSTYPE_BITMAP_ONLY)
            {
                // bitmap embedding only
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the fsType in the OS/2 table permits subsetting.
     */
    private boolean isSubsettingPermitted(TrueTypeFont ttf) throws IOException
    {
        if (ttf.getOS2Windows() != null)
        {
            int fsType = ttf.getOS2Windows().getFsType();
            if ((fsType & OS2WindowsMetricsTable.FSTYPE_NO_SUBSETTING) ==
                          OS2WindowsMetricsTable.FSTYPE_NO_SUBSETTING)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new font descriptor dictionary for the given TTF.
     */
    private PDFontDescriptor createFontDescriptor(TrueTypeFont ttf) throws IOException
    {
        PDFontDescriptor fd = new PDFontDescriptor();
        fd.setFontName(ttf.getName());

        OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
        if (os2 == null)
        {
            throw new IOException("os2 table is missing in font " + ttf.getName());
        }
        PostScriptTable post = ttf.getPostScript();
        if (post == null)
        {
            throw new IOException("post table is missing in font " + ttf.getName());            
        }

        // Flags
        fd.setFixedPitch(post.getIsFixedPitch() > 0 ||
                         ttf.getHorizontalHeader().getNumberOfHMetrics() == 1);

        int fsSelection = os2.getFsSelection();
        fd.setItalic(((fsSelection & (ITALIC | OBLIQUE)) != 0));

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
            default:
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
            fd.setCapHeight(os2.getCapHeight() * scaling);
            fd.setXHeight(os2.getHeight() * scaling);
        }
        else
        {
            GeneralPath capHPath = ttf.getPath("H");
            if (capHPath != null)
            {
                fd.setCapHeight(Math.round(capHPath.getBounds2D().getMaxY()) * scaling);
            }
            else
            {
                // estimate by summing the typographical +ve ascender and -ve descender
                fd.setCapHeight((os2.getTypoAscender() + os2.getTypoDescender()) * scaling);
            }
            GeneralPath xPath = ttf.getPath("x");
            if (xPath != null)
            {
                fd.setXHeight(Math.round(xPath.getBounds2D().getMaxY()) * scaling);
            }
            else
            {
                // estimate by halving the typographical ascender
                fd.setXHeight(os2.getTypoAscender() / 2.0f * scaling);
            }
        }

        // StemV - there's no true TTF equivalent of this, so we estimate it
        fd.setStemV(fd.getFontBoundingBox().getWidth() * .13f);

        return fd;
    }

    /**
     * Returns the font descriptor.
     */
    public PDFontDescriptor getFontDescriptor()
    {
        return fontDescriptor;
    }
    
    @Override
    public void addToSubset(int codePoint)
    {
        subsetCodePoints.add(codePoint);
    }
    
    public void addGlyphIds(Set<Integer> glyphIds)
    {
        allGlyphIds.addAll(glyphIds);
    }

    @Override
    public void subset() throws IOException
    {
        if (!isSubsettingPermitted(ttf))
        {
            throw new IOException("This font does not permit subsetting");
        }
        
        if (!embedSubset)
        {
            throw new IllegalStateException("Subsetting is disabled");
        }

        // PDF spec required tables (if present), all others will be removed
        List<String> tables = new ArrayList<>();
        tables.add("head");
        tables.add("hhea");
        tables.add("loca");
        tables.add("maxp");
        tables.add("cvt ");
        tables.add("prep");
        tables.add("glyf");
        tables.add("hmtx");
        tables.add("fpgm");
        // Windows ClearType
        tables.add("gasp");

        // set the GIDs to subset
        TTFSubsetter subsetter = new TTFSubsetter(ttf, tables);
        subsetter.addAll(subsetCodePoints);

        if (!allGlyphIds.isEmpty())
        {
            subsetter.addGlyphIds(allGlyphIds);
        }

        // calculate deterministic tag based on the chosen subset
        Map<Integer, Integer> gidToCid = subsetter.getGIDMap();
        String tag = getTag(gidToCid);
        subsetter.setPrefix(tag);

        // save the subset font
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        subsetter.writeToStream(out);

        // re-build the embedded font
        buildSubset(new ByteArrayInputStream(out.toByteArray()), tag, gidToCid);
        ttf.close();
    }

    /**
     * Returns true if the font needs to be subset.
     */
    public boolean needsSubset()
    {
        return embedSubset;
    }
    
    /**
     * Rebuild a font subset.
     */
    protected abstract void buildSubset(InputStream ttfSubset, String tag,
                                     Map<Integer, Integer> gidToCid) throws IOException;

    /**
     * Returns an uppercase 6-character unique tag for the given subset.
     */
    public String getTag(Map<Integer, Integer> gidToCid)
    {
        // deterministic
        long num = gidToCid.hashCode();

        // base25 encode
        StringBuilder sb = new StringBuilder();
        do
        {
            long div = num / 25;
            int mod = (int)(num % 25);
            sb.append(BASE25.charAt(mod));
            num = div;
        } while (num != 0 && sb.length() < 6);

        // pad
        while (sb.length() < 6)
        {
            sb.insert(0, 'A');
        }

        sb.append('+');
        return sb.toString();
    }
}
