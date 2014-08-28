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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.GlyphList;
import org.apache.pdfbox.encoding.MacOSRomanEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * TrueType font.
 * 
 * @author Ben Litchfield
 */
public class PDTrueTypeFont extends PDSimpleFont
{
    private static final Log LOG = LogFactory.getLog(PDTrueTypeFont.class);

    private static final int START_RANGE_F000 = 0xF000;
    private static final int START_RANGE_F100 = 0xF100;
    private static final int START_RANGE_F200 = 0xF200;

    private static final Map<String, Integer> INVERTED_MACOS_ROMAN = new HashMap<String, Integer>();
    static
    {
        Map<Integer, String> codeToName = MacOSRomanEncoding.INSTANCE.getCodeToNameMap();
        for (Map.Entry<Integer, String> entry : codeToName.entrySet())
        {
            if (!INVERTED_MACOS_ROMAN.containsKey(entry.getValue()))
            {
                INVERTED_MACOS_ROMAN.put(entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * Loads a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param file a ttf file.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDTrueTypeFont loadTTF(PDDocument doc, File file) throws IOException
    {
        return new PDTrueTypeFont(doc, new FileInputStream(file));
    }

    private CmapSubtable cmapWinUnicode = null;
    private CmapSubtable cmapWinSymbol = null;
    private CmapSubtable cmapMacRoman = null;
    private boolean cmapInitialized = false;

    private final TrueTypeFont ttf;
    private final boolean isEmbedded;

    /**
     * Creates a new TrueType font from a Font dictionary.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDTrueTypeFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);

        PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary) super.getFontDescriptor();
        TrueTypeFont ttfFont = null;
        if (fd != null)
        {
            PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                // embedded
                TTFParser ttfParser = new TTFParser(true);
                ttfFont = ttfParser.parseTTF(ff2Stream.createInputStream());
            }
        }
        isEmbedded = ttfFont != null;

        // substitute
        if (ttfFont == null)
        {
            ttfFont = ExternalFonts.getTrueTypeFont(getBaseFont());

            // fallback
            if (ttfFont == null)
            {
                LOG.warn("Using fallback font for " + getBaseFont());
                ttfFont = ExternalFonts.getFallbackFont(getFontDescriptor());
            }
        }
        ttf = ttfFont;
        readEncoding();
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        return null;
    }

    /**
     * Creates a new TrueType font for embedding.
     */
    private PDTrueTypeFont(PDDocument document, InputStream ttfStream) throws IOException
    {
        PDTrueTypeFontEmbedder embedder = new PDTrueTypeFontEmbedder(document, dict, ttfStream);
        encoding = embedder.getFontEncoding();
        ttf = embedder.getTrueTypeFont();
        isEmbedded = true;
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    /**
     * Returns the embedded or substituted TrueType font.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }

    @Override
    protected float getWidthFromFont(int code) throws IOException
    {
        int gid = codeToGID(code);
        float width = ttf.getAdvanceWidth(gid);
        float unitsPerEM = ttf.getUnitsPerEm();
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

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @return GID (glyph index)
     */
    public int codeToGID(int code) throws IOException
    {
        extractCmapTable();
        int gid = 0;

        if (getEncoding() != null && !isSymbolic()) // non-symbolic
        {
            String name = getEncoding().getName(code);
            if (name.equals(".notdef"))
            {
                return 0;
            }
            else
            {
                if (cmapWinUnicode != null) // (3, 1)
                {
                    String unicode = GlyphList.toUnicode(name);
                    if (unicode != null)
                    {
                        if (unicode.isEmpty())
                        {
                            LOG.error("getCharacterForName for code " + code +
                                    ", characterName '" + name +
                                    "' is empty");
                        }
                        else
                        {
                            gid = unicode.codePointAt(0);
                        }
                    }
                    gid = cmapWinUnicode.getGlyphId(gid);
                }
                else if (cmapMacRoman != null) // (1, 0)
                {
                    Integer macCode = INVERTED_MACOS_ROMAN.get(name);
                    if (macCode == null)
                    {
                        macCode = 0;
                    }
                    gid = cmapMacRoman.getGlyphId(macCode);
                }
                else if (cmapWinSymbol != null)
                {
                    // fallback scenario if the glyph can't be found yet, maybe the (3, 0) cmap
                    // provides a suitable mapping, see PDFBOX-2091
                    gid = cmapWinSymbol.getGlyphId(code);
                }
            }
        }
        else // symbolic
        {
            if (cmapWinSymbol != null)
            {
                gid = cmapWinSymbol.getGlyphId(code);
                if (code >= 0 && code <= 0xFF)
                {
                    // the CMap may use one of the following code ranges,
                    // so that we have to add the high byte to get the
                    // mapped value
                    if (gid == 0)
                    {
                        // F000 - F0FF
                        gid = cmapWinSymbol.getGlyphId(code + START_RANGE_F000);
                    }
                    if (gid == 0)
                    {
                        // F100 - F1FF
                        gid = cmapWinSymbol.getGlyphId(code + START_RANGE_F100);
                    }
                    if (gid == 0)
                    {
                        // F200 - F2FF
                        gid = cmapWinSymbol.getGlyphId(code + START_RANGE_F200);
                    }
                }
            }
            else if (cmapMacRoman != null)
            {
                gid = cmapMacRoman.getGlyphId(code);
            }
        }

        if (gid == 0)
        {
            LOG.warn("Can't map code " + code + " in font " + getBaseFont());
        }

        return gid;
    }

    /**
     * extract all useful "cmap" subtables.
     */
    private void extractCmapTable() throws IOException
    {
        if (cmapInitialized)
        {
            return;
        }

        CmapTable cmapTable = ttf.getCmap();
        if (cmapTable != null)
        {
            // get all relevant "cmap" subtables
            CmapSubtable[] cmaps = cmapTable.getCmaps();
            for (CmapSubtable cmap : cmaps)
            {
                if (CmapTable.PLATFORM_WINDOWS == cmap.getPlatformId())
                {
                    if (CmapTable.ENCODING_WIN_UNICODE == cmap.getPlatformEncodingId())
                    {
                        cmapWinUnicode = cmap;
                    }
                    else if (CmapTable.ENCODING_WIN_SYMBOL == cmap.getPlatformEncodingId())
                    {
                        cmapWinSymbol = cmap;
                    }
                }
                else if (CmapTable.PLATFORM_MACINTOSH == cmap.getPlatformId())
                {
                    if (CmapTable.ENCODING_MAC_ROMAN == cmap.getPlatformEncodingId())
                    {
                        cmapMacRoman = cmap;
                    }
                }
            }
        }
        cmapInitialized = true;
    }

    @Override
    public void clear()
    {
        super.clear();
        cmapWinUnicode = null;
        cmapWinSymbol = null;
        cmapMacRoman = null;
        cmapInitialized = false;
    }
}
