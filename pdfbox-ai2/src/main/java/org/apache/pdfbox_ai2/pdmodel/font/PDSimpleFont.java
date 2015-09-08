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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox_ai2.FontBoxFont;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.MacRomanEncoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.StandardEncoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.WinAnsiEncoding;

/**
 * A simple font. Simple fonts use a PostScript encoding vector.
 *
 * @author John Hewson
 */
public abstract class PDSimpleFont extends PDFont
{
    private static final Log LOG = LogFactory.getLog(PDSimpleFont.class);

    protected Encoding encoding;
    protected GlyphList glyphList;
    private Boolean isSymbolic;
    private final Set<Integer> noUnicode = new HashSet<Integer>(); // for logging
    private Map<String, Integer> invertedEncoding; // for writing
    
    /**
     * Constructor for embedding.
     */
    PDSimpleFont()
    {
        super();
    }

    /**
     * Constructor for Standard 14.
     */
    PDSimpleFont(String baseFont)
    {
        super(baseFont);

        this.encoding = WinAnsiEncoding.INSTANCE;

        // assign the glyph list based on the font
        if ("ZapfDingbats".equals(baseFont))
        {
            glyphList = GlyphList.getZapfDingbats();
        }
        else
        {
            glyphList = GlyphList.getAdobeGlyphList();
        }
    }

