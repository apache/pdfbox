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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.StandardEncoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;

/**
 * Type 2 CIDFont (TrueType).
 * 
 * @author Ben Litchfield
 */
public class PDCIDFontType2 extends PDCIDFont
{
    private static final Log LOG = LogFactory.getLog(PDCIDFontType2.class);

    private final TrueTypeFont ttf;
    private final int[] cid2gid;
    private final boolean hasIdentityCid2Gid;
    private final boolean isEmbedded;
    private final boolean isDamaged;
    private Matrix fontMatrix;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType2(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        super(fontDictionary, parent);

        PDFontDescriptor fd = getFontDescriptor();
        PDStream ff2Stream = fd.getFontFile2();
        PDStream ff3Stream = fd.getFontFile3();

        TrueTypeFont ttfFont = null;
        boolean fontIsDamaged = false;
        if (ff2Stream != null)
        {
            try
            {
                // embedded
                TTFParser ttfParser = new TTFParser(true);
                ttfFont = ttfParser.parse(ff2Stream.createInputStream());
            }
            catch (NullPointerException e) // TTF parser is buggy
            {
                LOG.warn("Could not read embedded TTF for font " + getBaseFont(), e);
                fontIsDamaged = true;
            }
            catch (IOException e)
            {
                LOG.warn("Could not read embedded TTF for font " + getBaseFont(), e);
                fontIsDamaged = true;
            }
        }
        else if (ff3Stream != null)
        {
            try
            {
                // embedded
                OTFParser otfParser = new OTFParser(true);
                OpenTypeFont otf = otfParser.parse(ff3Stream.createInputStream());
                ttfFont = otf;

                if (otf.isPostScript())
                {
                    // todo: we need more abstraction to support CFF fonts here
                    throw new IOException("Not implemented: OpenType font with CFF table " +
                                          getBaseFont());
                }

                if (otf.hasLayoutTables())
                {
                    LOG.error("OpenType Layout tables used in font " + getBaseFont() +
                              " are not implemented in PDFBox and will be ignored");
                }
            }
            catch (NullPointerException e) // TTF parser is buggy
            {
                fontIsDamaged = true;
                LOG.warn("Could not read embedded OTF for font " + getBaseFont(), e);
            }
            catch (IOException e)
            {
                fontIsDamaged = true;
                LOG.warn("Could not read embedded OTF for font " + getBaseFont(), e);
            }
        }
        isEmbedded = ttfFont != null;
        isDamaged = fontIsDamaged;

        if (ttfFont == null)
        {
            // substitute
            TrueTypeFont ttfSubstitute = ExternalFonts.getTrueTypeFont(getBaseFont());
            if (ttfSubstitute != null)
            {
                ttfFont = ttfSubstitute;
            }
            else
            {
                // fallback
                LOG.warn("Using fallback font for " + getBaseFont());
                ttfFont = ExternalFonts.getTrueTypeFallbackFont(getFontDescriptor());
            }
        }
        ttf = ttfFont;

        cid2gid = readCIDToGIDMap();
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        hasIdentityCid2Gid = map instanceof COSName && ((COSName) map).getName().equals("Identity");
    }

