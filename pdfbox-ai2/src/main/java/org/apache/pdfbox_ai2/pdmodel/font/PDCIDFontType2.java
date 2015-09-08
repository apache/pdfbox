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
package org.apache.pdfbox_ai2.pdmodel.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox_ai2.cff.Type2CharString;
import org.apache.fontbox_ai2.cmap.CMap;
import org.apache.fontbox_ai2.ttf.CmapSubtable;
import org.apache.fontbox_ai2.ttf.GlyphData;
import org.apache.fontbox_ai2.ttf.OTFParser;
import org.apache.fontbox_ai2.ttf.OpenTypeFont;
import org.apache.fontbox_ai2.ttf.TTFParser;
import org.apache.fontbox_ai2.ttf.TrueTypeFont;
import org.apache.fontbox_ai2.util.BoundingBox;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.cos.COSStream;
import org.apache.pdfbox_ai2.io.IOUtils;
import org.apache.pdfbox_ai2.pdmodel.common.PDStream;
import org.apache.pdfbox_ai2.util.Matrix;

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
    private final Map<Integer, Integer> gid2cid;
    private final boolean hasIdentityCid2Gid;
    private final boolean isEmbedded;
    private final boolean isDamaged;
    private final CmapSubtable cmap; // may be null
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

        // Acrobat looks in FontFile too, even though it is not in the spec, see PDFBOX-2599
        if (ff2Stream == null && ff3Stream == null)
        {
            ff2Stream = fd.getFontFile();
        }
        
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
            // find font or substitute
            CIDFontMapping mapping = FontMapper.getCIDFont(getBaseFont(), getFontDescriptor(),
                                                           getCIDSystemInfo());

            if (mapping.isCIDFont())
            {
                ttfFont = mapping.getFont();
            }
            else
            {
                ttfFont = (TrueTypeFont)mapping.getTrueTypeFont();
            }

            if (mapping.isFallback())
            {
                LOG.warn("Using fallback font " + ttfFont.getName() + " for CID-keyed TrueType font " + getBaseFont());
            }
        }
        ttf = ttfFont;
        cmap = ttf.getUnicodeCmap(false);

        cid2gid = readCIDToGIDMap();
        gid2cid = invert(cid2gid);
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

    private int[] readCIDToGIDMap() throws IOException
    {
        int[] cid2gid = null;
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        if (map instanceof COSStream)
        {
            COSStream stream = (COSStream) map;

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
        return cid2gid;
    }

    private Map<Integer, Integer> invert(int[] cid2gid)
    {
        if (cid2gid == null)
        {
            return null;
        }
        Map<Integer, Integer> inverse = new HashMap<Integer, Integer>();
        for (int i = 0; i < cid2gid.length; i++)
        {
            inverse.put(cid2gid[i], i);
        }
        return inverse;
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
            
            boolean hasUnicodeMap = parent.getCMapUCS2() != null;

            if (cid2gid != null)
            {
                // Acrobat allows non-embedded GIDs - todo: can we find a test PDF for this?
                int cid = codeToCID(code);
                return cid2gid[cid];
            }
            else if (hasIdentityCid2Gid || !hasUnicodeMap)
            {
                // same as above, but for the default Identity CID2GIDMap or when there is no
                // ToUnicode CMap to fallback to, see PDFBOX-2599 and PDFBOX-2560
                // todo: can we find a test PDF for the Identity case?
                return codeToCID(code);
            }
            else
            {
                // fallback to the ToUnicode CMap, test with PDFBOX-1422 and PDFBOX-2560
                String unicode = parent.toUnicode(code);
                if (unicode == null)
                {
                    LOG.warn("Failed to find a character mapping for " + code + " in " + getName());
                    return 0;
                }
                else if (unicode.length() > 1)
                {
                    LOG.warn("Trying to map multi-byte character using 'cmap', result will be poor");
                }
                
                // a non-embedded font always has a cmap (otherwise FontMapper won't load it)
                return cmap.getGlyphId(unicode.codePointAt(0));
            }
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
    public byte[] encode(int unicode)
    {
        int cid = -1;
        if (isEmbedded)
        {
            // embedded fonts always use CIDToGIDMap, with Identity as the default
            if (parent.getCMap().getName().startsWith("Identity-"))
            {
                if (cmap != null)
                {
                    cid = cmap.getGlyphId(unicode);
                }
            }
            else
            {
                // if the CMap is predefined then there will be a UCS-2 CMap
                if (parent.getCMapUCS2() != null)
                {
                    cid = parent.getCMapUCS2().toCID(unicode);
                }
            }

            // otherwise we require an explicit ToUnicode CMap
            if (cid == -1)
            {
                // todo: invert the ToUnicode CMap?
                cid = 0;
            }
        }
        else
        {
            // a non-embedded font always has a cmap (otherwise it we wouldn't load it)
            cid = cmap.getGlyphId(unicode);
        }

        if (cid == 0)
        {
            throw new IllegalArgumentException(
                    String.format("No glyph for U+%04X in font %s", unicode, getName()));
        }

        // CID is always 2-bytes (16-bit) for TrueType
        return new byte[] { (byte)(cid >> 8 & 0xff), (byte)(cid & 0xff) };
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
     * Returns the embedded or substituted TrueType font. May be an OpenType font if the font is
     * not embedded.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }

    @Override
    public GeneralPath getPath(int code) throws IOException
    {
        if (ttf instanceof OpenTypeFont && ((OpenTypeFont)ttf).isPostScript())
        {
            int cid = codeToCID(code);
            Type2CharString charstring = ((OpenTypeFont)ttf).getCFF().getFont().getType2CharString(cid);
            return charstring.getPath();
        }
        else
        {
            int gid = codeToGID(code);
            GlyphData glyph = ttf.getGlyph().getGlyph(gid);
            if (glyph != null)
            {
                return glyph.getPath();
            }
            return new GeneralPath();
        }
    }

    @Override
    public boolean hasGlyph(int code) throws IOException
    {
        return codeToGID(code) != 0;
    }
}