    /**
     * Constructor.
     *
     * @param fontDictionary Font dictionary.
     */
    PDSimpleFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
    }

    /**
     * Reads the Encoding from the Font dictionary or the embedded or substituted font file.
     * Must be called at the end of any subclass constructors.
     *
     * @throws IOException if the font file could not be read
     */
    protected void readEncoding() throws IOException
    {
        COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
        if (encoding != null)
        {
            if (encoding instanceof COSName)
            {
                COSName encodingName = (COSName)encoding;
                this.encoding = Encoding.getInstance(encodingName);
                if (this.encoding == null)
                {
                    LOG.warn("Unknown encoding: " + encodingName.getName());
                    this.encoding = readEncodingFromFont(); // fallback
                }
            }
            else if (encoding instanceof COSDictionary)
            {
                COSDictionary encodingDict = (COSDictionary)encoding;
                Encoding builtIn = null;
                Boolean symbolic = getSymbolicFlag();
                boolean isFlaggedAsSymbolic = symbolic != null && symbolic;
                if (!encodingDict.containsKey(COSName.BASE_ENCODING) && isFlaggedAsSymbolic)
                {
                    builtIn = readEncodingFromFont();
                }

                if (symbolic == null)
                {
                    symbolic = false;
                }
                this.encoding = new DictionaryEncoding(encodingDict, !symbolic, builtIn);
            }
        }
        else
        {
            this.encoding = readEncodingFromFont();
        }

        // normalise the standard 14 name, e.g "Symbol,Italic" -> "Symbol"
        String standard14Name = Standard14Fonts.getMappedFontName(getName());

        // assign the glyph list based on the font
        if ("ZapfDingbats".equals(standard14Name))
        {
            glyphList = GlyphList.getZapfDingbats();
        }
        else
        {
            // StandardEncoding and Symbol are in the AGL
            glyphList = GlyphList.getAdobeGlyphList();
        }
    }

    /**
     * Called by readEncoding() if the encoding needs to be extracted from the font file.
     *
     * @throws IOException if the font file could not be read.
     */
    protected abstract Encoding readEncodingFromFont() throws IOException;

    /**
     * Returns the Encoding vector.
     */
    public Encoding getEncoding()
    {
        return encoding;
    }

    /**
     * Returns the Encoding vector.
     */
    public GlyphList getGlyphList()
    {
        return glyphList;
    }

    /**
     * Inverts the font's Encoding. Any duplicate (Name -> Code) mappings will be lost.
     */
    protected Map<String, Integer> getInvertedEncoding()
    {
        if (invertedEncoding != null)
        {
            return invertedEncoding;
        }

        invertedEncoding = new HashMap<String, Integer>();
        Map<Integer, String> codeToName = encoding.getCodeToNameMap();
        for (Map.Entry<Integer, String> entry : codeToName.entrySet())
        {
            if (!invertedEncoding.containsKey(entry.getValue()))
            {
                invertedEncoding.put(entry.getValue(), entry.getKey());
            }
        }
        return invertedEncoding;
    }
    
    /**
     * Returns true the font is a symbolic (that is, it does not use the Adobe Standard Roman
     * character set).
     */
    public final boolean isSymbolic()
    {
        if (isSymbolic == null)
        {
            Boolean result = isFontSymbolic();
            if (result != null)
            {
                isSymbolic = result;
            }
            else
            {
                // unless we can prove that the font is non-symbolic, we assume that it is not
                isSymbolic = true;
            }
        }
        return isSymbolic;
    }

    /**
     * Internal implementation of isSymbolic, allowing for the fact that the result may be
     * indeterminate.
     */
    protected Boolean isFontSymbolic()
    {
        Boolean result = getSymbolicFlag();
        if (result != null)
        {
            return result;
        }
        else if (isStandard14())
        {
            String mappedName = Standard14Fonts.getMappedFontName(getName());
            return mappedName.equals("Symbol") || mappedName.equals("ZapfDingbats");
        }
        else
        {
            if (encoding == null)
            {
                // sanity check, should never happen
                if (!(this instanceof PDTrueTypeFont))
                {
                    throw new IllegalStateException("PDFBox bug: encoding should not be null!");
                }

                // TTF without its non-symbolic flag set must be symbolic
                return true;
            }
            else if (encoding instanceof WinAnsiEncoding ||
                     encoding instanceof MacRomanEncoding ||
                     encoding instanceof StandardEncoding)
            {
                return false;
            }
            else if (encoding instanceof DictionaryEncoding)
            {
                // each name in Differences array must also be in the latin character set
                for (String name : ((DictionaryEncoding)encoding).getDifferences().values())
                {
                    if (name.equals(".notdef"))
                    {
                        // skip
                    }
                    else if (!(WinAnsiEncoding.INSTANCE.contains(name) &&
                               MacRomanEncoding.INSTANCE.contains(name) &&
                               StandardEncoding.INSTANCE.contains(name)))
                    {
                        return true;
                    }

                }
                return false;
            }
            else
            {
                // we don't know
                return null;
            }
        }
    }

    /**
     * Returns the value of the symbolic flag,  allowing for the fact that the result may be
     * indeterminate.
     */
    protected final Boolean getSymbolicFlag()
    {
        if (getFontDescriptor() != null)
        {
            // fixme: isSymbolic() defaults to false if the flag is missing so we can't trust this
            return getFontDescriptor().isSymbolic();
        }
        return null;
    }

    @Override
    public String toUnicode(int code) throws IOException
    {
        return toUnicode(code, GlyphList.getAdobeGlyphList());
    }

    @Override
    public String toUnicode(int code, GlyphList customGlyphList) throws IOException
    {
        // allow the glyph list to be overridden for the purpose of extracting Unicode
        // we only do this when the font's glyph list is the AGL, to avoid breaking Zapf Dingbats
        GlyphList unicodeGlyphList;
        if (this.glyphList == GlyphList.getAdobeGlyphList())
        {
            unicodeGlyphList = customGlyphList;
        }
        else
        {
            unicodeGlyphList = this.glyphList;
        }

        // first try to use a ToUnicode CMap
        String unicode = super.toUnicode(code);
        if (unicode != null)
        {
            return unicode;
        }

        // if the font is a "simple font" and uses MacRoman/MacExpert/WinAnsi[Encoding]
        // or has Differences with names from only Adobe Standard and/or Symbol, then:
        //
        //    a) Map the character codes to names
        //    b) Look up the name in the Adobe Glyph List to obtain the Unicode value

        String name = null;
        if (encoding != null)
        {
            name = encoding.getName(code);
            unicode = unicodeGlyphList.toUnicode(name);
            if (unicode != null)
            {
                return unicode;
            }
        }

        // if no value has been produced, there is no way to obtain Unicode for the character.
        if (LOG.isWarnEnabled() && !noUnicode.contains(code))
        {
            // we keep track of which warnings have been issued, so we don't log multiple times
            noUnicode.add(code);
            if (name != null)
            {
                LOG.warn("No Unicode mapping for " + name + " (" + code + ") in font " +
                        getName());
            }
            else
            {
                LOG.warn("No Unicode mapping for character code " + code + " in font " +
                        getName());
            }
        }

        return null;
    }

    @Override
    public boolean isVertical()
    {
        return false;
    }

    @Override
    protected final float getStandard14Width(int code)
    {
        if (getStandard14AFM() != null)
        {
            String nameInAFM = getEncoding().getName(code);

            // the Adobe AFMs don't include .notdef, but Acrobat uses 250, test with PDFBOX-2334
            if (nameInAFM.equals(".notdef"))
            {
                return 250f;
            }

            return getStandard14AFM().getCharacterWidth(nameInAFM);
        }
        throw new IllegalStateException("No AFM");
    }

    @Override
    public boolean isStandard14()
    {
        // this logic is based on Acrobat's behaviour, see see PDFBOX-2372
        // the Encoding entry cannot have Differences if we want "standard 14" font handling
        if (getEncoding() instanceof DictionaryEncoding)
        {
            DictionaryEncoding dictionary = (DictionaryEncoding)getEncoding();
            if (dictionary.getDifferences().size() > 0)
            {
                // we also require that the differences are actually different, see PDFBOX-1900 with
                // the file from PDFBOX-2192 on Windows
                Encoding baseEncoding = dictionary.getBaseEncoding();
                for (Map.Entry<Integer, String> entry : dictionary.getDifferences().entrySet())
                {
                    if (!entry.getValue().equals(baseEncoding.getName(entry.getKey())))
                    {
                        return false;
                    }
                }
            }
        }
        return super.isStandard14();
    }

    /**
     * Returns the path for the character with the given name. For some fonts, GIDs may be used
     * instead of names when calling this method.
     *
     * @return glyph path
     * @throws IOException if the path could not be read
     */
    public abstract GeneralPath getPath(String name) throws IOException;

    /**
     * Returns true if the font contains the character with the given name.
     *
     * @throws IOException if the path could not be read
     */
    public abstract boolean hasGlyph(String name) throws IOException;

    /**
     * Returns the embedded or system font used for rendering. This is never null.
     */
    public abstract FontBoxFont getFontBoxFont();

    @Override
    public void addToSubset(int codePoint)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subset() throws IOException
    {
        // only TTF subsetting via PDType0Font is currently supported
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean willBeSubset()
    {
        return false;
    }
}