    @Override
    public Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            // 1000 upem, this is not strictly true
            fontMatrix = new Matrix(0.001f, 0, 0, 0.001f, 0, 0);
        }
        return fontMatrix;
    }

    @Override
    public BoundingBox getBoundingBox() throws IOException
    {
        return ttf.getFontBBox();
    }

    private int[] readCIDToGIDMap()
    {
        int[] cid2gid = null;
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        if (map instanceof COSStream)
        {
            COSStream stream = (COSStream) map;
            try
            {
                InputStream is = stream.getUnfilteredStream();
                byte[] mapAsBytes = IOUtils.toByteArray(is);
                IOUtils.closeQuietly(is);
                int numberOfInts = mapAsBytes.length / 2;
                cid2gid = new int[numberOfInts];
                int offset = 0;
                for (int index = 0; index < numberOfInts; index++)
                {
                    int gid = (mapAsBytes[offset] & 0xff) << 8 | mapAsBytes[offset + 1] & 0xff;
                    cid2gid[index] = gid;
                    offset += 2;
                }
            }
            catch (IOException exception)
            {
                LOG.error("Can't read the CIDToGIDMap", exception);
            }
        }
        return cid2gid;
    }

    @Override
    public int codeToCID(int code)
    {
        CMap cMap = parent.getCMap();

        // Acrobat allows bad PDFs to use Unicode CMaps here instead of CID CMaps, see PDFBOX-1283
        if (!cMap.hasCIDMappings() && cMap.hasUnicodeMappings())
        {
            return cMap.toUnicode(code).codePointAt(0); // actually: code -> CID
        }

        return cMap.toCID(code);
    }

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @return GID
     */
    public int codeToGID(int code) throws IOException
    {
        if (!isEmbedded)
        {
            // The conforming reader shall select glyphs by translating characters from the
            // encoding specified by the predefined CMap to one of the encodings in the TrueType
            // font's 'cmap' table. The means by which this is accomplished are implementation-
            // dependent.

            CmapSubtable cmap = getUnicodeCmap(ttf.getCmap());
            String unicode;

            if (cid2gid != null || hasIdentityCid2Gid)
            {
                int cid = codeToCID(code);
                // strange but true, Acrobat allows non-embedded GIDs, test with PDFBOX-2060
                if (hasIdentityCid2Gid)
                {
                    return cid;
                }
                else
                {
                    return cid2gid[cid];
                }
            }
            else if (!parent.isSymbolic())
            {
                // this nonsymbolic behaviour isn't well documented, test with PDFBOX-1422

                // if the font descriptor's Nonsymbolic flag is set, the conforming reader shall
                // create a table that maps from character codes to glyph names
                String name = null;

                // If the Encoding entry is one of the names MacRomanEncoding, WinAnsiEncoding,
                // or a dictionary, then the table is initialized as normal
                // todo: Encoding is not allowed though, right? So this never happens?
                /*if (getFontEncoding() != null)
                {
                    name = getFontEncoding().getName(cid);
                }*/

                // Any undefined entries in the table shall be filled using StandardEncoding
                if (name == null)
                {
                    name = StandardEncoding.INSTANCE.getName(code);
                }

                // map to a Unicode value using the Adobe Glyph List
                unicode = GlyphList.getAdobeGlyphList().toUnicode(name);
            }
            else
            {
                int cid = codeToCID(code);
                unicode = parent.toUnicode(cid); // code = CID for TTF
            }

            if (unicode == null)
            {
                return 0;
            }
            else if (unicode.length() > 1)
            {
                LOG.warn("trying to map a multi-byte character using 'cmap', result will be poor");
            }
            return cmap.getGlyphId(unicode.codePointAt(0));
        }
        else
        {
            // If the TrueType font program is embedded, the Type 2 CIDFont dictionary shall contain
            // a CIDToGIDMap entry that maps CIDs to the glyph indices for the appropriate glyph
            // descriptions in that font program.

            int cid = codeToCID(code);
            if (cid2gid != null)
            {
                // use CIDToGIDMap
                if (cid < cid2gid.length)
                {
                    return cid2gid[cid];
                }
                else
                {
                    return 0;
                }
            }
            else
            {
                // "Identity" is the default CIDToGIDMap
                if (cid < ttf.getNumberOfGlyphs())
                {
                    return cid;
                }
                else
                {
                    // out of range CIDs map to GID 0
                    return 0;
                }
            }
        }
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     */
    private CmapSubtable getUnicodeCmap(CmapTable cmapTable)
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
            // fallback to the first cmap (may not ne Unicode, so may produce poor results)
            LOG.warn("Used fallback cmap for font " + getBaseFont());
            cmap = cmapTable.getCmaps()[0];
        }
        return cmap;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        // todo: really we want the BBox, (for text extraction:)
        return (ttf.getHorizontalHeader().getAscender() + -ttf.getHorizontalHeader().getDescender())
                / ttf.getUnitsPerEm(); // todo: shouldn't this be the yMax/yMin?
    }

    @Override
    public float getWidthFromFont(int code) throws IOException
    {
        int gid = codeToGID(code);
        int width = ttf.getAdvanceWidth(gid);
        int unitsPerEM = ttf.getUnitsPerEm();
        if (unitsPerEM != 1000)
        {
            width *= 1000f / unitsPerEM;
        }
        return width;
    }

    @Override
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public boolean isDamaged()
    {
        return isDamaged;
    }

    /**
     * Returns the embedded or substituted TrueType font.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }
}
