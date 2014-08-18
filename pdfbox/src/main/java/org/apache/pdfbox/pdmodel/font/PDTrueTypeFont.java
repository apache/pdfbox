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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.MacOSRomanEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * TrueType font.
 * 
 * @author Ben Litchfield
 */
public class PDTrueTypeFont extends PDFont
{
    private static final Log LOG = LogFactory.getLog(PDTrueTypeFont.class);

    private static final int START_RANGE_F000 = 0xF000;
    private static final int START_RANGE_F100 = 0xF100;
    private static final int START_RANGE_F200 = 0xF200;

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

    private CMAPEncodingEntry cmapWinUnicode = null;
    private CMAPEncodingEntry cmapWinSymbol = null;
    private CMAPEncodingEntry cmapMacintoshSymbol = null;
    private boolean cmapInitialized = false;

    private final TrueTypeFont ttf;
    private final HashMap<Integer, Float> advanceWidths = new HashMap<Integer, Float> ();

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

        // substitute
        if (ttfFont == null)
        {
            ttfFont = ExternalFonts.getTrueTypeFont(getBaseFont());

            // fallback
            if (ttfFont == null)
            {
                LOG.warn("Using fallback font for " + getBaseFont());
                ttfFont = ExternalFonts.getFallbackFont();
            }
        }

        ttf = ttfFont;

        determineEncoding();
    }

    /**
     * Creates a new TrueType font for embedding.
     */
    private PDTrueTypeFont(PDDocument document, InputStream ttfStream) throws IOException
    {
        PDTrueTypeFontEmbedder embedder = new PDTrueTypeFontEmbedder(document, dict, ttfStream);
        fontEncoding = embedder.getFontEncoding();
        ttf = embedder.getTrueTypeFont();
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        if (super.getFontDescriptor() == null)
        {
            // todo: this is an experiment: we now allow this to be null (i.e. we no longer synthesise)
            //fontDescriptor = makeFontDescriptor(ttf);
        }
        return fontDescriptor;
    }

    /**
     * Returns the embedded or substituted TrueType font.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }

    @Override
    public float getFontWidth(int charCode) throws IOException
    {
        float width = super.getFontWidth(charCode);
        if (width <= 0)
        {
            if (advanceWidths.containsKey(charCode))
            {
                width = advanceWidths.get(charCode);
            }
            else
            {
                int code = getGIDForCharacterCode(charCode);
                width = ttf.getAdvanceWidth(code);
                int unitsPerEM = ttf.getUnitsPerEm();
                // do we have to scale the width
                if (unitsPerEM != 1000)
                {
                    width *= 1000f / unitsPerEM;
                }
                advanceWidths.put(charCode, width);
            }
        }
        return width;
    }

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @return GID (glyph index)
     */
    public int getGIDForCharacterCode(int code) throws IOException
    {
        extractCmapTable();
        int result = 0;
        if (getFontEncoding() != null && !isSymbolicFont())
        {
            try
            {
                String characterName = getFontEncoding().getName(code);
                if (characterName != null)
                {
                    if (cmapWinUnicode != null)
                    {
                        String unicode = Encoding.getCharacterForName(characterName);
                        if (unicode != null)
                        {
                            if (unicode.isEmpty())
                            {
                                LOG.error("getCharacterForName for code " + code + 
                                        ", characterName '" + characterName + 
                                        "' is empty");
                            }
                            else
                            {
                                result = unicode.codePointAt(0);
                            }
                        }
                        result = cmapWinUnicode.getGlyphId(result);
                    }
                    else if (cmapMacintoshSymbol != null &&
                             MacOSRomanEncoding.INSTANCE.hasCodeForName(characterName))
                    {
                        result = MacOSRomanEncoding.INSTANCE.getCode(characterName);
                        result = cmapMacintoshSymbol.getGlyphId(result);
                    }
                    else if (cmapWinSymbol != null)
                    {
                        // fallback scenario if the glyph can't be found yet
                        // maybe the 3,0 cmap provides a suitable mapping
                        // see PDFBOX-2091
                        result = cmapWinSymbol.getGlyphId(code);
                    }
                }
            }
            catch (IOException exception)
            {
                LOG.error("Caught an exception getGlyhcode: " + exception);
            }
        }

        if (getFontEncoding() == null || isSymbolicFont())
        {
            if (cmapWinSymbol != null)
            {
                result = cmapWinSymbol.getGlyphId(code);
                if (code >= 0 && code <= 0xFF)
                {
                    // the CMap may use one of the following code ranges,
                    // so that we have to add the high byte to get the
                    // mapped value
                    if (result == 0)
                    {
                        // F000 - F0FF
                        result = cmapWinSymbol.getGlyphId(code + START_RANGE_F000);
                    }
                    if (result == 0)
                    {
                        // F100 - F1FF
                        result = cmapWinSymbol.getGlyphId(code + START_RANGE_F100);
                    }
                    if (result == 0)
                    {
                        // F200 - F2FF
                        result = cmapWinSymbol.getGlyphId(code + START_RANGE_F200);
                    }
                }
            }
            else if (cmapMacintoshSymbol != null)
            {
                result = cmapMacintoshSymbol.getGlyphId(code);
            }
        }

        if (result == 0)
        {
            LOG.warn("Can't map code " + code + " in font " + getBaseFont());
        }

        return result;
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

        CMAPTable cmapTable = ttf.getCMAP();
        if (cmapTable != null)
        {
            // get all relevant "cmap" subtables
            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
            for (CMAPEncodingEntry cmap : cmaps)
            {
                if (CMAPTable.PLATFORM_WINDOWS == cmap.getPlatformId())
                {
                    if (CMAPTable.ENCODING_WIN_UNICODE == cmap.getPlatformEncodingId())
                    {
                        cmapWinUnicode = cmap;
                    }
                    else if (CMAPTable.ENCODING_WIN_SYMBOL == cmap.getPlatformEncodingId())
                    {
                        cmapWinSymbol = cmap;
                    }
                }
                else if (CMAPTable.PLATFORM_MACINTOSH == cmap.getPlatformId())
                {
                    if (CMAPTable.ENCODING_MAC_ROMAN == cmap.getPlatformEncodingId())
                    {
                        cmapMacintoshSymbol = cmap;
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
        cmapMacintoshSymbol = null;
        cmapInitialized = false;
        advanceWidths.clear();
    }
}
